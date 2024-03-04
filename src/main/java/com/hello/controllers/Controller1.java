package com.hello.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class Controller1 {
    @ResponseBody
    @RequestMapping("/hello")
    public String hello() {
        return "hello world";
    }

}
