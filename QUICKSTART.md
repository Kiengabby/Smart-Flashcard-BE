# 🚀 QUICK START GUIDE - Smart Flashcard Backend

## ✅ Đã Fix Xong - Sẵn Sàng Sử Dụng!

Tất cả vấn đề đã được khắc phục. Backend hiện có thể chạy hoàn hảo trên bất kỳ máy nào!

📖 **Chi tiết các vấn đề đã fix:** Xem file [FIXED_ISSUES.md](FIXED_ISSUES.md)

---

## 🏃‍♂️ Chạy Nhanh

### **Option 1: H2 In-Memory Database (Development/Testing)**
```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=test"
```

✅ **Không cần cài MySQL**  
✅ **Data tạm thời** (mất khi restart)  
✅ **Perfect cho testing**

**Access:**
- 🌐 API: http://localhost:8080
- 🗄️ H2 Console: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Username: `sa`
  - Password: _(để trống)_

### **Option 2: MySQL Database (Production)**
```bash
# 1. Tạo database
mysql -u root -p
CREATE DATABASE elearning_db;

# 2. Chạy app
./mvnw spring-boot:run
```

---

## 🧪 Test API

### **Register User**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "test123456",
    "displayName": "Test User"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "user": {
    "id": "1",
    "email": "test@example.com",
    "displayName": "Test User"
  },
  "message": "User registered successfully!"
}
```

### **Login**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "test123456"
  }'
```

---

## 📋 Checklist Setup

- [x] ✅ Entities package đã được tạo và commit
- [x] ✅ H2 database tương thích
- [x] ✅ Repository queries đã sửa
- [x] ✅ Application chạy thành công
- [x] ✅ API register/login hoạt động
- [x] ✅ Code đã push lên GitHub

---

## 🎯 Next Steps

1. **Kết nối với Frontend:**
   ```bash
   cd ../Smart-Flashcard-FE
   npm start
   ```
   Frontend sẽ call API tại `http://localhost:8080`

2. **Viết Unit Tests:**
   - Xem hướng dẫn: [UNIT_TEST_GUIDE.md](UNIT_TEST_GUIDE.md)
   - Chạy tests: `./mvnw test`

3. **Xem API Documentation:**
   - File: [API_DOCUMENTATION.md](API_DOCUMENTATION.md)

---

## 🆘 Troubleshooting

### **Port 8080 đã được sử dụng?**
```bash
# Tìm process đang dùng port 8080
lsof -i :8080

# Kill process
kill -9 <PID>
```

### **MySQL connection refused?**
```bash
# Start MySQL
mysql.server start

# Hoặc dùng H2 thay thế (Option 1)
```

---

## 📚 Tài Liệu Liên Quan

- 📖 [FIXED_ISSUES.md](FIXED_ISSUES.md) - Chi tiết các vấn đề đã fix
- 📊 [SM2_ALGORITHM.md](SM2_ALGORITHM.md) - Thuật toán Spaced Repetition
- 🧪 [UNIT_TEST_GUIDE.md](UNIT_TEST_GUIDE.md) - Hướng dẫn viết tests
- 📡 [API_DOCUMENTATION.md](API_DOCUMENTATION.md) - API reference

---

**Status:** 🟢 **READY FOR DEVELOPMENT**  
**Last Updated:** 16/10/2025  
**Commit:** `22690e1`
