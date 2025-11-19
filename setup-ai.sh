#!/bin/bash

# Smart Flashcard Environment Setup
# Run this script to set up your Google Gemini API key

echo "🚀 Smart Flashcard - Google Gemini AI Setup"
echo "==========================================="
echo ""

# Check if API key is provided as argument
if [ $# -eq 0 ]; then
    echo "❓ Please provide your Google Gemini API key:"
    echo "Usage: ./setup-ai.sh YOUR_GEMINI_API_KEY"
    echo ""
    echo "💡 To get your API key:"
    echo "1. Go to: https://makersuite.google.com/app/apikey"
    echo "2. Create a new API key"
    echo "3. Copy the key and run: ./setup-ai.sh YOUR_KEY"
    echo ""
    exit 1
fi

API_KEY=$1

# Validate API key format
if [[ ! $API_KEY =~ ^AIza[0-9A-Za-z_-]{35}$ ]]; then
    echo "⚠️  Warning: API key format doesn't match expected Gemini format"
    echo "Expected format: AIza followed by 35 characters"
    echo "Are you sure this is correct? (y/n)"
    read -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# Set environment variable for current session
export GEMINI_API_KEY=$API_KEY

# Add to shell profile for persistence
SHELL_PROFILE=""
if [[ "$SHELL" == */zsh ]]; then
    SHELL_PROFILE="$HOME/.zshrc"
elif [[ "$SHELL" == */bash ]]; then
    SHELL_PROFILE="$HOME/.bashrc"
fi

if [ ! -z "$SHELL_PROFILE" ]; then
    echo "📝 Adding GEMINI_API_KEY to $SHELL_PROFILE"
    
    # Remove existing GEMINI_API_KEY lines
    sed -i.bak '/export GEMINI_API_KEY=/d' "$SHELL_PROFILE" 2>/dev/null || true
    
    # Add new line
    echo "" >> "$SHELL_PROFILE"
    echo "# Smart Flashcard AI - Google Gemini API Key" >> "$SHELL_PROFILE"
    echo "export GEMINI_API_KEY=$API_KEY" >> "$SHELL_PROFILE"
    
    echo "✅ Environment variable added to $SHELL_PROFILE"
else
    echo "⚠️  Could not detect shell profile. Please manually add:"
    echo "export GEMINI_API_KEY=$API_KEY"
fi

# Test the API key
echo ""
echo "🧪 Testing API connection..."

# Create a simple test request
TEST_URL="https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"
TEST_BODY='{"contents":[{"parts":[{"text":"Translate \"hello\" to Vietnamese. Return only the translation."}]}]}'

HTTP_STATUS=$(curl -s -o /tmp/gemini_test_response -w "%{http_code}" \
  -H "Content-Type: application/json" \
  -H "X-goog-api-key: $API_KEY" \
  -d "$TEST_BODY" \
  "$TEST_URL")

if [ "$HTTP_STATUS" = "200" ]; then
    echo "✅ API key is valid! Gemini Pro is ready to use."
    echo ""
    echo "🎯 Next steps:"
    echo "1. Restart your terminal or run: source $SHELL_PROFILE"
    echo "2. Start your Spring Boot application"
    echo "3. Test the AI translation feature in your app"
    echo ""
    echo "🔥 Your Smart Flashcard app now has real AI translation powered by Google Gemini Pro!"
else
    echo "❌ API key test failed (HTTP $HTTP_STATUS)"
    echo "Please check your API key and try again."
    if [ -f /tmp/gemini_test_response ]; then
        echo "Error response:"
        cat /tmp/gemini_test_response
        rm /tmp/gemini_test_response
    fi
    exit 1
fi

# Clean up
rm -f /tmp/gemini_test_response
