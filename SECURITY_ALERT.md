# 🚨 SECURITY ALERT - API KEY EXPOSED

## ❌ Tình Huống

**API key Google Gemini bị lộ trong commit trước đó:**

```
Commit: 4dbd53e
File: README_API_KEY_SECURITY.md
Key exposed: AIzaSyCKOw0mavWVQfek9UawoVQVb_kwzAy3GM4
Date: 2025-01-04
```

## ✅ Đã Thực Hiện

1. ✅ **Đã xóa API key** khỏi tất cả file trong commit mới
2. ✅ **Đã thay thế** bằng placeholder trong README
3. ✅ **Đã commit** fix security issue

## ⚠️ HÀNH ĐỘNG BẮT BUỘC

### **1. XÓA API KEY CŨ NGAY LẬP TỨC**

Truy cập: https://aistudio.google.com/app/apikey

1. Tìm API key: `AIzaSyCKOw0mavWVQfek9UawoVQVb_kwzAy3GM4`
2. Click **Delete** / **Revoke**
3. Xác nhận xóa

### **2. TẠO API KEY MỚI**

1. Truy cập: https://aistudio.google.com/app/apikey
2. Click **"Create API key"**
3. Copy API key mới
4. Setup lại environment variable:

```bash
# Xóa key cũ trong .zshrc
nano ~/.zshrc
# Tìm và xóa dòng: export GOOGLE_API_KEY="AIzaSyCKOw..."

# Thêm key mới
echo 'export GOOGLE_API_KEY="YOUR_NEW_API_KEY_HERE"' >> ~/.zshrc

# Apply
source ~/.zshrc

# Verify
echo $GOOGLE_API_KEY
```

### **3. CẬP NHẬT CHO TEAM**

Thông báo cho team members:
- ❌ API key cũ đã bị compromised
- ✅ Cần setup API key mới theo hướng dẫn trong README
- ✅ Không sử dụng API key cũ nữa

## 📚 Tài Liệu Tham Khảo

- **Setup Guide**: `README_API_KEY_SECURITY.md`
- **Environment Setup**: `.env.example`

## 🔐 Best Practices

**KHÔNG BAO GIỜ:**
- ❌ Hardcode API key trong code
- ❌ Commit file `.env` lên Git
- ❌ Share API key qua Slack/Email/Chat
- ❌ Dùng API key production cho testing

**LUÔN LUÔN:**
- ✅ Dùng environment variables
- ✅ Add `.env` vào `.gitignore`
- ✅ Dùng `.env.example` làm template
- ✅ Rotate API key định kỳ

## 🆘 Nếu Cần Hỗ Trợ

Contact: Admin/DevOps team

---

**Updated:** 2025-01-04
**Status:** 🔴 CRITICAL - Action Required
