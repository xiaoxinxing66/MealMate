package com.sivan.feeds;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author 不知名网友鑫
 * @Date 2023/9/11
 **/
@MapperScan("com.sivan.feeds.mapper")
@SpringBootApplication
public class FeedsApplication {
    public static void main(String[] args) {
        SpringApplication.run(FeedsApplication.class,args);
    }
}
