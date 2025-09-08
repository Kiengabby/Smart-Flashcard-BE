# E-Learning Service - Backend API

## Giới thiệu

Đây là dự án backend cho hệ thống E-Learning, được phát triển như một phần của đồ án tốt nghiệp. Hệ thống cung cấp các API cho việc quản lý khóa học, học viên, giảng viên và các tính năng học tập trực tuyến.

## Công nghệ sử dụng

- **Backend Framework**: Spring Boot 3.0.13
- **Language**: Java 17
- **Database**: MySQL 8.0+
- **ORM**: Hibernate JPA + JOOQ
- **Security**: Spring Security + JWT
- **Build Tool**: Maven
- **Real-time Communication**: WebSocket
- **File Storage**: Cloudinary, Google Cloud Storage

## Tính năng chính

### 🔐 Quản lý người dùng
- Đăng ký và xác thực người dùng
- Phân quyền dựa trên vai trò (Admin, Giảng viên, Học viên)
- Quản lý thông tin cá nhân
- Bảo mật JWT

### 📚 Quản lý khóa học
- Tạo và quản lý khóa học theo môn học
- Quản lý bài giảng và tài liệu
- Phân loại nội dung theo cấp độ và chương trình
- Theo dõi tiến độ học tập

### 📝 Hệ thống bài tập và đánh giá
- Tạo và quản lý bài kiểm tra trắc nghiệm
- Hệ thống chấm điểm tự động
- Theo dõi tiến độ học tập
- Phân tích và báo cáo kết quả

### 💬 Giao tiếp tương tác
- Chat real-time giữa học viên và giảng viên
- Hệ thống thông báo
- Diễn đàn thảo luận
- Messaging qua WebSocket

### 📊 Phân tích và báo cáo
- Báo cáo tiến độ học viên
- Thống kê điểm số và xếp hạng
- Phân tích điểm mạnh/yếu theo môn học
- Dashboard hiệu suất

## Yêu cầu hệ thống

- Java 17 hoặc cao hơn
- MySQL 8.0+
- Maven 3.6+
- 4GB RAM (khuyến nghị)
- 10GB dung lượng trống

## Cài đặt và chạy dự án

### 1. Clone repository
```bash
git clone [repository-url]
cd ELearningService
```

### 2. Cấu hình cơ sở dữ liệu
- Tạo database MySQL với tên `elearning_db`
- Cập nhật thông tin kết nối database trong `src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/elearning_db
    username: your_username
    password: your_password
```

### 3. Cài đặt dependencies
```bash
mvn clean install
```

### 4. Chạy ứng dụng
```bash
mvn spring-boot:run
```

Hoặc sử dụng Maven wrapper:
```bash
./mvnw spring-boot:run
```

### 5. Truy cập ứng dụng
- API Base URL: `http://localhost:8080`
- API Documentation: `http://localhost:8080/swagger-ui.html`

## Cấu trúc dự án

```
src/
├── main/
│   ├── java/
│   │   └── com/elearning/service/
│   │       ├── ELearningServiceApplication.java
│   │       ├── configurations/          # Cấu hình Spring
│   │       ├── controllers/            # REST Controllers
│   │       ├── dtos/                   # Data Transfer Objects
│   │       ├── entities/               # JPA Entities
│   │       ├── repositories/           # Data Access Layer
│   │       ├── services/               # Business Logic Layer
│   │       ├── security/               # Cấu hình bảo mật
│   │       ├── constants/              # Hằng số
│   │       ├── exceptions/             # Xử lý ngoại lệ
│   │       └── utils/                  # Utility Classes
│   └── resources/
│       └── application.yml             # Cấu hình ứng dụng
└── test/                               # Unit và Integration Tests
```

## API Endpoints chính

- `/api/auth/**` - Xác thực & Phân quyền
- `/api/users/**` - Quản lý người dùng
- `/api/courses/**` - Quản lý khóa học
- `/api/lessons/**` - Quản lý bài giảng
- `/api/exercises/**` - Bài tập và kiểm tra
- `/api/chat/**` - Hệ thống chat real-time
- `/api/notifications/**` - Quản lý thông báo
- `/api/files/**` - Upload và quản lý file

## Xác thực

Tất cả các endpoint được bảo vệ đều yêu cầu JWT token trong header Authorization:
```
Authorization: Bearer <your_jwt_token>
```

## Testing

Chạy unit tests:
```bash
mvn test
```

Chạy với coverage:
```bash
mvn test jacoco:report
```

## Deployment

### Cấu hình Production
1. Cập nhật `application.yml` cho môi trường production
2. Cấu hình kết nối database production
3. Thiết lập cấu hình logging phù hợp
4. Cấu hình CORS cho frontend URL production

### Docker Deployment (Tùy chọn)
```bash
# Build ứng dụng
mvn clean package

# Build Docker image
docker build -t elearning-backend .

# Chạy container
docker run -p 8080:8080 elearning-backend
```

## Đóng góp

Contributions are welcome! Vui lòng làm theo các bước sau:

1. Fork repository
2. Tạo feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Tạo Pull Request

### Quy tắc code
- Tuân thủ Java naming conventions
- Sử dụng tên biến và method có ý nghĩa
- Thêm comment chi tiết cho logic phức tạp
- Viết unit tests cho features mới
- Đảm bảo tất cả tests pass trước khi submit PR

## License

This project is licensed under the MIT License.

## 🔄 Changelog

### Version 1.0.0 (Initial Release)
- Thiết lập project structure cơ bản
- Cấu hình Spring Boot với các dependencies chính
- Tạo base classes và interfaces
- Cấu hình JWT security
- Thiết lập exception handling
- Cấu hình database và JOOQ

---

**Note**: Đây là phiên bản base của dự án. Vui lòng cập nhật README này khi thêm các tính năng mới.
