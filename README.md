# Guavision: Guava Disease Detection App 🌿🔍

![Screenshot_20250717_074715](https://github.com/user-attachments/assets/5581f90b-9317-4871-8fda-82e7fcd22e5d)


## 📱 Application Overview
Guavision is an Android application that detects and classifies diseases in guava leaves using TensorFlow Lite machine learning models. The app provides instant diagnosis and treatment recommendations to help farmers maintain healthy guava plants.

## ✨ Key Features
- 🖼️ Image-based disease detection (Anthracnose, Rust, Healthy)
- 📊 Confidence percentage for predictions
- ℹ️ Disease information and treatment suggestions
- 📍 Location-based disease tracking
- 📂 Prediction history log
- 🌙 Dark/Light theme support

## 🧠 Machine Learning Model
- **Model Architecture:** Optimized EfficientNetB0
- **Input Size:** 224x224 pixels
- **Quantization:** INT8 (Model size: ~4.2MB)
- **Accuracy:** 92.4% on test dataset
- **Classes:** 
  - Guava Rust
  - Guava Anthracnose 
  - Healthy Leaf

## 🛠 Technical Specifications
- **Minimum Android Version:** 8.0 (API 26)
- **Built With:**
  - Kotlin
  - TensorFlow Lite 2.12
  - CameraX API
  - Jetpack Compose
  - Room Database
  - Google Maps SDK

## 📥 Installation
1. Download the latest APK from [Releases](https://github.com/ryJQ/Guavision_Apps/releases)
2. Enable "Install Unknown Sources" in Android settings
3. Install the APK file

Alternatively, build from source:
```bash
git clone https://github.com/ryJQ/Guavision_Apps.git
open in Android Studio
