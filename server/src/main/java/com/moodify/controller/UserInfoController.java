package com.moodify.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodify.dto.UserInfoResponse;
import com.moodify.dto.UserInfoUpdateRequest;
import com.moodify.entity.User;
import com.moodify.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserInfoController {

    private final UserService userService;
    private final ObjectMapper objectMapper;

    public UserInfoController(UserService userService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/{id}/info")
    public UserInfoResponse getUserInfo(@PathVariable UUID id) {
        User u = userService.getById(id);
        List<String> hobbies = Collections.emptyList();
        try {
            String json = u.getHobbiesJson();
            if (json != null && !json.isBlank()) {
                hobbies = objectMapper.readValue(json, new TypeReference<List<String>>(){});
            }
        } catch (Exception ignore) {}
        return new UserInfoResponse(u.getBirthDate(), u.getGender(), hobbies);
    }

    @PutMapping("/{id}/info")
    public UserInfoResponse updateUserInfo(@PathVariable UUID id, @Valid @RequestBody UserInfoUpdateRequest req) {
        User u = userService.getById(id);
        if (req.getBirthDate() != null) u.setBirthDate(req.getBirthDate());
        if (req.getGender() != null) u.setGender(req.getGender());
        if (req.getHobbies() != null) {
            try {
                u.setHobbiesJson(objectMapper.writeValueAsString(req.getHobbies()));
            } catch (Exception e) {
                u.setHobbiesJson("[]");
            }
        }
        userService.save(u);

        List<String> hobbies = Collections.emptyList();
        try {
            if (u.getHobbiesJson() != null && !u.getHobbiesJson().isBlank()) {
                hobbies = objectMapper.readValue(u.getHobbiesJson(), new TypeReference<List<String>>(){});
            }
        } catch (Exception ignore) {}
        return new UserInfoResponse(u.getBirthDate(), u.getGender(), hobbies);
    }
}
