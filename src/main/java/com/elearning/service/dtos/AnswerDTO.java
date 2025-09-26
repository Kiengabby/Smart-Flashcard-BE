package com.elearning.service.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AnswerDTO {
    
    @NotNull(message = "Card ID không được để trống")
    private Long cardId;
    
    @Min(value = 0, message = "Chất lượng phải từ 0 đến 5")
    @Max(value = 5, message = "Chất lượng phải từ 0 đến 5") 
    @NotNull(message = "Chất lượng câu trả lời không được để trống")
    private Integer quality; // Chất lượng câu trả lời do người dùng tự đánh giá, từ 0 đến 5
}