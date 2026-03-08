package com.example.demo.security;



import com.example.demo.service.FirebaseService;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Filter to validate the Firebase ID token from Authorization header,
 * get the user's role from Realtime DB and set an Authentication in SecurityContext.
 */
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

    private final FirebaseService firebaseService;

    public FirebaseAuthenticationFilter(FirebaseService firebaseService) {
        this.firebaseService = firebaseService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                FirebaseToken decoded = firebaseService.verifyIdToken(token);
                String uid = decoded.getUid();
                String email = decoded.getEmail();
                String role = firebaseService.getRoleForUid(uid);
                if (role == null) role = "ROLE_USER";

                // create authentication token with role as authority
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(uid, null, List.of(new SimpleGrantedAuthority(role)));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (FirebaseAuthException ex) {
                // invalid token -> leave unauthenticated (will be rejected by Spring Security if required)
                logger.debug("Invalid Firebase token: " + ex.getMessage());
            } catch (Exception ex) {
                logger.error("Error validating Firebase token", ex);
            }
        }

        filterChain.doFilter(request, response);
    }
}
