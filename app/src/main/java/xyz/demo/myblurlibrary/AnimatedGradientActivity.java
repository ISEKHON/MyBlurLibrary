package xyz.demo.myblurlibrary;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Demo activity showcasing animated gradient backgrounds
 */
public class AnimatedGradientActivity extends Activity {

    private MotionBackgroundDrawable motionBackground;
    private FrameLayout backgroundContainer;
    private GradientColorPicker colorPicker;
    private ColorPickerView colorPickerView;
    private FrameLayout colorPickerDialog;
    private SeekBar speedSeekBar;
    private TextView speedLabel;
    private Button playPauseButton;
    private Button hideShowButton;
    private ScrollView controlsScrollView;
    private boolean isPlaying = false;
    private int editingColorIndex = 0;
    private boolean controlsVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Main container
        FrameLayout rootLayout = new FrameLayout(this);
        rootLayout.setBackgroundColor(Color.BLACK);

        // Background container with animated gradient
        backgroundContainer = new FrameLayout(this);
        motionBackground = new MotionBackgroundDrawable();
        backgroundContainer.setBackground(motionBackground);
        rootLayout.addView(backgroundContainer, new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ));

        // Content scroll view
        controlsScrollView = new ScrollView(this);
        controlsScrollView.setFillViewport(true);
        
        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(dp(16), dp(40), dp(16), dp(16));

        // Title
        TextView title = new TextView(this);
        title.setText("Animated Gradient Background");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        title.setTextColor(Color.WHITE);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setShadowLayer(4, 0, 2, 0x80000000);
        contentLayout.addView(title, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        addSpacing(contentLayout, 24);

        // Description
        TextView description = new TextView(this);
        description.setText("Create beautiful animated backgrounds with multiple colors. " +
            "Based on Telegram's motion background system.");
        description.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        description.setTextColor(0xFFE0E0E0);
        description.setGravity(Gravity.CENTER);
        description.setShadowLayer(2, 0, 1, 0x80000000);
        contentLayout.addView(description, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        addSpacing(contentLayout, 32);

        // Control panel
        LinearLayout controlPanel = new LinearLayout(this);
        controlPanel.setOrientation(LinearLayout.VERTICAL);
        controlPanel.setBackgroundColor(0xDD000000);
        controlPanel.setPadding(dp(16), dp(16), dp(16), dp(16));
        controlPanel.setElevation(dp(8));
        
        // Play/Pause controls
        LinearLayout playbackControls = new LinearLayout(this);
        playbackControls.setOrientation(LinearLayout.HORIZONTAL);
        playbackControls.setGravity(Gravity.CENTER);

        playPauseButton = new Button(this);
        playPauseButton.setText("▶ Start Animation");
        playPauseButton.setTextColor(Color.WHITE);
        playPauseButton.setBackgroundColor(0xFF6B73FF);
        playPauseButton.setPadding(dp(24), dp(12), dp(24), dp(12));
        playPauseButton.setOnClickListener(v -> toggleAnimation());
        playbackControls.addView(playPauseButton, new LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
        ));

        Button nextPhaseButton = new Button(this);
        nextPhaseButton.setText("Next Phase");
        nextPhaseButton.setTextColor(Color.WHITE);
        nextPhaseButton.setBackgroundColor(0xFF10D0F6);
        nextPhaseButton.setPadding(dp(16), dp(12), dp(16), dp(12));
        nextPhaseButton.setOnClickListener(v -> motionBackground.switchToNextPosition(true));
        LinearLayout.LayoutParams nextParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        nextParams.leftMargin = dp(8);
        playbackControls.addView(nextPhaseButton, nextParams);

        controlPanel.addView(playbackControls);

        addSpacing(controlPanel, 16);

        // Speed control
        TextView speedTitle = new TextView(this);
        speedTitle.setText("Animation Speed");
        speedTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        speedTitle.setTextColor(Color.WHITE);
        speedTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        controlPanel.addView(speedTitle);

        addSpacing(controlPanel, 8);

        speedLabel = new TextView(this);
        speedLabel.setText("Speed: 1.0x");
        speedLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        speedLabel.setTextColor(0xFFCCCCCC);
        controlPanel.addView(speedLabel);

        addSpacing(controlPanel, 8);

        speedSeekBar = new SeekBar(this);
        speedSeekBar.setMax(100);
        speedSeekBar.setProgress(50); // 1.0x speed
        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Map 0-100 to 0.1x-3.0x
                float speed = 0.1f + (progress / 100f) * 2.9f;
                motionBackground.setIndeterminateSpeedScale(speed);
                speedLabel.setText(String.format("Speed: %.1fx", speed));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        controlPanel.addView(speedSeekBar);

        contentLayout.addView(controlPanel);

        addSpacing(contentLayout, 24);

        // Color picker
        TextView colorPickerTitle = new TextView(this);
        colorPickerTitle.setText("Gradient Colors");
        colorPickerTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        colorPickerTitle.setTextColor(Color.WHITE);
        colorPickerTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        colorPickerTitle.setGravity(Gravity.CENTER);
        colorPickerTitle.setShadowLayer(2, 0, 1, 0x80000000);
        contentLayout.addView(colorPickerTitle);

        addSpacing(contentLayout, 12);

        colorPicker = new GradientColorPicker(this);
        colorPicker.setBackgroundColor(0xDD000000);
        colorPicker.setElevation(dp(8));
        colorPicker.setColorChangeListener(colors -> {
            motionBackground.setColors(colors[0], colors[1], colors[2], colors[3]);
        });
        
        // Add long-click listeners to color buttons to open full color picker
        colorPicker.post(() -> {
            for (int i = 0; i < 4; i++) {
                final int index = i;
                View button = colorPicker.getColorButton(i);
                if (button != null) {
                    button.setOnLongClickListener(v -> {
                        editingColorIndex = index;
                        showColorPicker(colorPicker.getAllColors()[index]);
                        return true;
                    });
                }
            }
        });
        
        contentLayout.addView(colorPicker);
        
        // Add instruction
        TextView pickerHint = new TextView(this);
        pickerHint.setText("Tip: Long-press any color to open the color wheel picker");
        pickerHint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        pickerHint.setTextColor(0xFFAAAAAA);
        pickerHint.setGravity(Gravity.CENTER);
        pickerHint.setPadding(dp(16), dp(8), dp(16), 0);
        pickerHint.setShadowLayer(2, 0, 1, 0x80000000);
        contentLayout.addView(pickerHint);

        addSpacing(contentLayout, 24);

        // Preset colors section
        addPresetsSection(contentLayout);

        addSpacing(contentLayout, 24);

        // Info section
        addInfoSection(contentLayout);

        controlsScrollView.addView(contentLayout);
        rootLayout.addView(controlsScrollView);

        // Hide/Show controls button (floating)
        hideShowButton = new Button(this);
        hideShowButton.setText("Hide Controls");
        hideShowButton.setTextColor(Color.WHITE);
        hideShowButton.setBackgroundColor(0xDD000000);
        hideShowButton.setPadding(dp(16), dp(12), dp(16), dp(12));
        hideShowButton.setElevation(dp(8));
        hideShowButton.setOnClickListener(v -> toggleControls());
        
        FrameLayout.LayoutParams hideButtonParams = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        hideButtonParams.gravity = Gravity.TOP | Gravity.END;
        hideButtonParams.topMargin = dp(16);
        hideButtonParams.rightMargin = dp(16);
        rootLayout.addView(hideShowButton, hideButtonParams);

        setContentView(rootLayout);

        // Set parent view for animations
        motionBackground.setParentView(backgroundContainer);
    }

    private void addPresetsSection(LinearLayout parent) {
        TextView presetsTitle = new TextView(this);
        presetsTitle.setText("Preset Gradients");
        presetsTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        presetsTitle.setTextColor(Color.WHITE);
        presetsTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        presetsTitle.setGravity(Gravity.CENTER);
        presetsTitle.setShadowLayer(2, 0, 1, 0x80000000);
        parent.addView(presetsTitle);

        addSpacing(parent, 12);

        LinearLayout presetsContainer = new LinearLayout(this);
        presetsContainer.setOrientation(LinearLayout.VERTICAL);
        presetsContainer.setBackgroundColor(0xDD000000);
        presetsContainer.setPadding(dp(16), dp(16), dp(16), dp(16));
        presetsContainer.setElevation(dp(8));

        // Preset 1: Telegram Blue
        addPresetButton(presetsContainer, "Telegram Blue",
            0xFF6B73FF, 0xFF000000, 0xFF10D0F6, 0xFFF3558E);

        // Preset 2: Sunset
        addPresetButton(presetsContainer, "Sunset",
            0xFFFF6B6B, 0xFFFFD93D, 0xFFFF8E53, 0xFFFEA47F);

        // Preset 3: Ocean
        addPresetButton(presetsContainer, "Ocean Breeze",
            0xFF00C9FF, 0xFF92FE9D, 0xFF0099FF, 0xFF66FFD9);

        // Preset 4: Purple Dream
        addPresetButton(presetsContainer, "Purple Dream",
            0xFFDA22FF, 0xFF9733EE, 0xFF4776E6, 0xFF8E54E9);

        // Preset 5: Forest
        addPresetButton(presetsContainer, "Forest",
            0xFF134E5E, 0xFF71B280, 0xFF0F4C75, 0xFF95E1D3);

        parent.addView(presetsContainer);
    }

    private void addPresetButton(LinearLayout parent, String name, int c1, int c2, int c3, int c4) {
        Button button = new Button(this);
        button.setText(name);
        button.setTextColor(Color.WHITE);
        button.setBackgroundColor(0xFF333333);
        button.setPadding(dp(16), dp(12), dp(16), dp(12));
        button.setAllCaps(false);
        button.setOnClickListener(v -> {
            colorPicker.setColors(c1, c2, c3, c4);
        });

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        if (parent.getChildCount() > 0) {
            params.topMargin = dp(8);
        }
        parent.addView(button, params);
    }

    private void addInfoSection(LinearLayout parent) {
        LinearLayout infoPanel = new LinearLayout(this);
        infoPanel.setOrientation(LinearLayout.VERTICAL);
        infoPanel.setBackgroundColor(0xDD000000);
        infoPanel.setPadding(dp(16), dp(16), dp(16), dp(16));
        infoPanel.setElevation(dp(8));

        TextView infoTitle = new TextView(this);
        infoTitle.setText("How It Works");
        infoTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        infoTitle.setTextColor(Color.WHITE);
        infoTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        infoPanel.addView(infoTitle);

        addSpacing(infoPanel, 8);

        String[] infoTexts = {
            "• Supports 2-4 colors for complex gradients",
            "• 8 animation phases for smooth transitions",
            "• Efficient rendering with 60x80 bitmaps",
            "• Cubic Bezier interpolation for natural motion",
            "• Add/remove colors dynamically",
            "• Adjustable animation speed"
        };

        for (String text : infoTexts) {
            TextView infoText = new TextView(this);
            infoText.setText(text);
            infoText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            infoText.setTextColor(0xFFBBBBBB);
            infoText.setPadding(0, dp(4), 0, dp(4));
            infoPanel.addView(infoText);
        }

        parent.addView(infoPanel);
    }
