package com.elearning.service.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDeckDTO {
    
    @NotBlank(message = "Tên bộ thẻ không được để trống")
    private String name;
    
    private String description;
    
    private String language = "en"; // Mặc định là tiếng Anh
}
