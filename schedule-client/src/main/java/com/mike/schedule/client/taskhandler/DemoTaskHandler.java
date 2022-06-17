package com.mike.schedule.client.taskhandler;

import com.mike.schedule.core.ScheduleTask;
import com.mike.schedule.core.TaskHandler;
import org.springframework.stereotype.Component;

/**
 * @author michaeltang608@gmail.com
 * @date 2022/6/14.
 */

@Component
public class DemoTaskHandler implements TaskHandler {


    @ScheduleTask(name = "test")
    public void test() {
        System.out.println("this is schedule task");
    }
}
