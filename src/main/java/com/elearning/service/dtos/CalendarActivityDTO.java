package com.elearning.service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for calendar activity data
 * Contains information about study activities for each day
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarActivityDTO {
    
    /**
     * Day of month (1-31)
     */
    private Integer day;
    
    /**
     * Number of reviews done on this day
     */
    private Integer reviewCount;
    
    /**
     * Activity level (0-3)
     * 0 = no activity
     * 1 = 1-5 reviews
     * 2 = 6-15 reviews
     * 3 = 16+ reviews
     */
    private Integer activityLevel;
}
