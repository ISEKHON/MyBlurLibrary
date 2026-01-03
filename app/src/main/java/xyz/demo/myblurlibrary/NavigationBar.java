package xyz.demo.myblurlibrary;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;

import xyz.code.blur3.DownscaleScrollableNoiseSuppressor;

/**
 * Custom NavigationBar with blur effect, inspired by modern iOS navigation
 * Supports tab management, badges, active states, and custom styling
 */
public class NavigationBar extends FrameLayout {
    
    private static final int MAX_TABS = 5;
    
    /**
     * Animation curve types for tab transitions
     */
    public enum AnimationCurve {
        LINEAR,
        ACCELERATE,
        DECELERATE,
        ACCELERATE_DECELERATE,
        OVERSHOOT,
        ANTICIPATE,
        ANTICIPATE_OVERSHOOT,
        BOUNCE,
        ELASTIC
    }
    
    private LinearLayout tabContainer;
    private View animatedIndicator; // Animated circular indicator
    private android.animation.ValueAnimator indicatorAnimator;
    private List<NavigationTab> tabs = new ArrayList<>();
    private int activeTabIndex = 0;
    private OnTabSelectedListener listener;
    private boolean blurEnabled = true;
    private AnimationCurve animationCurve = AnimationCurve.OVERSHOOT;
    private long animationDuration = 400;
    
    // Blur components
    private DownscaleScrollableNoiseSuppressor noiseSuppressor;
    private android.graphics.Paint paintStrokeTop;
    private android.graphics.Paint paintStrokeBottom;
    private float[] radii = new float[8];
    
    // Styling
    private int backgroundColor = 0x50000000; // Dark semi-transparent (50% black)
    private int activeBackgroundColor = 0x40FFFFFF; // White with 25% opacity
    private int inactiveIconColor = 0xFFFFFFFF; // White
    private int activeIconColor = 0xFFFFFFFF; // White
    private float cornerRadius = 100f; // Pill shape
    private int badgeBackgroundColor = 0xFFFF0000; // Red
    private int badgeTextColor = 0xFFFFFFFF; // White
    
    public interface OnTabSelectedListener {
        void onTabSelected(int position, NavigationTab tab);
    }
    
    public static class NavigationTab {
        public int iconRes;
        public String label;
        public int badgeCount = 0;
        public boolean enabled = true;
        public Object tag;
        
        public NavigationTab(int iconRes, String label) {
            this.iconRes = iconRes;
            this.label = label;
        }
    }
    
    public NavigationBar(Context context) {
        super(context);
        init(context);
    }
    
