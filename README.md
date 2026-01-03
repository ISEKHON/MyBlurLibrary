# MyBlurLibrary - Android Blur Effects Library

A complete Android library demonstrating advanced blur effects based on Telegram's blur3 system.

## Project Structure

```
MyBlurLibrary/
├── Blur3/                    # The reusable blur library module
│   ├── src/main/java/xyz/code/blur3/
│   │   ├── DownscaleScrollableNoiseSuppressor.java
│   │   ├── LiquidGlassEffect.java
│   │   ├── drawable/         # Blur drawables
│   │   ├── source/           # Blur sources
│   │   └── util/             # Utility classes
│   └── README.md
│
└── app/                      # Demo application
    └── src/main/java/xyz/demo/myblurlib/
        ├── MainActivity.java
        └── BlurDemoActivity.java
```

## Features

✨ **Frosted Glass Effect** - iOS-style blur with configurable opacity  
🌊 **Liquid Glass Effect** - Advanced shader-based rendering (Android 13+)  
⚡ **High Performance** - Hardware-accelerated with RenderNode caching  
🎯 **Real-time Updates** - Dynamic blur that adapts to scrolling content  
📱 **Standalone Module** - Easy to integrate into any Android project  

## Quick Start

### 1. Build the Project

```bash
cd MyBlurLibrary
.\gradlew assembleDebug
```

### 2. Run the Demo

Install and run the demo app to see blur effects in action:
- Scrollable list with live blur updates
- Floating message container with frosted glass
- Real-time blur rendering

### 3. Use in Your Project

Copy the `Blur3` module to your project and add to `settings.gradle.kts`:

```kotlin
include(":Blur3")
```

Add dependency in your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":Blur3"))
}
```

## Basic Usage

```java
import xyz.code.blur3.DownscaleScrollableNoiseSuppressor;

// Requires Android 12+ (API 31)
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    final DownscaleScrollableNoiseSuppressor noiseSuppressor = 
        new DownscaleScrollableNoiseSuppressor();
    
    // Apply as background drawable
    view.setBackground(createBlurDrawable(noiseSuppressor));
    view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
}
```

See `BlurDemoActivity.java` for a complete implementation example.

## Module Details

### Blur3 Library

The `Blur3` module is a self-contained library with:
- **Core Classes**: `DownscaleScrollableNoiseSuppressor`, `LiquidGlassEffect`
- **Utilities**: `AndroidUtilities`, `Theme`, `ReferenceList`, `ViewPositionWatcher`
- **No External Dependencies**: All Telegram-specific code replaced with standalone implementations

### Demo App

The demo app (`app` module) showcases:
- Real-world blur effect implementation
- Scrollable content with dynamic blur updates
- Clean, documented code for reference

## Requirements

- **Minimum SDK**: 26 (Android 8.0)
- **Blur Effects**: 31+ (Android 12) - For RenderEffect
- **Liquid Glass**: 33+ (Android 13) - For RuntimeShader
- **Hardware Acceleration**: Required for all blur effects

## Build System

- **Gradle**: 8.13
- **AGP**: Latest
- **Java**: 11
- **Compile SDK**: 36

## Credits

Based on Telegram's sophisticated blur3 system, adapted as a standalone library for general Android development.

## License

This is a demonstration library. Review Telegram's licensing terms if using in production applications.

## Support

For issues or questions, refer to:
- `Blur3/README.md` - Detailed API documentation
- `BlurDemoActivity.java` - Implementation examples
- Build output - Error logs and compilation info
