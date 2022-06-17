package com.mike.schedule.core.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author michaeltang608@gmail.com
 * @date 2022/6/13.
 * 创建netty server实例
 */
public class EmbedServer {

    public static void start(int port) {
        EventLoopGroup accepter = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();

        AtomicLong cnt = new AtomicLong(0);
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                100,
                200,
                10L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(2000),
                r -> new Thread(r, "schedule-job-" + cnt.incrementAndGet()),
                (r, executor) -> {
                    throw new RuntimeException("schedule-job, threadPool is EXHAUSTED!");
                });

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap
                .group(accepter, worker)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new IdleStateHandler(0, 0, 30 * 3, TimeUnit.SECONDS))  // beat 3N, close if idle
                                .addLast(new HttpServerCodec())
                                .addLast(new HttpObjectAggregator(5 * 1024 * 1024))  // merge request & reponse to FULL
                                .addLast(new HandlerExecutor(threadPoolExecutor));
                    }
                })
        ;

        try {
            ChannelFuture future = bootstrap.bind(port).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
