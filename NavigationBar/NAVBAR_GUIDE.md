# NavigationBar Module

A modern, customizable navigation bar component with built-in blur effects for Android, inspired by iOS design patterns.

## Features

- ✨ **Modern iOS-style design** with blur effects
- 🎨 **Highly customizable** colors, animations, and styling
- 🏷️ **Badge support** with customizable appearance
- 🎯 **Multiple animation curves** (overshoot, bounce, elastic, etc.)
- 📱 **Hardware-accelerated blur** using RenderEffect (Android 12+)
- 🔢 **Up to 5 tabs** with icons and labels
- 🎛️ **XML and programmatic** configuration
- ♿ **Enable/disable tabs** individually
- 🏷️ **Tag support** for custom data association

## Requirements

- **Minimum SDK**: 26 (Android 8.0)
- **Blur Effects**: Android 12+ (SDK 31)

## Installation

### Step 1: Add Module Dependency

Add the NavigationBar module to your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":NavigationBar"))
}
```

### Step 2: Ensure NavigationBar Module is Included

In your `settings.gradle.kts`:

```kotlin
include(":NavigationBar")
```

## Usage

You can use NavigationBar in two ways:

### Option 1: Programmatic (Code-Only)

Complete setup without XML - ideal for dynamic UIs or when building layouts programmatically:

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    // Create root layout
    FrameLayout rootLayout = new FrameLayout(this);
    rootLayout.setBackgroundColor(Color.parseColor("#121212"));
    
    // Create NavigationBar programmatically
    NavigationBar navigationBar = new NavigationBar(this);
    
    // Configure and add tabs
    navigationBar
        .addTab(R.drawable.outline_home_24, "Home")
        .addTab(R.drawable.outline_search_24, "Search")
        .addTab(R.drawable.outline_history_24, "Library")
        .addTab(R.drawable.outline_settings_24, "Settings")
        .setActiveTab(0);
    
    // Set tab selection listener
    navigationBar.setOnTabSelectedListener((position, tab) -> {
        Toast.makeText(this, "Selected: " + tab.label, Toast.LENGTH_SHORT).show();
        // Handle navigation here
    });
    
    // Position at bottom center with margins
    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.WRAP_CONTENT,
        FrameLayout.LayoutParams.WRAP_CONTENT
    );
    params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
    params.setMargins(dp(16), 0, dp(16), dp(16));
    
    // Add to layout
    rootLayout.addView(navigationBar, params);
    setContentView(rootLayout);
}

private int dp(float value) {
    return (int) (value * getResources().getDisplayMetrics().density);
}
```

**Advantages of Programmatic Approach:**
- Full control over initialization timing
- Easy to create dynamic tabs based on runtime conditions
- No need for XML resources
- Simpler for simple use cases

### Option 2: XML Layout

Define NavigationBar in your layout XML file (`res/layout/activity_main.xml`):

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#121212">
    
    <!-- Your content here -->
    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:paddingBottom="100dp"
        android:clipToPadding="false">
        <!-- Content -->
    </ScrollView>
    
    <!-- NavigationBar with full XML configuration -->
    <xyz.code.navigationbar.NavigationBar
        android:id="@+id/navigationBar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom|center_horizontal"
        android:layout_marginBottom="16dp"
        android:layout_marginStart="16dp"
        android:layout_marginEnd="16dp"
        app:navBarBackgroundColor="#50000000"
        app:activeBackgroundColor="#40FFFFFF"
        app:inactiveIconColor="#FFFFFFFF"
        app:activeIconColor="#FFFFFFFF"
        app:cornerRadius="100dp"
        app:blurEnabled="true"
        app:animationCurve="overshoot"
        app:animationDuration="400"
        app:menu="@menu/bottom_navigation" />
        
</FrameLayout>
```

Create a menu resource (`res/menu/bottom_navigation.xml`):

```xml
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android">
    <item
        android:id="@+id/nav_home"
        android:icon="@drawable/outline_home_24"
        android:title="Home" />
    <item
        android:id="@+id/nav_search"
        android:icon="@drawable/outline_search_24"
        android:title="Search" />
    <item
        android:id="@+id/nav_library"
        android:icon="@drawable/outline_history_24"
        android:title="Library" />
    <item
        android:id="@+id/nav_settings"
        android:icon="@drawable/outline_settings_24"
        android:title="Settings" />
