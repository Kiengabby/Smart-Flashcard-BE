#!/bin/bash

echo "🧪 Testing Real Gemini API Integration with Backend"

# Backend URL
BACKEND_URL="http://localhost:8080"

# Test login first to get token
echo "1️⃣ Logging in to get token..."
LOGIN_RESPONSE=$(curl -s -X POST "$BACKEND_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "kienhm2004@gmail.com",
    "password": "123456"
  }')

TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.token // empty')

if [ -z "$TOKEN" ]; then
  echo "❌ Login failed!"
  echo "Response: $LOGIN_RESPONSE"
  exit 1
fi

echo "✅ Login successful, token: ${TOKEN:0:20}..."

# Test bulk create with specific words to see if Gemini works
echo ""
echo "2️⃣ Testing Bulk Create with Gemini API..."

BULK_RESPONSE=$(curl -s -X POST "$BACKEND_URL/api/cards/bulk-create" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "deckId": 1,
    "sourceLanguage": "en",
    "targetLanguage": "vi",
    "words": ["hello", "world", "friend"],
    "context": "basic greetings"
  }')

echo "📥 Bulk Create Response:"
echo "$BULK_RESPONSE" | jq '.'

echo ""
echo "3️⃣ Checking if Gemini translations are used..."

# Extract translations to see if they are real or mock
TRANSLATIONS=$(echo "$BULK_RESPONSE" | jq -r '.data.cards[].back')

echo "Translations received:"
for translation in $TRANSLATIONS; do
  echo "- $translation"
done

# Check if any translation contains "(cần tra cứu)" which indicates mock fallback
if echo "$TRANSLATIONS" | grep -q "cần tra cứu"; then
  echo ""
  echo "⚠️ Still using mock translations! Need to debug further."
else
  echo ""
  echo "🎉 SUCCESS! Real AI translations are working!"
fi

echo ""
echo "✅ Test completed!"