    public NavigationBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    
    public NavigationBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }
    
    private void init(Context context) {
        init(context, null);
    }
    
    private void init(Context context, AttributeSet attrs) {
        // Parse XML attributes if provided
        if (attrs != null) {
            android.content.res.TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NavigationBar);
            
            backgroundColor = a.getColor(R.styleable.NavigationBar_navBarBackgroundColor, backgroundColor);
            activeBackgroundColor = a.getColor(R.styleable.NavigationBar_activeBackgroundColor, activeBackgroundColor);
            inactiveIconColor = a.getColor(R.styleable.NavigationBar_inactiveIconColor, inactiveIconColor);
            activeIconColor = a.getColor(R.styleable.NavigationBar_activeIconColor, activeIconColor);
            cornerRadius = a.getDimension(R.styleable.NavigationBar_cornerRadius, cornerRadius);
            badgeBackgroundColor = a.getColor(R.styleable.NavigationBar_badgeBackgroundColor, badgeBackgroundColor);
            badgeTextColor = a.getColor(R.styleable.NavigationBar_badgeTextColor, badgeTextColor);
            blurEnabled = a.getBoolean(R.styleable.NavigationBar_blurEnabled, blurEnabled);
            animationDuration = a.getInteger(R.styleable.NavigationBar_animationDuration, (int) animationDuration);
            
            int curveIndex = a.getInt(R.styleable.NavigationBar_animationCurve, 4); // Default to OVERSHOOT
            animationCurve = AnimationCurve.values()[curveIndex];
            
            // Inflate menu if provided
            int menuRes = a.getResourceId(R.styleable.NavigationBar_menu, 0);
            if (menuRes != 0) {
                post(() -> inflateMenu(menuRes));
            }
            
            a.recycle();
        }
        // Initialize blur components
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            noiseSuppressor = new DownscaleScrollableNoiseSuppressor();
            
            paintStrokeTop = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            paintStrokeTop.setStyle(android.graphics.Paint.Style.STROKE);
            paintStrokeTop.setStrokeWidth(dpf(1));
            paintStrokeTop.setColor(0x40FFFFFF);
            
            paintStrokeBottom = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            paintStrokeBottom.setStyle(android.graphics.Paint.Style.STROKE);
            paintStrokeBottom.setStrokeWidth(dpf(0.67f));
            paintStrokeBottom.setColor(0x30FFFFFF);
            
            java.util.Arrays.fill(radii, dp(cornerRadius));
        }
        
        // Create tab container
        tabContainer = new LinearLayout(context);
        tabContainer.setOrientation(LinearLayout.HORIZONTAL);
        tabContainer.setGravity(Gravity.CENTER);
        int paddingH = context.getResources().getDimensionPixelSize(R.dimen.navbar_padding_horizontal);
        int paddingV = context.getResources().getDimensionPixelSize(R.dimen.navbar_padding_vertical);
        tabContainer.setPadding(paddingH, paddingV, paddingH, paddingV);
        
        // Add tab container to this FrameLayout
        FrameLayout.LayoutParams containerParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        );
        containerParams.gravity = Gravity.CENTER;
        addView(tabContainer, containerParams);
        
        setWillNotDraw(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurEnabled) {
            setLayerType(LAYER_TYPE_HARDWARE, null);
        }
    }
    
    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurEnabled && noiseSuppressor != null) {
            drawBlurBackground(canvas);
        } else {
            // Fallback for older versions or when blur is disabled
            android.graphics.Path path = new android.graphics.Path();
            android.graphics.RectF rect = new android.graphics.RectF(0, 0, getWidth(), getHeight());
            path.addRoundRect(rect, dp(cornerRadius), dp(cornerRadius), android.graphics.Path.Direction.CW);
            canvas.save();
            canvas.clipPath(path);
            canvas.drawColor(backgroundColor);
            canvas.restore();
        }
        
        super.dispatchDraw(canvas);
    }
    
    @RequiresApi(api = Build.VERSION_CODES.S)
    private void drawBlurBackground(Canvas canvas) {
        if (!canvas.isHardwareAccelerated()) {
            return;
        }
        
        // Setup render nodes if needed
        if (noiseSuppressor.getRenderNodesCount() == 0) {
            java.util.ArrayList<android.graphics.RectF> positions = new java.util.ArrayList<>();
            positions.add(new android.graphics.RectF(0, 0, getWidth(), getHeight()));
            noiseSuppressor.setupRenderNodes(positions, 1);
        }
        
        // Record content to blur
        try {
            android.graphics.RecordingCanvas recordingCanvas = noiseSuppressor.beginRecordingRect(0);
            recordingCanvas.save();
            // Account for translation (visual position vs layout position)
            recordingCanvas.translate(-getLeft() - getTranslationX(), -getTop() - getTranslationY());
            
            View parent = (View) getParent();
            if (parent != null) {
                int oldVisibility = getVisibility();
                setVisibility(View.INVISIBLE);
                parent.draw(recordingCanvas);
                setVisibility(oldVisibility);
            }
            
            recordingCanvas.restore();
            noiseSuppressor.endRecordingRect();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        
        // Clip to rounded rect
        android.graphics.Path clipPath = new android.graphics.Path();
        android.graphics.RectF clipRect = new android.graphics.RectF(0, 0, getWidth(), getHeight());
        clipPath.addRoundRect(clipRect, radii, android.graphics.Path.Direction.CW);
        canvas.save();
        canvas.clipPath(clipPath);
        
        // Draw blurred background
        noiseSuppressor.invalidateResultRenderNodes(getWidth(), getHeight());
        noiseSuppressor.draw(canvas, DownscaleScrollableNoiseSuppressor.DRAW_FROSTED_GLASS);
        
        // Draw overlay
        canvas.drawColor(backgroundColor);
        
        // Draw strokes
        xyz.code.blur3.drawable.BlurredBackgroundDrawable.drawStroke(
            canvas, 0, 0, getWidth(), getHeight(), radii,
            dpf(1), true, paintStrokeTop
        );
        
        xyz.code.blur3.drawable.BlurredBackgroundDrawable.drawStroke(
            canvas, 0, 0, getWidth(), getHeight(), radii,
            dpf(0.67f), false, paintStrokeBottom
        );
        
        canvas.restore();
    }
    
    /**
     * Adds a tab to the navigation bar
     */
    public NavigationBar addTab(int iconRes, String label) {
        if (tabs.size() >= MAX_TABS) {
            throw new IllegalStateException("Cannot add more than " + MAX_TABS + " tabs");
        }
        NavigationTab tab = new NavigationTab(iconRes, label);
        tabs.add(tab);
        createTabView(tab, tabs.size() - 1);
        return this;
    }
    
    /**
     * Adds a tab with custom NavigationTab object
     */
    public NavigationBar addTab(NavigationTab tab) {
        if (tabs.size() >= MAX_TABS) {
            throw new IllegalStateException("Cannot add more than " + MAX_TABS + " tabs");
        }
        tabs.add(tab);
        createTabView(tab, tabs.size() - 1);
        return this;
    }
    
    /**
     * Inserts a tab at specific position
     */
    public NavigationBar insertTab(int position, NavigationTab tab) {
        if (tabs.size() >= MAX_TABS) {
            throw new IllegalStateException("Cannot add more than " + MAX_TABS + " tabs");
        }
        if (position < 0 || position > tabs.size()) {
            throw new IndexOutOfBoundsException("Invalid position: " + position);
        }
        tabs.add(position, tab);
        rebuildTabs();
        return this;
    }
    
    /**
     * Removes a tab at specific position
     */
    public NavigationBar removeTab(int position) {
        if (position < 0 || position >= tabs.size()) {
            throw new IndexOutOfBoundsException("Invalid position: " + position);
        }
        tabs.remove(position);
        if (activeTabIndex >= tabs.size()) {
            activeTabIndex = Math.max(0, tabs.size() - 1);
        }
        rebuildTabs();
        return this;
    }
    
    /**
     * Removes all tabs
     */
    public NavigationBar clearTabs() {
        tabs.clear();
        tabContainer.removeAllViews();
        activeTabIndex = 0;
        return this;
    }
    
    /**
     * Gets the number of tabs
     */
    public int getTabCount() {
        return tabs.size();
    }
    
    /**
     * Gets the maximum number of tabs allowed
     */
    public int getMaxTabs() {
        return MAX_TABS;
    }
    
    /**
     * Gets tab at specific position
     */
    public NavigationTab getTab(int position) {
        if (position < 0 || position >= tabs.size()) {
            return null;
        }
        return tabs.get(position);
    }
    
    /**
     * Sets the active tab
     */
    public NavigationBar setActiveTab(int position) {
        if (position < 0 || position >= tabs.size()) {
            return this;
        }
        
        int oldIndex = activeTabIndex;
        activeTabIndex = position;
        
        // Update all tab states
        updateTabStates();
        
        // Notify listener
        if (listener != null && oldIndex != activeTabIndex) {
            listener.onTabSelected(activeTabIndex, tabs.get(activeTabIndex));
        }
        
        return this;
    }
    
    /**
     * Gets the active tab index
     */
    public int getActiveTabIndex() {
        return activeTabIndex;
    }
    
    /**
     * Gets the active tab
     */
    public NavigationTab getActiveTab() {
        if (activeTabIndex >= 0 && activeTabIndex < tabs.size()) {
            return tabs.get(activeTabIndex);
        }
        return null;
    }
    
    /**
     * Sets a badge count on a specific tab
     */
    public NavigationBar setBadge(int position, int count) {
        if (position >= 0 && position < tabs.size()) {
            tabs.get(position).badgeCount = count;
            updateTabView(position);
        }
        return this;
    }
    
    /**
     * Gets badge count for a tab
     */
    public int getBadge(int position) {
        if (position >= 0 && position < tabs.size()) {
            return tabs.get(position).badgeCount;
        }
        return 0;
    }
    
    /**
     * Clears badge on a tab
     */
    public NavigationBar clearBadge(int position) {
        return setBadge(position, 0);
    }
    
    /**
     * Enables or disables a tab
     */
    public NavigationBar setTabEnabled(int position, boolean enabled) {
        if (position >= 0 && position < tabs.size()) {
            tabs.get(position).enabled = enabled;
            updateTabView(position);
        }
        return this;
    }
    
    /**
     * Checks if a tab is enabled
     */
    public boolean isTabEnabled(int position) {
        if (position >= 0 && position < tabs.size()) {
            return tabs.get(position).enabled;
        }
        return false;
    }
    
    /**
     * Sets a tag for a tab
     */
    public NavigationBar setTabTag(int position, Object tag) {
        if (position >= 0 && position < tabs.size()) {
            tabs.get(position).tag = tag;
        }
        return this;
    }
    
    /**
     * Gets tag for a tab
     */
    public Object getTabTag(int position) {
        if (position >= 0 && position < tabs.size()) {
            return tabs.get(position).tag;
        }
        return null;
    }
    
    /**
     * Sets tab selection listener
     */
    public NavigationBar setOnTabSelectedListener(OnTabSelectedListener listener) {
        this.listener = listener;
        return this;
    }
    
    /**
     * Enables or disables blur effect
     */
    public NavigationBar setBlurEnabled(boolean enabled) {
        this.blurEnabled = enabled;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            setLayerType(enabled ? LAYER_TYPE_HARDWARE : LAYER_TYPE_NONE, null);
        }
        invalidate();
        return this;
    }
    
    /**
     * Checks if blur is enabled
     */
    public boolean isBlurEnabled() {
        return blurEnabled;
    }
    
    /**
     * Sets the animation curve for tab transitions
     */
    public NavigationBar setAnimationCurve(AnimationCurve curve) {
        this.animationCurve = curve;
        return this;
    }
    
    /**
     * Gets the current animation curve
     */
    public AnimationCurve getAnimationCurve() {
        return animationCurve;
    }
    
    /**
     * Sets the animation duration in milliseconds
     */
    public NavigationBar setAnimationDuration(long duration) {
        this.animationDuration = duration;
        return this;
    }
    
    /**
     * Gets the animation duration
     */
    public long getAnimationDuration() {
        return animationDuration;
    }
    
    /**
     * Gets the interpolator for the current animation curve
     */
    private android.view.animation.Interpolator getInterpolator() {
        switch (animationCurve) {
            case LINEAR:
                return new android.view.animation.LinearInterpolator();
            case ACCELERATE:
                return new android.view.animation.AccelerateInterpolator(1.5f);
            case DECELERATE:
                return new android.view.animation.DecelerateInterpolator(1.5f);
            case ACCELERATE_DECELERATE:
                return new android.view.animation.AccelerateDecelerateInterpolator();
            case OVERSHOOT:
                return new android.view.animation.OvershootInterpolator(1.5f);
            case ANTICIPATE:
                return new android.view.animation.AnticipateInterpolator(2.0f);
            case ANTICIPATE_OVERSHOOT:
                return new android.view.animation.AnticipateOvershootInterpolator(2.0f);
            case BOUNCE:
                return new android.view.animation.BounceInterpolator();
            case ELASTIC:
                return new android.view.animation.OvershootInterpolator(3.0f);
            default:
                return new android.view.animation.OvershootInterpolator(1.5f);
        }
    }
    
    /**
     * Sets background color for the navbar
     */
    public NavigationBar setNavBarBackgroundColor(int color) {
        this.backgroundColor = color;
        invalidate();
        return this;
    }
    
    /**
     * Sets active tab background color
     */
    public NavigationBar setActiveBackgroundColor(int color) {
        this.activeBackgroundColor = color;
        if (animatedIndicator != null) {
            android.graphics.drawable.GradientDrawable indicatorBg = 
                new android.graphics.drawable.GradientDrawable();
            indicatorBg.setColor(activeBackgroundColor);
            float circularRadius = getContext().getResources().getDimension(
                R.dimen.navbar_tab_circular_radius);
            indicatorBg.setCornerRadius(circularRadius);
            animatedIndicator.setBackground(indicatorBg);
        }
        updateTabStates();
        return this;
    }
    
    /**
     * Sets inactive icon color
     */
    public NavigationBar setInactiveIconColor(int color) {
        this.inactiveIconColor = color;
        updateTabStates();
        return this;
    }
    
    /**
     * Sets active icon color
     */
    public NavigationBar setActiveIconColor(int color) {
        this.activeIconColor = color;
        updateTabStates();
        return this;
    }
    
    /**
     * Sets corner radius
     */
    public NavigationBar setCornerRadius(float radius) {
        this.cornerRadius = radius;
        if (radii != null) {
            java.util.Arrays.fill(radii, dp(radius));
        }
        invalidate();
        return this;
    }
    
    /**
     * Sets badge colors
     */
    public NavigationBar setBadgeColors(int backgroundColor, int textColor) {
        this.badgeBackgroundColor = backgroundColor;
        this.badgeTextColor = textColor;
        rebuildTabs();
        return this;
    }
    
    /**
     * Inflates tabs from a menu resource
     */
    public NavigationBar inflateMenu(int menuRes) {
        android.view.MenuInflater inflater = new android.view.MenuInflater(getContext());
        android.view.Menu menu = new android.view.Menu() {
            private java.util.ArrayList<android.view.MenuItem> items = new java.util.ArrayList<>();
            
            @Override
            public android.view.MenuItem add(CharSequence title) {
                return add(0, 0, 0, title);
            }
            
            @Override
            public android.view.MenuItem add(int titleRes) {
                return add(0, 0, 0, titleRes);
            }
            
            @Override
            public android.view.MenuItem add(int groupId, int itemId, int order, CharSequence title) {
                android.view.MenuItem item = new MenuItemImpl(itemId, title);
                items.add(item);
                return item;
            }
            
            @Override
            public android.view.MenuItem add(int groupId, int itemId, int order, int titleRes) {
                return add(groupId, itemId, order, getContext().getString(titleRes));
            }
            
            @Override
            public android.view.SubMenu addSubMenu(CharSequence title) { return null; }
            @Override
            public android.view.SubMenu addSubMenu(int titleRes) { return null; }
            @Override
            public android.view.SubMenu addSubMenu(int groupId, int itemId, int order, CharSequence title) { return null; }
            @Override
            public android.view.SubMenu addSubMenu(int groupId, int itemId, int order, int titleRes) { return null; }
            @Override
            public int addIntentOptions(int groupId, int itemId, int order, android.content.ComponentName caller, android.content.Intent[] specifics, android.content.Intent intent, int flags, android.view.MenuItem[] outSpecificItems) { return 0; }
            @Override
            public void removeItem(int id) {}
            @Override
            public void removeGroup(int groupId) {}
            @Override
            public void clear() { items.clear(); }
            @Override
            public void setGroupCheckable(int group, boolean checkable, boolean exclusive) {}
            @Override
            public void setGroupVisible(int group, boolean visible) {}
            @Override
            public void setGroupEnabled(int group, boolean enabled) {}
            @Override
            public boolean hasVisibleItems() { return !items.isEmpty(); }
            @Override
            public android.view.MenuItem findItem(int id) {
                for (android.view.MenuItem item : items) {
                    if (item.getItemId() == id) return item;
                }
                return null;
            }
            @Override
            public int size() { return items.size(); }
            @Override
            public android.view.MenuItem getItem(int index) { return items.get(index); }
            @Override
            public void close() {}
            @Override
            public boolean performShortcut(int keyCode, android.view.KeyEvent event, int flags) { return false; }
            @Override
            public boolean isShortcutKey(int keyCode, android.view.KeyEvent event) { return false; }
            @Override
            public boolean performIdentifierAction(int id, int flags) { return false; }
            @Override
            public void setQwertyMode(boolean isQwerty) {}
        };
        
        inflater.inflate(menuRes, menu);
        
        // Add tabs from menu items
        for (int i = 0; i < menu.size() && i < MAX_TABS; i++) {
            android.view.MenuItem item = menu.getItem(i);
            MenuItemImpl menuItem = (MenuItemImpl) item;
            if (menuItem.iconRes != 0) {
                addTab(menuItem.iconRes, item.getTitle() != null ? item.getTitle().toString() : "");
            }
        }
        
        // Reset blur render nodes after tabs are added (critical for XML inflation)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurEnabled && noiseSuppressor != null) {
            post(() -> {
                // Force blur to reinitialize with correct dimensions
                if (noiseSuppressor.getRenderNodesCount() > 0) {
                    noiseSuppressor.setupRenderNodes(new java.util.ArrayList<>(), 0);
                }
                invalidate();
            });
        }
        
        return this;
    }
    
    /**
     * Finds tab index by tag
     */
    public int findTabByTag(Object tag) {
        for (int i = 0; i < tabs.size(); i++) {
            if (tabs.get(i).tag != null && tabs.get(i).tag.equals(tag)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Programmatically triggers tab animation (scale effect)
     */
    public NavigationBar animateTab(int position) {
        if (position >= 0 && position < tabContainer.getChildCount()) {
            View tabView = tabContainer.getChildAt(position);
            tabView.animate()
                .scaleX(0.85f)
                .scaleY(0.85f)
                .setDuration(100)
                .withEndAction(() -> {
                    tabView.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start();
                })
                .start();
        }
        return this;
    }
    
    private void createTabView(NavigationTab tab, int position) {
        LinearLayout tabView = new LinearLayout(getContext());
        tabView.setOrientation(LinearLayout.VERTICAL);
        tabView.setGravity(Gravity.CENTER);
        tabView.setClipChildren(false);
        tabView.setClipToPadding(false);
        int padding = dp(15);
        tabView.setPadding(padding, dp(8), padding, dp(8));
        
        // Icon container
        FrameLayout iconContainer = new FrameLayout(getContext());
        iconContainer.setId(View.generateViewId());
        
        // Icon
        ImageView icon = new ImageView(getContext());
        icon.setImageResource(tab.iconRes);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        
        // Icon size
        int iconSize = dp(25);
        FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(iconSize, iconSize);
        iconParams.gravity = Gravity.CENTER;
        iconContainer.addView(icon, iconParams);
        
        // Badge
        if (tab.badgeCount > 0) {
            TextView badge = createBadgeView(tab.badgeCount);
            FrameLayout.LayoutParams badgeParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            );
            badgeParams.gravity = Gravity.TOP | Gravity.END;
            badgeParams.setMargins(0, dp(0), dp(0), 0);
            iconContainer.addView(badge, badgeParams);
        }
        
        tabView.addView(iconContainer);
        
        // Add label text below icon
        TextView label = new TextView(getContext());
        label.setText(tab.label);
        label.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 10);
        label.setGravity(Gravity.CENTER);
        label.setSingleLine(true);
        label.setMaxWidth(dp(80));
        // Apply OpenSans Regular font
        try {
            android.graphics.Typeface typeface = androidx.core.content.res.ResourcesCompat.getFont(getContext(), R.font.opensans_regular);
            label.setTypeface(typeface);
        } catch (Exception e) {
            // Fallback to default if font fails to load
            e.printStackTrace();
        }
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        labelParams.topMargin = dp(4);
        tabView.addView(label, labelParams);
        
        // Click listener
        tabView.setOnClickListener(v -> {
            if (tab.enabled) {
                setActiveTab(position);
            }
        });
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, 
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 0);
        tabContainer.addView(tabView, params);
        
        // Update tab states after first tab is added
        if (position == 0 && tabContainer.getChildCount() == 1) {
            tabContainer.post(() -> {
                if (tabContainer.getChildCount() > 0) {
                    updateTabStates();
                }
            });
        }
    }
    
    private TextView createBadgeView(int count) {
        TextView badge = new TextView(getContext());
        badge.setText(count > 99 ? "99+" : String.valueOf(count));
        badge.setTextColor(badgeTextColor);
        badge.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 9);
        badge.setGravity(Gravity.CENTER);
        int padding = dp(3);
        badge.setPadding(padding, dp(1), padding, dp(1));
        int minSize = dp(16);
        badge.setMinWidth(minSize);
        badge.setMinHeight(minSize);
        
        // Rounded background
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(badgeBackgroundColor);
        bg.setCornerRadius(dp(8));
        badge.setBackground(bg);
        
        return badge;
    }
    
    private void updateTabStates() {
        for (int i = 0; i < tabs.size(); i++) {
            updateTabState(i);
        }
    }
    
    private void animateIndicatorToTab(int fromPosition, int toPosition) {
        if (tabContainer.getChildCount() == 0 || toPosition >= tabContainer.getChildCount()) {
            return;
        }
        
        // Cancel any running animation
        if (indicatorAnimator != null && indicatorAnimator.isRunning()) {
            indicatorAnimator.cancel();
        }
        
        // Make indicator visible
        if (animatedIndicator.getVisibility() != VISIBLE) {
            animatedIndicator.setVisibility(VISIBLE);
        }
        
        View fromTab = fromPosition >= 0 && fromPosition < tabContainer.getChildCount() 
            ? tabContainer.getChildAt(fromPosition) : null;
        View toTab = tabContainer.getChildAt(toPosition);
        
        // Calculate positions
        float startX = fromTab != null ? getTabCenterX(fromPosition) : getTabCenterX(toPosition);
        float endX = getTabCenterX(toPosition);
        
        // Update icon colors immediately
        updateTabStates();
        
        // Animate with selected curve
        indicatorAnimator = android.animation.ValueAnimator.ofFloat(startX, endX);
        indicatorAnimator.setDuration(animationDuration);
        indicatorAnimator.setInterpolator(getInterpolator());
        indicatorAnimator.addUpdateListener(animation -> {
            float x = (float) animation.getAnimatedValue();
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) animatedIndicator.getLayoutParams();
            int iconSize = getContext().getResources().getDimensionPixelSize(R.dimen.navbar_tab_icon_size);
            params.leftMargin = (int) (x - iconSize / 2);
            animatedIndicator.setLayoutParams(params);
        });
        
        // Add scale animation for smooth effect
        android.animation.ObjectAnimator scaleX = android.animation.ObjectAnimator.ofFloat(
            animatedIndicator, "scaleX", 1f, 1.15f, 1f);
        android.animation.ObjectAnimator scaleY = android.animation.ObjectAnimator.ofFloat(
            animatedIndicator, "scaleY", 1f, 1.15f, 1f);
        
        android.animation.AnimatorSet animatorSet = new android.animation.AnimatorSet();
        animatorSet.playTogether(indicatorAnimator, scaleX, scaleY);
        animatorSet.start();
    }
    
    private float getTabCenterX(int position) {
        if (position < 0 || position >= tabContainer.getChildCount()) {
            return 0;
        }
        
        View tabView = tabContainer.getChildAt(position);
        
        // Calculate tab center relative to this NavigationBar
        float tabCenterX = tabView.getLeft() + tabView.getWidth() / 2f;
        
        // Add tab container's offset
        tabCenterX += tabContainer.getLeft();
        
        return tabCenterX;
    }
    
    private void updateTabView(int position) {
        if (position >= 0 && position < tabContainer.getChildCount()) {
            LinearLayout tabView = (LinearLayout) tabContainer.getChildAt(position);
            FrameLayout iconContainer = (FrameLayout) tabView.getChildAt(0);
            
            // Update badge
            NavigationTab tab = tabs.get(position);
            if (iconContainer.getChildCount() > 1) {
                iconContainer.removeViewAt(1); // Remove old badge
            }
            
            if (tab.badgeCount > 0) {
                TextView badge = createBadgeView(tab.badgeCount);
                FrameLayout.LayoutParams badgeParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                );
                badgeParams.gravity = Gravity.TOP | Gravity.END;
                int badgeMarginTop = getContext().getResources().getDimensionPixelSize(R.dimen.badge_margin_top);
                int badgeMarginEnd = getContext().getResources().getDimensionPixelSize(R.dimen.badge_margin_end);
                badgeParams.setMargins(0, badgeMarginTop, badgeMarginEnd, 0);
                iconContainer.addView(badge, badgeParams);
            }
            
            updateTabState(position);
        }
    }
    
    private void updateTabState(int position) {
        if (position >= 0 && position < tabContainer.getChildCount()) {
            LinearLayout tabView = (LinearLayout) tabContainer.getChildAt(position);
            FrameLayout iconContainer = (FrameLayout) tabView.getChildAt(0);
            ImageView icon = (ImageView) iconContainer.getChildAt(0);
            TextView label = (TextView) tabView.getChildAt(1);
            NavigationTab tab = tabs.get(position);
            
            boolean isActive = position == activeTabIndex;
            
            // Add glass effect with stroke to active tab
            if (isActive) {
                android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
                bg.setColor(0x40FFFFFF); // Brighter white background (25% opacity) for better visibility
                bg.setCornerRadius(dp(25)); // Rounded pill shape
                bg.setStroke(dp(1), 0x60FFFFFF); // Brighter glass stroke border (38% opacity)
                tabView.setBackground(bg);
            } else {
                tabView.setBackground(null);
            }
            
            // Update icon color - selected tab is always white
            icon.setColorFilter(isActive ? 0xFFFFFFFF : inactiveIconColor, 
                android.graphics.PorterDuff.Mode.SRC_IN);
            
            // Update label color - selected tab is always white
            label.setTextColor(isActive ? 0xFFFFFFFF : inactiveIconColor);
            
            // Update enabled state
            tabView.setAlpha(tab.enabled ? 1f : 0.4f);
            tabView.setEnabled(tab.enabled);
        }
    }
    
    private void rebuildTabs() {
        tabContainer.removeAllViews();
        for (int i = 0; i < tabs.size(); i++) {
            createTabView(tabs.get(i), i);
        }
    }
    
    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
    
    private float dpf(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
    
    /**
     * Simple MenuItem implementation for menu inflation
     */
    private static class MenuItemImpl implements android.view.MenuItem {
        private int itemId;
        private CharSequence title;
        int iconRes;
        
        MenuItemImpl(int itemId, CharSequence title) {
            this.itemId = itemId;
            this.title = title;
        }
        
        @Override public int getItemId() { return itemId; }
        @Override public CharSequence getTitle() { return title; }
        @Override public android.view.MenuItem setTitle(CharSequence title) { this.title = title; return this; }
        @Override public android.view.MenuItem setTitle(int title) { return this; }
        @Override public android.graphics.drawable.Drawable getIcon() { return null; }
        @Override public android.view.MenuItem setIcon(android.graphics.drawable.Drawable icon) { return this; }
        @Override 
        public android.view.MenuItem setIcon(int iconRes) { 
            this.iconRes = iconRes; 
            return this; 
        }
        @Override public int getGroupId() { return 0; }
        @Override public int getOrder() { return 0; }
        @Override public android.view.MenuItem setCheckable(boolean checkable) { return this; }
        @Override public boolean isCheckable() { return false; }
        @Override public android.view.MenuItem setChecked(boolean checked) { return this; }
        @Override public boolean isChecked() { return false; }
        @Override public android.view.MenuItem setVisible(boolean visible) { return this; }
        @Override public boolean isVisible() { return true; }
        @Override public android.view.MenuItem setEnabled(boolean enabled) { return this; }
        @Override public boolean isEnabled() { return true; }
        @Override public boolean hasSubMenu() { return false; }
        @Override public android.view.SubMenu getSubMenu() { return null; }
        @Override public android.view.MenuItem setOnMenuItemClickListener(OnMenuItemClickListener menuItemClickListener) { return this; }
        @Override public android.view.ContextMenu.ContextMenuInfo getMenuInfo() { return null; }
        @Override public void setShowAsAction(int actionEnum) {}
        @Override public android.view.MenuItem setShowAsActionFlags(int actionEnum) { return this; }
        @Override public android.view.MenuItem setActionView(android.view.View view) { return this; }
        @Override public android.view.MenuItem setActionView(int resId) { return this; }
        @Override public android.view.View getActionView() { return null; }
        @Override public android.view.MenuItem setActionProvider(android.view.ActionProvider actionProvider) { return this; }
        @Override public android.view.ActionProvider getActionProvider() { return null; }
        @Override public boolean expandActionView() { return false; }
        @Override public boolean collapseActionView() { return false; }
        @Override public boolean isActionViewExpanded() { return false; }
        @Override public android.view.MenuItem setOnActionExpandListener(OnActionExpandListener listener) { return this; }
        @Override public android.view.MenuItem setIntent(android.content.Intent intent) { return this; }
        @Override public android.content.Intent getIntent() { return null; }
        @Override public android.view.MenuItem setShortcut(char numericChar, char alphaChar) { return this; }
        @Override public android.view.MenuItem setNumericShortcut(char numericChar) { return this; }
        @Override public char getNumericShortcut() { return 0; }
        @Override public android.view.MenuItem setAlphabeticShortcut(char alphaChar) { return this; }
        @Override public char getAlphabeticShortcut() { return 0; }
        @Override public android.view.MenuItem setAlphabeticShortcut(char alphaChar, int modifiers) { return this; }
        @Override public int getAlphabeticModifiers() { return 0; }
        @Override public android.view.MenuItem setNumericShortcut(char numericChar, int modifiers) { return this; }
        @Override public int getNumericModifiers() { return 0; }
        @Override public android.view.MenuItem setShortcut(char numericChar, char alphaChar, int numericModifiers, int alphaModifiers) { return this; }
        @Override public CharSequence getTitleCondensed() { return title; }
        @Override public android.view.MenuItem setTitleCondensed(CharSequence title) { return this; }
        @Override public android.view.MenuItem setIconTintList(android.content.res.ColorStateList tint) { return this; }
        @Override public android.content.res.ColorStateList getIconTintList() { return null; }
        @Override public android.view.MenuItem setIconTintMode(android.graphics.PorterDuff.Mode tintMode) { return this; }
        @Override public android.graphics.PorterDuff.Mode getIconTintMode() { return null; }
        @Override public android.view.MenuItem setTooltipText(CharSequence tooltipText) { return this; }
        @Override public CharSequence getTooltipText() { return null; }
        @Override public android.view.MenuItem setContentDescription(CharSequence contentDescription) { return this; }
        @Override public CharSequence getContentDescription() { return null; }
    }
}
