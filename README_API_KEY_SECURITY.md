# 🔐 API Key Security & Environment Setup Guide

## 📋 Tổng Quan

Document này hướng dẫn cách **bảo mật API key** và setup biến môi trường cho Smart Flashcard Backend.

---

## ⚠️ Vấn Đề Bảo Mật

### **KHÔNG BAO GIỜ** làm những việc sau:

❌ **Hardcode API key trong code**
```java
// ❌ WRONG - API key sẽ bị commit lên Git
private String apiKey = "AIzaSyCKOw0mavWVQfek9UawoVQVb_kwzAy3GM4";
```

❌ **Hardcode API key trong application.yml**
```yaml
# ❌ WRONG - API key sẽ bị commit lên Git
ai:
  translation:
    gemini:
      api-key: AIzaSyCKOw0mavWVQfek9UawoVQVb_kwzAy3GM4
```

❌ **Commit .env file lên Git**
```bash
# ❌ WRONG - File chứa API key sẽ public
git add .env
git commit -m "Add environment variables"
```

### **LUÔN LUÔN** làm đúng cách:

✅ **Dùng biến môi trường**
```yaml
# ✅ CORRECT - API key được load từ environment variable
ai:
  translation:
    gemini:
      api-key: ${GOOGLE_API_KEY}
```

✅ **Thêm .env vào .gitignore**
```gitignore
# Environment Variables
.env
.env.local
.env.production
```

✅ **Dùng .env.example làm template**
```bash
# .env.example - Safe to commit
GOOGLE_API_KEY=your_api_key_here

# .env - Never commit
GOOGLE_API_KEY=AIzaSyCKOw0mavWVQfek9UawoVQVb_kwzAy3GM4
```

---

## 🚀 Hướng Dẫn Setup

### **Phương Pháp 1: Dùng Script Tự Động (KHUYÊN DÙNG)**

```bash
# Chạy script setup
cd /Users/manhkien/Documents/DATN_HMK/Smart-Flashcard-BE
./set-api-key.sh

# Script sẽ hỏi API key của bạn
# Nhập API key khi được prompt
```

**Script sẽ tự động:**
- Backup file `.zshrc` hiện tại
- Xóa API key cũ (nếu có)
- Thêm API key mới vào `.zshrc`
- Apply cho terminal session hiện tại

---

### **Phương Pháp 2: Setup Thủ Công**

#### **Bước 1: Lấy API Key**

1. Truy cập: https://aistudio.google.com/app/apikey
2. Đăng nhập với Google account
3. Click "Create API key"
4. Copy API key (format: `AIza...`)

#### **Bước 2: Set Biến Môi Trường**

**Option A: Permanent (Khuyên dùng)**
```bash
# Thêm vào ~/.zshrc
echo 'export GOOGLE_API_KEY="AIzaSyCKOw0mavWVQfek9UawoVQVb_kwzAy3GM4"' >> ~/.zshrc

# Apply ngay lập tức
source ~/.zshrc

# Kiểm tra
echo $GOOGLE_API_KEY
```

**Option B: Temporary (Chỉ cho session hiện tại)**
```bash
# Set cho terminal hiện tại
export GOOGLE_API_KEY="AIzaSyCKOw0mavWVQfek9UawoVQVb_kwzAy3GM4"

# Kiểm tra
echo $GOOGLE_API_KEY
```

#### **Bước 3: Verify API Key**

```bash
# Test API key với Gemini API
curl "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent" \
  -H 'Content-Type: application/json' \
  -H "X-goog-api-key: $GOOGLE_API_KEY" \
  -X POST \
  -d '{
    "contents": [{
      "parts": [{
        "text": "Translate hello to Vietnamese"
      }]
    }]
  }'
```

**Expected Response:**
```json
{
  "candidates": [{
    "content": {
      "parts": [{
        "text": "xin chào"
      }]
    }
  }]
}
```

---

### **Phương Pháp 3: Dùng .env File (Development)**

```bash
# Bước 1: Copy template
cp .env.example .env

# Bước 2: Edit .env file
nano .env

# Bước 3: Thay thế với API key thực
GOOGLE_API_KEY=AIzaSyCKOw0mavWVQfek9UawoVQVb_kwzAy3GM4

# Bước 4: Đảm bảo .env trong .gitignore
cat .gitignore | grep .env
```

**Lưu ý:** Phương pháp này yêu cầu library `spring-boot-dotenv` hoặc chạy với IDE hỗ trợ `.env`.

---

## 🧪 Kiểm Tra Setup

### **1. Kiểm tra biến môi trường**
```bash
# Kiểm tra giá trị
echo $GOOGLE_API_KEY

# Kiểm tra có set chưa
env | grep GOOGLE_API_KEY
```

