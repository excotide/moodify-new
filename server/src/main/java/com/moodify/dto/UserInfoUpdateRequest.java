package com.moodify.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public class UserInfoUpdateRequest {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate; // format: yyyy-MM-dd

    @Size(max = 20)
    private String gender; // bebas, contoh: male/female/other

    private List<@Size(max = 50) String> hobbies; // optional

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public List<String> getHobbies() { return hobbies; }
    public void setHobbies(List<String> hobbies) { this.hobbies = hobbies; }
}
