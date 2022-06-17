package com.mike.schedule.core;

import java.lang.annotation.*;

/**
 * @author michaeltang608@gmail.com
 * @date 2022/6/14.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ScheduleTask {

    String name();
}
