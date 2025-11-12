package com.elearning.service.controllers;

import com.elearning.service.dtos.StudyStatsDTO;
import com.elearning.service.services.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatisticsController {

    private final CardService cardService;

    /**
     * Lấy thống kê học tập tổng quan
     */
    @GetMapping("/study")
    public ResponseEntity<StudyStatsDTO> getStudyStats() {
        StudyStatsDTO stats = cardService.getStudyStats();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Lấy các ngày có hoạt động học tập trong tháng
     */
    @GetMapping("/activity-dates")
    public ResponseEntity<java.util.List<Integer>> getActivityDates(
            @RequestParam int year, 
            @RequestParam int month) {
        java.util.List<Integer> activityDates = cardService.getActivityDatesInMonth(year, month);
        return ResponseEntity.ok(activityDates);
    }
}
