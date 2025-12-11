package com.ahana.botmonitoring.Security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionService {

    private final Map<String, LocalDateTime> activeSessions = new ConcurrentHashMap<>();
    
    private static final int SESSION_TIMEOUT_MINUTES = 30;

    public void createOrUpdateSession(String username) {
        activeSessions.put(username, LocalDateTime.now());
    }

    public boolean isSessionActive(String username) {
        LocalDateTime lastActivity = activeSessions.get(username);
        if (lastActivity == null) {
            return false; 
        }
        
        LocalDateTime now = LocalDateTime.now();
        long minutesSinceLastActivity = java.time.Duration.between(lastActivity, now).toMinutes();
        
        if (minutesSinceLastActivity >= SESSION_TIMEOUT_MINUTES) {
            // Session expired due to inactivity
            activeSessions.remove(username);
            return false;
        }
        
        return true;
    }

    public void updateLastActivity(String username) {
        if (activeSessions.containsKey(username)) {
            activeSessions.put(username, LocalDateTime.now());
        }
    }

    public void removeSession(String username) {
        activeSessions.remove(username);
    }

    public long getRemainingSessionTime(String username) {
        LocalDateTime lastActivity = activeSessions.get(username);
        if (lastActivity == null) {
            return 0;
        }
        
        LocalDateTime now = LocalDateTime.now();
        long minutesSinceLastActivity = java.time.Duration.between(lastActivity, now).toMinutes();
        long remaining = SESSION_TIMEOUT_MINUTES - minutesSinceLastActivity;
        
        return Math.max(0, remaining);
    }

    @Scheduled(fixedRate = 300000) // 5 minutes in milliseconds
    public void cleanupExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        activeSessions.entrySet().removeIf(entry -> {
            long minutesSinceLastActivity = java.time.Duration.between(entry.getValue(), now).toMinutes();
            return minutesSinceLastActivity >= SESSION_TIMEOUT_MINUTES;
        });
    }
}

