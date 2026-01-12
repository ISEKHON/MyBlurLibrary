# Blur Effects Guide

Complete guide to creating modern blur effects using the Blur3 library, featuring real-time background blurring with frosted glass appearance.

## Overview

This guide demonstrates how to create iOS-style blur effects that:
- ✨ Blur content behind UI elements in real-time
- 🎨 Add frosted glass appearance with strokes
- 🚀 Use hardware acceleration for smooth performance
- 🔄 Update dynamically as content scrolls
- 📱 Work on Android 12+ devices

## Requirements

- **Minimum SDK**: 26 (Android 8.0)
- **Blur Support**: Android 12+ (API 31) for blur effects
- **Hardware Acceleration**: Required for blur rendering

## Basic Concepts

### DownscaleScrollableNoiseSuppressor

The core component that handles blur rendering. It:
- Records content to be blurred
- Applies downscaling for performance
- Suppresses noise artifacts
- Renders with frosted glass effect

### Glass Strokes

Two-layer stroke system for realistic glass appearance:
1. **Top Stroke**: Bright highlight (simulates light reflection)
2. **Bottom Stroke**: Subtle shadow/border

## Implementation

### Step 1: Setup Dependencies

Add Blur3 module to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":Blur3"))
}
```

### Step 2: Basic Blur Container

Create a container with blur effect:

```java
@RequiresApi(api = Build.VERSION_CODES.S)
private void applyBlurEffect(View container) {
    // Create the downscale suppressor
    final DownscaleScrollableNoiseSuppressor noiseSuppressor =
        new DownscaleScrollableNoiseSuppressor();
    
    // Create paints for glass strokes
    final android.graphics.Paint paintStrokeTop = new android.graphics.Paint(
        android.graphics.Paint.ANTI_ALIAS_FLAG);
    paintStrokeTop.setStyle(android.graphics.Paint.Style.STROKE);
    paintStrokeTop.setStrokeWidth(dpf(1));
    paintStrokeTop.setColor(0x60FFFFFF); // White highlight
    
    final android.graphics.Paint paintStrokeBottom = new android.graphics.Paint(
        android.graphics.Paint.ANTI_ALIAS_FLAG);
    paintStrokeBottom.setStyle(android.graphics.Paint.Style.STROKE);
    paintStrokeBottom.setStrokeWidth(dpf(2 / 3f));
    paintStrokeBottom.setColor(0x40FFFFFF); // Subtle border
    
    final float cornerRadius = dp(24);
    final float[] radii = new float[8];
    java.util.Arrays.fill(radii, cornerRadius);
    
    // Create blur drawable
    android.graphics.drawable.Drawable blurDrawable = createBlurDrawable(
        container, noiseSuppressor, paintStrokeTop, paintStrokeBottom, radii
    );
    
    container.setBackground(blurDrawable);
    container.setLayerType(View.LAYER_TYPE_HARDWARE, null);
}
```

### Step 3: Blur Drawable Implementation

```java
private android.graphics.drawable.Drawable createBlurDrawable(
    View container,
    DownscaleScrollableNoiseSuppressor noiseSuppressor,
    android.graphics.Paint paintStrokeTop,
    android.graphics.Paint paintStrokeBottom,
    float[] radii) {
    
    return new android.graphics.drawable.Drawable() {
        private boolean isRecording = false;
        private final android.graphics.Path clipPath = new android.graphics.Path();
        private final android.graphics.RectF clipRect = new android.graphics.RectF();
        
        @Override
        public void draw(Canvas canvas) {
            if (!canvas.isHardwareAccelerated()) {
                return;
            }
            
            if (isRecording) {
                return;
            }

            // Setup render nodes if needed
            if (noiseSuppressor.getRenderNodesCount() == 0) {
                java.util.ArrayList<android.graphics.RectF> positions = 
                    new java.util.ArrayList<>();
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
                
                // Translate to capture background behind container
                recordingCanvas.translate(-container.getLeft(), -container.getTop());
                
                // Draw parent to capture the background content
                View parentView = (View) container.getParent();
                if (parentView != null) {
                    int oldVisibility = container.getVisibility();
                    container.setVisibility(View.INVISIBLE);
                    parentView.draw(recordingCanvas);
                    container.setVisibility(oldVisibility);
                }
                
                recordingCanvas.restore();
                noiseSuppressor.endRecordingRect();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isRecording = false;
            }

            // Clip canvas to rounded rectangle shape
            android.graphics.Rect bounds = getBounds();
            clipRect.set(bounds);
            clipPath.reset();
            clipPath.addRoundRect(clipRect, radii, android.graphics.Path.Direction.CW);
            canvas.save();
            canvas.clipPath(clipPath);

            // Draw the blurred result
            noiseSuppressor.invalidateResultRenderNodes(bounds.width(), bounds.height());
            noiseSuppressor.draw(canvas, DownscaleScrollableNoiseSuppressor.DRAW_FROSTED_GLASS);

            // Draw semi-transparent overlay
            canvas.drawColor(0x4D1E1E1E); // Dark theme
            // OR for light theme:
            // canvas.drawColor(0x4DFFFFFF);
            
            // Draw glass strokes
            xyz.code.blur3.drawable.BlurredBackgroundDrawable.drawStroke(
                canvas, 0, 0, bounds.width(), bounds.height(), radii,
                dpf(1), true, paintStrokeTop
            );
            
            xyz.code.blur3.drawable.BlurredBackgroundDrawable.drawStroke(
                canvas, 0, 0, bounds.width(), bounds.height(), radii,
                dpf(2 / 3f), false, paintStrokeBottom
            );

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
}
```

### Step 4: Dynamic Updates

Update blur when content changes (e.g., scrolling):

```java
// For ListView
listView.setOnScrollListener(new android.widget.AbsListView.OnScrollListener() {
    @Override
    public void onScrollStateChanged(android.widget.AbsListView view, int scrollState) {}
    
    @Override
    public void onScroll(android.widget.AbsListView view, int firstVisibleItem, 
                        int visibleItemCount, int totalItemCount) {
        container.invalidate(); // Trigger blur redraw
    }
});

// For RecyclerView
recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        container.invalidate(); // Trigger blur redraw
    }
});

