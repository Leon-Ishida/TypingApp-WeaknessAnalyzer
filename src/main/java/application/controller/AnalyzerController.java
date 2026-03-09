package application.controller;

import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
public class AnalyzerController {
    @GetMapping("/hello")
    public String hello(@RequestParam(defaultValue = "test") String param) {
        return String.format("hello world. param is %s", param);
    }

    @GetMapping("/test")
    public Map<String, String> test() {
        return Map.of("message", "動いた", "status", "ok");
    }

    @PostMapping("/echo")
    public Map<String, String> echo(@RequestBody Map<String, String> input) {
        //TODO: process POST request
        
        return input;
    }
    
    
}
