package com.elearning.service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO chứa thống kê ôn tập của một deck
 * 
 * @author Smart Flashcard Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewStatsDTO {
    
    private Integer totalCards;      // Tổng số thẻ trong deck
    private Integer newCards;        // Số thẻ mới chưa học
    private Integer dueCards;        // Tổng số thẻ cần ôn tập (bao gồm cả mới và cũ)
    private Integer reviewCards;     // Số thẻ cần ôn lại
}