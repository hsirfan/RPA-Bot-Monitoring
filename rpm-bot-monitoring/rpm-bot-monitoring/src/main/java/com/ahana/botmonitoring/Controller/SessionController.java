package com.ahana.botmonitoring.Controller;

import com.ahana.botmonitoring.Security.JwtUtil;
import com.ahana.botmonitoring.Security.SessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@RestController
@CrossOrigin(origins = "${app.cors.allowed-origin}")
public class SessionController {

    private final SessionService sessionService;
    private final JwtUtil jwtUtil;

    public SessionController(SessionService sessionService, JwtUtil jwtUtil) {
        this.sessionService = sessionService;
        this.jwtUtil = jwtUtil;
    }

   
    @GetMapping("/api/session/validate")
    public ResponseEntity<Map<String, Object>> validateSession(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String username = null;
            String token = null;
            
            // Extract token from header
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
                try {
                    username = jwtUtil.extractUsername(token);
                } catch (Exception e) {
                    log.warn("Invalid token in session validation: {}", e.getMessage());
                }
            }
            
            // Also try to get from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (username == null && authentication != null && authentication.isAuthenticated()) {
                username = authentication.getName();
            }
            
            if (username == null || username.equals("anonymousUser")) {
                response.put("authenticated", false);
                response.put("message", "No active session found");
                return ResponseEntity.status(401).body(response);
            }
            
            // Check if session is active
            if (!sessionService.isSessionActive(username)) {
                response.put("authenticated", false);
                response.put("message", "Session expired due to inactivity");
                response.put("expired", true);
                return ResponseEntity.status(401).body(response);
            }
            
            // Validate token if provided
            if (token != null && !jwtUtil.validateToken(token)) {
                response.put("authenticated", false);
                response.put("message", "Invalid or expired token");
                return ResponseEntity.status(401).body(response);
            }
            
            // Extract role from token or authorities
            String role = null;
            if (token != null) {
                try {
                    role = jwtUtil.extractRole(token);
                } catch (Exception e) {
                    log.warn("Could not extract role from token: {}", e.getMessage());
                }
            }
            
            if (role == null && authentication != null) {
                role = authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .filter(auth -> auth.startsWith("ROLE_"))
                        .map(auth -> auth.substring(5)) // Remove "ROLE_" prefix
                        .findFirst()
                        .orElse("USER");
            }
            
            sessionService.updateLastActivity(username);
            
            response.put("authenticated", true);
            response.put("username", username);
            response.put("role", role != null ? role : "USER");
            response.put("remainingSessionTime", sessionService.getRemainingSessionTime(username));
            response.put("sessionTimeoutMinutes", 30);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error validating session: {}", e.getMessage(), e);
            response.put("authenticated", false);
            response.put("message", "Error validating session: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

   
    @GetMapping("/api/session/check-admin")
    public ResponseEntity<Map<String, Object>> checkAdminRole(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String username = null;
            String token = null;
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
                try {
                    username = jwtUtil.extractUsername(token);
                } catch (Exception e) {
                    log.warn("Invalid token in admin check: {}", e.getMessage());
                }
            }
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (username == null && authentication != null && authentication.isAuthenticated()) {
                username = authentication.getName();
            }
            
            if (username == null || username.equals("anonymousUser")) {
                response.put("isAdmin", false);
                response.put("authenticated", false);
                response.put("message", "No active session found");
                return ResponseEntity.status(401).body(response);
            }
            
            // Check session
            if (!sessionService.isSessionActive(username)) {
                response.put("isAdmin", false);
                response.put("authenticated", false);
                response.put("message", "Session expired");
                return ResponseEntity.status(401).body(response);
            }
            
            // Check role
            String role = null;
            if (token != null) {
                try {
                    role = jwtUtil.extractRole(token);
                } catch (Exception e) {
                    // Ignore
                }
            }
            
            if (role == null && authentication != null) {
                boolean hasAdminRole = authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(auth -> auth.equals("ROLE_ADMIN"));
                
                response.put("isAdmin", hasAdminRole);
                response.put("authenticated", true);
                response.put("role", hasAdminRole ? "ADMIN" : "USER");
                return ResponseEntity.ok(response);
            }
            
            boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
            response.put("isAdmin", isAdmin);
            response.put("authenticated", true);
            response.put("role", role != null ? role : "USER");
            
            // Update last activity
            sessionService.updateLastActivity(username);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error checking admin role: {}", e.getMessage(), e);
            response.put("isAdmin", false);
            response.put("authenticated", false);
            response.put("message", "Error checking role: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}

