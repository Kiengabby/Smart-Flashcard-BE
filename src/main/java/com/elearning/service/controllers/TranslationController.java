package com.elearning.service.controllers;

import com.elearning.service.services.AITranslationService;
import com.elearning.service.services.TranslationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/translation")
@RequiredArgsConstructor
@Slf4j
public class TranslationController {

    private final TranslationService translationService;
    private final AITranslationService aiTranslationService;

    /**
     * Test single word translation
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testTranslation(
            @RequestParam String word,
            @RequestParam(defaultValue = "en") String source,
            @RequestParam(defaultValue = "vi") String target) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Try AI translation first
            String aiTranslation = aiTranslationService.translateWithAI(word, source, target, "");
            
            response.put("word", word);
            response.put("translation", aiTranslation);
            response.put("service", "AI Translation");
            response.put("success", true);
            
        } catch (Exception e) {
            log.error("Translation test failed: {}", e.getMessage());
            response.put("word", word);
            response.put("error", e.getMessage());
            response.put("success", false);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test batch translation
     */
    @PostMapping("/test-batch")
    public ResponseEntity<Map<String, Object>> testBatchTranslation(
            @RequestBody Map<String, Object> request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            @SuppressWarnings("unchecked")
            List<String> words = (List<String>) request.get("words");
            String source = (String) request.getOrDefault("source", "en");
            String target = (String) request.getOrDefault("target", "vi");
            String context = (String) request.getOrDefault("context", "");
            
            Map<String, String> translations = aiTranslationService.batchTranslateWithAI(words, source, target, context);
            
            response.put("words", words);
            response.put("translations", translations);
            response.put("service", "AI Batch Translation");
            response.put("success", true);
            response.put("count", translations.size());
            
        } catch (Exception e) {
            log.error("Batch translation test failed: {}", e.getMessage());
            response.put("error", e.getMessage());
            response.put("success", false);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Compare translation services
     */
    @GetMapping("/compare")
    public ResponseEntity<Map<String, Object>> compareTranslations(
            @RequestParam String word,
            @RequestParam(defaultValue = "en") String source,
            @RequestParam(defaultValue = "vi") String target) {
        
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> results = new HashMap<>();
        
        // Test AI Translation
        try {
            String aiResult = aiTranslationService.translateWithAI(word, source, target, "");
            results.put("ai_translation", aiResult);
        } catch (Exception e) {
            results.put("ai_translation", "Error: " + e.getMessage());
        }
        
        // Test Google Translate
        try {
            String googleResult = translationService.translateText(word, source, target);
            results.put("google_translate", googleResult);
        } catch (Exception e) {
            results.put("google_translate", "Error: " + e.getMessage());
        }
        
        response.put("word", word);
        response.put("results", results);
        response.put("recommendation", "AI Translation is recommended for best quality");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Health check for translation services
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        // Check AI Translation Service
        try {
            String testResult = aiTranslationService.translateWithAI("hello", "en", "vi", "");
            health.put("ai_translation", "OK - " + testResult);
        } catch (Exception e) {
            health.put("ai_translation", "ERROR - " + e.getMessage());
        }
        
        // Check Google Translate Service  
        try {
            String testResult = translationService.translateText("hello", "en", "vi");
            health.put("google_translate", "OK - " + testResult);
        } catch (Exception e) {
            health.put("google_translate", "ERROR - " + e.getMessage());
        }
        
        return ResponseEntity.ok(health);
    }
}
