package com.yinger.batch.source;


import org.apache.flink.addons.hbase.AbstractTableInputFormat;
import org.apache.flink.api.common.io.LocatableInputSplitAssigner;
import org.apache.flink.api.common.io.RichInputFormat;
import org.apache.flink.api.common.io.statistics.BaseStatistics;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.io.InputSplitAssigner;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.FSUtils;
import org.apache.hadoop.hbase.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Yang JianQiu
 * @Date: 2019/3/19 11:16
 *
 * 由于flink-hbase_2.12_1.7.2 jar包所引用的是hbase1.4.3版本，而现在用到的是hbase2.1.2，版本不匹配
 * 故需要重写flink-hbase_2.12_1.7.2里面的AbstractTableInputFormat，主要原因是AbstractTableInputFormat里面调用的是hbase1.4.3版本的api，
 * 而新版本hbase2.1.2已经去掉某些api
 */
public abstract class CustomAbstractTableInputFormat<RowData> extends RichInputFormat<RowData, CustomTableInputSplit> {
    protected static final Logger LOG = LoggerFactory.getLogger(AbstractTableInputFormat.class);

    // helper variable to decide whether the input is exhausted or not
    protected boolean endReached = false;
    protected transient org.apache.hadoop.conf.Configuration conf;
    protected transient HTable hTable;
    protected transient Scan scan = null;
    private transient Configuration parameters;
    /** HBase iterator wrapper. */
    protected ResultScanner resultScanner = null;
    private transient Table table;
    protected byte[] currentRow;
    protected long scannedRows;

    /**
     * Returns an instance of Scan that retrieves the required subset of records from the HBase table.
     *
     * @return The appropriate instance of Scan for this use case.
     */
    protected abstract Scan getScanner();

    /**
     * What table is to be read.
     *
     * <p>Per instance of a TableInputFormat derivative only a single table name is possible.
     *
     * @return The name of the table
     */
    protected abstract String getTableName();

    /**
     * HBase returns an instance of {@link Result}.
     *
     * <p>This method maps the returned {@link Result} instance into the output type {@link RowData}.
     *
     * @param r The Result instance from HBase that needs to be converted
     * @return The appropriate instance of {@link RowData} that contains the data of Result.
     */
    protected abstract RowData mapResultToOutType(Result r);

    /**
     * Creates a {@link Scan} object and opens the {@link HTable} connection.
     *
     * <p>These are opened here because they are needed in the createInputSplits
     * which is called before the openInputFormat method.
     *
     * <p>The connection is opened in this method and closed in {@link #closeInputFormat()}.
     *
     * @param parameters The configuration that is to be used
     * @see Configuration
     */
    @Override
    public  void configure(Configuration parameters){
        this.parameters = parameters;

        this.conf = HBaseConfiguration.create();
        this.conf.set("hbase.zookeeper.quorum", "192.168.19.163,192.168.19.164,192.168.19.165");
        this.conf.set("hbase.zookeeper.property.clientPort", "2181");

        this.conf.set("zookeeper.session.timeout", "180000");
        this.conf.set("hbase.rpc.timeout", "120000");
        this.conf.set("hbase.client.retries.number", "2");
        this.conf.set("zookeeper.recovery.retry", "3");
        this.conf.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");


        try {
            Connection connection = ConnectionFactory.createConnection(conf);
            TableName tableName = TableName.valueOf("trait_c2");
            this.table = connection.getTable(tableName);
            this.hTable = new HTable(conf, tableName);
            this.scan = new Scan();
            scan.setCaching(1000);
            scan.addFamily(Bytes.toBytes("d"));
            // 批量读取请求设值为false,不把读出的数据放到缓存.
            scan.setCacheBlocks(false);

        } catch (IOException e) {
            e.printStackTrace();
        }
    };

    @Override
    public void open(CustomTableInputSplit split) throws IOException {
        if (table == null) {
            throw new IOException("The HBase table has not been opened! " +
                    "This needs to be done in configure().");
        }
        if (scan == null) {
            throw new IOException("Scan has not been initialized! " +
                    "This needs to be done in configure().");
        }
        if (split == null) {
            throw new IOException("Input split is null!");
        }

        logSplitInfo("opening", split);

        // set scan range
        currentRow = split.getStartRow();
       scan.setStartRow(currentRow);
        scan.setStopRow(split.getEndRow());
        //scan.withStartRow(currentRow);
        //scan.withStopRow(split.getEndRow());

        resultScanner = table.getScanner(scan);
        endReached = false;
        scannedRows = 0;
    }

