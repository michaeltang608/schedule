package com.mike.schedule.server.controller.param;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * @author michaeltang608@gmail.com
 * @date 2022/6/15.
 */

@Data
@Accessors(chain = true)
public class DeleteTaskParam {

    @NotNull
    private String taskName;


}