### **2. Kiểm tra Backend có nhận được không**
```bash
# Start backend
cd /Users/manhkien/Documents/DATN_HMK/Smart-Flashcard-BE
./mvnw spring-boot:run

# Test translation API (terminal khác)
curl "http://localhost:8080/public/test/translation?word=hello&source=en&target=vi"
```

**Expected Response:**
```json
{
  "word": "hello",
  "translation": "xin chào",
  "service": "Google Gemini Pro API",
  "success": true
}
```

### **3. Kiểm tra logs**
```bash
# Xem backend logs
tail -f backend.log

# Tìm dòng chứa "Gemini"
# Phải thấy: "Translating ... using Google Gemini Pro API"
# KHÔNG được thấy: "API key expired" hoặc "API_KEY_INVALID"
```

---

## 🔄 Update API Key

Khi API key hết hạn hoặc cần đổi:

```bash
# Method 1: Dùng script
./set-api-key.sh
# Nhập API key mới khi được prompt

# Method 2: Update thủ công
nano ~/.zshrc
# Tìm dòng: export GOOGLE_API_KEY="..."
# Thay thế với API key mới
source ~/.zshrc

# Method 3: Export trực tiếp
export GOOGLE_API_KEY="new_api_key_here"
```

**Sau khi update, RESTART backend:**
```bash
# Tìm process ID
lsof -i :8080 | grep LISTEN

# Kill process
kill <PID>

# Start lại
./mvnw spring-boot:run
```

---

## 📁 File Structure

```
Smart-Flashcard-BE/
├── .env.example          ✅ Safe to commit - Template
├── .env                  ❌ In .gitignore - Your actual keys
├── .gitignore           ✅ Contains .env exclusion
├── set-api-key.sh       ✅ Setup script
├── src/
│   └── main/
│       └── resources/
│           └── application.yml  ✅ Uses ${GOOGLE_API_KEY}
└── README_API_KEY_SECURITY.md  📖 This file
```

---

## 🐛 Troubleshooting

### **Lỗi: "API key is not configured"**

**Nguyên nhân:** Backend không nhận được biến môi trường

**Giải pháp:**
```bash
# 1. Kiểm tra biến môi trường có set chưa
echo $GOOGLE_API_KEY

# 2. Nếu empty, set lại
export GOOGLE_API_KEY="your_api_key_here"

# 3. Restart backend
```

---

### **Lỗi: "API key expired"**

**Nguyên nhân:** API key đã hết hạn hoặc bị revoke

**Giải pháp:**
```bash
# 1. Lấy API key mới từ Google AI Studio
# https://aistudio.google.com/app/apikey

# 2. Update biến môi trường
./set-api-key.sh

# 3. Restart backend
```

---

### **Lỗi: Backend vẫn dùng API key cũ**

**Nguyên nhân:** Backend chưa restart sau khi update

**Giải pháp:**
```bash
# 1. Kill process hiện tại
lsof -i :8080 | grep LISTEN
kill <PID>

# 2. Verify biến môi trường
echo $GOOGLE_API_KEY

# 3. Start backend mới
./mvnw spring-boot:run
```

---

## 👥 Team Setup

Khi team member clone project:

```bash
# 1. Clone repository
git clone <repository-url>
cd Smart-Flashcard-BE

# 2. Copy environment template
cp .env.example .env

# 3. Lấy API key riêng
# https://aistudio.google.com/app/apikey

# 4. Setup với script
./set-api-key.sh

# 5. Hoặc thêm vào .env
nano .env
# GOOGLE_API_KEY=your_api_key_here

# 6. Start backend
./mvnw spring-boot:run
```

---

## 📝 Best Practices

### ✅ **DO (NÊN LÀM)**

- ✅ Dùng biến môi trường cho tất cả sensitive data
- ✅ Commit `.env.example` với placeholder values
- ✅ Thêm `.env` vào `.gitignore`
- ✅ Rotate API keys định kỳ
- ✅ Dùng API keys riêng cho dev/prod
- ✅ Document setup process cho team

### ❌ **DON'T (KHÔNG NÊN LÀM)**

- ❌ Hardcode API keys trong code
- ❌ Commit `.env` lên Git
- ❌ Share API keys qua chat/email
- ❌ Dùng chung API key cho nhiều người
- ❌ Push API keys lên public repository
- ❌ Screenshot code chứa API keys

---

## 🔗 Resources

- **Google AI Studio:** https://aistudio.google.com/app/apikey
- **Gemini API Docs:** https://ai.google.dev/gemini-api/docs
- **Spring Boot Environment Variables:** https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config

---

## 📞 Support

Nếu gặp vấn đề với API key setup:

1. Kiểm tra `.env.example` file
2. Chạy `./set-api-key.sh` script
3. Đọc phần Troubleshooting
4. Kiểm tra backend logs

---

**Last Updated:** 2025-12-03  
**Author:** GitHub Copilot  
**Version:** 1.0
