package com.atguigu.logger.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Author: pqk
 * @Date: 2024/10/16 14:21
 * @Description: 接收模拟器生成的数据，并对数据进行处理
 *
 */
//@Controller   //将对象的创建交给Spring容器  方法返回String，默认会当做跳转页面处理
//@RestController =  @Controller + @ResponseBoby     方法返回Object，底层会转换为json格式字符串进行相应

@RestController
@Slf4j
public class LoggerController {
    //Spring提供的对Kafka的支持
    @Autowired  //  将KafkaTemplate注入到Controller中
    KafkaTemplate kafkaTemplate;
    @RequestMapping("/applog")
    public String applog(@RequestBody String mockLog){
       //落盘
       log.info(mockLog);
       //根据日志的类型，发送到kafka的不同主题中去
       //将接收到的字符串数据转换为json对象
       JSONObject jsonObject = JSON.parseObject(mockLog);
       JSONObject startJson = jsonObject.getJSONObject("start");
       
       if(startJson != null){
           //启动日志
           kafkaTemplate.send("gmall_start_1016",mockLog);
       }else {
           kafkaTemplate.send("gmall_event_1016",mockLog);
       
       }
       
       return "success";
        
    }
            
            
    
}
