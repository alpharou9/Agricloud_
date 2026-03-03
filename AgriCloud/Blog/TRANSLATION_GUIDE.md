# 🌍 AgriCloud Blog - WITH TRANSLATION API

## ✅ What's New

**LibreTranslate API Integration!**
- Translate any blog post to 8+ languages
- Green "🌍 Translate" button on each post
- Dropdown menu with language options
- FREE API - no signup needed!

---

## 🚀 Setup

### Step 1: Build with New Dependency
```bash
mvn clean install
mvn javafx:run
```

This will download the JSON library needed for the API.

---

## 🌍 How to Use Translation

### 1. On Any Blog Post Card:
- Look for the green **"🌍 Translate"** button
- Click it to see language options

### 2. Choose a Language:
- French (Français)
- Spanish (Español)
- Arabic (العربية)
- German (Deutsch)
- Italian (Italiano)
- Portuguese (Português)
- Chinese (中文)
- Japanese (日本語)

### 3. Watch It Translate!
- Title and content translate instantly
- Post stays translated until page refresh
- Success message appears when done

---

## 🎨 Features

✅ **Green themed button** - Matches AgriCloud design
✅ **Dropdown menu** - Easy language selection
✅ **Background translation** - Doesn't freeze UI
✅ **Success notifications** - Know when translation completes
✅ **FREE API** - No limits, no API key needed

---

## 📸 How It Looks

Each post card now has:
```
[Post Image]
───────────────────────────
Author Name
Posted 2 hours ago

Post Title Here

Post content preview text goes here...
───────────────────────────
💬 5 comments    🌍 Translate ▼    💬 View Comments
```

Click "🌍 Translate" → Choose language → Post translates!

---

## 🔧 Technical Details

**API Used:** LibreTranslate (https://libretranslate.com)
- Free and open source
- No API key required
- Supports 20+ languages
- REST API with JSON responses

**How It Works:**
1. User clicks translate button
2. Sends POST request to LibreTranslate API
3. Gets translated text back as JSON
4. Updates post title and content on screen

---

## ⚠️ Notes

- Translation happens in real-time (may take 1-2 seconds)
- Original text is NOT saved - just displayed translated
- Refresh page to see original language again
- Works with all posts, including those with images

---

## 💡 Troubleshooting

**"Translation Failed" message?**
- Check internet connection
- API might be temporarily down (rare)
- Try again in a few seconds

**Nothing happens when clicking?**
- Check console for errors
- Make sure you ran `mvn clean install`
- Verify JSON library was downloaded

---

Enjoy your multilingual blog! 🌿🌍
