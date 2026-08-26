package application.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class CustomAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute("unLoginedResults") != null) {
            return "/test/inherit";
        } else {
            return "/";
        }
    }
}
