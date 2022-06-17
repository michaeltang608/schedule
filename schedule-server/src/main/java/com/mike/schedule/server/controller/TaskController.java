package com.mike.schedule.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mike.schedule.server.controller.param.AddTaskParam;
import com.mike.schedule.server.controller.param.DeleteTaskParam;
import com.mike.schedule.server.controller.param.UpdateTaskParam;
import com.mike.schedule.server.orm.entity.TaskInfo;
import com.mike.schedule.server.utils.CronUtils;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;

/**
 * @author michaeltang608@gmail.com
 * @date 2022/6/15.
 */

@RestController
@RequestMapping("/task")
public class TaskController {

    @PostMapping
    public Boolean addTask(@RequestBody @Valid AddTaskParam param) {
        return TaskInfo.builder()
                .appName(param.getAppName())
                .taskName(param.getTaskName())
                .cron(param.getTaskCron())
                .triggerNextTime(CronUtils.queryNextTime(param.getTaskCron(), new Date()))
                .build().insert();

    }

    @PutMapping
    public Boolean updateTask(@RequestBody @Valid UpdateTaskParam param) {
        LambdaQueryWrapper<TaskInfo> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(TaskInfo::getTaskName, param.getTaskName());
        TaskInfo taskInfo = TaskInfo.builder().build().selectOne(wrapper);
        Assert.isTrue(taskInfo != null, "taskName not exist");
        return TaskInfo.builder()
                .id(taskInfo.getId()).cron(param.getTaskCron())
                .triggerNextTime(CronUtils.queryNextTime(param.getTaskCron(), new Date()))
                .build().updateById();

    }

    @DeleteMapping
    public Boolean deleteTask(@RequestBody @Valid DeleteTaskParam param) {
        LambdaQueryWrapper<TaskInfo> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(TaskInfo::getTaskName, param.getTaskName());
        TaskInfo taskInfo = TaskInfo.builder().build().selectOne(wrapper);
        Assert.isTrue(taskInfo != null, "taskName not exist");
        return TaskInfo.builder()
                .id(taskInfo.getId())
                .build().deleteById();

    }
}