// For ScrollView
scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
    container.invalidate(); // Trigger blur redraw
});
```

## Complete Example

Here's a complete implementation from BlurDemoActivity:

```java
public class BlurDemoActivity extends Activity {
    
    private ListView listView;
    private LinearLayout messageContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.parseColor("#121212"));

        // Create scrollable content
        listView = new ListView(this);
        listView.setAdapter(new DemoAdapter());
        listView.setPadding(0, 0, 0, dp(100));
        listView.setClipToPadding(false);
        root.addView(listView);

        // Create floating container at bottom
        messageContainer = createMessageContainer();
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM
        );
        params.setMargins(dp(16), 0, dp(16), dp(16));
        root.addView(messageContainer, params);

        // Apply blur effect
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            root.post(() -> applyBlurEffect(messageContainer));
        }

        setContentView(root);
    }

    private LinearLayout createMessageContainer() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setBackgroundColor(Color.TRANSPARENT);
        container.setPadding(dp(16), dp(12), dp(16), dp(12));
        container.setGravity(Gravity.CENTER_VERTICAL);

        TextView textView = new TextView(this);
        textView.setText("Floating blurred container");
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(16);
        container.addView(textView);

        return container;
    }
    
    // ... applyBlurEffect implementation from Step 2-3 ...
    
    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private float dpf(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
```

## Customization

### Theme-Aware Blur

Adapt blur appearance to light/dark themes:

```java
// Detect theme
boolean isDarkMode = (getResources().getConfiguration().uiMode & 
    android.content.res.Configuration.UI_MODE_NIGHT_MASK) == 
    android.content.res.Configuration.UI_MODE_NIGHT_YES;

// Adjust stroke colors
paintStrokeTop.setColor(isDarkMode ? 0x60FFFFFF : 0x60FFFFFF);
paintStrokeBottom.setColor(isDarkMode ? 0x40FFFFFF : 0x40000000);

// Adjust overlay color
canvas.drawColor(isDarkMode ? 0x4D1E1E1E : 0x4DFFFFFF);
```

### Corner Radius Variations

```java
// Pill shape (full rounded)
float cornerRadius = dp(100);

// Moderate rounding
float cornerRadius = dp(24);

// Slight rounding
float cornerRadius = dp(12);

// No rounding
float cornerRadius = 0;
```

### Blur Intensity

Control blur intensity through overlay transparency:

```java
// Lighter blur (more transparent)
canvas.drawColor(0x30FFFFFF); // 19% opacity

// Medium blur
canvas.drawColor(0x4DFFFFFF); // 30% opacity

// Heavier blur (less transparent)
canvas.drawColor(0x80FFFFFF); // 50% opacity
```

### Stroke Customization

```java
// Thicker strokes for prominence
paintStrokeTop.setStrokeWidth(dpf(2));
paintStrokeBottom.setStrokeWidth(dpf(1.5f));

// Different colors
paintStrokeTop.setColor(0x80FFFFFF);  // Brighter highlight
paintStrokeBottom.setColor(0x20000000); // Darker shadow

// No bottom stroke
xyz.code.blur3.drawable.BlurredBackgroundDrawable.drawStroke(
    canvas, 0, 0, bounds.width(), bounds.height(), radii,
    dpf(1), true, paintStrokeTop
);
// Don't draw bottom stroke
```

## Performance Optimization

### 1. Limit Blur Updates

```java
// Throttle updates
private long lastUpdateTime = 0;
private static final long UPDATE_INTERVAL = 16; // ~60fps

listView.setOnScrollListener(new OnScrollListener() {
    @Override
    public void onScroll(...) {
        long now = System.currentTimeMillis();
        if (now - lastUpdateTime > UPDATE_INTERVAL) {
            container.invalidate();
            lastUpdateTime = now;
        }
    }
});
```

### 2. Conditional Blur

Only enable blur on capable devices:

```java
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && 
    hasGoodPerformance()) {
    applyBlurEffect(container);
} else {
    // Fallback to simple semi-transparent background
    container.setBackgroundColor(0x80000000);
}
```

### 3. Optimize Render Nodes

```java
// Setup render nodes once
if (noiseSuppressor.getRenderNodesCount() == 0) {
    java.util.ArrayList<android.graphics.RectF> positions = new java.util.ArrayList<>();
    positions.add(new android.graphics.RectF(0, 0, getWidth(), getHeight()));
    noiseSuppressor.setupRenderNodes(positions, 1);
}
```

## Common Use Cases

### 1. Bottom Sheet with Blur

```java
// Create bottom sheet with blur
BottomSheetDialog dialog = new BottomSheetDialog(this);
View contentView = createContentView();
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    applyBlurEffect(contentView);
}
dialog.setContentView(contentView);
dialog.show();
```

### 2. Floating Action Bar

```java
// Create floating toolbar with blur
LinearLayout toolbar = new LinearLayout(this);
toolbar.setOrientation(LinearLayout.HORIZONTAL);
// ... add buttons ...

