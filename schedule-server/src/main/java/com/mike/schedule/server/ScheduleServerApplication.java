package com.mike.schedule.server;

import com.mike.schedule.server.core.TriggerExecutor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author michaeltang608@gmail.com
 * @date 2022/6/15.
 */

@SpringBootApplication
public class ScheduleServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScheduleServerApplication.class, args);
        TriggerExecutor.start();
    }

}
