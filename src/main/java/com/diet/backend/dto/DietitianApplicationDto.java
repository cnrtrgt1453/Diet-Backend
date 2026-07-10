package com.diet.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DietitianApplicationDto {
    private String fullName;
    private String email;
    private String university;
    private String diplomaNumber;
    private Integer experienceYears;
    private String documentUrl;
    private String note;
    private String password;
}
