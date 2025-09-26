# Changelog

Tất cả những thay đổi đáng chú ý của dự án sẽ được ghi lại trong file này.

Format dựa trên [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
và dự án tuân theo [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2025-09-26

### Added
- **Hệ thống ôn tập thông minh với thuật toán SM-2**
  - Thuật toán lặp lại ngắt quãng SuperMemo 2 để tối ưu quá trình ghi nhớ
  - API để lấy thẻ cần ôn tập: `GET /api/reviews/due`
  - API xử lý câu trả lời: `POST /api/reviews/answer`
  - API thống kê ôn tập: `GET /api/reviews/stats`
  - Tự động tính toán lịch ôn tập dựa trên chất lượng câu trả lời

- **Entity và Database Enhancement**
  - Thêm các trường SM-2 vào `Card` entity: `repetitions`, `easiness_factor`, `interval_days`, `next_review_date`
  - Repository methods mới cho việc tìm thẻ cần ôn tập
  - Migration tự động với JPA DDL

- **DTOs mới**
  - `AnswerDTO`: Nhận dữ liệu câu trả lời từ frontend
  - `ReviewStatsDTO`: Thống kê ôn tập của deck
  - `ReviewCardDTO`: Thông tin chi tiết thẻ ôn tập
  - Cập nhật `CardDTO` với thông tin SM-2

- **Services**
  - `ReviewService`: Logic core của thuật toán SM-2
  - Unit tests cho `ReviewService` với coverage cao
  - Validation và security cho tất cả operations

- **Documentation**
  - `SM2_ALGORITHM.md`: Tài liệu chi tiết thuật toán SM-2
  - `API_DOCUMENTATION.md`: Hướng dẫn sử dụng API
  - Examples và curl commands

### Changed
- **Java Version Upgrade**
  - Nâng cấp từ Java 17 lên Java 21 (LTS)
  - Spring Boot từ 3.0.13 lên 3.3.5
  - Cập nhật tất cả dependencies tương thích với Java 21

- **JWT Library Update**
  - Cập nhật JJWT từ 0.11.5 lên 0.12.6
  - Sử dụng modern JWT APIs thay thế deprecated methods
  - Cải thiện security và performance

- **Dependencies Update**
  - MySQL Connector: `mysql-connector-java` → `mysql-connector-j` 8.4.0
  - Gson: 2.8.2 → 2.11.0
  - Apache Commons Lang3: 3.12.0 → 3.17.0
  - Jakarta Validation: 2.0.0.Final → 3.1.0

### Fixed
- Deprecated JWT methods được thay thế bằng modern APIs
- Import statements cleanup
- Code formatting và style consistency

### Technical Improvements
- **Testing**: Comprehensive unit tests cho ReviewService
- **Logging**: Detailed logging cho review operations
- **Security**: Authorization checks cho tất cả review endpoints
- **Performance**: Transaction management và database optimization
- **Documentation**: API examples và integration guides

## [1.0.0] - 2025-09-25

### Added
- Khởi tạo dự án với Spring Boot 3.0.13 và Java 17
- Hệ thống authentication và authorization với JWT
- CRUD operations cho Users, Decks, Cards
- Database integration với MySQL
- Basic security configuration
- REST API endpoints cơ bản
- Docker support và deployment scripts

### Core Features
- User registration và login
- Deck management (tạo, sửa, xóa bộ thẻ)
- Card management (tạo, sửa, xóa thẻ flashcard)
- Security với Spring Security
- Exception handling
- Validation với Jakarta Bean Validation

### Infrastructure
- Maven build configuration
- MySQL database integration
- JOOQ code generation
- Application configuration
- Logging setup
- Development profiles