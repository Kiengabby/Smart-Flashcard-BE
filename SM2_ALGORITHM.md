# Thuật toán Lặp lại Ngắt quãng SM-2

## Giới thiệu

Ứng dụng Smart Flashcard sử dụng thuật toán SM-2 (SuperMemo 2) để tối ưu hóa quá trình ôn tập và ghi nhớ. Thuật toán này giúp xác định thời điểm tối ưu để ôn tập lại mỗi thẻ dựa trên mức độ khó khăn và hiệu quả ghi nhớ của người học.

## Nguyên lý hoạt động

### Các thông số chính:

1. **Repetitions (n)**: Số lần trả lời đúng liên tiếp
2. **Easiness Factor (EF)**: Hệ số dễ, mặc định là 2.5
3. **Interval (I)**: Khoảng thời gian đến lần ôn tập tiếp theo (tính bằng ngày)
4. **Quality (q)**: Chất lượng câu trả lời (0-5)

### Quy tắc tính toán:

#### Khi trả lời đúng (q ≥ 3):
- Lần đầu tiên (n=0): I = 1 ngày
- Lần thứ hai (n=1): I = 6 ngày  
- Lần tiếp theo (n≥2): I = I(n-1) × EF

#### Khi trả lời sai (q < 3):
- Đặt lại n = 0
- I = 1 ngày

#### Cập nhật hệ số dễ (EF):
```
EF' = EF + (0.1 - (5-q) × (0.08 + (5-q) × 0.02))
```
- EF tối thiểu = 1.3

### Thang đánh giá chất lượng:
- **5**: Hoàn hảo - Nhớ rất rõ, dễ dàng
- **4**: Đúng - Nhớ sau khi suy nghĩ một chút  
- **3**: Đúng - Nhớ với khó khăn nghiêm trọng
- **2**: Sai - Nhớ được câu trả lời đúng khi được nhắc
- **1**: Sai - Câu trả lời đúng quen thuộc
- **0**: Hoàn toàn sai - Không nhớ gì

## Cấu trúc Implementation

### Entities
- `Card`: Chứa thông tin thẻ và các thông số SM-2
- `Deck`: Bộ thẻ chứa nhiều thẻ con

### Services  
- `ReviewService`: Xử lý logic thuật toán SM-2
- `CardService`: Quản lý CRUD thẻ

### DTOs
- `AnswerDTO`: Nhận dữ liệu câu trả lời từ frontend
- `ReviewCardDTO`: Thông tin thẻ dành cho ôn tập
- `ReviewStatsDTO`: Thống kê ôn tập của deck

### API Endpoints
- `GET /api/reviews/due?deckId=X`: Lấy thẻ cần ôn tập
- `POST /api/reviews/answer`: Xử lý câu trả lời
- `GET /api/reviews/stats?deckId=X`: Thống kê ôn tập

## Luồng hoạt động

1. **Khởi tạo thẻ mới**: 
   - repetitions = 0
   - easinessFactor = 2.5
   - interval = 0
   - nextReviewDate = null

2. **Lần ôn tập đầu tiên**:
   - Người dùng đánh giá chất lượng (0-5)
   - Áp dụng thuật toán SM-2
   - Cập nhật nextReviewDate

3. **Các lần ôn tập tiếp theo**:
   - Kiểm tra nextReviewDate
   - Hiển thị thẻ cần ôn tập
   - Xử lý câu trả lời và cập nhật lịch

## Database Schema

```sql
CREATE TABLE cards (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    front_text TEXT NOT NULL,
    back_text TEXT NOT NULL,
    deck_id BIGINT NOT NULL,
    repetitions INT DEFAULT 0,
    easiness_factor DOUBLE DEFAULT 2.5,
    interval_days INT DEFAULT 0,
    next_review_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (deck_id) REFERENCES decks(id)
);
```

## Ví dụ minh họa

### Thẻ mới:
```json
{
    "id": 1,
    "frontText": "What is Java?",
    "backText": "A programming language",
    "repetitions": 0,
    "easinessFactor": 2.5,
    "interval": 0,
    "nextReviewDate": null
}
```

### Sau lần trả lời đúng đầu tiên (quality = 4):
```json
{
    "id": 1,
    "repetitions": 1,
    "easinessFactor": 2.5,
    "interval": 1,
    "nextReviewDate": "2025-09-27"
}
```

### Sau lần trả lời đúng thứ hai (quality = 5):
```json
{
    "id": 1,
    "repetitions": 2,
    "easinessFactor": 2.6,
    "interval": 6,
    "nextReviewDate": "2025-10-03"
}
```

## Lợi ích của thuật toán SM-2

1. **Tối ưu thời gian**: Chỉ ôn tập khi cần thiết
2. **Cá nhân hóa**: Thích ứng với khả năng của từng người
3. **Hiệu quả cao**: Tăng cường ghi nhớ dài hạn
4. **Khoa học**: Dựa trên nghiên cứu tâm lý học nhận thức

## Tài liệu tham khảo

- [SuperMemo Algorithm](https://www.supermemo.com/en/archives1990-2015/english/ol/sm2)
- [Spaced Repetition Research](https://en.wikipedia.org/wiki/Spaced_repetition)