package com.yinger.batch.source;


import java.io.Serializable;
import java.util.Map;


public class RowData implements Serializable {

  private String row;

  private Map<String, String> keyValue;

  public String getRow() {
    return row;
  }

  public void setRow(final String row) {
    this.row = row;
  }

  public Map<String, String> getKeyValue() {
    return keyValue;
  }

  public void setKeyValue(final Map<String, String> keyValue) {
    this.keyValue = keyValue;
  }
  @Override
  public String toString() {
    System.out.println("################### 6 ###############################");

    return "RowData{" +
            "row='" + row + '\'' +
            ", keyValue=" + keyValue +
            '}';
  }

}
