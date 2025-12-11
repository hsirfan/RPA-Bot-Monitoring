package com.ahana.botmonitoring.Security;

import com.ahana.botmonitoring.Entity.UserModel;
import com.ahana.botmonitoring.Repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserModel user = userRepository.findByEmailID(email);
        
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        // Build authorities based on user role
        List<GrantedAuthority> authorities = new ArrayList<>();
//        if (user.getRole() != null && !user.getRole().isEmpty()) {
//            // Add role with ROLE_ prefix for Spring Security
//            String role = user.getRole().toUpperCase();
//            if (role.equals("ADMIN") || role.equals("USER")) {
//                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
//            } else {
//                // Default to USER role if role is not recognized
//                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
//            }
//        } else {
//            // Default to USER role if no role is set
//            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
//        }

        return User.builder()
                .username(user.getEmailID())
                .password(user.getPassword() != null ? user.getPassword() : "")
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}

