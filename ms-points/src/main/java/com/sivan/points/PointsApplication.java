package com.sivan.points;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author 不知名网友鑫
 * @Date 2023/9/12
 **/
@SpringBootApplication
@MapperScan("com.sivan.points.mapper")
public class PointsApplication {
    public static void main(String[] args) {
        SpringApplication.run(PointsApplication.class ,args);
    }
}
