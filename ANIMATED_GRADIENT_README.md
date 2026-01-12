# Animated Gradient Background Implementation

This implementation brings Telegram's animated gradient background system to your MyBlurLibrary app. It allows users to create beautiful, smoothly animated multi-color backgrounds.

## 🎨 Features

- **Multi-Color Gradients**: Support for 2-4 colors
- **Smooth Animation**: 8 animation phases with cubic Bezier interpolation
- **Color Picker**: Easy-to-use interface for selecting colors
- **Preset Gradients**: 5 pre-configured beautiful gradients
- **Adjustable Speed**: Control animation speed from 0.1x to 3.0x
- **Efficient Rendering**: Uses 60x80 bitmaps for performance
- **Dynamic Colors**: Add/remove colors on the fly

## 📁 Files Added

### Core Components

1. **MotionBackgroundDrawable.java**
   - Main drawable class that handles animated gradients
   - Manages animation loop and phase transitions
   - Supports 1-4 colors with smooth blending
   - Uses cubic Bezier interpolation for natural motion

2. **GradientUtils.java**
   - Gradient generation algorithms
   - 8-phase positioning system
   - Distance-based color blending
   - Complementary color generation

3. **GradientColorPicker.java**
   - UI component for color selection
   - Support for 2-4 color gradients
   - Hex color input fields
   - Add/remove color buttons
   - Real-time color preview

4. **AnimatedGradientActivity.java**
   - Demo activity showcasing all features
   - Playback controls (play/pause, next phase)
   - Speed adjustment slider
   - Preset gradient buttons
   - Information panel

## 🚀 How to Use

### In Your Activity

```java
// Create the animated background
MotionBackgroundDrawable motionBackground = new MotionBackgroundDrawable();

// Set colors (supports 1-4 colors)
motionBackground.setColors(
    0xFF6B73FF,  // Color 1
    0xFF000000,  // Color 2
    0xFF10D0F6,  // Color 3
    0xFFF3558E   // Color 4
);

// Apply to view
yourView.setBackground(motionBackground);

// Set parent view for invalidation
motionBackground.setParentView(yourView);

// Start animation
motionBackground.startAnimation();

// Adjust speed (1.0 is normal)
motionBackground.setIndeterminateSpeedScale(1.5f);

// Stop animation
motionBackground.stopAnimation();
```

### Using the Color Picker

```java
GradientColorPicker colorPicker = new GradientColorPicker(context);

// Set initial colors
colorPicker.setColors(color1, color2, color3, color4);

// Listen for color changes
colorPicker.setColorChangeListener(colors -> {
    // colors is an int[4] array
    motionBackground.setColors(colors[0], colors[1], colors[2], colors[3]);
});

// Add to your layout
layout.addView(colorPicker);
```

## 🎯 Key Concepts

### Animation System

The animation works through **8 distinct phases** (0-7), each with different color point positions:

1. **Phase 0**: Colors spread from corners
2. **Phase 1**: Rotating clockwise
3. **Phase 2**: Diagonal spread
4. **Phase 3**: Vertical emphasis
5. **Phase 4**: Center spread
6. **Phase 5**: Rotating counter-clockwise
7. **Phase 6**: Horizontal emphasis
8. **Phase 7**: Diagonal opposite

Each phase smoothly transitions to the next using cubic Bezier interpolation.

### Gradient Generation

Colors are positioned at specific points on the canvas, and each pixel's color is calculated using **inverse distance weighting**:

```
weight = (1 / distance) / totalDistance
finalColor = blend all colors by their weights
```

This creates smooth, natural-looking gradients that flow organically across the screen.

### Performance Optimization

- Small bitmap generation (60×80 pixels)
- Scaled to screen size
- Cached intermediate frames (3 bitmaps)
- Efficient pixel-by-pixel blending
- Reuses bitmaps to avoid allocations

## 🎨 Preset Gradients

The demo includes 5 beautiful presets:

1. **Telegram Blue**: Classic Telegram gradient
2. **Sunset**: Warm reds, oranges, and yellows
3. **Ocean Breeze**: Cool blues and greens
4. **Purple Dream**: Vibrant purples and blues
5. **Forest**: Natural greens and teals

## 🔧 Customization

### Generate Complementary Colors

```java
int baseColor = 0xFF6B73FF;
int complementary = GradientUtils.generateComplementaryColor(baseColor);
int third = GradientUtils.generateThirdColor(baseColor);
```

### Manual Phase Control

```java
// Set specific phase (0-7)
motionBackground.setPhase(3);

// Advance to next phase quickly
motionBackground.switchToNextPosition(true);
```

### Check if Gradient is Dark

```java
boolean isDark = GradientUtils.isDarkGradient(color1, color2, color3, color4);
// Use this to determine text color overlay
```

## 📱 Running the Demo

1. Build and run the app
2. Tap "Open Animated Gradient Demo" from the main menu
3. Experiment with:
   - Play/pause animation
   - Adjusting speed slider
   - Selecting different colors
   - Trying preset gradients
   - Adding/removing colors (2-4 colors)

## 🧠 Implementation Details

### CubicBezierInterpolator

Uses control points (0.33, 0.0, 0.0, 1.0) for smooth easing:
- Natural acceleration at start
- Smooth deceleration at end
- Creates organic, non-linear motion

### Bitmap Blending

Transitions between phases use alpha blending:
```java
blendedPixel = ColorUtils.blendARGB(pixel1, pixel2, progress)
```

### Update Loop

- Runs every ~16ms (60fps target)
- Calculates delta time for consistent speed
- Updates animation progress
- Regenerates gradient bitmap
- Invalidates view for redraw

## 🎓 Based on Telegram

This implementation is inspired by Telegram's motion background system:
- Similar 8-phase animation
- Cubic Bezier interpolation
- Multi-color support (up to 4 colors)
- Efficient bitmap rendering
- Smooth, continuous animation

## 💡 Tips

1. **Start with 2 colors** for simpler gradients
2. **Use complementary colors** for harmonious results
3. **Adjust speed** based on your app's mood
4. **Test on real devices** for best performance
5. **Consider battery** - pause when app is in background

## 🐛 Troubleshooting

**Animation stutters?**
- Reduce speed scale
- Check if running on main thread
- Verify hardware acceleration is enabled

**Colors look wrong?**
- Ensure colors are in ARGB format (0xAARRGGBB)
- Check alpha channel is FF for full opacity

**High memory usage?**
- Bitmap size is fixed at 60×80 (very small)
- Check for memory leaks in parent view references

## 🔮 Future Enhancements

Possible additions:
- Pattern overlay support (like Telegram)
- Rotation angle control
- Custom phase configurations
- Export gradient as image
- Swipe to change phases
- Touch-based color point control

---

**Enjoy creating beautiful animated backgrounds!** 🌈
