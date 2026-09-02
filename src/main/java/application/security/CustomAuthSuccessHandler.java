package application.security;

import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import application.repository.TestResultRepository;
import application.session.GuestResultsSessionStore;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class CustomAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final TestResultRepository repository;

    public CustomAuthSuccessHandler(TestResultRepository repository) {
        super();
        this.repository = repository;
    }
    
    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return "/";
        }

        Set<Long> candidateIds = GuestResultsSessionStore.getSessionCandidateIds(session.getAttribute("unLoginedResults"));

        if (candidateIds.isEmpty()) {
            return "/";
        }
        
        boolean hasValidCandidates = repository.existsByIdInAndUserIdIsNullAndTimestampGreaterThanEqual(candidateIds, LocalDateTime.now().minusMinutes(30));

        if (hasValidCandidates) {
            return "/test/inherit";
        } else {
            return "/";
        }
    }
}
