#!/bin/bash

# Debug Gemini API Direct Call
API_KEY="AIzaSyDuaUewqzM5HZD7nQ_N03nT89zMwtEbMYk"
API_URL="https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"

echo "🔍 Debug: Testing Gemini API Direct Call"
echo "API URL: $API_URL"
echo "API Key: ${API_KEY:0:20}..."

# Test prompt (properly escaped)
PROMPT="Translate this English word to Vietnamese for a flashcard learning application.\n\nText to translate: \"hello\"\n\nRequirements:\n- Provide the most accurate and commonly used translation\n- Keep it concise but informative for language learners\n- Use natural Vietnamese that students can easily understand\n- Return ONLY the translation, no explanations\n\nTranslation:"

# Create request payload
REQUEST_BODY=$(cat <<EOF
{
  "contents": [
    {
      "parts": [
        {
          "text": "$PROMPT"
        }
      ]
    }
  ],
  "generationConfig": {
    "temperature": 0.3,
    "topK": 1,
    "topP": 0.8,
    "maxOutputTokens": 200
  }
}
EOF
)

echo ""
echo "📤 Request Payload:"
echo "$REQUEST_BODY" | jq '.'

echo ""
echo "📡 Making API call..."

# Make API call
RESPONSE=$(curl -s -X POST "$API_URL" \
  -H "Content-Type: application/json" \
  -H "X-goog-api-key: $API_KEY" \
  -d "$REQUEST_BODY")

echo ""
echo "📥 Raw Response:"
echo "$RESPONSE" | jq '.'

echo ""
echo "🎯 Extracted Translation:"
echo "$RESPONSE" | jq -r '.candidates[0].content.parts[0].text // "No translation found"'

echo ""
echo "✅ Debug complete!"