private void showColorPicker(int initialColor) {
        // Create fullscreen color picker dialog
        colorPickerDialog = new FrameLayout(this);
        colorPickerDialog.setBackgroundColor(0xE0000000);
        colorPickerDialog.setClickable(true);
        
        FrameLayout dialogContent = new FrameLayout(this);
        dialogContent.setBackgroundColor(0xFF1A1A1A);
        dialogContent.setElevation(dp(16));
        
        // Title bar
        LinearLayout titleBar = new LinearLayout(this);
        titleBar.setOrientation(LinearLayout.HORIZONTAL);
        titleBar.setPadding(dp(16), dp(16), dp(16), dp(16));
        titleBar.setGravity(Gravity.CENTER_VERTICAL);
        titleBar.setBackgroundColor(0xFF252525);
        
        TextView titleText = new TextView(this);
        titleText.setText("Pick Color");
        titleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        titleText.setTextColor(Color.WHITE);
        titleText.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
        );
        titleBar.addView(titleText, titleParams);
        
        Button closeButton = new Button(this);
        closeButton.setText("✕");
        closeButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        closeButton.setTextColor(Color.WHITE);
        closeButton.setBackgroundColor(0x00000000);
        closeButton.setOnClickListener(v -> hideColorPicker());
        titleBar.addView(closeButton);
        
        FrameLayout.LayoutParams titleBarParams = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleBarParams.gravity = Gravity.TOP;
        dialogContent.addView(titleBar, titleBarParams);
        
        // Color picker
        colorPickerView = new ColorPickerView(this);
        colorPickerView.setColor(initialColor);
        colorPickerView.setOnColorChangeListener(color -> {
            int[] colors = colorPicker.getAllColors();
            colors[editingColorIndex] = color;
            colorPicker.setColors(colors[0], colors[1], colors[2], colors[3]);
        });
        
        FrameLayout.LayoutParams pickerParams = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        );
        pickerParams.topMargin = dp(60);
        dialogContent.addView(colorPickerView, pickerParams);
        
        // Position dialog content
        FrameLayout.LayoutParams contentParams = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(500)
        );
        contentParams.gravity = Gravity.CENTER;
        contentParams.leftMargin = dp(16);
        contentParams.rightMargin = dp(16);
        colorPickerDialog.addView(dialogContent, contentParams);
        
        // Add to root
        ((ViewGroup) getWindow().getDecorView()).addView(colorPickerDialog,
            new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        );
        
        colorPickerDialog.setAlpha(0f);
        colorPickerDialog.animate().alpha(1f).setDuration(200).start();
    }
    
    private void hideColorPicker() {
        if (colorPickerDialog != null) {
            colorPickerDialog.animate().alpha(0f).setDuration(200)
                .withEndAction(() -> {
                    ((ViewGroup) colorPickerDialog.getParent()).removeView(colorPickerDialog);
                    colorPickerDialog = null;
                    colorPickerView = null;
                })
                .start();
        }
    }
    
    private void toggleControls() {
        controlsVisible = !controlsVisible;
        
        if (controlsVisible) {
            controlsScrollView.setVisibility(View.VISIBLE);
            hideShowButton.setText("Hide Controls");
            controlsScrollView.animate()
                .alpha(1f)
                .setDuration(200)
                .start();
        } else {
            hideShowButton.setText("Show Controls");
            controlsScrollView.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction(() -> controlsScrollView.setVisibility(View.GONE))
                .start();
        }
    }
    
    @Override
    public void onBackPressed() {
        if (colorPickerDialog != null) {
            hideColorPicker();
        } else {
            super.onBackPressed();
        }
    }
    
    private void toggleAnimation() {
        if (isPlaying) {
            motionBackground.stopAnimation();
            playPauseButton.setText("▶ Start Animation");
            playPauseButton.setBackgroundColor(0xFF6B73FF);
        } else {
            motionBackground.startAnimation();
            playPauseButton.setText("⏸ Pause Animation");
            playPauseButton.setBackgroundColor(0xFFF3558E);
        }
        isPlaying = !isPlaying;
    }

    private void addSpacing(LinearLayout parent, int dp) {
        View spacer = new View(this);
        parent.addView(spacer, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(dp)
        ));
    }

    private int dp(float dp) {
        return (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp,
            getResources().getDisplayMetrics());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (motionBackground != null) {
            motionBackground.stopAnimation();
        }
    }
}
