package com.elearning.service.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AITTSService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    @Value("${openai.tts.url:https://api.openai.com/v1/audio/speech}")
    private String openaiTTSUrl;

    @Value("${app.audio.storage.path:./audio}")
    private String audioStoragePath;

    @Value("${app.audio.base-url:http://localhost:8080/api/audio}")
    private String audioBaseUrl;

    /**
     * Generate high-quality AI speech using OpenAI TTS
     */
    public String generateAIAudioForText(String text, String language, String voiceType) {
        try {
            if (openaiApiKey.isEmpty()) {
                log.warn("OpenAI API key not configured, using fallback TTS");
                return generateFallbackAudio(text, language);
            }

            // Generate audio using OpenAI TTS
            byte[] audioData = callOpenAITTS(text, language, voiceType);
            
            if (audioData != null) {
                // Save audio file
                String fileName = saveAudioFile(audioData, text, "openai");
                return audioBaseUrl + "/" + fileName;
            }

        } catch (Exception e) {
            log.error("Error generating AI audio for text: {}", text, e);
        }

        // Fallback to basic TTS
        return generateFallbackAudio(text, language);
    }

    /**
     * Call OpenAI TTS API
     */
    private byte[] callOpenAITTS(String text, String language, String voiceType) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openaiApiKey);

        // Choose voice based on language and type
        String voice = selectOptimalVoice(language, voiceType);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "tts-1-hd"); // High quality model
        requestBody.put("input", text);
        requestBody.put("voice", voice);
        requestBody.put("response_format", "mp3");
        requestBody.put("speed", 1.0); // Normal speed

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<byte[]> response = restTemplate.exchange(
                openaiTTSUrl,
                HttpMethod.POST,
                entity,
                byte[].class
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            log.info("Successfully generated OpenAI TTS for: {}", text.substring(0, Math.min(50, text.length())));
            return response.getBody();
        }

        throw new Exception("OpenAI TTS API returned error: " + response.getStatusCode());
    }

    /**
     * Select optimal voice based on language and user preference
     */
    private String selectOptimalVoice(String language, String voiceType) {
        Map<String, Map<String, String>> voiceMapping = Map.of(
            "en", Map.of(
                "female", "nova",      // Warm, engaging female
                "male", "onyx",        // Deep, authoritative male  
                "neutral", "alloy",    // Balanced, clear
                "young", "shimmer",    // Youthful, energetic
                "mature", "echo",      // Mature, professional
                "friendly", "fable"    // Warm, storytelling
            ),
            "vi", Map.of(
                "female", "nova",      // Works well for Vietnamese
                "male", "onyx",
                "neutral", "alloy",
                "default", "nova"
            ),
            "ja", Map.of(
                "female", "nova",      // Good for Japanese
                "male", "onyx", 
                "default", "alloy"
            ),
            "ko", Map.of(
                "female", "shimmer",   // Good for Korean
                "male", "onyx",
                "default", "alloy"
            )
        );

        String lang = language.toLowerCase();
        String type = (voiceType != null) ? voiceType.toLowerCase() : "default";
        
        return voiceMapping.getOrDefault(lang, voiceMapping.get("en"))
                          .getOrDefault(type, "alloy"); // Default fallback
    }

    /**
     * Generate audio for batch text (multiple cards)
     */
    public Map<String, String> generateBatchAIAudio(List<String> texts, String language, String voiceType) {
        Map<String, String> audioUrls = new HashMap<>();
        
        for (String text : texts) {
            try {
                String audioUrl = generateAIAudioForText(text, language, voiceType);
                if (audioUrl != null) {
                    audioUrls.put(text, audioUrl);
                }
                
                // Small delay to respect rate limits
                Thread.sleep(200);
                
            } catch (Exception e) {
                log.error("Error generating batch audio for: {}", text, e);
                // Continue with other texts
            }
        }
        
        return audioUrls;
    }

    /**
     * Save audio file to storage
     */
    private String saveAudioFile(byte[] audioData, String originalText, String provider) throws IOException {
        // Create directory if not exists
        Path storagePath = Paths.get(audioStoragePath);
        if (!Files.exists(storagePath)) {
            Files.createDirectories(storagePath);
        }
        
        // Generate unique filename
        String fileName = generateFileName(originalText, provider) + ".mp3";
        Path filePath = storagePath.resolve(fileName);
        
        // Save file
        Files.write(filePath, audioData);
        
        log.info("Saved AI audio file: {} (size: {} bytes)", fileName, audioData.length);
        return fileName;
    }

    /**
     * Generate filename for audio file
     */
    private String generateFileName(String text, String provider) {
        // Clean text for filename
        String cleanText = text.replaceAll("[^a-zA-Z0-9\\s]", "")
                              .replaceAll("\\s+", "_")
                              .toLowerCase();
        
        if (cleanText.length() > 30) {
            cleanText = cleanText.substring(0, 30);
        }
        
        // Add provider and UUID for uniqueness
        return String.format("%s_%s_%s", 
                           provider, 
                           cleanText, 
                           UUID.randomUUID().toString().substring(0, 8));
    }

    /**
     * Fallback to basic TTS when AI is unavailable
     */
    private String generateFallbackAudio(String text, String language) {
        log.info("Using fallback TTS for: {}", text);
        // Return null to indicate fallback should be handled by existing AudioService
        return null;
    }

    /**
     * Get available voices for a language
     */
    public Map<String, Object> getAvailableVoices(String language) {
        Map<String, Object> response = new HashMap<>();
        
        String lang = language.toLowerCase();
        Map<String, String> voices = Map.of(
            "alloy", "Balanced, neutral voice",
            "echo", "Mature, professional voice", 
            "fable", "Warm, storytelling voice",
            "onyx", "Deep, authoritative voice",
            "nova", "Warm, engaging voice",
            "shimmer", "Youthful, energetic voice"
        );
        
        response.put("language", language);
        response.put("voices", voices);
        response.put("recommended", selectOptimalVoice(language, "default"));
        response.put("provider", "OpenAI TTS-HD");
        
        return response;
    }

    /**
     * Get audio generation statistics
     */
    public Map<String, Object> getAudioStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            Path audioPath = Paths.get(audioStoragePath);
            if (Files.exists(audioPath)) {
                long totalFiles = Files.list(audioPath).count();
                long totalSize = Files.walk(audioPath)
                                     .filter(Files::isRegularFile)
                                     .mapToLong(path -> {
                                         try {
                                             return Files.size(path);
                                         } catch (IOException e) {
                                             return 0;
                                         }
                                     })
                                     .sum();
                
                stats.put("totalFiles", totalFiles);
                stats.put("totalSizeMB", totalSize / (1024 * 1024));
                stats.put("storagePath", audioStoragePath);
            }
        } catch (Exception e) {
            log.error("Error getting audio stats: ", e);
        }
        
        stats.put("aiProvider", openaiApiKey.isEmpty() ? "Fallback TTS" : "OpenAI TTS-HD");
        return stats;
    }
}
