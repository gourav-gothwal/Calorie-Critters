# 🍎 Calorie Critters: Your Smart Nutrition Companion

A modern, AI-powered mobile application designed to make nutrition
tracking intuitive, personalized, and effective.\
Go beyond simple calorie counting and build a healthier relationship
with food.

------------------------------------------------------------------------

## ✨ Key Features

🥗 **1. Effortless Calorie & Macro Tracking**\
Log your meals with our extensive food database or by scanning
barcodes.\
Get a detailed breakdown of your daily calories, proteins, fats, and
carbohydrates to stay on top of your goals.

💧 **2. Hydration Monitoring**\
Easily track your daily water intake with simple taps.\
Get reminders to stay hydrated throughout the day, a crucial but often
overlooked aspect of health.

📚 **3. Explore Thousands of Recipes**\
Browse our integrated database of thousands of healthy recipes.\
Filter by dietary needs (vegan, keto, gluten-free), ingredients you have
on hand, or calorie count.

👨‍⚕️ **4. Connect with Nutrition Experts**\
Interact with registered nutritionists for personalized advice and
guidance.

📸 **5. AI-Powered Food Recognition (Snap & Track)**\
Take a picture of your meal and let our AI model:
- Identify food items
- Estimate portion sizes
- Provide calorie & nutrition breakdown (fats, carbs, protein)

------------------------------------------------------------------------

## 🛠️ Technology Stack

-   **Language:** Kotlin
-   **Architecture:** MVVM (Model-View-ViewModel)
-   **UI:** Android XML with Material Design Components
-   **Asynchronous Programming:** Kotlin Coroutines & Flow
-   **Networking:** Retrofit & OkHttp
-   **Image Recognition Model:** Spoonacular API / Nutritionix API
-   **Image Handling:** ML Kit Image Labeling

------------------------------------------------------------------------

## 🚀 Getting Started

### Prerequisites

-   Android Studio (latest version recommended)
-   API Key from a food recognition service (Spoonacular or
    Nutritionix).

### Installation

1.  Clone the repository:

    ``` bash
    git clone https://github.com/gourav-gothwal/calorie-critters.git
    ```

2.  Open the project in Android Studio.

3.  Set up your API Keys:

    -   Open the `local.properties` file in your project's root
        directory (create it if it doesn't exist).

    -   Add your API key and App ID (if applicable):

        ``` properties
        NUTRITION_APP_ID="YOUR_NUTRITIONIX_APP_ID"
        NUTRITION_APP_KEY="YOUR_NUTRITIONIX_APP_KEY"
        ```

4.  Add keys to your `build.gradle.kts` file:

    ``` kotlin
    android {
        defaultConfig {
            buildConfigField("String", "NUTRITION_APP_ID", "\"YOUR_APP_ID\"")
            buildConfigField("String", "NUTRITION_APP_KEY", "\"YOUR_APP_KEY\"")
        }
    }
    ```

5.  Build and run the application on an emulator or physical device.

------------------------------------------------------------------------

## 🤝 How to Contribute

Contributions are welcome!

1.  Fork the project
2.  Create your feature branch
    (`git checkout -b feature/AmazingFeature`)
3.  Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4.  Push to the branch (`git push origin feature/AmazingFeature`)
5.  Open a Pull Request

------------------------------------------------------------------------

## 📄 License

Distributed under the MIT License.
See `LICENSE` for more information.
