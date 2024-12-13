package com.atguigu.bean;/**
 * ClassName: AppActive_background
 * Package: com.atguigu.bean
 * Description:
 *
 * @Author pqk
 * @Create 2023/12/14 11:23
 * @Version 1.0
 */

/**
 * @Author: pqk
 * @Date: 2023/12/14 11:23
 * @Description: 用户后台活跃
 *
 */
public class AppActive_background {
    private String active_source;//1=upgrade,2=download(下载),3=plugin_upgrade
    
    public String getActive_source() {
        return active_source;
    }
    
    public void setActive_source(String active_source) {
        this.active_source = active_source;
    }
}
