package com.mike.schedule.server;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.Collections;

/**
 * @author michaeltang608@gmail.com
 * @date 2022/4/25.
 */
public class PlusGenerator {

    public static void main(String[] args) {
        FastAutoGenerator.create("jdbc:mysql://localhost:3306/schedule", "root", "123456")
                .globalConfig(builder -> {
                    builder.outputDir("/Users/apple/Documents/code/mike/schedule/schedule-server/src/main/java");
                })
                .packageConfig(builder -> {
                    builder
                            .parent("com.mike.schedule.server.orm")
                            .pathInfo(Collections.singletonMap(
                                    OutputFile.xml,
                                    "/Users/apple/Documents/code/mike/schedule/schedule-server/src/main/resources/mappers"));
                })
                .strategyConfig(builder -> {
                    builder.addInclude("trigger_log")
                            .entityBuilder()
                            .enableActiveRecord().enableLombok().enableTableFieldAnnotation();
                })
                .templateEngine(new FreemarkerTemplateEngine())
                .templateConfig(builder -> {
                    builder.controller("").service("").serviceImpl("");
                })
                .execute();
    }
}
