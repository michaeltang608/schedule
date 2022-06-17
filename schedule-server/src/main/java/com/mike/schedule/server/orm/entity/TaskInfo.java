package com.mike.schedule.server.orm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author 作者
 * @since 2022-06-15
 */
@Data
@Builder
@TableName("task_info")
public class TaskInfo extends Model<TaskInfo> {


    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * appName
     */
    @TableField("app_name")
    private String appName;

    /**
     * taskName
     */
    @TableField("task_name")
    private String taskName;

    /**
     * cron表达式
     */
    @TableField("cron")
    private String cron;

    @TableField("trigger_last_time")
    private Date triggerLastTime;

    @TableField("trigger_next_time")
    private Date triggerNextTime;

    @TableField("update_time")
    private Date updateTime;


}
