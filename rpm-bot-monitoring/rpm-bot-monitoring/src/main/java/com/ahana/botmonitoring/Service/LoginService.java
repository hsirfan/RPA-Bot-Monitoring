package com.ahana.botmonitoring.Service;

import com.ahana.botmonitoring.Entity.UserModel;
import com.ahana.botmonitoring.Mapper.UserMapper;
import com.ahana.botmonitoring.Repository.UserRepository;
import com.ahana.botmonitoring.Security.CustomUserDetailsService;
import com.ahana.botmonitoring.Security.JwtUtil;
import com.ahana.botmonitoring.Security.SessionService;
import com.ahana.botmonitoring.generated.model.LoginDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class LoginService {
    
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;

    public LoginService(UserRepository userRepository, 
                       JwtUtil jwtUtil, 
                       UserMapper userMapper,
                       CustomUserDetailsService userDetailsService,
                       PasswordEncoder passwordEncoder,
                       SessionService sessionService) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.sessionService = sessionService;
    }

    public Map<String, Object> login(LoginDTO loginDTO) {
        try {
            log.debug("Login attempt for email: {}", loginDTO.getEmailID());
            
            UserModel user = userRepository.findByEmailID(loginDTO.getEmailID());

            if (user == null) {
                log.warn("Login failed: User not found with email: {}", loginDTO.getEmailID());
                throw new RuntimeException("Invalid email address");
            }

            // For backward compatibility: support both plain text and encoded passwords
            // In production, always use passwordEncoder.matches()
            boolean passwordMatches = false;
            if (user.getPassword() != null && loginDTO.getPassword() != null) {
                String storedPassword = user.getPassword();
                String providedPassword = loginDTO.getPassword();
                
                // Check if stored password is a BCrypt hash (starts with $2a$, $2b$, or $2y$)
                if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
                    // Password is BCrypt encoded, use password encoder
                    passwordMatches = passwordEncoder.matches(providedPassword, storedPassword);
                    log.debug("Password comparison: BCrypt encoded");
                } else {
                    // Password is plain text, do direct comparison
                    passwordMatches = storedPassword.equals(providedPassword);
                    log.debug("Password comparison: Plain text");
                }
            }

            if (!passwordMatches) {
                log.warn("Login failed: Invalid password for email: {}", loginDTO.getEmailID());
                throw new RuntimeException("Invalid password");
            }

            // Load user details for token generation
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmailID());
            
            // Get user role (default to USER if not set)
            String role = (user.getRole() != null && !user.getRole().isEmpty()) 
                    ? user.getRole().toUpperCase() 
                    : "USER";

            // Generate JWT token
            String token = jwtUtil.generateToken(userDetails, role);

            // Create or update user session (30-minute inactivity timeout)
            sessionService.createOrUpdateSession(user.getEmailID());

            // Build response map
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("token", token);
            response.put("user", userMapper.toDTO(user));  // include full user details as DTO
            response.put("role", role);
            response.put("sessionTimeoutMinutes", 30);
            response.put("remainingSessionTime", sessionService.getRemainingSessionTime(user.getEmailID()));

            log.info("User {} logged in successfully with role: {}", user.getEmailID(), role);
            return response;
        } catch (RuntimeException e) {
            log.error("Login error: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during login: {}", e.getMessage(), e);
            throw new RuntimeException("Login failed: " + e.getMessage(), e);
        }
    }
}
