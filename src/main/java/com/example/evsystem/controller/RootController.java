package com.example.evsystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootController {

    private static final String MINTLIFY_DOCS_URL = "https://egeuniversity.mintlify.app/";

    @GetMapping("/")
    public String redirectToHome() {
        return "redirect:/ui/home";
    }

    @GetMapping("/docs")
    public String redirectToDocs() {
        return "redirect:" + MINTLIFY_DOCS_URL;
    }
}
