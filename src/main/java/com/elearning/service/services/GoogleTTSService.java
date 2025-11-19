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
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleTTSService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${google.translate.api.key:}")
    private String googleApiKey;

    @Value("${google.tts.url:https://texttospeech.googleapis.com/v1/text:synthesize}")
    private String googleTTSUrl;

    @Value("${app.audio.storage.path:./audio}")
    private String audioStoragePath;

    @Value("${app.audio.base-url:http://localhost:8080/api/audio}")
    private String audioBaseUrl;

    /**
     * Generate high-quality neural audio using Google Cloud TTS
     */
    public String generateGoogleTTSAudio(String text, String language, String voiceType) {
        try {
            if (googleApiKey.isEmpty()) {
                log.warn("Google API key not configured, using fallback TTS");
                return null;
            }

            // Generate audio using Google Cloud TTS
            byte[] audioData = callGoogleTTSAPI(text, language, voiceType);
            
            if (audioData != null) {
                // Save audio file
                String fileName = saveAudioFile(audioData, text, "google_neural");
                return audioBaseUrl + "/" + fileName;
            }

        } catch (Exception e) {
            log.error("Error generating Google TTS audio for text: {}", text, e);
        }

        return null;
    }

    /**
     * Call Google Cloud Text-to-Speech API
     */
    private byte[] callGoogleTTSAPI(String text, String language, String voiceType) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Select optimal voice for language and type
        Map<String, String> voiceConfig = selectOptimalGoogleVoice(language, voiceType);
        
        Map<String, Object> requestBody = new HashMap<>();
        
        // Input text
        Map<String, String> input = new HashMap<>();
        input.put("text", text);
        requestBody.put("input", input);
        
        // Voice configuration
        Map<String, Object> voice = new HashMap<>();
        voice.put("languageCode", voiceConfig.get("languageCode"));
        voice.put("name", voiceConfig.get("name"));
        voice.put("ssmlGender", voiceConfig.get("gender"));
        requestBody.put("voice", voice);
        
        // Audio configuration - Use Neural WaveNet for best quality
        Map<String, Object> audioConfig = new HashMap<>();
        audioConfig.put("audioEncoding", "MP3");
        audioConfig.put("speakingRate", 1.0);
        audioConfig.put("pitch", 0.0);
        audioConfig.put("volumeGainDb", 0.0);
        requestBody.put("audioConfig", audioConfig);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        // Add API key to URL
        String urlWithKey = googleTTSUrl + "?key=" + googleApiKey;
        
        ResponseEntity<String> response = restTemplate.exchange(
                urlWithKey,
                HttpMethod.POST,
                entity,
                String.class
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            // Parse response to get audioContent
            JsonNode root = objectMapper.readTree(response.getBody());
            String audioContent = root.path("audioContent").asText();
            
            if (!audioContent.isEmpty()) {
                log.info("Successfully generated Google TTS for: {}", text.substring(0, Math.min(50, text.length())));
                return Base64.getDecoder().decode(audioContent);
            }
        }

        throw new Exception("Google TTS API returned error: " + response.getStatusCode());
    }

    /**
     * Select optimal Google voice based on language and preference
     */
    private Map<String, String> selectOptimalGoogleVoice(String language, String voiceType) {
        Map<String, String> voiceConfig = new HashMap<>();
        String lang = language.toLowerCase();
        String type = (voiceType != null) ? voiceType.toLowerCase() : "default";
        
        switch (lang) {
            case "en":
                voiceConfig.put("languageCode", "en-US");
                if ("female".equals(type) || "default".equals(type)) {
                    voiceConfig.put("name", "en-US-Neural2-F"); // Premium Neural voice
                    voiceConfig.put("gender", "FEMALE");
                } else if ("male".equals(type)) {
                    voiceConfig.put("name", "en-US-Neural2-D"); // Premium Neural male
                    voiceConfig.put("gender", "MALE");
                } else {
                    voiceConfig.put("name", "en-US-Neural2-A"); // Neutral
                    voiceConfig.put("gender", "NEUTRAL");
                }
                break;
                
            case "vi":
                voiceConfig.put("languageCode", "vi-VN");
                if ("female".equals(type) || "default".equals(type)) {
                    voiceConfig.put("name", "vi-VN-Neural2-A"); // Premium Vietnamese Neural
                    voiceConfig.put("gender", "FEMALE");
                } else {
                    voiceConfig.put("name", "vi-VN-Neural2-D"); // Male Vietnamese
                    voiceConfig.put("gender", "MALE");
                }
                break;
                
            case "ja":
                voiceConfig.put("languageCode", "ja-JP");
                voiceConfig.put("name", "ja-JP-Neural2-B"); // Premium Japanese Neural
                voiceConfig.put("gender", "FEMALE");
                break;
                
            case "ko":
                voiceConfig.put("languageCode", "ko-KR");
                voiceConfig.put("name", "ko-KR-Neural2-A"); // Premium Korean Neural
                voiceConfig.put("gender", "FEMALE");
                break;
                
            case "zh":
                voiceConfig.put("languageCode", "zh-CN");
                voiceConfig.put("name", "zh-CN-Neural2-A"); // Premium Chinese Neural
                voiceConfig.put("gender", "FEMALE");
                break;
                
            case "fr":
                voiceConfig.put("languageCode", "fr-FR");
                voiceConfig.put("name", "fr-FR-Neural2-A"); // Premium French Neural
                voiceConfig.put("gender", "FEMALE");
                break;
                
            case "de":
                voiceConfig.put("languageCode", "de-DE");
                voiceConfig.put("name", "de-DE-Neural2-A"); // Premium German Neural
                voiceConfig.put("gender", "FEMALE");
                break;
                
            case "es":
                voiceConfig.put("languageCode", "es-ES");
                voiceConfig.put("name", "es-ES-Neural2-A"); // Premium Spanish Neural
                voiceConfig.put("gender", "FEMALE");
                break;
                
            default:
                // Default to English
                voiceConfig.put("languageCode", "en-US");
                voiceConfig.put("name", "en-US-Neural2-F");
                voiceConfig.put("gender", "FEMALE");
        }
        
        return voiceConfig;
    }

    /**
     * Generate audio for batch text (multiple cards)
     */
    public Map<String, String> generateBatchGoogleTTS(List<String> texts, String language, String voiceType) {
        Map<String, String> audioUrls = new HashMap<>();
        
        for (String text : texts) {
            try {
                String audioUrl = generateGoogleTTSAudio(text, language, voiceType);
                if (audioUrl != null) {
                    audioUrls.put(text, audioUrl);
                }
                
                // Small delay to respect rate limits
                Thread.sleep(150);
                
            } catch (Exception e) {
                log.error("Error generating batch Google TTS for: {}", text, e);
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
        
        log.info("Saved Google Neural audio file: {} (size: {} bytes)", fileName, audioData.length);
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
     * Get available Google voices for a language
     */
    public Map<String, Object> getAvailableGoogleVoices(String language) {
        Map<String, Object> response = new HashMap<>();
        
        String lang = language.toLowerCase();
        
        Map<String, String> voices = new HashMap<>();
        Map<String, String> descriptions = new HashMap<>();
        
        switch (lang) {
            case "en":
                voices.put("en-US-Neural2-F", "Premium Neural Female (Recommended)");
                voices.put("en-US-Neural2-D", "Premium Neural Male");
                voices.put("en-US-Neural2-A", "Premium Neural Neutral");
                voices.put("en-US-Neural2-C", "Premium Neural Child");
                descriptions.put("quality", "WaveNet Neural - 9/10");
                descriptions.put("naturalness", "Extremely Natural");
                break;
                
            case "vi":
                voices.put("vi-VN-Neural2-A", "Premium Vietnamese Female");
                voices.put("vi-VN-Neural2-D", "Premium Vietnamese Male");
                descriptions.put("quality", "Native Vietnamese Neural - 9/10");
                descriptions.put("naturalness", "Perfect Vietnamese pronunciation");
                break;
                
            case "ja":
                voices.put("ja-JP-Neural2-B", "Premium Japanese Female");
                voices.put("ja-JP-Neural2-C", "Premium Japanese Male");
                descriptions.put("quality", "Native Japanese Neural - 9/10");
                break;
                
            default:
                voices.put("Multilingual", "40+ languages supported with Neural voices");
        }
        
        response.put("language", language);
        response.put("voices", voices);
        response.put("descriptions", descriptions);
        response.put("provider", "Google Cloud TTS Neural");
        response.put("cost", "$0.016 per 1000 characters");
        response.put("features", List.of("WaveNet Neural", "Emotional range", "Perfect pronunciation"));
        
        return response;
    }

    /**
     * Get Google TTS statistics and capabilities
     */
    public Map<String, Object> getGoogleTTSStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            Path audioPath = Paths.get(audioStoragePath);
            if (Files.exists(audioPath)) {
                long totalFiles = Files.list(audioPath)
                                     .filter(p -> p.getFileName().toString().contains("google_neural"))
                                     .count();
                                     
                stats.put("googleTTSFiles", totalFiles);
            }
        } catch (Exception e) {
            log.error("Error getting Google TTS stats: ", e);
        }
        
        stats.put("provider", "Google Cloud Text-to-Speech");
        stats.put("technology", "WaveNet Neural");
        stats.put("quality", "Premium (9/10)");
        stats.put("voices", "220+ voices, 40+ languages");
        stats.put("cost", "$0.016 per 1000 characters");
        stats.put("features", List.of(
            "WaveNet Neural technology",
            "Emotional expression",
            "Perfect multilingual pronunciation",
            "Real-time synthesis",
            "High fidelity audio"
        ));
        stats.put("apiKeyConfigured", !googleApiKey.isEmpty());
        
        return stats;
    }
}
