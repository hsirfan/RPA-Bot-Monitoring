package com.ahana.botmonitoring.Controller;

import com.ahana.botmonitoring.Security.JwtUtil;
import com.ahana.botmonitoring.Security.SessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@CrossOrigin(origins = "${app.cors.allowed-origin}")
public class LogoutController {

    private final SessionService sessionService;
    private final JwtUtil jwtUtil;

    public LogoutController(SessionService sessionService, JwtUtil jwtUtil) {
        this.sessionService = sessionService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String username = null;
            
            // Extract username from token if provided
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    username = jwtUtil.extractUsername(token);
                } catch (Exception e) {
                    log.warn("Invalid token during logout: {}", e.getMessage());
                }
            }
            
            // Also try to get from security context
            if (username == null) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated()) {
                    username = authentication.getName();
                }
            }
            
            if (username != null) {
                sessionService.removeSession(username);
                log.info("User {} logged out successfully", username);
                response.put("success", true);
                response.put("message", "Logged out successfully");
            } else {
                response.put("success", false);
                response.put("message", "No active session found");
            }
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Logout failed: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}

