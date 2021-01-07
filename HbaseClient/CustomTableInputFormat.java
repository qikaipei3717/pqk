package com.yinger.batch.source;


import org.apache.flink.configuration.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
/**
 * @Author: Yang JianQiu
 * @Date: 2019/3/19 11:15
 * 由于flink-hbase_2.12_1.7.2 jar包所引用的是hbase1.4.3版本，而现在用到的是hbase2.1.2，版本不匹配
 * 故需要重写flink-hbase_2.12_1.7.2里面的TableInputFormat
 */
public abstract class CustomTableInputFormat<RowData> extends CustomAbstractTableInputFormat<RowData> {

    private static final long serialVersionUID = 1L;

    /**
     * Returns an instance of Scan that retrieves the required subset of records from the HBase table.
     * @return The appropriate instance of Scan for this usecase.
     */
    @Override
    protected abstract Scan getScanner();

    /**
     * What table is to be read.
     * Per instance of a TableInputFormat derivative only a single tablename is possible.
     * @return The name of the table
     */
    @Override
    protected abstract String getTableName();

    /**
     * The output from HBase is always an instance of {@link Result}.
     * This method is to copy the data in the Result instance into the required {@link RowData}
     * @param r The Result instance from HBase that needs to be converted
     * @return The appropriate instance of {@link RowData} that contains the needed information.
     */
    protected abstract RowData mapResultToRowData(Result r);

    /**
     * Creates a {@link Scan} object and opens the {@link HTable} connection.
     * These are opened here because they are needed in the createInputSplits
     * which is called before the openInputFormat method.
     * So the connection is opened in {@link #configure(Configuration)} and closed in {@link #closeInputFormat()}.
     *
     * @param parameters The configuration that is to be used
     * @see Configuration
     */


    /**
     * Create an {@link HTable} instance and set it into this format.
     */
    private HTable createTable() {
        LOG.info("Initializing HBaseConfiguration");
        //use files found in the classpath
        org.apache.hadoop.conf.Configuration hConf = HBaseConfiguration.create();

        try {
            return null;
        } catch (Exception e) {
            LOG.error("Error instantiating a new HTable instance", e);
        }
        return null;
    }

    @Override
    protected RowData mapResultToOutType(Result r) {
        return mapResultToRowData(r);
    }
}