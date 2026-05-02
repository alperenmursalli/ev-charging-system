package com.example.evsystem.config;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
public class CurrentUserModelAdvice {

    @ModelAttribute("currentUsername")
    public String currentUsername(Authentication authentication) {
        return isLoggedIn(authentication) ? authentication.getName() : "";
    }

    @ModelAttribute("isAuthenticated")
    public boolean isAuthenticated(Authentication authentication) {
        return isLoggedIn(authentication);
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin(Authentication authentication) {
        return isLoggedIn(authentication) && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    @ModelAttribute("currentUserRoleLabel")
    public String currentUserRoleLabel(Authentication authentication) {
        if (!isLoggedIn(authentication)) {
            return "";
        }
        return isAdmin(authentication) ? "ADMIN" : "USER";
    }

    private boolean isLoggedIn(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