</menu>
```

Then in your Activity:

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    
    // Find NavigationBar by ID
    NavigationBar navigationBar = findViewById(R.id.navigationBar);
    
    // Set tab selection listener
    navigationBar.setOnTabSelectedListener((position, tab) -> {
        Toast.makeText(this, "Selected: " + tab.label, Toast.LENGTH_SHORT).show();
        // Handle navigation here
    });
    
    // Optionally modify programmatically
    navigationBar.setBadge(2, 5); // Add badge to Library tab
}
```

**Advantages of XML Approach:**
- Declarative and easier to visualize in layout editor
- Separation of UI structure from logic
- Easy to adjust styling and positioning
- Better for design-time preview
- Can inflate tabs from menu resources

### Option 3: Hybrid (XML + Programmatic)

You can also use XML for basic setup and add tabs programmatically:

**Layout XML (`res/layout/activity_main.xml`):**

```xml
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <!-- NavigationBar without menu - tabs added in code -->
    <xyz.code.navigationbar.NavigationBar
        android:id="@+id/navigationBar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom|center_horizontal"
        android:layout_marginBottom="16dp"
        app:blurEnabled="true"
        app:animationCurve="elastic" />
        
</FrameLayout>
```

**Activity Code:**

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    
    NavigationBar navigationBar = findViewById(R.id.navigationBar);
    
    // Add tabs programmatically
    navigationBar
        .addTab(R.drawable.outline_home_24, "Home")
        .addTab(R.drawable.outline_search_24, "Search")
        .addTab(R.drawable.outline_settings_24, "Settings")
        .setOnTabSelectedListener((position, tab) -> {
            // Handle selection
        });
}
```

## Customization

### Colors

```java
navigationBar
    .setNavBarBackgroundColor(0x50000000)      // Semi-transparent black
    .setActiveBackgroundColor(0x40FFFFFF)       // White with transparency
    .setInactiveIconColor(0xFFFFFFFF)          // White
    .setActiveIconColor(0xFFFFFFFF);           // White
```

### Animation

```java
navigationBar
    .setAnimationCurve(NavigationBar.AnimationCurve.OVERSHOOT)
    .setAnimationDuration(400);
```

Available animation curves:
- `LINEAR` - No easing
- `ACCELERATE` - Starts slow, speeds up
- `DECELERATE` - Starts fast, slows down
- `ACCELERATE_DECELERATE` - Smooth in and out
- `OVERSHOOT` - Overshoots target slightly (default)
- `ANTICIPATE` - Pulls back before moving
- `ANTICIPATE_OVERSHOOT` - Combines anticipate and overshoot
- `BOUNCE` - Bounces at the end
- `ELASTIC` - Stronger overshoot effect

### Badges

```java
// Set badge count
navigationBar.setBadge(0, 5);  // 5 notifications on first tab

// Clear badge
navigationBar.clearBadge(0);

// Get badge count
int count = navigationBar.getBadge(0);
```

### Tab Management

```java
// Add tabs
navigationBar.addTab(R.drawable.icon, "Label");

// Insert tab at position
NavigationBar.NavigationTab tab = new NavigationBar.NavigationTab(R.drawable.icon, "New Tab");
navigationBar.insertTab(2, tab);

// Remove tab
navigationBar.removeTab(2);

// Clear all tabs
navigationBar.clearTabs();

// Get tab count
int count = navigationBar.getTabCount();

// Enable/disable tab
navigationBar.setTabEnabled(1, false);
boolean enabled = navigationBar.isTabEnabled(1);
```

### Active Tab

```java
// Set active tab
navigationBar.setActiveTab(2);

// Get active tab index
int activeIndex = navigationBar.getActiveTabIndex();

// Get active tab object
NavigationBar.NavigationTab activeTab = navigationBar.getActiveTab();
```

### Tags

Attach custom data to tabs:

```java
// Set tag
navigationBar.setTabTag(0, "home_fragment");

// Get tag
String tag = (String) navigationBar.getTabTag(0);

