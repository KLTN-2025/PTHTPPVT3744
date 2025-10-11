package com.example.do_an_tot_nghiep.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("")
public class HomeController {
    @RequestMapping({"", "/home"})
    public String home() {
        return "home";
    }
}
