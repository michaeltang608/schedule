package com.mike.schedule.server.controller;

import com.mike.schedule.server.controller.param.AppParam;
import com.mike.schedule.server.utils.CacheApp;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author michaeltang608@gmail.com
 * @date 2022/6/15.
 */
@RestController
@RequestMapping("/app")
public class AppController {
    @PostMapping("/heartbeat")
    public void heartBeat(@RequestBody @Valid AppParam param) {
        CacheApp.add(param);
    }
}
