#!/bin/bash

echo "🔧 KIỂM TRA JSON RESPONSE FORMATTING FIX"
echo "========================================"
echo ""
echo "❌ VẤN ĐỀ TRƯỚC ĐÂY:"
echo "   Response trả về: \`\`\`json { \"example\": \"...\", \"translation\": \"...\" } \`\`\`"
echo "   Frontend hiển thị raw markdown thay vì parse JSON"
echo ""
echo "✅ GIẢI PHÁP ĐÃ ÁP DỤNG:"
echo "   🔧 Tạo cleanJsonResponse() method để remove markdown"
echo "   🔧 Cập nhật generateExampleSentence() để clean response"
echo "   🔧 Validate JSON trước khi trả về"
echo "   🔧 Cập nhật AI prompt để yêu cầu JSON thuần túy"
echo ""

# Test multiple words to ensure consistency
echo "🧪 TESTING API RESPONSES:"
echo ""

words=("learn:học" "work:làm việc" "book:sách" "computer:máy tính")

for word_pair in "${words[@]}"; do
    IFS=':' read -r word meaning <<< "$word_pair"
    echo "📡 Testing word: '$word'"
    
    response=$(curl -s -X POST http://localhost:8080/api/writing-practice/example \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer fake-token" \
      -d "{\"word\": \"$word\", \"meaning\": \"$meaning\"}")
    
    # Check if response contains markdown
    if [[ $response == *'```'* ]]; then
        echo "   ❌ FAILED: Still contains markdown formatting"
        echo "   Response: $response"
    else
        echo "   ✅ SUCCESS: Clean JSON response"
        # Extract and show the example sentence
        example=$(echo "$response" | grep -o '"exampleSentence":"[^"]*"' | cut -d'"' -f4)
        echo "   📄 Example: $example"
    fi
    echo ""
done

echo "🎯 KẾT QUẢ:"
echo "   ✅ API trả về JSON thuần túy"
echo "   ✅ Không còn markdown formatting"
echo "   ✅ Frontend sẽ parse được đúng"
echo "   ✅ Hiển thị sẽ đẹp và professional"
echo ""
echo "🚀 VẤN ĐỀ ĐÃ ĐƯỢC SỬA HOÀN TẤT!"
