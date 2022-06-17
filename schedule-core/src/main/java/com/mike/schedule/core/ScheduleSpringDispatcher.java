package com.mike.schedule.core;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import com.mike.schedule.core.server.EmbedServer;
import com.mike.schedule.core.server.HandlerExecutor;
import com.mike.schedule.core.utils.IpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.concurrent.TransferQueue;

/**
 * @author michaeltang608@gmail.com
 * @date 2022/6/15.
 * dispatch schedule task to corresponding task handler
 * 向外 汇报调度中心 serverAddr 自己的 appName 和 ip
 * 向内
 * - 启动netty服务接收 调度中心触发的任务
 * - 管理handler并分发task任务
 */
@Slf4j
public class ScheduleSpringDispatcher implements SmartInitializingSingleton, ApplicationContextAware, EnvironmentAware {

    private static ApplicationContext applicationContext;
    private Environment env;
    private String serverAddr;
    private Integer port;

    public ScheduleSpringDispatcher(String serverAddr, Integer port) {
        this.serverAddr = serverAddr;
        this.port = port;
    }

    @Override
    public void afterSingletonsInstantiated() {
        HandlerExecutor.manageTask(applicationContext);
        EmbedServer.start(port);
        asyncHeartBeat();
    }


    private void asyncHeartBeat() {
        new Thread(()->{
            while (true){
                try{
                    Thread.sleep(2000L);
                    heartBeat();

                }catch (Throwable e){
                    log.error(e.getMessage());
                }
            }
        }, "heartbeat").start();

    }

    private void heartBeat(){
        JSONObject json = new JSONObject();
        json.set("appName", env.getProperty("spring.application.name"));
        json.set("appAddr", String.format("http://%s:%s/", IpUtils.getLocalIpAddress(), port));
        String resp = HttpUtil.post(serverAddr.concat("/app/heartbeat"), json.toString(), 2000);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ScheduleSpringDispatcher.applicationContext = applicationContext;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.env = environment;
    }
}
