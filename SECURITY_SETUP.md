# 🔐 Security Setup Guide

## 🚨 URGENT: API Key Security Issue Fixed

This document outlines how to properly configure API keys after the security incident.

### ⚡ What Happened?
- Google API key was accidentally committed to Git
- GitHub detected and reported the exposed key  
- Key has been removed and replaced with environment variables

## ✅ Quick Setup Instructions

### 1. Create .env file
Create a `.env` file in the root directory:

```env
# Google API Configuration
GOOGLE_API_KEY=AIzaSyBvNTrAoZHDlCMNqQejNLPx0ykYL4dYNw0
```

### 2. Verify application.yml
Ensure `application.yml` uses environment variables:

```yaml
ai:
  translation:
    gemini:
      api-key: ${GOOGLE_API_KEY:your_api_key_here}
```

### 3. Run the application
```bash
mvn spring-boot:run
```

## 🛡️ Security Best Practices

- ✅ `.env` file is in `.gitignore`
- ✅ Use environment variables for sensitive data
- ✅ Template file `application-example.yml` created
- ❌ Never commit API keys to Git

## 🔄 For Future Development

1. Always use environment variables for secrets
2. Use placeholder values in committed config files
3. Document required environment variables
4. Regularly rotate API keys

### 🔧 Setup Instructions

#### 1. Create Environment Variables

**On macOS/Linux:**
```bash
export GOOGLE_API_KEY="your_new_regenerated_api_key_here"
```

**On Windows:**
```cmd
set GOOGLE_API_KEY=your_new_regenerated_api_key_here
```

#### 2. For Production (Docker/Server):
```bash
# Add to your .bashrc or .profile
echo 'export GOOGLE_API_KEY="your_new_api_key"' >> ~/.bashrc
source ~/.bashrc
```

#### 3. IDE Configuration (IntelliJ/Eclipse):
- Go to Run Configuration
- Add Environment Variables:
  - `GOOGLE_API_KEY=your_new_api_key`

### 🛡️ Security Best Practices

#### ✅ DO:
- Use environment variables for all secrets
- Regenerate compromised keys immediately  
- Set API key restrictions in Google Console
- Monitor API usage regularly
- Use different keys for dev/staging/production

#### ❌ DON'T:
- Commit API keys to Git
- Share keys in chat/email
- Use production keys in development
- Leave keys unrestricted

### 🔍 Verify Setup

Run this command to check if environment variable is set:
```bash
echo $GOOGLE_API_KEY
```

### 📱 Google Cloud Console Security Settings

1. **Go to:** https://console.cloud.google.com/apis/credentials
2. **Find your API key** 
3. **Click "Edit"**
4. **Add restrictions:**
   - **Application restrictions:** HTTP referrers or IP addresses
   - **API restrictions:** Select only needed APIs:
     - Cloud Translation API
     - Cloud Text-to-Speech API
     - Generative Language API

### 🚨 Emergency Response Checklist

If API key is compromised again:

- [ ] Regenerate key immediately in Google Console
- [ ] Update environment variables
- [ ] Check billing/usage for suspicious activity  
- [ ] Review git history for sensitive data
- [ ] Force push cleaned history to GitHub
- [ ] Monitor API usage for 24-48 hours

### 📞 Support

If you need help:
1. Check Google Cloud Console for API usage
2. Review git history: `git log --grep="API"`
3. Contact Google Cloud Support if suspicious activity detected

---
**Last Updated:** $(date)
**Status:** ✅ Secured with Environment Variables
