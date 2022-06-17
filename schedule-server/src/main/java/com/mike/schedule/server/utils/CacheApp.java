package com.mike.schedule.server.utils;

import com.mike.schedule.server.controller.param.AppParam;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author michaeltang608@gmail.com
 * @date 2022/6/15.
 */
@Slf4j
public class CacheApp {

    private static long TTL = 1000 * 10;
    private static final String SEP = ";;;;";
    private static ConcurrentMap<String, Set<String>> appAddress = new ConcurrentHashMap<>();

    //for the sake of faster retrieve
    private static ConcurrentMap<String, List<String>> appAddress2 = new ConcurrentHashMap<>();
    private static ConcurrentMap<String, Long> addressAppTtl = new ConcurrentHashMap<>();

//    private static Ra;


    public static void add(AppParam param) {
        // if not already registered, register
        if (addressAppTtl.get(param.getAppAddr().concat(SEP).concat(param.getAppName())) == null) {
            Set<String> addrSet = appAddress.computeIfAbsent(param.getAppName(), k -> new HashSet<>());
            synchronized (addrSet) {
                addrSet.add(param.getAppAddr());
                syncAppAddress2(param.getAppName(), addrSet);
            }
        }
        //update heartbeat time
        addressAppTtl.put(param.getAppAddr().concat(SEP).concat(param.getAppName()), System.currentTimeMillis());

    }

    private static void syncAppAddress2(String app, Set<String> addrSet) {
        appAddress2.put(app, new ArrayList<>(addrSet));
    }

    // evict timeout address
    public static void startRefresh() {
        Executors.newScheduledThreadPool(1)
                .scheduleWithFixedDelay(
                        () -> {
                            List<String> toEvict = new ArrayList<>();
                            addressAppTtl.forEach((k, v) -> {
                                if (System.currentTimeMillis() > (v + TTL)) {
                                    addressAppTtl.remove(k);
                                    toEvict.add(k);
                                }
                            });
                            if (toEvict.size() > 0) {
                                toEvict.forEach(k -> {
                                    String[] ary = k.split(SEP);
                                    String addr = ary[0];
                                    String app = ary[1];
                                    Set<String> addrSet = appAddress.computeIfAbsent(app, k_ -> new HashSet<>());
                                    synchronized (addrSet) {
                                        addrSet.remove(addr);
                                        syncAppAddress2(app, addrSet);
                                    }

                                });
                            }

                        },
                        1,
                        1,
                        TimeUnit.SECONDS);
    }

    public static String selectUrl(String appName) {

        return Optional.ofNullable(appAddress2.get(appName))
                .filter(x->x.size()>0)
                .map(x->x.get(RandomUtil.randomInt(x.size())))
                .orElse(null);
    }
}

