# Tasotool

Tasotool is a lightweight, native Android sum ledger app designed for rapid numerical entry and real-time calculations. It's a port of the original `Tasotool.html` sum ledger, optimized for mobile efficiency.

## Features
- **Fast Entry:** Custom virtual keyboard designed for numerical data entry without obscuring the list.
- **Real-time Sums:** Automatically calculates totals as you type.
- **Persistent Storage:** Uses Room Database to keep your records safe across sessions.
- **Material3 UI:** Clean, modern interface following the latest Android design standards.
- **Keyboard Shortcuts:** Built-in "Next" shortcut (comma key) for rapid-fire entry.

## Installation
Currently, you can build the APK from source. 

### Prerequisites
- Android SDK (or Termux with `android-sdk` and `gradle`)
- JDK 17

### Building in Termux
1. Ensure `aapt2` is installed: `pkg install aapt2`
2. Run the build command:
   ```bash
   ./gradlew assembleDebug
   ```
3. Find your APK at `app/build/outputs/apk/debug/app-debug.apk`.

### Building in Android Studio
1. Clone the repository.
2. Open the project in Android Studio (Iguana or newer recommended).
3. Note: You may need to comment out the `android.aapt2FromMavenOverride` line in `gradle.properties` if you are not building on Termux.

## Contributing
Contributions are welcome! Please feel free to submit a Pull Request.
1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License
Distributed under the MIT License. See `LICENSE` for more information.

## Project Structure
- **UI Layouts:** `app/src/main/res/layout/`
- **Logic:** `app/src/main/java/com/taso/tasotool/`
- **Database:** `Data.kt` using Room.

## Maintenance Tips
- **Material3 Attributes:** Use app-namespace attributes for Material components.
- **Database Migrations:** Increment the version in `Data.kt` if schemas change.