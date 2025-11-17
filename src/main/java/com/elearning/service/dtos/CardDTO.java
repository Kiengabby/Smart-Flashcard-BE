package com.elearning.service.dtos;

import lombok.Data;

import java.util.Date;

@Data
public class CardDTO {
    
    private Long id;
    private String frontText;
    private String backText;
    private String audioUrl;
    
    // Thông tin ôn tập (cho thuật toán SM-2)
    private Integer repetitions;
    private Double easinessFactor;
    private Integer interval;
    private Date nextReviewDate;
}
