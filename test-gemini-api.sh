#!/bin/bash

echo "🧪 KIỂM TRA GOOGLE GEMINI API KEY"
echo "================================="

# Check if API key exists
if [ -z "$GEMINI_API_KEY" ]; then
    echo "❌ GEMINI_API_KEY không được thiết lập!"
    echo "Chạy lệnh: export GEMINI_API_KEY='your_key_here'"
    exit 1
fi

echo "✅ API Key được tìm thấy: ${GEMINI_API_KEY:0:20}..."
echo ""

echo "🔍 Testing API key với Gemini..."
response=$(curl -s -w "%{http_code}" -o /tmp/gemini_test.json \
  -H "Content-Type: application/json" \
  -d '{
    "contents": [{
      "parts": [{"text": "Hello, test message"}]
    }],
    "generationConfig": {
      "temperature": 0.7,
      "maxOutputTokens": 100
    }
  }' \
  "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$GEMINI_API_KEY")

status_code="${response: -3}"

echo "📡 HTTP Status Code: $status_code"

if [ "$status_code" == "200" ]; then
    echo "🎉 SUCCESS! API key hoạt động tốt!"
    echo "📄 Response preview:"
    cat /tmp/gemini_test.json | head -5
elif [ "$status_code" == "400" ]; then
    echo "❌ BAD REQUEST - API key có thể bị sai format"
    cat /tmp/gemini_test.json
elif [ "$status_code" == "403" ]; then
    echo "❌ FORBIDDEN - API key không có quyền hoặc đã hết hạn"
    echo "💡 Cần tạo API key mới tại: https://aistudio.google.com/app/apikey"
    cat /tmp/gemini_test.json
elif [ "$status_code" == "429" ]; then
    echo "⏰ RATE LIMIT - Đã vượt giới hạn requests"
    echo "💡 Đợi 1 phút rồi thử lại"
    cat /tmp/gemini_test.json
else
    echo "❓ UNKNOWN ERROR - Status: $status_code"
    cat /tmp/gemini_test.json
fi

echo ""
echo "🔧 CÁCH KHẮC PHỤC NẾU LỖI:"
echo "1. Tạo API key mới: https://aistudio.google.com/app/apikey"
echo "2. Chạy: export GEMINI_API_KEY='new_key_here'"
echo "3. Restart backend"

# Cleanup
rm -f /tmp/gemini_test.json
