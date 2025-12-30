# 🥗 Calorie Critters

A modern Android app for tracking calories, water intake, and discovering healthy meal ideas — powered by AI food recognition.

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)

---

## 📱 Screenshots

| Home | Scan Food | Profile | Recipe Details |
|:----:|:---------:|:-------:|:--------------:|
| Daily tracking | AI food analysis | User settings | Meal info |

---

## ✨ Features

### 🏠 **Home Dashboard**
- Welcome greeting with user's name
- Daily calorie and water intake tracking
- Circular progress indicators with animated updates
- Quick action buttons to log meals and water
- Recommended meal ideas from Spoonacular API

### 📷 **Food Scanner**
- Capture or upload food images
- AI-powered food recognition using Spoonacular's Image Analysis API
- Instant nutrition information (calories, protein, carbs, fat)
- Beautiful result cards with detailed breakdown

### 👤 **Profile**
- Clean, minimalist user profile
- Firebase authentication integration
- Dark mode toggle
- Settings and preferences
- Logout functionality

### 🍽️ **Recipe Details**
- Detailed recipe information with hero images
- Cook time, servings, and health score
- Diet tags (vegetarian, vegan, gluten-free, dairy-free)
- Full ingredient list
- Step-by-step cooking instructions

---

## 🛠️ Tech Stack

| Category | Technology |
|----------|------------|
| **Language** | Kotlin |
| **UI** | XML with Material Design 3 |
| **Architecture** | MVVM with ViewBinding |
| **Networking** | Retrofit 2 + OkHttp |
| **Image Loading** | Coil |
| **Authentication** | Firebase Auth (Email + Google Sign-In) |
| **API** | [Spoonacular Food API](https://spoonacular.com/food-api) |
| **Build System** | Gradle (Kotlin DSL) |

---

## 📋 Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 11 or higher
- Android SDK 24+ (Android 7.0)
- Spoonacular API key ([Get one here](https://spoonacular.com/food-api))
- Firebase project with Authentication enabled

---

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/gourav-gothwal/Calorie-Critters.git
cd Calorie-Critters
```

### 2. Configure Firebase

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or use existing one
3. Add an Android app with package name: `com.example.nutrisnapapp`
4. Download `google-services.json`
5. Place it in the `app/` directory

### 3. Configure Spoonacular API

1. Get your API key from [Spoonacular](https://spoonacular.com/food-api/console)
2. Open `app/src/main/res/values/strings.xml`
3. Replace the API key:

```xml
<string name="api_key">YOUR_SPOONACULAR_API_KEY</string>
```

### 4. Build and Run

```bash
# Using Gradle
./gradlew assembleDebug

# Or open in Android Studio and click Run
```

---

## 📁 Project Structure

```
app/
├── src/main/
│   ├── java/com/example/nutrisnapapp/
│   │   ├── adapters/          # RecyclerView adapters
│   │   ├── data/
│   │   │   ├── models/        # Data classes
│   │   │   └── remote/        # API services & Retrofit
│   │   ├── viewmodel/         # ViewModels
│   │   ├── HomePage.kt        # Home dashboard fragment
│   │   ├── ScanPage.kt        # Food scanner fragment
│   │   ├── ProfilePage.kt     # User profile fragment
│   │   ├── RecipeDetailFragment.kt
│   │   ├── MainActivity.kt    # Main activity with bottom nav
│   │   ├── LoginScreen.kt     # Authentication
│   │   └── ...
│   ├── res/
│   │   ├── layout/            # XML layouts
│   │   ├── drawable/          # Icons & shapes
│   │   ├── values/            # Colors, strings, themes
│   │   └── navigation/        # Nav graph
│   └── AndroidManifest.xml
├── build.gradle.kts           # App-level build config
└── google-services.json       # Firebase config (not in repo)
```

---

## 🔑 API Endpoints Used

### Spoonacular API

| Endpoint | Description |
|----------|-------------|
| `GET /recipes/random` | Fetch random meal ideas |
| `GET /recipes/{id}/information` | Get detailed recipe info |
| `POST /food/images/analyze` | AI food image analysis |

---

## 🎨 Design System

### Colors

| Color | Hex | Usage |
|-------|-----|-------|
| Primary Gradient Start | `#FF6B6B` | Accent buttons, icons |
| Primary Gradient End | `#FFA726` | Gradient effects |
| Calories | `#FF7043` | Calorie indicators |
| Water | `#42A5F5` | Water indicators |
| Text Primary | `#212121` | Main text |
| Text Secondary | `#757575` | Subtle text |
| Background | `#F8F9FA` | Screen backgrounds |

### Typography

- **Titles**: Bold, 22-26sp
- **Section Headers**: Bold, 18sp
- **Body**: Regular, 14-16sp
- **Captions**: Regular, 11-13sp

---

## 📦 Dependencies

```kotlin
// Core Android
implementation("androidx.core:core-ktx")
implementation("androidx.appcompat:appcompat")
implementation("com.google.android.material:material")

// Networking
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.retrofit2:converter-gson:2.11.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// Image Loading
implementation("io.coil-kt:coil:2.4.0")

// Firebase
implementation(platform("com.google.firebase:firebase-bom:32.8.0"))
implementation("com.google.firebase:firebase-auth-ktx")

// UI Components
implementation("com.mikhaellopez:circularprogressbar:3.1.0")
```

---

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

**Gourav Gothwal**

- GitHub: [@gourav-gothwal](https://github.com/gourav-gothwal)

---

## 🙏 Acknowledgments

- [Spoonacular](https://spoonacular.com/) for the Food API
- [Firebase](https://firebase.google.com/) for authentication
- [Material Design](https://material.io/) for design guidelines
- [Circular Progress Bar](https://github.com/lopspower/CircularProgressBar) library

---

<p align="center">
  Made with ❤️ by Gourav Gothwal
</p>
