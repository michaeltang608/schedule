package com.mike.schedule.core.server;

import com.mike.schedule.core.ScheduleTask;
import com.mike.schedule.core.TaskHandler;
import com.mike.schedule.core.utils.GsonUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author michaeltang608@gmail.com
 * @date 2022/6/13.
 */
@Slf4j
public class HandlerExecutor extends SimpleChannelInboundHandler<FullHttpRequest> {
    private ThreadPoolExecutor executor;
    private static ConcurrentMap<String, Task> cachedTasks = new ConcurrentHashMap<>();
    public HandlerExecutor(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        // request parse
        String reqJson = msg.content().toString(CharsetUtil.UTF_8);
        String uri = msg.uri();
        HttpMethod httpMethod = msg.method();
        boolean keepAlive = HttpUtil.isKeepAlive(msg);

        executor.execute(new Runnable() {
            @Override
            public void run() {
                // do invoke
                Object responseObj = process(httpMethod, uri.substring(1), reqJson);

                // to json
                String respJson = GsonUtil.toJson(responseObj);

                // write response
                writeResponse(ctx, keepAlive, respJson);
            }
        });


    }
    private Object process(HttpMethod httpMethod, String taskName, String reqJson) {
        log.info("requestData={}, taskName={}, httpMethod={}", reqJson, taskName, httpMethod);
        if(! cachedTasks.containsKey(taskName)) return "no such task";
        Task task = cachedTasks.get(taskName);
        try {
            //todo: the following part can be optimized to async mode for performance concern, and potential error in
            // task handling process can be report back to schedule-center asynchronously
            task.getMethod().invoke(task.getBean());
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "exception";
        }
    }

    private void writeResponse(ChannelHandlerContext ctx, boolean keepAlive, String responseJson) {
        FullHttpResponse response =
                new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpResponseStatus.OK,
                        Unpooled.copiedBuffer(responseJson, CharsetUtil.UTF_8));   //  Unpooled.wrappedBuffer(responseJson)
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");       // HttpHeaderValues.TEXT_PLAIN.toString()
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        if (keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        ctx.writeAndFlush(response);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class Student implements Serializable{
        private Integer age;
        private String name;

    }

    public static void manageTask(ApplicationContext applicationContext) {
        Map<String, TaskHandler> taskHandlerMap =
                applicationContext.getBeansOfType(TaskHandler.class, false, true);

        for (TaskHandler bean : taskHandlerMap.values()) {
            // method
            Method[] methods = bean.getClass().getDeclaredMethods();
            if (methods.length == 0) {
                continue;
            }
            for (Method executeMethod : methods) {
                ScheduleTask task = executeMethod.getAnnotation(ScheduleTask.class);
                // registry
                cacheTask(task.name(), bean, executeMethod);
            }
        }
    }

    private static void cacheTask(String name, TaskHandler bean, Method executeMethod) {
        Assert.isTrue(!cachedTasks.containsKey(name), "task name duplicated");
        executeMethod.setAccessible(true);
        cachedTasks.putIfAbsent(name, new Task(bean, executeMethod));
    }

    @Data
    static class Task {
        private TaskHandler bean;
        private Method method;

        public Task(TaskHandler bean, Method method) {
            this.bean = bean;
            this.method = method;
        }
    }

}
