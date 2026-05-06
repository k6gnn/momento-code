package com.momento.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Component
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(FirebaseAuthenticationFilter.class);

    /**
     * Prevent Spring Boot from also registering this filter outside the Security
     * filter chain (which would apply it twice – once by Security, once by the
     * servlet container's normal FilterRegistrationBean scan).
     */
    @Bean
    public FilterRegistrationBean<FirebaseAuthenticationFilter> disableAutoRegistration(
            FirebaseAuthenticationFilter filter) {
        FilterRegistrationBean<FirebaseAuthenticationFilter> reg = new FilterRegistrationBean<>(filter);
        reg.setEnabled(false);
        return reg;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                String token = header.substring(7);
                FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(token);

                // Use UUID.nameUUIDFromBytes on a namespaced byte sequence to reduce
                // collision risk vs. bare UID strings. The Firebase UID is prefixed
                // with a fixed namespace so different short UIDs always differ.
                String namespacedUid = "firebase:" + decoded.getUid();
                UUID internalId = UUID.nameUUIDFromBytes(namespacedUid.getBytes(StandardCharsets.UTF_8));

                var principal = new AuthenticatedUser(
                        internalId,
                        decoded.getUid(),
                        decoded.getEmail(),
                        decoded.getName());
                var auth = new UsernamePasswordAuthenticationToken(
                        principal, token, List.of(new SimpleGrantedAuthority("ROLE_USER")));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception e) {
                // Invalid or expired token — request continues unauthenticated
                // and Spring Security will reject it at the authorisation layer.
                log.warn("Firebase token verification failed: {}", e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
}
