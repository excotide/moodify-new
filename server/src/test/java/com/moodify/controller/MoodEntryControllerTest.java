package com.moodify.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodify.dto.MoodEntryRequest;
import com.moodify.entity.User;
import com.moodify.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.context.ActiveProfiles("dev")
class MoodEntryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID userId;

    @BeforeEach
    void setUp() {
        // Ensure at least one user exists for foreign key constraint
        User user = new User();
        user.setUsername("tester");
        user = userRepository.save(user);
        userId = user.getId();
    }

    @Test
    @DisplayName("Create, fetch list, fetch single, update, delete mood entry")
    void fullCrudFlow() throws Exception {
        // Create
        MoodEntryRequest createReq = new MoodEntryRequest();
        createReq.setUserId(userId);
        createReq.setMood("happy");
        createReq.setScore(8);

        String createJson = objectMapper.writeValueAsString(createReq);

        String location = mockMvc.perform(post("/api/mood-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mood").value("happy"))
                .andExpect(jsonPath("$.score").value(8))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract created ID
        UUID entryId = objectMapper.readTree(location).get("id").traverse(objectMapper).readValueAs(UUID.class);

        // List
        mockMvc.perform(get("/api/mood-entries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists());

        // Get single
        mockMvc.perform(get("/api/mood-entries/" + entryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(entryId.toString()));

        // Update
        MoodEntryRequest updateReq = new MoodEntryRequest();
        updateReq.setUserId(userId); // userId not changed but required by DTO
        updateReq.setMood("sad");
        updateReq.setScore(3);
        String updateJson = objectMapper.writeValueAsString(updateReq);

        mockMvc.perform(put("/api/mood-entries/" + entryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mood").value("sad"))
                .andExpect(jsonPath("$.score").value(3));

        // Delete
        mockMvc.perform(delete("/api/mood-entries/" + entryId))
                .andExpect(status().isNoContent());

        // Get after delete should 404 now that service throws ResponseStatusException
        mockMvc.perform(get("/api/mood-entries/" + entryId))
                .andExpect(status().isNotFound());
    }
}
