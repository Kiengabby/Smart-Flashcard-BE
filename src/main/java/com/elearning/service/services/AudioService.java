package com.elearning.service.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Service để tạo âm thanh tự động cho từ vựng
 */
@Service
@Slf4j
public class AudioService {

    @Value("${app.audio.storage.path:./audio}")
    private String audioStoragePath;

    @Value("${app.audio.base-url:http://localhost:8080/api/audio}")
    private String audioBaseUrl;

    @Value("${app.audio.ai-enabled:true}")
    private boolean aiEnabled;

    @Value("${app.audio.default-voice:female}")
    private String defaultVoice;

    private final RestTemplate restTemplate;
    private final AITTSService aiTTSService;
    private final GoogleTTSService googleTTSService;

    public AudioService(RestTemplate restTemplate, AITTSService aiTTSService, GoogleTTSService googleTTSService) {
        this.restTemplate = restTemplate;
        this.aiTTSService = aiTTSService;
        this.googleTTSService = googleTTSService;
    }

    /**
     * Tạo âm thanh tự động cho text với AI enhancement
     * @param text Văn bản cần chuyển thành âm thanh
     * @param language Ngôn ngữ (en, vi, etc.)
     * @return URL của file âm thanh đã tạo
     */
    public String generateAudioForText(String text, String language) {
        return generateAudioForText(text, language, defaultVoice);
    }

    /**
     * Tạo âm thanh với voice type cụ thể
     * @param text Văn bản cần chuyển thành âm thanh
     * @param language Ngôn ngữ (en, vi, etc.)
     * @param voiceType Loại giọng (female, male, neutral, etc.)
     * @return URL của file âm thanh đã tạo
     */
    public String generateAudioForText(String text, String language, String voiceType) {
        try {
            // 1. Try Google Neural TTS first (best quality with existing API key)
            if (aiEnabled) {
                log.info("Generating Google Neural audio for: {} (language: {}, voice: {})", 
                        text.substring(0, Math.min(30, text.length())), language, voiceType);
                
                String googleAudioUrl = googleTTSService.generateGoogleTTSAudio(text, language, voiceType);
                if (googleAudioUrl != null) {
                    log.info("✅ Google TTS success: High-quality neural voice");
                    return googleAudioUrl;
                }
                
                log.warn("Google TTS failed, trying OpenAI TTS...");
                
                // 2. Fallback to OpenAI TTS
                String openaiAudioUrl = aiTTSService.generateAIAudioForText(text, language, voiceType);
                if (openaiAudioUrl != null) {
                    log.info("✅ OpenAI TTS success: Premium voice");
                    return openaiAudioUrl;
                }
                
                log.warn("Both AI TTS failed, falling back to basic TTS");
            }
            
            // 3. Final fallback to ResponsiveVoice API
            String audioData = callTextToSpeechAPI(text, language);
            
            if (audioData != null) {
                String fileName = saveAudioFile(audioData, text);
                log.info("✅ Basic TTS fallback used");
                return audioBaseUrl + "/" + fileName;
            }
            
        } catch (Exception e) {
            log.error("Lỗi khi tạo âm thanh cho text: {}", text, e);
        }
        
        return null;
    }

    /**
     * Tạo âm thanh chất lượng cao bằng AI (Google Neural TTS ưu tiên)
     * @param text Văn bản cần chuyển thành âm thanh
     * @param language Ngôn ngữ
     * @param voiceType Loại giọng nói
     * @return URL của file âm thanh AI premium
     */
    public String generatePremiumAudioForText(String text, String language, String voiceType) {
        if (!aiEnabled) {
            log.warn("AI TTS is disabled, using standard audio generation");
            return generateAudioForText(text, language, voiceType);
        }
        
        try {
            // Priority: Google Neural TTS (using existing Google API key)
            log.info("🎤 Generating premium neural audio with Google TTS");
            String googleAudioUrl = googleTTSService.generateGoogleTTSAudio(text, language, voiceType);
            if (googleAudioUrl != null) {
                log.info("🚀 Google Neural TTS success: WaveNet quality");
                return googleAudioUrl;
            }
            
            // Fallback: OpenAI TTS
            log.info("🔄 Fallback to OpenAI TTS");
            String openaiAudioUrl = aiTTSService.generateAIAudioForText(text, language, voiceType);
            if (openaiAudioUrl != null) {
                log.info("✅ OpenAI TTS success");
                return openaiAudioUrl;
            }
            
        } catch (Exception e) {
            log.error("Premium AI audio generation failed: {}", e.getMessage());
        }
        
        // Final fallback to standard generation
        return generateAudioForText(text, language, voiceType);
    }

    /**
     * Gọi ResponsiveVoice API để tạo âm thanh
     */
    private String callTextToSpeechAPI(String text, String language) {
        try {
            // ResponsiveVoice API endpoint
            String url = "https://responsivevoice.org/responsivevoice/getvoice.php";
            
            // Tham số
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("t", text);
            params.add("tl", language.equals("vi") ? "Vietnamese Female" : "US English Female");
            params.add("sv", "g1"); // Service version
            params.add("vn", ""); // Voice name
            params.add("pitch", "0.5");
            params.add("rate", "0.5");
            params.add("vol", "1");
            
            // Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            
            // Gọi API
            ResponseEntity<byte[]> response = restTemplate.exchange(
                url, HttpMethod.POST, request, byte[].class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return java.util.Base64.getEncoder().encodeToString(response.getBody());
            }
            
        } catch (Exception e) {
            log.error("Lỗi khi gọi TTS API: ", e);
        }
        
        return null;
    }

    /**
     * Lưu file âm thanh vào thư mục storage
     */
    private String saveAudioFile(String audioData, String originalText) throws IOException {
        // Tạo thư mục nếu chưa tồn tại
        Path storagePath = Paths.get(audioStoragePath);
        if (!Files.exists(storagePath)) {
            Files.createDirectories(storagePath);
        }
        
        // Tạo tên file duy nhất
        String fileName = generateFileName(originalText) + ".mp3";
        Path filePath = storagePath.resolve(fileName);
        
        // Decode base64 và lưu file
        byte[] audioBytes = java.util.Base64.getDecoder().decode(audioData);
        Files.write(filePath, audioBytes);
        
        log.info("Đã lưu file âm thanh: {}", fileName);
        return fileName;
    }

    /**
     * Tạo tên file từ text gốc
     */
    private String generateFileName(String text) {
        // Làm sạch text và tạo tên file
        String cleanText = text.replaceAll("[^a-zA-Z0-9\\s]", "")
                              .replaceAll("\\s+", "_")
                              .toLowerCase();
        
        if (cleanText.length() > 50) {
            cleanText = cleanText.substring(0, 50);
        }
        
        // Thêm UUID để đảm bảo duy nhất
        return cleanText + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Xóa file âm thanh cũ khi cập nhật card
     */
    public void deleteAudioFile(String audioUrl) {
        if (audioUrl == null || !audioUrl.contains(audioBaseUrl)) {
            return;
        }
        
        try {
            String fileName = audioUrl.substring(audioUrl.lastIndexOf("/") + 1);
            Path filePath = Paths.get(audioStoragePath, fileName);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Đã xóa file âm thanh: {}", fileName);
            }
        } catch (Exception e) {
            log.error("Lỗi khi xóa file âm thanh: {}", audioUrl, e);
        }
    }
}