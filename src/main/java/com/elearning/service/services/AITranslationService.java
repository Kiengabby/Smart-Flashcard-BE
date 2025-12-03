package com.elearning.service.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * AI Translation Service using ONLY Google Gemini Pro API
 * 
 * This service provides translation capabilities for flashcard learning.
 * All mock translation and OpenAI fallback code have been removed.
 * Only Google Gemini API is used for all translation requests.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AITranslationService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.translation.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${ai.translation.gemini.api-url:https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent}")
    private String geminiApiUrl;

    /**
     * Translate text using Google Gemini Pro API
     * 
     * @param text The text to translate
     * @param sourceLanguage Source language code (e.g., "en", "vi")
     * @param targetLanguage Target language code (e.g., "en", "vi")
     * @param context Optional context for better translation accuracy
     * @return Translated text
     * @throws RuntimeException if Gemini API key is not configured or translation fails
     */
    public String translateWithAI(String text, String sourceLanguage, String targetLanguage, String context) {
        validateApiKey();
        
        log.info("Translating '{}' from {} to {} using Google Gemini Pro API", text, sourceLanguage, targetLanguage);
        
        try {
            return callGeminiAPI(text, sourceLanguage, targetLanguage, context);
        } catch (Exception e) {
            log.error("Error calling Gemini API for translation: {}", e.getMessage());
            throw new RuntimeException("Translation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Batch translate multiple words using Google Gemini Pro API
     * 
     * @param texts List of texts to translate
     * @param sourceLanguage Source language code
     * @param targetLanguage Target language code
     * @param context Optional context for better translation accuracy
     * @return Map of original text to translated text
     * @throws RuntimeException if Gemini API key is not configured or translation fails
     */
    public Map<String, String> batchTranslateWithAI(List<String> texts, String sourceLanguage, String targetLanguage, String context) {
        validateApiKey();
        
        if (texts == null || texts.isEmpty()) {
            return new HashMap<>();
        }
        
        log.info("Batch translating {} words from {} to {} using Google Gemini Pro API", 
                texts.size(), sourceLanguage, targetLanguage);
        
        try {
            return callGeminiBatchTranslation(texts, sourceLanguage, targetLanguage, context);
        } catch (Exception e) {
            log.error("Error calling Gemini API for batch translation: {}", e.getMessage());
            throw new RuntimeException("Batch translation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Validate that Gemini API key is configured
     * 
     * @throws RuntimeException if API key is missing or empty
     */
    private void validateApiKey() {
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
            throw new RuntimeException("Google Gemini API key is not configured. Please set ai.translation.gemini.api-key in application.yml");
        }
    }

    /**
     * Call Google Gemini Pro API for single text translation
     */
    private String callGeminiAPI(String text, String sourceLanguage, String targetLanguage, String context) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-goog-api-key", geminiApiKey);

        String prompt = buildTranslationPrompt(text, sourceLanguage, targetLanguage, context);
        Map<String, Object> requestBody = buildGeminiRequestBody(prompt);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
                geminiApiUrl,
                HttpMethod.POST,
                entity,
                String.class
        );

        return parseGeminiResponse(response.getBody());
    }

    /**
     * Call Google Gemini Pro API for batch translation
     */
    private Map<String, String> callGeminiBatchTranslation(List<String> texts, String sourceLanguage, String targetLanguage, String context) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-goog-api-key", geminiApiKey);

        String batchPrompt = buildBatchTranslationPrompt(texts, sourceLanguage, targetLanguage, context);
        Map<String, Object> requestBody = buildGeminiRequestBody(batchPrompt);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
                geminiApiUrl,
                HttpMethod.POST,
                entity,
                String.class
        );

        String result = parseGeminiResponse(response.getBody());
        return parseBatchResponse(result, texts);
    }

    /**
     * Build translation prompt for single text
     */
    private String buildTranslationPrompt(String text, String sourceLanguage, String targetLanguage, String context) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Translate the ");
        prompt.append(getLanguageName(sourceLanguage));
        prompt.append(" word/phrase '").append(text).append("' to ");
        prompt.append(getLanguageName(targetLanguage));
        prompt.append(" for a flashcard learning app. ");
        prompt.append("Provide the most accurate and commonly used translation. ");
        prompt.append("Return only the translation without additional explanations.");
        
        if (context != null && !context.trim().isEmpty()) {
            prompt.append(" Context: ").append(context);
        }
        
        return prompt.toString();
    }

    /**
     * Build batch translation prompt for multiple texts
     */
    private String buildBatchTranslationPrompt(List<String> texts, String sourceLanguage, String targetLanguage, String context) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Translate these ");
        prompt.append(getLanguageName(sourceLanguage));
        prompt.append(" words/phrases to ");
        prompt.append(getLanguageName(targetLanguage));
        prompt.append(" for a flashcard learning app.\n\n");
        
        prompt.append("Words to translate:\n");
        for (String text : texts) {
            prompt.append("- ").append(text).append("\n");
        }
        
        if (context != null && !context.trim().isEmpty()) {
            prompt.append("\nContext: ").append(context).append("\n");
        }
        
        prompt.append("\nProvide accurate translations in the format: original_word|translation\n");
        prompt.append("One translation per line. Return only the translations without additional explanations.");
        
        return prompt.toString();
    }

    /**
     * Build Gemini API request body
     */
    private Map<String, Object> buildGeminiRequestBody(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        
        // Contents array with parts
        Map<String, String> part = new HashMap<>();
        part.put("text", prompt);
        
        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(part));
        
        requestBody.put("contents", List.of(content));
        
        // Generation config for better translation quality
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.3);  // Lower temperature for more deterministic translations
        generationConfig.put("topK", 1);
        generationConfig.put("topP", 0.8);
        generationConfig.put("maxOutputTokens", 500);
        requestBody.put("generationConfig", generationConfig);
        
        return requestBody;
    }

    /**
     * Parse Gemini API response to extract translated text
     */
    private String parseGeminiResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode candidates = root.path("candidates");
        
        if (candidates.isArray() && candidates.size() > 0) {
            JsonNode content = candidates.get(0).path("content");
            JsonNode parts = content.path("parts");
            
            if (parts.isArray() && parts.size() > 0) {
                String text = parts.get(0).path("text").asText().trim();
                
                // Clean up common response prefixes
                if (text.startsWith("Translation:")) {
                    text = text.substring("Translation:".length()).trim();
                }
                if (text.startsWith("Translated:")) {
                    text = text.substring("Translated:".length()).trim();
                }
                
                return text;
            }
        }
        
        throw new Exception("Invalid Gemini API response format: no candidates found");
    }

    /**
     * Parse batch translation response into a map
     */
    private Map<String, String> parseBatchResponse(String response, List<String> originalTexts) {
        Map<String, String> translations = new HashMap<>();
        
        if (response == null || response.trim().isEmpty()) {
            log.warn("Empty batch translation response");
            return translations;
        }
        
        // Split response by newlines
        String[] lines = response.split("\n");
        
        for (String line : lines) {
            line = line.trim();
            
            // Skip empty lines and lines without pipe separator
            if (line.isEmpty() || !line.contains("|")) {
                continue;
            }
            
            // Parse "original|translation" format
            String[] parts = line.split("\\|", 2);
            if (parts.length == 2) {
                String originalWord = parts[0].trim();
                String translation = parts[1].trim();
                
                // Clean up common prefixes like "- " or numbered lists
                originalWord = originalWord.replaceAll("^[-*•\\d]+\\.?\\s*", "");
                
                // Match with exact input text (case-insensitive for better matching)
                for (String inputText : originalTexts) {
                    if (inputText.equalsIgnoreCase(originalWord)) {
                        translations.put(inputText, translation);
                        break;
                    }
                }
            }
        }
        
        // Log if some translations are missing
        if (translations.size() < originalTexts.size()) {
            log.warn("Batch translation incomplete: got {} translations for {} requested words", 
                    translations.size(), originalTexts.size());
        }
        
        return translations;
    }

    /**
     * Get human-readable language name from language code
     */
    private String getLanguageName(String code) {
        if (code == null) {
            return "Unknown";
        }
        
        switch (code.toLowerCase()) {
            case "en": return "English";
            case "vi": return "Vietnamese";
            case "ja": return "Japanese";
            case "ko": return "Korean";
            case "zh": return "Chinese";
            case "fr": return "French";
            case "de": return "German";
            case "es": return "Spanish";
            case "it": return "Italian";
            case "pt": return "Portuguese";
            case "ru": return "Russian";
            case "ar": return "Arabic";
            case "th": return "Thai";
            case "id": return "Indonesian";
            default: return code.toUpperCase();
        }
    }
}
