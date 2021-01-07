package com.yinger.batch.source;

import org.apache.flink.core.io.LocatableInputSplit;

/**
 * @Author: Yang JianQiu
 * @Date: 2019/3/19 11:50
 */
public class CustomTableInputSplit extends LocatableInputSplit {
    private static final long serialVersionUID = 1L;

    /** The name of the table to retrieve data from. */
    private final byte[] tableName;

    /** The start row of the split. */
    private final byte[] startRow;

    /** The end row of the split. */
    private final byte[] endRow;


    CustomTableInputSplit(final int splitNumber, final String[] hostnames, final byte[] tableName, final byte[] startRow,
                          final byte[] endRow) {
        super(splitNumber, hostnames);

        this.tableName = tableName;
        this.startRow = startRow;
        this.endRow = endRow;
    }


    public byte[] getTableName() {
        return this.tableName;
    }

    public byte[] getStartRow() {
        return this.startRow;
    }


    public byte[] getEndRow() {
        return this.endRow;
    }
}