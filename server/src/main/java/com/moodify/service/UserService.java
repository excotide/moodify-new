package com.moodify.service;

import com.moodify.entity.User;
import com.moodify.dto.UserRegisterRequest;
import com.moodify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User register(UserRegisterRequest req) {
        userRepository.findByUsername(req.getUsername()).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        });
        User u = new User();
        u.setUsername(req.getUsername());
        u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        return userRepository.save(u);
    }

    public Optional<User> findUserById(UUID id) {
        return userRepository.findById(Objects.requireNonNull(id));
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void deleteUser(UUID id) {
        userRepository.deleteById(Objects.requireNonNull(id));
    }

    public User getById(UUID id) {
        return findUserById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public String login(String username, String password) {
        // Find user by username
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Validate password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        // Set firstLogin if null and always update lastLogin
        OffsetDateTime now = OffsetDateTime.now();
        if (user.getFirstLogin() == null) {
            user.setFirstLogin(now);
        }
        user.setLastLogin(now);
        userRepository.save(user);

        // Return a success message or user details
        return "Login successful for user: " + user.getUsername();
    }

    public User save(User user) {
        return userRepository.save(Objects.requireNonNull(user));
    }
}