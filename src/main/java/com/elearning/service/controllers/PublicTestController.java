package com.elearning.service.controllers;

import com.elearning.service.services.AITranslationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/public/test")
@RequiredArgsConstructor
@Slf4j
public class PublicTestController {

    private final AITranslationService aiTranslationService;

    /**
     * Public test endpoint - không cần authentication
     */
    @GetMapping("/translation")
    public Map<String, Object> testTranslation(
            @RequestParam(defaultValue = "Parade") String word,
            @RequestParam(defaultValue = "en") String source,
            @RequestParam(defaultValue = "vi") String target) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("Testing translation for: {} from {} to {}", word, source, target);
            
            String translation = aiTranslationService.translateWithAI(word, source, target, "");
            
            response.put("word", word);
            response.put("translation", translation);
            response.put("service", "Enhanced Mock Translation");
            response.put("success", true);
            response.put("timestamp", System.currentTimeMillis());
            
            log.info("Translation result: {} -> {}", word, translation);
            
        } catch (Exception e) {
            log.error("Translation test failed: {}", e.getMessage());
            response.put("word", word);
            response.put("error", e.getMessage());
            response.put("success", false);
        }
        
        return response;
    }

    /**
     * Test batch translation - public endpoint
     */
    @PostMapping("/batch-translation")
    public Map<String, Object> testBatchTranslation(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            @SuppressWarnings("unchecked")
            List<String> words = (List<String>) request.getOrDefault("words", 
                List.of("Parade", "Prosperity", "Symbol", "Take part in", "Thanksgiving"));
            String source = (String) request.getOrDefault("source", "en");
            String target = (String) request.getOrDefault("target", "vi");
            
            log.info("Testing batch translation for {} words", words.size());
            
            Map<String, String> translations = aiTranslationService.batchTranslateWithAI(words, source, target, "");
            
            response.put("input_words", words);
            response.put("translations", translations);
            response.put("service", "Enhanced Mock Batch Translation");
            response.put("success", true);
            response.put("count", translations.size());
            response.put("timestamp", System.currentTimeMillis());
            
            log.info("Batch translation completed: {} results", translations.size());
            
        } catch (Exception e) {
            log.error("Batch translation test failed: {}", e.getMessage());
            response.put("error", e.getMessage());
            response.put("success", false);
        }
        
        return response;
    }

    /**
     * Demo comparison: old vs new translation
     */
    @GetMapping("/demo-comparison")
    public Map<String, Object> demoComparison() {
        Map<String, Object> response = new HashMap<>();
        
        List<String> testWords = List.of("Parade", "Prosperity", "Symbol", "Take part in", "Thanksgiving");
        
        // Old translation results (mock)
        Map<String, String> oldTranslations = new HashMap<>();
        oldTranslations.put("Parade", "parade (từ tiếng Anh)");
        oldTranslations.put("Prosperity", "prosperity (từ tiếng Anh)");  
        oldTranslations.put("Symbol", "symbol (từ tiếng Anh)");
        oldTranslations.put("Take part in", "take part in (từ tiếng Anh)");
        oldTranslations.put("Thanksgiving", "việc thanksgiv");
        
        // New Enhanced Mock translations
        Map<String, String> newTranslations = new HashMap<>();
        for (String word : testWords) {
            try {
                String translation = aiTranslationService.translateWithAI(word, "en", "vi", "");
                newTranslations.put(word, translation);
            } catch (Exception e) {
                newTranslations.put(word, "Error: " + e.getMessage());
            }
        }
        
        response.put("test_words", testWords);
        response.put("old_translations", oldTranslations);
        response.put("new_enhanced_translations", newTranslations);
        response.put("improvement", "Enhanced Mock provides meaningful translations instead of generic placeholders");
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }
}