    @Override
    public RowData nextRecord(RowData reuse) throws IOException {
        if (resultScanner == null) {
            throw new IOException("No table result scanner provided!");
        }
        try {
            Result res = resultScanner.next();
            if (res != null) {
                scannedRows++;
                currentRow = res.getRow();
                return mapResultToOutType(res);
            }
        } catch (Exception e) {
            resultScanner.close();
            //workaround for timeout on scan
            LOG.warn("Error after scan of " + scannedRows + " rows. Retry with a new scanner...", e);
            scan.setStartRow(currentRow);
            //scan.withStartRow(currentRow);
            resultScanner = table.getScanner(scan);
            Result res = resultScanner.next();
            if (res != null) {
                scannedRows++;
                currentRow = res.getRow();
                return mapResultToOutType(res);
            }
        }

        endReached = true;
        return null;
    }

    private void logSplitInfo(String action, CustomTableInputSplit split) {
        int splitId = split.getSplitNumber();
        String splitStart = Bytes.toString(split.getStartRow());
        String splitEnd = Bytes.toString(split.getEndRow());
        String splitStartKey = splitStart.isEmpty() ? "-" : splitStart;
        String splitStopKey = splitEnd.isEmpty() ? "-" : splitEnd;
        String[] hostnames = split.getHostnames();
        LOG.info("{} split (this={})[{}|{}|{}|{}]", action, this, splitId, hostnames, splitStartKey, splitStopKey);
    }

    @Override
    public boolean reachedEnd() throws IOException {
        return endReached;
    }

    @Override
    public void close() throws IOException {
        LOG.info("Closing split (scanned {} rows)", scannedRows);
        currentRow = null;
        try {
            if (resultScanner != null) {
                resultScanner.close();
            }
        } finally {
            resultScanner = null;
        }
    }

    @Override
    public void closeInputFormat() throws IOException {
        try {
            if (table != null) {
                table.close();
            }
        } finally {
            table = null;
        }
    }

    @Override
    public CustomTableInputSplit[] createInputSplits(final int minNumSplits) throws IOException {
        if (table == null) {
            throw new IOException("The HBase table has not been opened! " +
                    "This needs to be done in configure().");
        }
        if (scan == null) {
            throw new IOException("Scan has not been initialized! " +
                    "This needs to be done in configure().");
        }

        // Get the starting and ending row keys for every region in the currently open table
        final Pair<byte[][], byte[][]> keys = hTable.getRegionLocator().getStartEndKeys();
        if (keys == null || keys.getFirst() == null || keys.getFirst().length == 0) {
            throw new IOException("Expecting at least one region.");
        }
        final byte[] startRow = scan.getStartRow();
        final byte[] stopRow = scan.getStopRow();
        final boolean scanWithNoLowerBound = startRow.length == 0;
        final boolean scanWithNoUpperBound = stopRow.length == 0;

        final List<CustomTableInputSplit> splits = new ArrayList<CustomTableInputSplit>(minNumSplits);
        for (int i = 0; i < keys.getFirst().length; i++) {
            final byte[] startKey = keys.getFirst()[i];
            final byte[] endKey = keys.getSecond()[i];
            final String regionLocation = hTable.getRegionLocator().getRegionLocation(startKey, false).getHostnamePort();
            // Test if the given region is to be included in the InputSplit while splitting the regions of a table
            if (!includeRegionInScan(startKey, endKey)) {
                continue;
            }
            // Find the region on which the given row is being served
            final String[] hosts = new String[]{regionLocation};

            // Determine if regions contains keys used by the scan
            boolean isLastRegion = endKey.length == 0;
            if ((scanWithNoLowerBound || isLastRegion || Bytes.compareTo(startRow, endKey) < 0) &&
                    (scanWithNoUpperBound || Bytes.compareTo(stopRow, startKey) > 0)) {

                final byte[] splitStart = scanWithNoLowerBound || Bytes.compareTo(startKey, startRow) >= 0 ? startKey : startRow;
                final byte[] splitStop = (scanWithNoUpperBound || Bytes.compareTo(endKey, stopRow) <= 0)
                        && !isLastRegion ? endKey : stopRow;
                int id = splits.size();
                final CustomTableInputSplit split = new CustomTableInputSplit(id, hosts, table.getName().getName(), splitStart, splitStop);
                splits.add(split);
            }
        }
        LOG.info("Created " + splits.size() + " splits");
        for (CustomTableInputSplit split : splits) {
            logSplitInfo("created", split);
        }
        return splits.toArray(new CustomTableInputSplit[splits.size()]);
    }

    /**
     * Test if the given region is to be included in the scan while splitting the regions of a table.
     *
     * @param startKey Start key of the region
     * @param endKey   End key of the region
     * @return true, if this region needs to be included as part of the input (default).
     */
    protected boolean includeRegionInScan(final byte[] startKey, final byte[] endKey) {
        return true;
    }

    @Override
    public InputSplitAssigner getInputSplitAssigner(CustomTableInputSplit[] inputSplits) {
        return new LocatableInputSplitAssigner(inputSplits);
    }

    @Override
    public BaseStatistics getStatistics(BaseStatistics cachedStatistics) {
        return null;
    }
}