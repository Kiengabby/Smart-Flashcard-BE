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

    private final RestTemplate restTemplate;

    public AudioService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Tạo âm thanh tự động cho text bằng ResponsiveVoice API
     * @param text Văn bản cần chuyển thành âm thanh
     * @param language Ngôn ngữ (en, vi, etc.)
     * @return URL của file âm thanh đã tạo
     */
    public String generateAudioForText(String text, String language) {
        try {
            // 1. Tạo âm thanh từ ResponsiveVoice API (miễn phí)
            String audioData = callTextToSpeechAPI(text, language);
            
            // 2. Lưu file âm thanh
            String fileName = saveAudioFile(audioData, text);
            
            // 3. Trả về URL public
            return audioBaseUrl + "/" + fileName;
            
        } catch (Exception e) {
            log.error("Lỗi khi tạo âm thanh cho text: {}", text, e);
            return null;
        }
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