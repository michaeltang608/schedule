package com.mike.schedule.server.utils;

import org.omg.Messaging.SYNC_WITH_TRANSPORT;
import org.quartz.CronExpression;

import java.util.Date;

/**
 * @author michaeltang608@gmail.com
 * @date 2022/6/16.
 */
public class CronUtils {

    public static Date queryNextTime(String cron, Date startTime) {

        try {
            CronExpression cronExpression = new CronExpression(cron);
            Date nextTime =  cronExpression.getNextValidTimeAfter(startTime);
            if(nextTime.getTime() < System.currentTimeMillis()){
                nextTime =  queryNextTime(cron, new Date());
            }
            return nextTime;
        } catch (Exception e) {
            throw new IllegalArgumentException("无效的cron表达式");
        }
    }

    public static void main(String[] args) {
        queryNextTime("0/5 * * * * ?", new Date());
    }
}
