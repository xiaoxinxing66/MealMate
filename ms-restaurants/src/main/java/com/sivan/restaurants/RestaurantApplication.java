package com.sivan.restaurants;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author 不知名网友鑫
 * @Date 2023/9/15
 **/
@SpringBootApplication
@MapperScan("com.sivan.restaurants.mapper")
public class RestaurantApplication {
    public static void main(String[] args) {
        SpringApplication.run(RestaurantApplication.class , args);
    }
}
