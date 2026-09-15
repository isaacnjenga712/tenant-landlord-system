package com.apex.auth.controller;

import com.apex.auth.entity.User;
import com.apex.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PutMapping("/users/{userId}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public String enableUser(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        return "User " + user.getEmail() + " enabled: " + user.isEnabled();
    }

    @DeleteMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteUser(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
        return "User " + user.getEmail() + " deleted";
    }
}
