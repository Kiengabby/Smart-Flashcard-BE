#!/bin/bash

# Script to set Google Gemini API key permanently
# Author: GitHub Copilot
# Date: 2025-12-03

API_KEY="AIzaSyCKOw0mavWVQfek9UawoVQVb_kwzAy3GM4"

echo "🔑 Setting up Google Gemini API Key..."

# Update .zshrc with new API key
ZSHRC="$HOME/.zshrc"

# Remove old GOOGLE_API_KEY if exists
sed -i.bak '/export GOOGLE_API_KEY=/d' "$ZSHRC" 2>/dev/null

# Add new API key
echo "" >> "$ZSHRC"
echo "# Google Gemini API Key for Smart Flashcard" >> "$ZSHRC"
echo "export GOOGLE_API_KEY=\"$API_KEY\"" >> "$ZSHRC"

# Apply immediately to current session
export GOOGLE_API_KEY="$API_KEY"

echo "✅ API Key updated in $ZSHRC"
echo "✅ Current session updated"
echo ""
echo "🔄 To apply in new terminals, run: source ~/.zshrc"
echo ""
echo "📝 Current API Key: $GOOGLE_API_KEY"
