package com.mike.schedule.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class ScheduleClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScheduleClientApplication.class, args);
	}

}
