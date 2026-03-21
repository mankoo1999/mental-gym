# Mental Gym - Cognitive Fitness Platform for Android

![Mental Gym Banner](https://img.shields.io/badge/Platform-Android-green)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.20-purple)
![Min SDK](https://img.shields.io/badge/Min%20SDK-26-blue)
![License](https://img.shields.io/badge/License-MIT-orange)

**Train your brain like you train your body. Maintain cognitive fitness in the AI era.**

Mental Gym is a structured cognitive training platform that helps individuals maintain and improve their cognitive abilities through short daily training sessions, just as physical gyms help people maintain physical health.

## 📱 Screenshots

*[Add screenshots here]*

## ✨ Features

### Core Functionality
- **🧠 7 Cognitive Systems Training**
  - Attention & Focus
  - Working Memory
  - Reasoning & Logic
  - Cognitive Flexibility
  - Processing Speed
  - Memory Systems
  - Creative Thinking

- **💪 3 Training Programs**
  - **Essential** (3 days/week) - 15-20 min sessions
  - **Standard** (5 days/week) - 20-25 min sessions
  - **Elite** (7 days/week) - 20-30 min sessions

- **📊 Progress Tracking**
  - Daily streak tracking
  - Performance metrics
  - System-specific scores
  - Weekly activity charts
  - Historical workout data

- **🎯 Structured Workouts**
  - Daily cognitive exercises
  - Guided instructions
  - Built-in timers
  - Progressive difficulty
  - Performance scoring

### Technical Features
- Modern Material Design 3 UI
- Dark/Light theme support
- Offline-first architecture
- Local database persistence
- Smooth animations and transitions
- Clean architecture pattern

## 🏗️ Architecture

The app follows **Clean Architecture** principles with clear separation of concerns:

```
app/
├── data/               # Data layer
│   ├── local/         # Room database, DAOs, entities
│   └── repository/    # Repository implementations
├── domain/            # Business logic layer
│   ├── model/        # Domain models
│   └── content/      # Workout content provider
├── di/               # Dependency injection (Hilt)
└── ui/               # Presentation layer
    ├── home/         # Home screen & onboarding
    ├── workout/      # Workout execution
    ├── progress/     # Progress tracking
    ├── navigation/   # Navigation setup
    └── theme/        # UI theming
```

### Tech Stack

**Core**
- Kotlin 1.9.20
- Android SDK 26+ (Android 8.0 Oreo)
- Jetpack Compose (Modern UI toolkit)

**Architecture Components**
- MVVM Pattern
- StateFlow for reactive state management
- Kotlin Coroutines for async operations
- Hilt for dependency injection

**Database**
- Room for local persistence
- DataStore for preferences

**UI/UX**
- Material Design 3
- Compose Navigation
- Custom animations and transitions

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34
- Gradle 8.2+

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/mental-gym-android.git
   cd mental-gym-android
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory
   - Click "OK"

3. **Sync Gradle**
   - Android Studio will automatically prompt to sync
   - Click "Sync Now"
   - Wait for dependencies to download

4. **Run the app**
   - Connect an Android device or start an emulator
   - Click the "Run" button (▶️) or press `Shift + F10`

### Build from Command Line

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug
```

## 📖 Usage

### First Launch
1. Open the app
2. Choose your training program (Essential/Standard/Elite)
3. Start your first workout

### Daily Workflow
1. Open the app to see today's workout
2. Tap "Start Workout"
3. Follow the guided exercises
4. Complete each exercise within the time limit
5. Track your progress over time

### Viewing Progress
- Tap the bar chart icon in the home screen
- View your overall stats, weekly activity, and system-specific scores
- Review recent workout history

## 🎨 Design Philosophy

Mental Gym uses a **neuromorphic design aesthetic** inspired by brain neural networks:

- **Color Palette**: Vibrant gradients (Neural Purple, Cognitive Teal, Energy Orange)
- **Typography**: Bold, confident, and clear
- **Animations**: Smooth, purposeful transitions
- **Layout**: Clean, spacious, and focused

The design avoids generic "brain training app" aesthetics in favor of a premium, fitness-inspired look that positions cognitive training as a serious discipline.

## 📂 Project Structure

```
mental-gym-android/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/mentalgym/app/
│   │   │   │   ├── MentalGymApplication.kt
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── data/
│   │   │   │   ├── domain/
│   │   │   │   ├── di/
│   │   │   │   └── ui/
│   │   │   ├── res/
│   │   │   │   ├── values/
│   │   │   │   └── xml/
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

## 🧪 Testing

```bash
# Run unit tests
./gradlew test

# Run instrumentation tests
./gradlew connectedAndroidTest
```

## 🔧 Configuration

### Changing Workout Content
Edit `WorkoutContentProvider.kt` to modify exercises, durations, or add new cognitive systems.

### Adjusting Program Levels
Modify `TrainingProgram` enum in `Models.kt` to change session counts or add new programs.

### Customizing Theme
Edit files in `ui/theme/` directory:
- `Color.kt` - Color palette
- `Type.kt` - Typography
- `Theme.kt` - Overall theme configuration

## 📊 Database Schema

### Tables
- **workout_completions** - Stores completed workout records
- **user_preferences** - User settings and program selection
- **exercise_progress** - Individual exercise progression tracking

## 🔐 Privacy & Data

- All data is stored **locally on device**
- No user accounts or cloud sync
- No analytics or tracking
- Optional backup via Android's built-in backup system

## 🛣️ Roadmap

### Phase 1 - MVP ✅
- [x] Core workout system
- [x] 3 training programs
- [x] 7 cognitive systems
- [x] Progress tracking
- [x] Streak system

### Phase 2 - Personalization
- [ ] AI-driven exercise recommendations
- [ ] Adaptive difficulty
- [ ] Custom workout plans
- [ ] Performance insights

### Phase 3 - Social & Gamification
- [ ] Leaderboards
- [ ] Achievements system
- [ ] Share progress
- [ ] Challenges

### Phase 4 - Advanced Features
- [ ] Wear OS integration
- [ ] Widget support
- [ ] Cloud sync (optional)
- [ ] Advanced analytics

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👥 Authors

- Your Name - Initial work

## 🙏 Acknowledgments

- Inspired by the concept paper "Mental Gym: Cognitive Fitness Platform"
- Built with modern Android development best practices
- Material Design 3 guidelines

## 📞 Support

For support, email support@mentalgym.app or open an issue in this repository.

## 🔗 Links

- [Documentation](https://docs.mentalgym.app)
- [Website](https://mentalgym.app)
- [Blog](https://blog.mentalgym.app)

---

**Built with ❤️ and 🧠 for cognitive fitness enthusiasts**
