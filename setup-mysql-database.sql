-- Script tạo database cho Smart Flashcard Application
-- Chạy script này trong MySQL Workbench

-- Tạo database
DROP DATABASE IF EXISTS DATN;
CREATE DATABASE DATN 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

-- Sử dụng database
USE DATN;

-- Tạo user riêng cho ứng dụng (tùy chọn, có thể dùng root)
-- CREATE USER IF NOT EXISTS 'smartflashcard'@'localhost' IDENTIFIED BY '123456';
-- GRANT ALL PRIVILEGES ON DATN.* TO 'smartflashcard'@'localhost';
-- FLUSH PRIVILEGES;

-- Kiểm tra database đã được tạo
SHOW DATABASES LIKE 'DATN';

-- Hiển thị thông báo
SELECT 'Database DATN đã được tạo thành công!' AS Message;
