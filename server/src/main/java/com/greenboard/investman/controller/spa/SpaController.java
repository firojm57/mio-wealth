package com.greenboard.investman.controller.spa;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping(value = {
            "/",
            "/overview",
            "/investments",
            "/balance",
            "/cash-flow",
            "/expenses",
            "/performance",
            "/goals",
            "/settings",
            "/login",
            "/signup"
    })
    public String forwardToSpa() {
        return "forward:/index.html";
    }
}
