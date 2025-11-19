package com.elearning.service.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class BulkCreateCardsRequest {
    
    @NotEmpty(message = "Danh sách từ không được rỗng")
    @Size(max = 50, message = "Tối đa 50 từ trong một lần tạo")
    private List<String> words;
    
    @NotBlank(message = "Ngôn ngữ nguồn không được rỗng")
    private String sourceLanguage; // ja, en, zh, ko, etc.
    
    @NotBlank(message = "Ngôn ngữ đích không được rỗng") 
    private String targetLanguage; // vi, en, etc.
    
    private String context; // Optional context for better translation
    
    private boolean autoDetectLanguage = false; // Auto detect source language
}
