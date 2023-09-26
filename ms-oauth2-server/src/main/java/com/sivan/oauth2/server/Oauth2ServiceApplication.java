package com.sivan.oauth2.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author 不知名网友鑫
 * @Date 2023/9/5
 **/
@MapperScan("com.sivan.oauth2.server.mapper")
@SpringBootApplication
public class Oauth2ServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(Oauth2ServiceApplication.class , args);
    }
}
