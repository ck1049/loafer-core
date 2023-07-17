package com.loafer.core.utils;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;

import java.util.*;

public class MybatisPlusFastAutoGeneratorUtils {

    private static final String tables = "file_storage";

    public static void main(String[] args) {
        String outputDir = "src\\main\\java";
        Map<OutputFile, String> pathInfo = new HashMap<>();
        pathInfo.put(OutputFile.xml, "src\\main\\resources\\mapper");
        FastAutoGenerator.create("jdbc:mysql://127.0.0.1:3306/loafer_core?useUnicode=true&characterEncoding=utf8" +
                        "&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8", "root", "123456")
                .globalConfig(builder -> {
                    builder.author("loafer") // 设置作者
                            .enableSwagger() // 开启 swagger 模式
                            .outputDir(outputDir); // 指定输出目录
                })
                .packageConfig(builder -> {
                    builder.parent("com.loafer.core") // 设置父包名
//                            .moduleName("system") // 设置父包模块名
                            .entity("model")
                            .controller("")
                            .pathInfo(pathInfo); // 设置mapperXml生成路径
                })
                .templateConfig(builder -> builder.controller("")) // 不生成controller
                .strategyConfig(builder -> {
                    builder.addInclude(getTables(tables)) // 设置需要生成的表名
                            .entityBuilder().enableLombok(); // 使用lombok注解
//                            .addTablePrefix("t_", "c_"); // 设置过滤表前缀
                })
//                .templateEngine(new FreemarkerTemplateEngine()) // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .execute();
    }

    // 处理 all 情况
    protected static List<String> getTables(String tables) {
        return "all".equals(tables) ? Collections.emptyList() : Arrays.asList(tables.split(","));
    }

}
