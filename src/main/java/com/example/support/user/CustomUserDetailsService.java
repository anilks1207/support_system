package com.example.support.user;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

//    @Override
//    public UserDetails loadUserByUsername(String username)
//            throws UsernameNotFoundException {
//
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new UsernameNotFoundException(
//                        "User not found"));
//
//        return org.springframework.security.core.userdetails.User
//                .withUsername(user.getUsername())
//                .password(user.getPasswordHash())
//                .disabled(!user.isEnabled())
//                .authorities("ROLE_USER")
//                .build();
//    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        System.out.println("LOGIN DEBUG: Looking up username: " + username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    System.out.println("LOGIN DEBUG: User not found");
                    return new UsernameNotFoundException("User not found");
                });

        System.out.println("LOGIN DEBUG: User found: " + user.getUsername());
        System.out.println("LOGIN DEBUG: Enabled: " + user.isEnabled());

//        return org.springframework.security.core.userdetails.User
//                .withUsername(user.getUsername())
//                .password(user.getPasswordHash())
//                .disabled(!user.isEnabled())
//                .authorities("ROLE_USER")
//                .build();

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .disabled(!user.isEnabled())
                .authorities("ROLE_" + user.getRole())
                .build();
    }

}
