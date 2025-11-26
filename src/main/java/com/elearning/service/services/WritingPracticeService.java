package com.elearning.service.services;

import com.elearning.service.dto.WritingEvaluationRequest;
import com.elearning.service.dto.WritingFeedbackResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WritingPracticeService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.translation.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${ai.translation.gemini.api-url:https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent}")
    private String geminiApiUrl;

    /**
     * Evaluate a sentence using AI
     */
    public WritingFeedbackResponse evaluateSentence(WritingEvaluationRequest request) {
        try {
            String prompt = createEvaluationPrompt(request);
            String aiResponse = callGeminiAPI(prompt);
            
            return parseAIResponse(aiResponse);
            
        } catch (Exception e) {
            log.error("Error evaluating sentence: {}", e.getMessage());
            return createFallbackResponse();
        }
    }

    /**
     * Generate an example sentence for a word
     */
    public String generateExampleSentence(String word, String meaning) {
        try {
            String prompt = String.format(
                "Tạo 1 câu ví dụ tiếng Anh sử dụng từ '%s' (nghĩa: %s) và dịch sang tiếng Việt.\n\n" +
                "Yêu cầu:\n" +
                "- Câu tiếng Anh đơn giản, dễ hiểu cho người Việt học\n" +
                "- Thể hiện rõ cách sử dụng từ trong ngữ cảnh thực tế\n" +
                "- Dịch tiếng Việt chính xác, tự nhiên\n\n" +
                "Chỉ trả về JSON thuần túy, không thêm markdown hay text khác:\n" +
                "{\n" +
                "  \"example\": \"câu ví dụ tiếng Anh\",\n" +
                "  \"translation\": \"bản dịch tiếng Việt\"\n" +
                "}",
                word, meaning
            );
            
            String response = callGeminiAPI(prompt);
            String cleanedResponse = cleanJsonResponse(response);
            
            // Validate JSON and return
            JsonNode jsonNode = objectMapper.readTree(cleanedResponse);
            return cleanedResponse;
            
        } catch (Exception e) {
            log.error("Error generating example sentence: {}", e.getMessage());
            // Fallback JSON format
            return String.format(
                "{\"example\": \"I use %s in my daily life.\", \"translation\": \"Tôi sử dụng %s trong cuộc sống hàng ngày.\"}",
                word.toLowerCase(), meaning
            );
        }
    }

    /**
     * Clean JSON response by removing markdown formatting
     */
    private String cleanJsonResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return "{}";
        }
        
        String cleaned = response.trim();
        
        // Remove markdown code blocks
        if (cleaned.contains("```json")) {
            int start = cleaned.indexOf("```json") + 7;
            int end = cleaned.lastIndexOf("```");
            if (end > start) {
                cleaned = cleaned.substring(start, end).trim();
            }
        } else if (cleaned.contains("```")) {
            int start = cleaned.indexOf("```") + 3;
            int end = cleaned.lastIndexOf("```");
            if (end > start) {
                cleaned = cleaned.substring(start, end).trim();
            }
        }
        
        // Find JSON object boundaries
        int jsonStart = cleaned.indexOf('{');
        int jsonEnd = cleaned.lastIndexOf('}');
        
        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            cleaned = cleaned.substring(jsonStart, jsonEnd + 1);
        }
        
        return cleaned;
    }

    private String createEvaluationPrompt(WritingEvaluationRequest request) {
        return String.format("""
            Bạn là một giáo viên tiếng Anh chuyên nghiệp, đang giúp học sinh Việt Nam cải thiện kỹ năng viết.
            Hãy đánh giá câu của học sinh một cách chi tiết và khuyến khích.
            
            TỪ VỰNG CẦN SỬ DỤNG: %s
            NGHĨA CỦA TỪ: %s
            CÂU CỦA HỌC SINH: "%s"
            
            TIÊU CHÍ ĐÁNH GIÁ:
            1. SỬ DỤNG TỪ (40%%): Từ có được dùng đúng nghĩa và tự nhiên?
            2. NGỮ PHÁP (30%%): Cấu trúc câu, thì, ngữ pháp có chính xác?
            3. Ý NGHĨA (20%%): Câu có rõ ràng, logic và dễ hiểu?
            4. TỰ NHIÊN (10%%): Câu có nghe tự nhiên như người bản ngữ?
            
            THANG ĐIỂM:
            • 9-10: Xuất sắc - Sử dụng hoàn hảo, tự nhiên
            • 7-8: Tốt - Chỉ có lỗi nhỏ, phần lớn đúng
            • 5-6: Khá - Hiểu được nhưng có vấn đề rõ ràng
            • 3-4: Cần cải thiện - Nhiều lỗi ảnh hưởng nghĩa
            • 1-2: Cần học thêm - Lỗi nghiêm trọng
            
            Trả về JSON đúng format này (không thêm text nào khác):
            {
                "score": [1-10],
                "suggestion": "Lời khuyên ngắn gọn, khuyến khích (30-40 từ)",
                "positivePoints": ["Điểm làm tốt cụ thể 1", "Điểm làm tốt cụ thể 2"],
                "improvementAreas": ["Điều cần sửa cụ thể 1", "Điều cần sửa cụ thể 2"],
                "grammarCheck": "Phân tích ngữ pháp chi tiết với ví dụ sửa (nếu có lỗi)",
                "vocabularyLevel": "Beginner/Intermediate/Advanced",
                "isCorrect": true/false
            }
            
            LƯU Ý QUAN TRỌNG:
            - Luôn khuyến khích học sinh, kể cả khi điểm thấp
            - Đưa ra lời khuyên cụ thể, có thể áp dụng ngay
            - Giải thích TẠI SAO đúng hoặc sai
            - Nếu điểm < 6, đưa ra câu sửa đề xuất
            - Nếu không dùng từ hoặc dùng sai, hướng dẫn cách dùng đúng
            - Tập trung vào việc HỌC, không chỉ chấm điểm
            """, request.getWord(), request.getMeaning(), request.getSentence());
    }

    private String callGeminiAPI(String prompt) throws Exception {
        if (geminiApiKey.isEmpty()) {
            throw new RuntimeException("Gemini API key not configured");
        }

        String url = geminiApiUrl + "?key=" + geminiApiKey;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(
                    Map.of("text", prompt)
                ))
            ),
            "generationConfig", Map.of(
                "temperature", 0.7,
                "topP", 0.8,
                "maxOutputTokens", 1000
            )
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode candidates = root.get("candidates");
            
            if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).get("content");
                if (content != null) {
                    JsonNode parts = content.get("parts");
                    if (parts != null && parts.isArray() && parts.size() > 0) {
                        return parts.get(0).get("text").asText();
                    }
                }
            }
        }
        
        throw new RuntimeException("Invalid response from Gemini API");
    }

    private WritingFeedbackResponse parseAIResponse(String aiResponse) {
        try {
            // Use the same cleaning logic for consistency
            String jsonStr = cleanJsonResponse(aiResponse);

            JsonNode root = objectMapper.readTree(jsonStr);
            
            WritingFeedbackResponse.WritingFeedbackResponseBuilder builder = WritingFeedbackResponse.builder()
                .score(root.get("score").asInt())
                .suggestion(root.get("suggestion").asText())
                .grammarCheck(root.get("grammarCheck").asText())
                .vocabularyLevel(root.get("vocabularyLevel").asText())
                .isCorrect(root.has("isCorrect") ? root.get("isCorrect").asBoolean() : false);

            // Parse arrays
            List<String> positivePoints = new ArrayList<>();
            JsonNode positiveNode = root.get("positivePoints");
            if (positiveNode != null && positiveNode.isArray()) {
                for (JsonNode point : positiveNode) {
                    positivePoints.add(point.asText());
                }
            }
            builder.positivePoints(positivePoints);

            List<String> improvementAreas = new ArrayList<>();
            JsonNode improvementNode = root.get("improvementAreas");
            if (improvementNode != null && improvementNode.isArray()) {
                for (JsonNode area : improvementNode) {
                    improvementAreas.add(area.asText());
                }
            }
            builder.improvementAreas(improvementAreas);

            return builder.build();
            
        } catch (Exception e) {
            log.error("Error parsing AI response: {}", e.getMessage());
            return createFallbackResponse();
        }
    }

    private WritingFeedbackResponse createFallbackResponse() {
        return WritingFeedbackResponse.builder()
            .score(7)
            .suggestion("Câu của bạn có thể hiểu được. Hãy tiếp tục luyện tập để cải thiện!")
            .positivePoints(List.of("Bạn đã sử dụng từ vựng đúng ngữ cảnh"))
            .improvementAreas(List.of("Có thể cải thiện ngữ pháp và cấu trúc câu"))
            .grammarCheck("Ngữ pháp cần được kiểm tra và điều chỉnh")
            .vocabularyLevel("Intermediate")
            .isCorrect(true)
            .build();
    }
}
