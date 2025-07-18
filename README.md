# Guavision Apps: Guava Disease Detection Apps 🌿🔍

![Screenshot_20250717_074715](https://github.com/user-attachments/assets/5581f90b-9317-4871-8fda-82e7fcd22e5d)


## 📱 Application Overview
Guavision is an Android application that detects and classifies diseases in guava leaves using TensorFlow Lite machine learning models. The app provides instant diagnosis and treatment recommendations to help farmers maintain healthy guava plants.

## ✨ Key Features
- 🖼️ Image-based disease detection (Anthracnose, Fruit_fly, Healthy)
- 📊 Confidence percentage for predictions
- ℹ️ Disease information and treatment suggestions
- 📂 Prediction history log

## 🧠 Machine Learning Model
- **Model Architecture:** Optimized EfficientNetB0
- **Input Size:** 224x224 pixels
- **Quantization:** INT8 (Model size: ~4.2MB)
- **Accuracy:** 97.4% on test dataset
- **Classes:** 
  - Guava Amthracnose
  - Guava Fruit Fly 
  - Healthy Guava

## 🛠 Technical Specifications
- **Minimum Android Version:** 8.0 (API 26)
```gradle
dependencies {
    implementation(libs.androidx.constraintlayout)
    implementation(libs.ucrop)
    implementation(libs.tensorflow.lite.support)
    implementation "org.tensorflow:tensorflow-lite-metadata:0.4.4"
    implementation(libs.tensorflow.lite.metadata)

    // ML Binding (Generated automatically during build)
    implementation project(":ml") 
}
```
## 📥 Installation
1. Download the latest APK from [Releases](https://github.com/ryJQ/Guavision_Apps/releases)
2. Enable "Install Unknown Sources" in Android settings
3. Install the APK file

Alternatively, build from source:
```bash
git clone https://github.com/ryJQ/Guavision_Apps.git
open in Android Studio
