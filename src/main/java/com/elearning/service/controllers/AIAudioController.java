package com.elearning.service.controllers;

import com.elearning.service.services.AITTSService;
import com.elearning.service.services.AudioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai-audio")
@RequiredArgsConstructor
@Slf4j
public class AIAudioController {

    private final AITTSService aiTTSService;
    private final AudioService audioService;

    /**
     * Generate AI audio for single text
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateAIAudio(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String text = request.get("text");
            String language = request.getOrDefault("language", "en");
            String voiceType = request.getOrDefault("voiceType", "female");
            
            if (text == null || text.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "Text is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            log.info("Generating AI audio for: {} (lang: {}, voice: {})", text, language, voiceType);
            
            String audioUrl = audioService.generatePremiumAudioForText(text, language, voiceType);
            
            if (audioUrl != null) {
                response.put("success", true);
                response.put("text", text);
                response.put("audioUrl", audioUrl);
                response.put("language", language);
                response.put("voiceType", voiceType);
                response.put("provider", "OpenAI TTS-HD");
            } else {
                response.put("success", false);
                response.put("error", "Failed to generate audio");
            }
            
        } catch (Exception e) {
            log.error("Error generating AI audio: {}", e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Generate audio for multiple texts (batch)
     */
    @PostMapping("/generate-batch")
    public ResponseEntity<Map<String, Object>> generateBatchAIAudio(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            @SuppressWarnings("unchecked")
            List<String> texts = (List<String>) request.get("texts");
            String language = (String) request.getOrDefault("language", "en");
            String voiceType = (String) request.getOrDefault("voiceType", "female");
            
            if (texts == null || texts.isEmpty()) {
                response.put("success", false);
                response.put("error", "Texts array is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            log.info("Generating batch AI audio for {} texts", texts.size());
            
            Map<String, String> audioUrls = aiTTSService.generateBatchAIAudio(texts, language, voiceType);
            
            response.put("success", true);
            response.put("audioUrls", audioUrls);
            response.put("language", language);
            response.put("voiceType", voiceType);
            response.put("totalRequested", texts.size());
            response.put("totalGenerated", audioUrls.size());
            response.put("provider", "OpenAI TTS-HD");
            
        } catch (Exception e) {
            log.error("Error generating batch AI audio: {}", e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get available voices for a language
     */
    @GetMapping("/voices")
    public ResponseEntity<Map<String, Object>> getAvailableVoices(
            @RequestParam(defaultValue = "en") String language) {
        
        try {
            Map<String, Object> voices = aiTTSService.getAvailableVoices(language);
            return ResponseEntity.ok(voices);
        } catch (Exception e) {
            log.error("Error getting available voices: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * Compare audio quality between basic TTS and AI TTS
     */
    @PostMapping("/compare")
    public ResponseEntity<Map<String, Object>> compareAudioQuality(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String text = request.get("text");
            String language = request.getOrDefault("language", "en");
            String voiceType = request.getOrDefault("voiceType", "female");
            
            if (text == null || text.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "Text is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            log.info("Comparing audio quality for: {}", text);
            
            // Generate AI TTS audio
            String aiAudioUrl = aiTTSService.generateAIAudioForText(text, language, voiceType);
            
            // Generate basic TTS audio (fallback method)
            String basicAudioUrl = generateBasicAudio(text, language);
            
            Map<String, Object> comparison = new HashMap<>();
            comparison.put("ai_tts", Map.of(
                "audioUrl", aiAudioUrl != null ? aiAudioUrl : "Failed to generate",
                "provider", "OpenAI TTS-HD",
                "quality", "High (9/10)",
                "naturalness", "Very Natural",
                "cost", "$0.015 per 1000 chars"
            ));
            
            comparison.put("basic_tts", Map.of(
                "audioUrl", basicAudioUrl != null ? basicAudioUrl : "Failed to generate",
                "provider", "ResponsiveVoice",
                "quality", "Basic (3/10)",
                "naturalness", "Robotic",
                "cost", "Free"
            ));
            
            response.put("success", true);
            response.put("text", text);
            response.put("comparison", comparison);
            response.put("recommendation", "AI TTS provides significantly better quality and naturalness");
            
        } catch (Exception e) {
            log.error("Error comparing audio quality: {}", e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get AI audio statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAudioStats() {
        try {
            Map<String, Object> stats = aiTTSService.getAudioStats();
            stats.put("success", true);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting audio stats: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * Demo: Generate audio for flashcard vocabulary  
     */
    @GetMapping("/demo")
    public ResponseEntity<Map<String, Object>> demoFlashcardAudio(
            @RequestParam(defaultValue = "Hello World") String text,
            @RequestParam(defaultValue = "en") String language,
            @RequestParam(defaultValue = "female") String voice) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("Demo: Generating flashcard audio for '{}' in {} with {} voice", text, language, voice);
            
            long startTime = System.currentTimeMillis();
            String audioUrl = audioService.generatePremiumAudioForText(text, language, voice);
            long duration = System.currentTimeMillis() - startTime;
            
            response.put("success", audioUrl != null);
            response.put("text", text);
            response.put("language", language);
            response.put("voice", voice);
            response.put("audioUrl", audioUrl);
            response.put("generationTime", duration + "ms");
            response.put("provider", "AI TTS (OpenAI)");
            response.put("quality", "Premium HD");
            
            if (audioUrl == null) {
                response.put("error", "Failed to generate audio. Check API key configuration.");
            }
            
        } catch (Exception e) {
            log.error("Demo error: {}", e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Helper method to generate basic audio for comparison
     */
    private String generateBasicAudio(String text, String language) {
        try {
            // Temporarily disable AI to force fallback
            return audioService.generateAudioForText(text, language);
        } catch (Exception e) {
            log.error("Error generating basic audio: {}", e.getMessage());
            return null;
        }
    }
}
