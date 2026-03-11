package application.controller;

import org.springframework.web.bind.annotation.RestController;

import application.entity.Memo;
import application.repository.MemoRepository;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
public class AnalyzerController {
    @Autowired
    private MemoRepository memoRepository;

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

    @PostMapping("/memo")
    public Memo createMemo(@RequestBody Map<String, String> input) {
        Memo memo = new Memo(input.get("text"));
        return memoRepository.save(memo);
    }

    @GetMapping("/memo")
    public List<Memo> getAllMemos() {
        return memoRepository.findAll();
    }
    
     
}
