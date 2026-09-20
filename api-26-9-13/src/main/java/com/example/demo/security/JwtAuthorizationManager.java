package com.example.demo.security;

import java.util.Set;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.example.demo.common.CurrentUser;
import com.example.demo.security.authentication.JwtAuthorization;
import com.example.demo.security.jwt.JwtService;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtAuthorizationManager implements AuthorizationManager<Object>{

    @Value("${spring.profiles.active}")
    String environment;
    @Value("${devToken}")
    String devToken;
    @Value("${guestToken}")
    String guestToken;
    @Value("${guest.username}")
    String guestUsername;

    private static final Set<String> READ_ONLY_METHODS = Set.of("GET", "HEAD", "OPTIONS");
    /** POST-based list/query endpoints that are read-only (body carries the query). */
    private static final String[] READ_ONLY_POST_PREFIXES = {"/alarm", "/data", "/entry"};
    @Override
    @Nullable
    public AuthorizationDecision check(Supplier<Authentication> authentication,@Nullable Object object) {

        AuthorizationDecision authorizationDecision;
        JwtAuthorization jwtAuthorization = (JwtAuthorization)authentication.get();

        if(environment.equals("dev")&& jwtAuthorization.getCredentials().equals(devToken)) {
            jwtAuthorization.setAuthenticated(true);
            jwtAuthorization.setCurrentUser( CurrentUser.getDevFakeCurrentUser());
            SecurityContext emptyContext = SecurityContextHolder.createEmptyContext();
            emptyContext.setAuthentication(jwtAuthorization);
            SecurityContextHolder.setContext(emptyContext);
            return new AuthorizationDecision(true);
        }
        if(jwtAuthorization.getCredentials().equals(guestToken)) {
            if(!isReadOnlyRequest(object)) {
                return new AuthorizationDecision(false);
            }
            jwtAuthorization.setAuthenticated(true);
            jwtAuthorization.setCurrentUser(CurrentUser.getGuestFakeCurrentUser(guestUsername));
            SecurityContext emptyContext = SecurityContextHolder.createEmptyContext();
            emptyContext.setAuthentication(jwtAuthorization);
            SecurityContextHolder.setContext(emptyContext);
            return new AuthorizationDecision(true);
        }
        try {
            CurrentUser currentUser = JwtService.parseToken(jwtAuthorization.getCredentials().toString());
            jwtAuthorization.setCurrentUser(currentUser);
            jwtAuthorization.setAuthenticated(true);
            SecurityContext emptyContext = SecurityContextHolder.createEmptyContext();
            emptyContext.setAuthentication(jwtAuthorization);
            SecurityContextHolder.setContext(emptyContext);
            authorizationDecision = new AuthorizationDecision(true);
        } catch (Exception e) {
            authorizationDecision = new AuthorizationDecision(false);
        }
        return authorizationDecision;
    }

    /**
     * Guest token is read-only: only safe HTTP methods (GET/HEAD/OPTIONS) and the
     * POST-based list/query endpoints (frontend sends queries in the request body)
     * are allowed. Everything else (POST/PUT/DELETE/PATCH) is denied.
     */
    private boolean isReadOnlyRequest(@Nullable Object object) {
        if (!(object instanceof HttpServletRequest request)) {
            // Non-HTTP call (e.g. WebSocket handshake via WsAuthInterceptor) — not an API request.
            return true;
        }
        String method = request.getMethod();
        if (READ_ONLY_METHODS.contains(method.toUpperCase())) {
            return true;
        }
        if ("POST".equalsIgnoreCase(method)) {
            String path = request.getServletPath();
            for (String prefix : READ_ONLY_POST_PREFIXES) {
                if (path.matches(java.util.regex.Pattern.quote(prefix) + "/[^/]+/[^/]+")) {
                    return true;
                }
            }
        }
        return false;
    }
    
}
