package com.mike.schedule.server.controller;

import com.mike.schedule.core.utils.GsonUtil;
import com.mike.schedule.server.orm.entity.TaskInfo;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author michaeltang608@gmail.com
 * @date 2022/6/15.
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @RequestMapping("/demo1")
    public Object test1(){
        TaskInfo taskInfo = TaskInfo.builder().build().selectById(1);
        return taskInfo;
    }
}
