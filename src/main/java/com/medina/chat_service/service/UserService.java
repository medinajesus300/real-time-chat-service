package com.medina.chat_service.service;

import com.medina.chat_service.model.User;
import com.medina.chat_service.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.Optional;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Register a new user with a hashed password.
     * @param username desired username
     * @param rawPassword plaintext password
     * @return saved User entity
     * @throws IllegalArgumentException if username is already taken
     */
    public User register(String username, String rawPassword) {
        userRepository.findByUsername(username).ifPresent(u ->
                { throw new IllegalArgumentException("Username already exists: " + username); }
        );
        String hash = passwordEncoder.encode(rawPassword);
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(hash);
        return userRepository.save(user);
    }

    /**
     * Authenticate a user by matching raw password against stored hash.
     * @param username username to authenticate
     * @param rawPassword plaintext password
     * @return true if credentials match, false otherwise
     */
    public boolean authenticate(String username, String rawPassword) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        return userOpt.map(user -> passwordEncoder.matches(rawPassword, user.getPasswordHash()))
                .orElse(false);
    }

    /**
     * Find a user by username.
     * @param username the username to search
     * @return Optional containing User if found
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

}
