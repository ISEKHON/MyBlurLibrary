# Blur3 Library

A powerful Android blur effects library based on Telegram's blur3 system, providing sophisticated frosted glass and liquid blur effects.

## Features

- 🎨 **Frosted Glass Effect**: iOS-style frosted glass blur with configurable opacity
- 🌊 **Liquid Glass Effect**: Advanced shader-based liquid glass rendering (Android 13+)
- ⚡ **High Performance**: Hardware-accelerated rendering with RenderNode caching
- 🎯 **Real-time Updates**: Dynamic blur that updates with scrolling content
- 📱 **Android 12+**: Requires API 31+ for RenderEffect blur support

## Installation

### 1. Add Blur3 Module to Your Project

Copy the `Blur3` folder to your project and add it to `settings.gradle.kts`:

```kotlin
include(":Blur3")
```

### 2. Add Dependency

In your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":Blur3"))
}
```

## Usage

### Basic Blur Effect

```java
// Requires Android 12+ (API 31)
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    applyBlurEffect(yourView);
}

@RequiresApi(api = Build.VERSION_CODES.S)
private void applyBlurEffect(View view) {
    final DownscaleScrollableNoiseSuppressor noiseSuppressor = 
        new DownscaleScrollableNoiseSuppressor();
    
    final float cornerRadius = dp(24);
    final float[] radii = new float[8];
    Arrays.fill(radii, cornerRadius);
    
    android.graphics.drawable.Drawable blurDrawable = new android.graphics.drawable.Drawable() {
        private boolean isRecording = false;
        private final android.graphics.Path clipPath = new android.graphics.Path();
        private final android.graphics.RectF clipRect = new android.graphics.RectF();
        
        @Override
        public void draw(Canvas canvas) {
            if (!canvas.isHardwareAccelerated() || isRecording) {
                return;
            }

            // Setup render nodes
            if (noiseSuppressor.getRenderNodesCount() == 0) {
                ArrayList<android.graphics.RectF> positions = new ArrayList<>();
                android.graphics.Rect bounds = getBounds();
                positions.add(new android.graphics.RectF(bounds));
                noiseSuppressor.setupRenderNodes(positions, 1);
            }

            // Record content to blur
            isRecording = true;
            try {
                android.graphics.RecordingCanvas recordingCanvas = 
                    noiseSuppressor.beginRecordingRect(0);
                recordingCanvas.save();
                recordingCanvas.translate(-view.getLeft(), -view.getTop());
                
                View parentView = (View) view.getParent();
                if (parentView != null) {
                    int oldVisibility = view.getVisibility();
                    view.setVisibility(View.INVISIBLE);
                    parentView.draw(recordingCanvas);
                    view.setVisibility(oldVisibility);
                }
                
                recordingCanvas.restore();
                noiseSuppressor.endRecordingRect();
            } finally {
                isRecording = false;
            }

            // Draw blurred result with rounded corners
            android.graphics.Rect bounds = getBounds();
            clipRect.set(bounds);
            clipPath.reset();
            clipPath.addRoundRect(clipRect, radii, android.graphics.Path.Direction.CW);
            canvas.save();
            canvas.clipPath(clipPath);

            // Apply frosted glass effect
            noiseSuppressor.invalidateResultRenderNodes(bounds.width(), bounds.height());
            noiseSuppressor.draw(canvas, 
                DownscaleScrollableNoiseSuppressor.DRAW_FROSTED_GLASS);

            // Semi-transparent background overlay
            canvas.drawColor(Color.argb((int)(255 * 0.30f), 255, 255, 255));

            canvas.restore();
        }
        
        @Override
        public void setAlpha(int alpha) {}
        
        @Override
        public void setColorFilter(android.graphics.ColorFilter colorFilter) {}
        
        @Override
        public int getOpacity() {
            return android.graphics.PixelFormat.TRANSLUCENT;
        }
    };
    
    view.setBackground(blurDrawable);
    view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
}
```

### Update Blur on Scroll

```java
listView.setOnScrollListener(new AbsListView.OnScrollListener() {
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {}
    
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, 
                        int visibleItemCount, int totalItemCount) {
        blurredView.invalidate();
    }
});
```

## Blur Modes

### 1. DRAW_FROSTED_GLASS
Telegram-style frosted glass effect with subtle noise and high-quality blur.

```java
noiseSuppressor.draw(canvas, DownscaleScrollableNoiseSuppressor.DRAW_FROSTED_GLASS);
```

### 2. DRAW_GLASS
Simpler glass effect with less noise.

```java
noiseSuppressor.draw(canvas, DownscaleScrollableNoiseSuppressor.DRAW_GLASS);
```

### 3. DRAW_LIQUID_GLASS (Android 13+)
Advanced liquid glass effect with shader-based rendering.

```java
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    noiseSuppressor.draw(canvas, DownscaleScrollableNoiseSuppressor.DRAW_LIQUID_GLASS);
}
```

## Demo App

Check out the included demo app (`BlurDemoActivity`) for a complete working example with:
- Scrollable list with blur effect
- Floating message container with frosted glass
- Real-time blur updates

## Requirements

- **Minimum SDK**: 26 (Android 8.0)
- **Blur Effects**: 31+ (Android 12)
- **Liquid Glass**: 33+ (Android 13)
- **Hardware Acceleration**: Required

## Performance Tips

1. **Reuse RenderNodes**: The library automatically caches render nodes for performance
2. **Limit Updates**: Only invalidate when content changes (e.g., on scroll)
3. **Hardware Layer**: Always use `LAYER_TYPE_HARDWARE` on blurred views
4. **Optimize Radius**: Larger corner radius requires more processing

## Credits

Based on Telegram's blur3 system, adapted for general Android use.

## License

This is a demonstration library. Please ensure compliance with Telegram's licensing if using in production.
