package com.moodify.dto;

import java.time.LocalDate;
import java.util.List;

public class UserInfoResponse {
    private final LocalDate birthDate;
    private final String gender;
    private final List<String> hobbies;

    public UserInfoResponse(LocalDate birthDate, String gender, List<String> hobbies) {
        this.birthDate = birthDate;
        this.gender = gender;
        this.hobbies = hobbies;
    }

    public LocalDate getBirthDate() { return birthDate; }
    public String getGender() { return gender; }
    public List<String> getHobbies() { return hobbies; }
}
