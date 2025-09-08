package com.elearning.service.dtos.base;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Base DTO class containing common fields for all DTOs
 * 
 * @author Your Name
 * @version 1.0.0
 */
@Data
public abstract class BaseDTO {
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
