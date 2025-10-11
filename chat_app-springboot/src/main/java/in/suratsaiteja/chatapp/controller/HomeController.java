package in.suratsaiteja.chatapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String landing() {
        return "forward:/landing.html";
    }
}
