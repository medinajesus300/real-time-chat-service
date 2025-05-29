package com.medina.chat_service.controller;

import com.medina.chat_service.dto.UserDto;
import com.medina.chat_service.model.User;
import com.medina.chat_service.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Handles user registration and login endpoints.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final HttpSession session;

    public AuthController(UserService userService, HttpSession session) {
        this.userService = userService;
        this.session = session;
    }

    /**
     * Register a new user.
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserDto dto) {
        try {
            User user = userService.register(dto.getUsername(), dto.getPassword());
            return ResponseEntity.ok("User registered: " + user.getUsername());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Authenticate and start a session.
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserDto dto) {
        boolean ok = userService.authenticate(dto.getUsername(), dto.getPassword());
        if (!ok) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
        session.setAttribute("username", dto.getUsername());
        return ResponseEntity.ok("Logged in as: " + dto.getUsername());
    }


}
