package com.mike.schedule.server.utils;

import java.util.Random;

/**
 * @author michaeltang608@gmail.com
 * @date 2022/6/17.
 */
public class RandomUtil {
//    static Ran
    public static int randomInt(int bound){
        Random r = new Random();
        return r.nextInt(bound);
    }

    public static void main(String[] args) {

        System.out.println(randomInt(3));
    }
}
