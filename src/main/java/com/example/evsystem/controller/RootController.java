package com.example.evsystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootController {

    private final String docsUrl;

    public RootController(@Value("${app.docs.url:/swagger-ui/index.html}") String docsUrl) {
        this.docsUrl = docsUrl;
    }

    @GetMapping("/")
    public String redirectToHome() {
        return "redirect:/ui/home";
    }

    @GetMapping({"/docs", "/docs/"})
    public String redirectToDocs() {
        return "redirect:" + docsUrl;
    }
}
