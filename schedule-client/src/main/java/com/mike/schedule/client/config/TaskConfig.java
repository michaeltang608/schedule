package com.mike.schedule.client.config;

import com.mike.schedule.core.ScheduleSpringDispatcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author michaeltang608@gmail.com
 * @date 2022/6/15.
 */
@Configuration
public class TaskConfig {

    @Value("${schedule.config.serverAddr}")
    private String serverAddr;
    @Value("${schedule.config.port}")
    private Integer port;

    @Bean
    public ScheduleSpringDispatcher scheduleSpringDispatcher(){
        return new ScheduleSpringDispatcher(serverAddr, port);
    }
}
