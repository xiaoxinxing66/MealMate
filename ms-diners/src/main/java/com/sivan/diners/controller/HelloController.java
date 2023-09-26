package com.sivan.diners.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author 不知名网友鑫
 * @Date 2023/6/28
 **/
@RestController
@RequestMapping("hello")
public class HelloController {
    @GetMapping
    public String hello(String name){
        return "hello"  + name;
    }
}
