package application.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class AnalysisViewController {
    @GetMapping("/analysis")
    public String analysis() {
        return "analysis";
    }
    
}
