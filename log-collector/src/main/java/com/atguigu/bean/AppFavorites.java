package com.atguigu.bean;/**
 * ClassName: AppFavorites
 * Package: com.atguigu.bean
 * Description:
 *
 * @Author pqk
 * @Create 2023/12/14 11:24
 * @Version 1.0
 */

/**
 * @Author: pqk
 * @Date: 2023/12/14 11:24
 * @Description: 收藏
 *
 */
public class AppFavorites {
    private int id;//主键
    private int course_id;//商品id
    private int userid;//用户ID
    private String add_time;//创建时间
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getCourse_id() {
        return course_id;
    }
    
    public void setCourse_id(int course_id) {
        this.course_id = course_id;
    }
    
    public int getUserid() {
        return userid;
    }
    
    public void setUserid(int userid) {
        this.userid = userid;
    }
    
    public String getAdd_time() {
        return add_time;
    }
    
    public void setAdd_time(String add_time) {
        this.add_time = add_time;
    }
}
