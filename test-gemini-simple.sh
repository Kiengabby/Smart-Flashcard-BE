#!/bin/bash

# Simple Gemini API Test
API_KEY="AIzaSyDuaUewqzM5HZD7nQ_N03nT89zMwtEbMYk"
API_URL="https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"

echo "🔍 Testing Simple Gemini API Call"

# Simple JSON payload
cat > /tmp/gemini_request.json << 'EOF'
{
  "contents": [
    {
      "parts": [
        {
          "text": "Translate the English word 'hello' to Vietnamese. Return only the translation."
        }
      ]
    }
  ],
  "generationConfig": {
    "temperature": 0.3,
    "maxOutputTokens": 50
  }
}
EOF

echo "📤 Request:"
cat /tmp/gemini_request.json

echo ""
echo "📡 Making API call..."

RESPONSE=$(curl -s -X POST "$API_URL" \
  -H "Content-Type: application/json" \
  -H "X-goog-api-key: $API_KEY" \
  -d @/tmp/gemini_request.json)

echo ""
echo "📥 Response:"
echo "$RESPONSE" | jq '.'

echo ""
echo "🎯 Translation:"
echo "$RESPONSE" | jq -r '.candidates[0].content.parts[0].text // "ERROR"'

# Cleanup
rm -f /tmp/gemini_request.json
