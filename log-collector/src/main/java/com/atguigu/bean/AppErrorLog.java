package com.atguigu.bean;/**
 * ClassName: AppErrorLog
 * Package: com.atguigu.bean
 * Description:
 *
 * @Author pqk
 * @Create 2023/12/14 11:19
 * @Version 1.0
 */

/**
 * @Author: pqk
 * @Date: 2023/12/14 11:19
 * @Description: 错误日志
 *
 */
public class AppErrorLog {
    private String errorBrief;    //错误摘要
    private String errorDetail;   //错误详情
    
    public String getErrorBrief() {
        return errorBrief;
    }
    
    public void setErrorBrief(String errorBrief) {
        this.errorBrief = errorBrief;
    }
    
    public String getErrorDetail() {
        return errorDetail;
    }
    
    public void setErrorDetail(String errorDetail) {
        this.errorDetail = errorDetail;
    }
}
