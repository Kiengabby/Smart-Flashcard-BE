# 🎯 SMART FLASHCARD - SPACED REPETITION SYSTEM 

## 🏆 **HOÀN THÀNH TRIỂN KHAI THUẬT TOÁN SM-2**

### ✅ **ĐÃ HOÀN THÀNH**

#### **1. Entity Layer - Lớp Thực Thể**
- **BaseEntity.java** ✅ 
  - Common fields: id, createdAt, updatedAt, deletedAt, status
  - @MappedSuperclass cho inheritance pattern
  
- **User.java** ✅
  - Extend BaseEntity với displayName, email, password, avatar
  - @OneToMany relationship với Deck entities
  
- **Deck.java** ✅  
  - Extend BaseEntity với name, description
  - @ManyToOne với User, @OneToMany với Card
  
- **Card.java** ✅
  - Extend BaseEntity với front, back text
  - SM-2 algorithm fields: easinessFactor, intervalDays, repetitions, nextReviewDate
  
- **UserCardProgress.java** ✅ **[NEW]**
  - UUID primary key cho user progress tracking
  - SM-2 fields: easeFactor (2.5), interval (0), repetitions (0), nextReviewDate
  - Unique constraint trên user+card combination
  - Audit fields: totalReviews, correctReviews, lastReviewedDate

#### **2. Repository Layer - Lớp Truy Cập Dữ Liệu**
- **UserRepository.java** ✅
- **DeckRepository.java** ✅  
- **CardRepository.java** ✅
- **UserCardProgressRepository.java** ✅ **[NEW]**
  - `findByUserAndCard()` - Tìm progress theo user và card
  - `findDueForReview()` - Tìm thẻ đến hạn ôn tập với complex date logic
  - `countDueForReview()` - Đếm số thẻ cần ôn tập
  - `findNewCardsByUserAndDeck()` - Tìm thẻ mới chưa học

#### **3. Service Layer - Lớp Dịch Vụ**
- **ReviewService.java** ✅ **[COMPLETELY REFACTORED]**
  - **getReviewsForToday()** - Lấy thẻ cần ôn tập hôm nay theo user context
  - **submitAnswer(AnswerDTO)** - Xử lý câu trả lời với full SM-2 algorithm
  - **applySM2Algorithm()** - Core thuật toán SM-2:
    ```java
    // Công thức SM-2 chuẩn
    newEaseFactor = easeFactor + (0.1 - (5-quality) * (0.08 + (5-quality) * 0.02))
    
    // Logic intervals
    if (repetitions == 1) interval = 1
    else if (repetitions == 2) interval = 6  
    else interval = interval_cũ * easeFactor
    ```
  - **Security integration** với getCurrentUser()
  - **Error handling** comprehensive

#### **4. Controller Layer - Lớp Điều Khiển**
- **ReviewController.java** ✅ **[UPDATED]**
  - **GET /api/v1/reviews** - Lấy thẻ cần ôn tập hôm nay
  - **POST /api/v1/reviews** - Gửi câu trả lời 
  - **GET /api/v1/reviews/stats** - Thống kê ôn tập
  - **@PreAuthorize("hasRole('USER')")** - Spring Security protection
  - **Swagger documentation** với @Operation annotations
  - **ResponseDTO wrapper** cho consistent API response

#### **5. DTO Layer - Data Transfer Objects**
- **AnswerDTO.java** ✅ - cardId + quality (0-5) với validation
- **ReviewStatsDTO.java** ✅ - newCards, dueCards, reviewCards, totalCards  
- **CardDTO.java** ✅ - Complete card information

---

## 🧠 **THUẬT TOÁN SM-2 CHI TIẾT**

### **Nguyên Lý Hoạt Động:**
1. **Easiness Factor (EF)** bắt đầu từ 2.5
2. **Quality** từ 0-5 (0=hoàn toàn sai, 5=hoàn toàn đúng)  
3. **Repetitions** đếm số lần trả lời đúng liên tiếp
4. **Interval** là số ngày đến lần ôn tập tiếp theo

### **Quy Tắc Tính Toán:**
```java
// Cập nhật Easiness Factor
newEF = EF + (0.1 - (5-quality) * (0.08 + (5-quality) * 0.02))
if (newEF < 1.3) newEF = 1.3

// Logic Interval  
if (quality < 3) {
    repetitions = 0
    interval = 1  // Reset về đầu
} else {
    repetitions++
    if (repetitions == 1) interval = 1
    else if (repetitions == 2) interval = 6
    else interval = Math.ceil(interval * newEF)
}

// Ngày ôn tập tiếp theo
nextReviewDate = today + interval (days)
```

---

## 🚀 **API ENDPOINTS**

### **Review Management APIs**
| Method | Endpoint | Description | Security |
|--------|----------|-------------|-----------|
| `GET` | `/api/v1/reviews` | Lấy thẻ cần ôn tập hôm nay | USER role |
| `POST` | `/api/v1/reviews` | Gửi câu trả lời | USER role |  
| `GET` | `/api/v1/reviews/stats` | Thống kê ôn tập | USER role |

### **Request/Response Examples**

#### **POST /api/v1/reviews**
```json
{
  "cardId": "123e4567-e89b-12d3-a456-426614174000",
  "quality": 4
}
```

#### **Response Format**
```json
{
  "success": true,
  "message": "Câu trả lời đã được xử lý thành công", 
  "data": "Tiến độ học tập đã được cập nhật theo thuật toán SM-2"
}
```

---

## 📊 **DATABASE SCHEMA**

### **UserCardProgress Table**
```sql
CREATE TABLE user_card_progress (
    id VARCHAR(36) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    card_id BIGINT NOT NULL,
    ease_factor DOUBLE DEFAULT 2.5,
    interval_days INTEGER DEFAULT 0,
    repetitions INTEGER DEFAULT 0,
    next_review_date DATE,
    last_reviewed_date DATE,
    total_reviews INTEGER DEFAULT 0,
    correct_reviews INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_card (user_id, card_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (card_id) REFERENCES cards(id)
);
```

---

## 🔧 **BUILD STATUS**

### **✅ Compilation Success**
```bash
[INFO] BUILD SUCCESS
[INFO] Total time:  1.055 s
[INFO] Finished at: 2025-10-08T10:34:02+07:00
```

### **📦 Dependencies Ready**
- Spring Boot 3.3.5
- Spring Data JPA  
- Spring Security with JWT
- PostgreSQL Driver
- Lombok annotations
- ModelMapper for DTO conversion
- Jakarta Validation

---

## 🎯 **NEXT STEPS**

### **Immediate Actions:**
1. **Database Setup** - Tạo PostgreSQL database và run migrations
2. **Testing** - Viết unit tests cho SM-2 algorithm
3. **Integration** - Kết nối với Angular frontend
4. **Documentation** - API documentation với Swagger UI

### **Advanced Features:**
1. **Statistics Dashboard** - Biểu đồ tiến độ học tập
2. **Study Sessions** - Nhóm thẻ theo session học
3. **Difficulty Adjustment** - Tự động điều chỉnh độ khó
4. **Performance Analytics** - Phân tích hiệu quả học tập

---

## 👨‍💻 **DEVELOPMENT TEAM**
- **Senior Java Developer** ✅ 
- **Framework:** Spring Boot 3.3.5 + Spring Security + JPA
- **Algorithm:** SM-2 Spaced Repetition System
- **Architecture:** Clean Architecture with proper layering

**🎉 HOÀN THÀNH TRIỂN KHAI SPACED REPETITION SYSTEM VỚI THUẬT TOÁN SM-2!** 🎉