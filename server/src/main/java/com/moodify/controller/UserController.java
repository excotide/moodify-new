package com.moodify.controller;

import com.moodify.dto.UserRegisterRequest;
import com.moodify.dto.UserResponse;
import com.moodify.entity.User;
import com.moodify.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping({"", "/register"})
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegisterRequest req) {
        User saved = userService.register(req);
        UserResponse resp = new UserResponse(saved.getId(), saved.getUsername(), saved.getCreatedAt());
        return ResponseEntity.created(URI.create("/api/users/" + saved.getId())).body(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> get(@PathVariable UUID id) {
        User u = userService.getById(id);
        UserResponse resp = new UserResponse(u.getId(), u.getUsername(), u.getCreatedAt());
        return ResponseEntity.ok(resp);
    }
}
