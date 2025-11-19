# AI Translation Service Setup Guide

## Tổng quan
Hệ thống đã được nâng cấp với AI Translation Service để cung cấp khả năng dịch từ thông minh và chính xác hơn so với mock translation cũ.

## Vấn đề đã được giải quyết

### Trước đây:
- "Feast" → "feast (từ tiếng Anh)" ❌
- "Feature" → "feature (một dạng cấu trúc/hình thức)" ❌
- "Fireworks display" → "Fireworks display (cụm từ - cần tra từ điển)" ❌
- "Float" → "float (từ tiếng Anh)" ❌
- "Folk dance" → "Folk dance (cụm từ - cần tra từ điển)" ❌

### Sau khi cải thiện:
- "Feast" → "bữa tiệc, tiệc lớn" ✅
- "Feature" → "tính năng, đặc điểm" ✅  
- "Fireworks display" → "màn bắn pháo hoa" ✅
- "Float" → "nổi, trôi nổi" ✅
- "Folk dance" → "múa dân gian" ✅
- "Mid-Autumn Festival" → "Tết Trung Thu" ✅

## Cấu hình API Keys

### Option 1: OpenAI API (Khuyến nghị - Thông minh nhất)
1. Đăng ký OpenAI account tại https://platform.openai.com/
2. Tạo API key trong phần API keys  
3. Thêm vào `application.yml`:
```yaml
openai:
  api:
    key: sk-your-openai-api-key-here
```

### Option 2: Google Translate API (Thay thế)
1. Tạo Google Cloud project
2. Enable Google Translate API
3. Tạo API key
4. Thêm vào `application.yml`:
```yaml
google:
  translate:
    api:
      key: your-google-translate-api-key
```

### Option 3: Enhanced Mock Translation (Miễn phí)
Nếu không có API key, hệ thống sẽ tự động sử dụng enhanced mock translation với từ điển được cải thiện đáng kể.

## Tính năng mới

### 1. AI Translation Service
- Sử dụng OpenAI GPT để dịch từ với context
- Hiểu nghĩa sâu hơn của từ vựng
- Cung cấp nhiều nghĩa nếu cần thiết
- Format phù hợp cho flashcard

### 2. Enhanced Mock Translation  
- Database từ vựng được mở rộng
- Pattern recognition thông minh
- Context-aware translation
- Xử lý đặc biệt cho festival, celebration terms

### 3. Batch Translation Optimization
- Xử lý nhiều từ cùng lúc hiệu quả hơn
- Fallback mechanism đảm bảo luôn có kết quả
- Context support cho translation chính xác hơn

### 4. Priority System
```
1. OpenAI AI Translation (Best quality)
2. Google Translate API (Good quality) 
3. Enhanced Mock Translation (Acceptable quality)
```

## API Endpoints

### Bulk Create Cards (Đã được cải thiện)
```http
POST /api/decks/{deckId}/cards/bulk-create
Content-Type: application/json

{
    "words": ["Feast", "Feature", "Fireworks display", "Float", "Folk dance", "Mid-Autumn Festival"],
    "sourceLanguage": "en",
    "targetLanguage": "vi", 
    "context": "festival and celebration vocabulary",
    "autoDetectLanguage": false
}
```

### Response mới:
```json
{
    "createdCards": [
        {
            "id": 76,
            "frontText": "Feast", 
            "backText": "bữa tiệc, tiệc lớn",
            "audioUrl": null,
            "repetitions": 0,
            "easinessFactor": 2.5,
            "interval": 0,
            "nextReviewDate": null
        },
        {
            "id": 77,
            "frontText": "Feature",
            "backText": "tính năng, đặc điểm", 
            "audioUrl": null,
            "repetitions": 0,
            "easinessFactor": 2.5,
            "interval": 0,
            "nextReviewDate": null
        }
    ],
    "failedCards": [],
    "totalRequested": 6,
    "successCount": 6,
    "failureCount": 0
}
```

## Testing

### Test Case 1: Festival Vocabulary
```json
{
    "words": ["carnival", "parade", "lantern", "mooncake", "traditional dance"],
    "sourceLanguage": "en",
    "targetLanguage": "vi",
    "context": "Mid-Autumn Festival celebrations"
}
```

### Test Case 2: Technology Terms
```json
{
    "words": ["algorithm", "framework", "database", "authentication", "deployment"],
    "sourceLanguage": "en", 
    "targetLanguage": "vi",
    "context": "software development"
}
```

## Cost Considerations

### OpenAI API Pricing (gpt-3.5-turbo)
- Input: $0.0015 / 1K tokens
- Output: $0.002 / 1K tokens
- Ví dụ: Dịch 50 từ ≈ $0.01-0.02

### Khuyến nghị:
1. Sử dụng OpenAI cho production (quality tốt nhất)
2. Sử dụng Enhanced Mock cho development/demo
3. Set rate limiting để tránh overage

## Monitoring & Logs

Hệ thống sẽ log các thông tin sau:
```
INFO: Using AI translation service for batch translation
WARN: AI translation failed, falling back to Google Translate
INFO: OpenAI API key not configured, using enhanced mock translation
```

## Troubleshooting

### Lỗi thường gặp:
1. **API Key không hợp lệ**: Kiểm tra key trong application.yml
2. **Rate limit exceeded**: Thêm delay hoặc giảm batch size  
3. **Network timeout**: Kiểm tra kết nối internet
4. **Quota exceeded**: Kiểm tra billing OpenAI/Google

### Performance Tips:
1. Sử dụng context để cải thiện accuracy
2. Batch size tối ưu: 10-20 từ
3. Cache translations để tránh duplicate calls
4. Monitor API usage thường xuyên

## Roadmap

### Version 2.0:
- [ ] Caching layer cho translations
- [ ] Offline dictionary database
- [ ] Multiple AI providers support
- [ ] Custom domain-specific dictionaries
- [ ] Audio pronunciation integration

Hệ thống translation mới này sẽ cải thiện đáng kể trải nghiệm học từ vựng của người dùng!
