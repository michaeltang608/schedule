package com.mike.schedule.server.core;

import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mike.schedule.server.orm.entity.TaskInfo;
import com.mike.schedule.server.orm.entity.TriggerLog;
import com.mike.schedule.server.utils.CacheApp;
import com.mike.schedule.server.utils.CronUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author michaeltang608@gmail.com
 * @date 2022/6/16.
 */
@Slf4j
public class TriggerExecutor {

    private static final ScheduledExecutorService scheduleExecutor =
            Executors.newScheduledThreadPool(
                    Runtime.getRuntime().availableProcessors() * 2,
                    new NamedThreadFactory("task-schedule", true));

    private static final ConcurrentHashMap<Integer, String> isExecuting = new ConcurrentHashMap<>();

    static public void start() {
        //iterate tasks, update nextTriggerTime
        new Thread(
                TriggerExecutor::patrolTasks, "task-iterator"
        ).start();
        CacheApp.startRefresh();
    }

    private static void patrolTasks() {
        while (true) {
            try {
                doIterateTask();
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void doIterateTask() {

        // todo, may introduce distributed lock here in the future to support scalability of schedule server
        /*
        query now-5 <=trigger_next_time <= now+5
        if now <= trigger_next_time <= now+5, put in ring
        if now-5 <=trigger_next_time < =now, execute At once
        ignore for this moment : trigger_next_time < now-5
         */
        long now = System.currentTimeMillis();
        LambdaQueryWrapper<TaskInfo> wrapper = Wrappers.lambdaQuery();
        wrapper.ge(TaskInfo::getTriggerNextTime, new Date(now - Duration.ofHours(1).toMillis()));
        wrapper.le(TaskInfo::getTriggerNextTime, new Date(now + 5000));
        List<TaskInfo> taskInfoList = TaskInfo.builder().build().selectList(wrapper);

        if (CollectionUtils.isEmpty(taskInfoList)) return;
        List<TaskInfo> toTriggerList = new ArrayList<>();
        taskInfoList.forEach(x -> {
            String preVal = isExecuting.put(x.getId(), x.getTaskName());
            if (preVal == null)
                toTriggerList.add(x);
        });
        if (CollectionUtils.isEmpty(toTriggerList)) return;
        List<TaskInfo> copyAll = new ArrayList<>();
        toTriggerList.forEach(x -> copyAll.add(TaskInfo.builder().id(x.getId()).cron(x.getCron()).build()));

        //old task, only refresh
        List<TaskInfo> missedTasks = toTriggerList.stream()
                .filter(x -> x.getTriggerNextTime().getTime() <= (now - Duration.ofMinutes(5).toMillis()))
                .collect(Collectors.toList());
        if(! CollectionUtils.isEmpty(missedTasks)){
            handleMissedTasks(missedTasks);
        }

        toTriggerList.removeAll(missedTasks);
        if (!CollectionUtils.isEmpty(toTriggerList)) {
            List<TaskInfo> executeInstantlyList =
                    toTriggerList.stream()
                            .filter(x -> x.getTriggerNextTime().getTime() <= now)
                            .collect(Collectors.toList());

            triggerInstant(executeInstantlyList);
            toTriggerList.removeAll(executeInstantlyList);
        }
        if (!CollectionUtils.isEmpty(toTriggerList)) {
            triggerSchedule(toTriggerList);
        }
    }

    private static void handleMissedTasks(List<TaskInfo> missedTasks) {
        missedTasks.forEach(t->{
            refreshNextTriggerTime(t.getId(), t.getCron(), t.getTriggerNextTime());
            isExecuting.remove(t.getId());
        });
    }

    private static void triggerSchedule(List<TaskInfo> list) {
        for (TaskInfo task : list) {
            long delay = task.getTriggerNextTime().getTime() - System.currentTimeMillis();
            delay = Math.max(delay,1);
            scheduleExecutor.schedule(() ->
                            doTrigger(task),
                    delay, TimeUnit.MILLISECONDS);
        }
    }

    private static void triggerInstant(List<TaskInfo> list) {
        for (TaskInfo task : list) {
            doTrigger(task);
        }
    }

    private static void doTrigger(TaskInfo task) {
        try {
            refreshNextTriggerTime(task.getId(), task.getCron(), task.getTriggerNextTime());
            String url = CacheApp.selectUrl(task.getAppName());
            if (url != null) {
                TriggerLog.builder().taskId(task.getId()).createTime(new Date()).build().insert();
                url = assembleUrl(url, task.getTaskName());
                HttpUtil.post(url, "", 1000);
                TaskInfo.builder().id(task.getId()).triggerLastTime(new Date()).build().updateById();

            } else {
                log.error("找不到服务了, appName={}", task.getAppName());
            }
        } catch (Exception e) {
            log.error("doTrigger error", e);
        } finally {
            isExecuting.remove(task.getId());
        }


    }

    private static String assembleUrl(String url, String taskName) {
        if (!url.endsWith("/"))
            url = url.concat("/");
        return url.concat(taskName);
    }

    private static void refreshNextTriggerTime(Integer taskId, String cron, Date triggerNextTime) {
        boolean res =
                TaskInfo.builder()
                        .id(taskId).triggerNextTime(CronUtils.queryNextTime(cron, triggerNextTime))
                        .build().updateById();
        log.info("refresh result={}", res);
    }


}