if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    applyBlurEffect(toolbar);
}

FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
    FrameLayout.LayoutParams.MATCH_PARENT,
    FrameLayout.LayoutParams.WRAP_CONTENT,
    Gravity.TOP
);
rootLayout.addView(toolbar, params);
```

### 3. Card with Blur Background

```java
// Create card that blurs content behind it
FrameLayout card = new FrameLayout(this);
card.setPadding(dp(16), dp(16), dp(16), dp(16));
// ... add content to card ...

if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    applyBlurEffect(card);
}
```

## Troubleshooting

### Blur Not Appearing

**Problem**: Container shows solid color instead of blur.

**Solutions**:
- Verify Android 12+ (API 31)
- Check hardware acceleration: `canvas.isHardwareAccelerated()`
- Ensure `setLayerType(LAYER_TYPE_HARDWARE, null)` is called
- Verify parent view has drawable content

### Performance Issues

**Problem**: Lag or stuttering when scrolling.

**Solutions**:
- Reduce update frequency (throttle invalidations)
- Use smaller blur radius
- Limit number of blurred elements on screen
- Profile with Android Studio

### Clipping Issues

**Problem**: Blur extends beyond rounded corners.

**Solutions**:
- Ensure clip path is properly set
- Verify radii array is correct
- Call `canvas.clipPath()` before drawing blur

### Content Not Captured

**Problem**: Blur shows empty or wrong content.

**Solutions**:
- Check translation values in `recordingCanvas.translate()`
- Ensure parent view is valid
- Verify visibility toggle (INVISIBLE → VISIBLE)

## Best Practices

1. **Check API Level**: Always verify Android 12+ before enabling blur
2. **Hardware Acceleration**: Required for blur to work
3. **Update Sparingly**: Only invalidate when content changes
4. **Theme Awareness**: Adapt colors to light/dark themes
5. **Fallback UI**: Provide non-blur alternative for older devices
6. **Layer Type**: Set to HARDWARE for best performance
7. **Corner Radius**: Match container's actual shape
8. **Testing**: Test on real devices, emulators may not support blur

## Related Components

- **NavigationBar**: Uses same blur technique for tab bar
- **NavBarContainer**: Container for navigation with unified blur
- **MiniPlayer**: Music player with blur background

## Additional Resources

- [Android RenderEffect Documentation](https://developer.android.com/reference/android/graphics/RenderEffect)
- [Blur3 Library](../Blur3/README.md)
- [NavigationBar Guide](NavigationBar/README.md)

## Credits

Blur implementation based on Telegram's blur3 technology, optimized for Android modern UI patterns.
