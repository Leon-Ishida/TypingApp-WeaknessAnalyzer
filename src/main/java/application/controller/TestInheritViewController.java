package application.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class TestInheritViewController {
    @GetMapping("/test/inherit")
    public String testInherit() {
        return "test_inherit";
    }
    
}
