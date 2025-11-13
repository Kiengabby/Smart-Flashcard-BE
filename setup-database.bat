@echo off
echo =============================================================================
echo SMART FLASHCARD - MySQL Database Setup Script
echo Author: Kien - Smart Flashcard Team  
echo =============================================================================
echo.

echo 🚀 Đang thiết lập database MySQL cho Smart Flashcard...
echo.

REM Kiểm tra xem MySQL có đang chạy không
echo 🔍 Kiểm tra MySQL service...
sc query MySQL80 >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ MySQL service không tìm thấy hoặc chưa được cài đặt
    echo 📝 Vui lòng cài đặt MySQL Server trước khi chạy script này
    pause
    exit /b 1
)

REM Kiểm tra trạng thái MySQL service
for /f "tokens=3 delims=: " %%H in ('sc query MySQL80 ^| findstr "        STATE"') do (
    if /i "%%H" neq "RUNNING" (
        echo 🔄 Đang khởi động MySQL service...
        net start MySQL80
        if %errorlevel% neq 0 (
            echo ❌ Không thể khởi động MySQL service
            pause
            exit /b 1
        )
    )
)

echo ✅ MySQL service đang chạy
echo.

echo 📂 Chạy script tạo database và bảng...
mysql -u root -p123456 < mysql-setup-complete.sql

if %errorlevel% equ 0 (
    echo.
    echo ✅ ĐÃ THIẾT LẬP DATABASE THÀNH CÔNG!
    echo.
    echo 📊 Thông tin kết nối:
    echo    - Host: localhost:3306
    echo    - Database: smart_flashcard
    echo    - Username: root  
    echo    - Password: 123456
    echo.
    echo 🎯 Các bảng đã được tạo:
    echo    ✓ users (người dùng)
    echo    ✓ decks (bộ thẻ học)
    echo    ✓ cards (thẻ học)
    echo    ✓ user_card_progress (tiến độ học SM-2)
    echo    ✓ quiz_sessions (phiên quiz)
    echo.
    echo 📋 Dữ liệu demo đã được thêm:
    echo    ✓ 3 users (bao gồm kiengabby@gmail.com)
    echo    ✓ 5 bộ thẻ học
    echo    ✓ 25 thẻ học mẫu
    echo.
    echo 🚀 Sẵn sàng để chạy Spring Boot application!
    echo.
) else (
    echo ❌ LỖI KHI THIẾT LẬP DATABASE
    echo 📝 Vui lòng kiểm tra:
    echo    - MySQL đã được cài đặt và đang chạy
    echo    - Username/password đúng: root/123456
    echo    - File mysql-setup-complete.sql tồn tại
    echo.
)

echo 💡 Để kiểm tra database, bạn có thể chạy:
echo    mysql -u root -p123456 -e "USE smart_flashcard; SHOW TABLES;"
echo.
pause