package application.controller;

import java.util.List;
import java.util.Set;

import application.dto.TestInheritResponse;
import application.service.TestInheritService;
import application.session.GuestResultsSessionStore;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class TestInheritAPIController {
    private final TestInheritService testInheritService;

    public TestInheritAPIController(TestInheritService testInheritService) {
        this.testInheritService = testInheritService;
    }

    @GetMapping("/inherit")
    public List<TestInheritResponse> getInheritableList(HttpSession session) {
        Set<Long> sessionCandidateIds = GuestResultsSessionStore.getSessionCandidateIds(session.getAttribute("unLoginedResults"));
        return testInheritService.makeInheritList(sessionCandidateIds);
    }

    @PostMapping("/inherit/post")
    public int postSelectedResults(@RequestBody Set<Long> id, HttpSession session) {
         Set<Long> sessionCandidateIds = GuestResultsSessionStore.getSessionCandidateIds(session.getAttribute("unLoginedResults"));
        int response = testInheritService.tieResultsWithUser(id, sessionCandidateIds);
        if (response >= 0) {
            session.removeAttribute("unLoginedResults");
        }
        return response;
    }
}
