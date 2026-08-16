package application.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class PracticeViewController {
    @GetMapping("/practice")
    public String practice() {
        return "practice";
    }
    
}
