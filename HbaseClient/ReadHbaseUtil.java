package com.yinger.batch.source;

import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;

public class ReadHbaseUtil {
    public static void main(String[] args) {
        System.out.println("################### 1 ###############################");

        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        try{
            HBaseInputFormat hbaseinput = new HBaseInputFormat();
            DataSource<RowData> calcResult = env.createInput(hbaseinput);
            System.out.println("################### 2 ###############################");
            calcResult.print();
            //calcResult.output();
            env.execute("dfdsg");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
