# API Documentation - Review System

## Tổng quan

Hệ thống Review cung cấp các API để thực hiện ôn tập thẻ flashcard với thuật toán SM-2. Hệ thống này giúp tối ưu hóa quá trình học tập bằng cách xác định thời điểm tối ưu để ôn tập lại mỗi thẻ.

## Base URL
```
http://localhost:8080/api/reviews
```

## Authentication
Tất cả API đều yêu cầu JWT token trong header:
```
Authorization: Bearer <your-jwt-token>
```

## Endpoints

### 1. Lấy thẻ cần ôn tập

**GET** `/due`

Lấy danh sách các thẻ cần ôn tập trong một deck cụ thể.

#### Parameters:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| deckId | Long | Yes | ID của deck cần lấy thẻ ôn tập |

#### Example Request:
```http
GET /api/reviews/due?deckId=1
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

#### Example Response:
```json
{
  "success": true,
  "message": "Lấy thẻ cần ôn tập thành công",
  "data": [
    {
      "id": 1,
      "frontText": "What is Java?",
      "backText": "A programming language",
      "repetitions": 0,
      "easinessFactor": 2.5,
      "interval": 0,
      "nextReviewDate": null
    },
    {
      "id": 2,
      "frontText": "What is Spring Boot?",
      "backText": "Java framework for building applications",
      "repetitions": 2,
      "easinessFactor": 2.6,
      "interval": 6,
      "nextReviewDate": "2025-09-26"
    }
  ],
  "errorCode": null
}
```

### 2. Xử lý câu trả lời

**POST** `/answer`

Xử lý câu trả lời của người dùng và cập nhật lịch ôn tập theo thuật toán SM-2.

#### Request Body:
```json
{
  "cardId": 1,
  "quality": 4
}
```

#### Parameters:
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| cardId | Long | Yes | ID của thẻ được trả lời |
| quality | Integer | Yes | Chất lượng câu trả lời (0-5) |

#### Quality Scale:
- **5**: Hoàn hảo - Nhớ rất rõ, dễ dàng
- **4**: Đúng - Nhớ sau khi suy nghĩ một chút
- **3**: Đúng - Nhớ với khó khăn nghiêm trọng
- **2**: Sai - Nhớ được câu trả lời đúng khi được nhắc
- **1**: Sai - Câu trả lời đúng quen thuộc
- **0**: Hoàn toàn sai - Không nhớ gì

#### Example Request:
```http
POST /api/reviews/answer
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
Content-Type: application/json

{
  "cardId": 1,
  "quality": 4
}
```

#### Example Response:
```json
{
  "success": true,
  "message": "Xử lý câu trả lời thành công",
  "data": "Thẻ đã được cập nhật lịch ôn tập",
  "errorCode": null
}
```

### 3. Lấy thống kê ôn tập

**GET** `/stats`

Lấy thông tin thống kê về trạng thái ôn tập của một deck.

#### Parameters:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| deckId | Long | Yes | ID của deck cần lấy thống kê |

#### Example Request:
```http
GET /api/reviews/stats?deckId=1
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

#### Example Response:
```json
{
  "success": true,
  "message": "Lấy thống kê ôn tập thành công",
  "data": {
    "totalCards": 20,
    "newCards": 5,
    "dueCards": 8,
    "reviewCards": 3
  },
  "errorCode": null
}
```

#### Response Fields:
| Field | Type | Description |
|-------|------|-------------|
| totalCards | Integer | Tổng số thẻ trong deck |
| newCards | Integer | Số thẻ mới chưa học |
| dueCards | Integer | Tổng số thẻ cần ôn tập |
| reviewCards | Integer | Số thẻ cần ôn lại |

## Error Responses

### 400 Bad Request
```json
{
  "success": false,
  "message": "Chất lượng phải nằm trong khoảng từ 0 đến 5",
  "data": null,
  "errorCode": "BAD_REQUEST"
}
```

### 401 Unauthorized
```json
{
  "success": false,
  "message": "Token không hợp lệ",
  "data": null,
  "errorCode": "UNAUTHORIZED"
}
```

### 403 Forbidden
```json
{
  "success": false,
  "message": "Bạn không có quyền truy cập bộ thẻ này",
  "data": null,
  "errorCode": "FORBIDDEN"
}
```

### 404 Not Found
```json
{
  "success": false,
  "message": "Không tìm thấy thẻ với ID: 999",
  "data": null,
  "errorCode": "NOT_FOUND"
}
```

## Validation Rules

### AnswerDTO Validation:
- `cardId`: Không được null
- `quality`: Phải từ 0 đến 5, không được null

## Example Usage Flow

### 1. Bắt đầu phiên ôn tập
```javascript
// Lấy thống kê trước khi bắt đầu
const stats = await fetch('/api/reviews/stats?deckId=1');
console.log(`Có ${stats.data.dueCards} thẻ cần ôn tập`);

// Lấy danh sách thẻ cần ôn tập  
const dueCards = await fetch('/api/reviews/due?deckId=1');
const cards = dueCards.data;
```

### 2. Ôn tập từng thẻ
```javascript
for (const card of cards) {
    // Hiển thị câu hỏi
    showQuestion(card.frontText);
    
    // Đợi người dùng trả lời và đánh giá
    const quality = await getUserQualityRating();
    
    // Gửi kết quả lên server
    await fetch('/api/reviews/answer', {
        method: 'POST',
        body: JSON.stringify({
            cardId: card.id,
            quality: quality
        })
    });
}
```

### 3. Kết thúc phiên ôn tập
```javascript
// Lấy thống kê sau khi ôn tập
const finalStats = await fetch('/api/reviews/stats?deckId=1');
console.log(`Còn lại ${finalStats.data.dueCards} thẻ cần ôn tập`);
```

## Testing

### Curl Examples

#### Lấy thẻ cần ôn tập:
```bash
curl -X GET "http://localhost:8080/api/reviews/due?deckId=1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Trả lời thẻ:
```bash
curl -X POST "http://localhost:8080/api/reviews/answer" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"cardId": 1, "quality": 4}'
```

#### Lấy thống kê:
```bash
curl -X GET "http://localhost:8080/api/reviews/stats?deckId=1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```