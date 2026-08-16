package application.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class TestViewController {
    @GetMapping("/")
    public String test() {
        return "test";
    }
    
}
