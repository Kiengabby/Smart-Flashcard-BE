package com.elearning.service.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCardDTO {
    
    @NotBlank(message = "Nội dung mặt trước không được để trống")
    private String frontText;
    
    @NotBlank(message = "Nội dung mặt sau không được để trống")
    private String backText;
}
