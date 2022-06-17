package com.mike.schedule.server;


import org.quartz.CronExpression;

import java.util.*;

/**
 * @author michaeltang608@gmail.com
 * @date 2022/6/16.
 */
public class Test {

    public static void main(String[] args) throws Exception {

        List<String> l1 = new LinkedList<>();
        Set<String> s1 = new HashSet<>();
        l1 = new ArrayList<>(s1);


        try {
            CronExpression cronExpression = new CronExpression("0 5 0/5 * * ?");
            Date next = cronExpression.getNextValidTimeAfter(new Date() );
            System.out.println(next);
        } catch (Exception e) {
            throw new IllegalArgumentException("无效的cron表达式");
        }
    }
}