// Find tab by tag
int position = navigationBar.findTabByTag("home_fragment");
```

### Blur Control

```java
// Enable/disable blur
navigationBar.setBlurEnabled(true);
boolean isBlurEnabled = navigationBar.isBlurEnabled();
```

**Note**: Blur effects require Android 12+ (API 31). On older devices, a fallback semi-transparent background is used.

## XML Attributes

| Attribute | Type | Description | Default |
|-----------|------|-------------|---------|
| `navBarBackgroundColor` | color | Background color of navbar | `#50000000` |
| `activeBackgroundColor` | color | Background color of active tab | `#40FFFFFF` |
| `inactiveIconColor` | color | Color of inactive tab icons | `#FFFFFFFF` |
| `activeIconColor` | color | Color of active tab icon | `#FFFFFFFF` |
| `cornerRadius` | dimension | Corner radius for pill shape | `100dp` |
| `badgeBackgroundColor` | color | Badge background color | `#FFFF0000` |
| `badgeTextColor` | color | Badge text color | `#FFFFFFFF` |
| `blurEnabled` | boolean | Enable blur effect | `true` |
| `animationCurve` | enum | Animation easing curve | `overshoot` |
| `animationDuration` | integer | Animation duration (ms) | `400` |
| `menu` | reference | Menu resource to inflate | - |

## Advanced Features

### Custom Tab Objects

```java
NavigationBar.NavigationTab tab = new NavigationBar.NavigationTab(
    R.drawable.icon,
    "Custom Tab"
);
tab.badgeCount = 3;
tab.enabled = true;
tab.tag = "custom_data";

navigationBar.addTab(tab);
```

### Programmatic Tab Animation

Trigger a scale animation on a specific tab:

```java
navigationBar.animateTab(0); // Animate first tab
```

### Update Tab View

After modifying tab properties directly:

```java
NavigationBar.NavigationTab tab = navigationBar.getTab(0);
tab.badgeCount = 10;
// Tab view updates automatically when using setBadge()
navigationBar.setBadge(0, 10);
```

## Examples

### Example 1: Simple 3-Tab Navigation

```java
NavigationBar navigationBar = new NavigationBar(this);
navigationBar
    .addTab(R.drawable.outline_home_24, "Home")
    .addTab(R.drawable.outline_search_24, "Search")
    .addTab(R.drawable.outline_settings_24, "Settings")
    .setOnTabSelectedListener((position, tab) -> {
        switch (position) {
            case 0: showHomeFragment(); break;
            case 1: showSearchFragment(); break;
            case 2: showSettingsFragment(); break;
        }
    });
```

### Example 2: Music App Navigation

```java
NavigationBar navigationBar = new NavigationBar(this);
navigationBar
    .addTab(R.drawable.outline_home_24, "Home")
    .addTab(R.drawable.outline_search_24, "Search")
    .addTab(R.drawable.outline_history_24, "Library")
    .addTab(R.drawable.outline_schedule_24, "Playlists")
    .addTab(R.drawable.outline_settings_24, "Settings")
    .setAnimationCurve(NavigationBar.AnimationCurve.ELASTIC)
    .setAnimationDuration(500)
    .setActiveTab(0);

// Add notification badge to library
navigationBar.setBadge(2, 3);
```

### Example 3: Dark Theme Navigation

```java
NavigationBar navigationBar = new NavigationBar(this);
navigationBar
    .setNavBarBackgroundColor(0x80000000)      // Darker background
    .setActiveBackgroundColor(0x50FFFFFF)       // Brighter active state
    .setInactiveIconColor(0xAAFFFFFF)          // Slightly dimmed
    .setActiveIconColor(0xFFFFFFFF)            // Full white
    .addTab(R.drawable.outline_home_24, "Home")
    .addTab(R.drawable.outline_search_24, "Explore")
    .addTab(R.drawable.outline_settings_24, "Profile");
```

## Best Practices

1. **Limit Tabs**: Use 3-5 tabs for optimal UX. Maximum is 5.
2. **Clear Labels**: Keep tab labels short and descriptive (1-2 words).
3. **Consistent Icons**: Use outline-style icons for uniformity.
4. **Blur Requirement**: Check for Android 12+ before enabling blur, or handle gracefully.
5. **Bottom Placement**: Position navbar at bottom with proper margins.
6. **Padding**: Ensure content doesn't overlap with navbar (add bottom padding).

## Troubleshooting

### Blur Not Working

- Ensure device is running Android 12+ (API 31)
- Verify hardware acceleration is enabled
- Check that `blurEnabled` is set to `true`

### Tabs Not Appearing

- Verify icons exist in `res/drawable`
- Check that at least one tab is added
- Ensure NavigationBar is added to layout with proper params

### Animation Stuttering

- Reduce animation duration
- Use simpler animation curves (LINEAR, DECELERATE)
- Check for heavy operations on main thread

## Dependencies

This module depends on:
- `Blur3` module (for blur effects)
- AndroidX AppCompat
- Material Components

## License

Part of MyBlurLibrary project.
