package com.sivan.diners;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * @Author 不知名网友鑫
 * @Date 2023/6/28
 **/
@MapperScan("com.sivan.diners.mapper")
@SpringBootApplication
public class DinersApplication {
    public static void main(String[] args) {
        SpringApplication.run(DinersApplication.class , args);
    }
}
