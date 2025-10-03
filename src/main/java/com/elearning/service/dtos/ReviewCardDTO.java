package com.elearning.service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO chứa thông tin thẻ dành cho việc ôn tập
 * Bao gồm cả nội dung thẻ và thông tin trạng thái học tập
 * 
 * @author Smart Flashcard Team
 * @version 2.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewCardDTO {
    
    private Long id;
    private String frontText;
    private String backText;
    private Long deckId;
    private String deckName;
    
    // Thông tin trạng thái ôn tập
    private Integer repetitions;
    private Double easinessFactor;
    private Integer interval;
    private LocalDate nextReviewDate;
    
    // Thông tin bổ sung
    private Boolean isNewCard;       // Thẻ mới chưa học lần nào
    private Boolean isDue;           // Thẻ cần ôn tập ngay bây giờ
    private Integer daysOverdue;     // Số ngày quá hạn ôn tập (nếu có)
}