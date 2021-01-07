package com.yinger.batch.source;

import org.apache.flink.configuration.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: Yang JianQiu
 * @Date: 2019/3/1 1:14
 *
 * 从HBase读取数据
 * 第二种：实现TableInputFormat接口
 */
public class HBaseInputFormat extends CustomTableInputFormat<RowData>{

    @Override
    protected Scan getScanner() {
        return scan;
    }

    @Override
    protected String getTableName() {
        return "trait_c2";
    }


    @Override
    protected RowData mapResultToRowData(Result r) {
        RowData rowData = new RowData();
        rowData.setRow(Bytes.toString(r.getRow()));
        Map<String,String> map = new HashMap<>(256);
        for (Cell cell : r.rawCells()) {
            String cellName = Bytes.toString(CellUtil.cloneQualifier(cell));
            map.put(cellName,
                    Bytes.toString(CellUtil.cloneValue(cell)));
        }
        rowData.setKeyValue(map);
        //rowData.toString();
        System.out.println("rowkey=" +rowData.getRow() + "  value="+rowData.getKeyValue());
        return rowData;
    }
}
