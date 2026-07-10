package com.diet.backend.dto;

import com.diet.backend.model.ClientCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {
    private Double height;
    private Double currentWeight;
    private Double targetWeight;
    private ClientCategory category;
    private String glp1InjectionDay;
    private String glp1Dosage;
    private Integer lipedemaStage;
    private Boolean antiInflammatoryCompliant;
    private String hormoneTargetCycle;
    private String name;
    private String instagramUrl;
    private String linkedinUrl;
    private String youtubeUrl;
    private String notes;
    private String profilePictureUrl;
}
