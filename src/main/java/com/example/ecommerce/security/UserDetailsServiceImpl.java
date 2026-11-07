package com.example.ecommerce.security;

import com.example.ecommerce.entity.User;
import com.example.ecommerce.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .or(() -> userRepository.findByEmail(username))
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return new UserPrincipal(user);
    }
}
