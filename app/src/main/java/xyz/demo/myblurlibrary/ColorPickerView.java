package xyz.demo.myblurlibrary;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Telegram-style color picker with color wheel and brightness slider
 */
public class ColorPickerView extends FrameLayout {

    public interface OnColorChangeListener {
        void onColorChanged(int color);
    }

    private OnColorChangeListener listener;
    
    private Paint colorWheelPaint;
    private Paint brightnessSliderPaint;
    private Paint circlePaint;
    private Paint linePaint;
    
    private Bitmap colorWheelBitmap;
    private RectF colorWheelRect = new RectF();
    private RectF brightnessSliderRect = new RectF();
    
    private float[] colorHSV = new float[]{0.0f, 1.0f, 1.0f}; // Hue, Saturation, Value
    private int currentColor;
    
    private boolean colorWheelPressed;
    private boolean brightnessSliderPressed;
    
    private float colorWheelRadius;
    private float centerX, centerY;
    
    private EditText hexEditText;
    private View colorPreview;
    
    private float circleSize;
    private float sliderHeight = dp(32);
    private float sliderMarginTop = dp(16);
    
    public ColorPickerView(Context context) {
        super(context);
        init();
    }
    
    private void init() {
        setWillNotDraw(false);
        
        colorWheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        colorWheelPaint.setStyle(Paint.Style.FILL);
        
        brightnessSliderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(dp(2));
        circlePaint.setColor(Color.WHITE);
        
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(dp(2));
        linePaint.setColor(Color.WHITE);
        
        circleSize = dp(12);
        
        // Create bottom control panel
        createControlPanel();
        
        currentColor = Color.HSVToColor(colorHSV);
    }
    
    private void createControlPanel() {
        LinearLayout controlPanel = new LinearLayout(getContext());
        controlPanel.setOrientation(LinearLayout.HORIZONTAL);
        controlPanel.setGravity(Gravity.CENTER_VERTICAL);
        controlPanel.setPadding(dp(16), dp(8), dp(16), dp(16));
        controlPanel.setBackgroundColor(0x40000000);
        
        // Color preview
        colorPreview = new View(getContext());
        colorPreview.setBackgroundColor(currentColor);
        LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(dp(48), dp(48));
        previewParams.rightMargin = dp(12);
        controlPanel.addView(colorPreview, previewParams);
        
        // Hex input
        LinearLayout hexContainer = new LinearLayout(getContext());
        hexContainer.setOrientation(LinearLayout.VERTICAL);
        
        TextView hexLabel = new TextView(getContext());
        hexLabel.setText("HEX");
        hexLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        hexLabel.setTextColor(0xFFAAAAAA);
        hexContainer.addView(hexLabel);
        
        hexEditText = new EditText(getContext());
        hexEditText.setText(String.format("%06X", (0xFFFFFF & currentColor)));
        hexEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        hexEditText.setTextColor(Color.WHITE);
        hexEditText.setSingleLine(true);
        hexEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6), new InputFilter.AllCaps()});
        hexEditText.setBackgroundColor(0x30FFFFFF);
        hexEditText.setPadding(dp(8), dp(4), dp(8), dp(4));
        hexEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 6) {
                    try {
                        int color = Color.parseColor("#" + s.toString());
                        setColorFromInt(color, false);
                    } catch (Exception e) {
                        // Invalid color
                    }
                }
            }
        });
        hexContainer.addView(hexEditText);
        
        controlPanel.addView(hexContainer, new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
        ));
        
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.BOTTOM;
        addView(controlPanel, params);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        // Calculate dimensions
        int padding = dp(32);
        int bottomControlHeight = dp(80);
        int availableHeight = h - bottomControlHeight - padding * 2;
        
        colorWheelRadius = Math.min((w - padding * 2) / 2f, availableHeight - sliderHeight - sliderMarginTop) * 0.85f;
        centerX = w / 2f;
        centerY = padding + colorWheelRadius;
        
        colorWheelRect.set(
            centerX - colorWheelRadius,
            centerY - colorWheelRadius,
            centerX + colorWheelRadius,
            centerY + colorWheelRadius
        );
        
        float sliderTop = centerY + colorWheelRadius + sliderMarginTop;
        brightnessSliderRect.set(
            padding,
            sliderTop,
            w - padding,
            sliderTop + sliderHeight
        );
        
        createColorWheel();
    }
    
    private void createColorWheel() {
        if (colorWheelRadius <= 0) return;
        
        int size = (int) (colorWheelRadius * 2);
        if (size <= 0) return;
        
        colorWheelBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(colorWheelBitmap);
        
        float radius = size / 2f;
        
        // Create color wheel with hue gradient
        int[] hueColors = new int[361];
        for (int i = 0; i < 361; i++) {
            hueColors[i] = Color.HSVToColor(new float[]{i, 1f, 1f});
        }
        
        SweepGradient sweepGradient = new SweepGradient(radius, radius, hueColors, null);
        RadialGradient saturationGradient = new RadialGradient(
            radius, radius, radius,
            Color.WHITE, 0x00FFFFFF,
            Shader.TileMode.CLAMP
        );
        
        ComposeShader shader = new ComposeShader(
            sweepGradient, saturationGradient,
            PorterDuff.Mode.SRC_OVER
        );
        
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShader(shader);
        canvas.drawCircle(radius, radius, radius, paint);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (colorWheelBitmap != null) {
            // Draw color wheel
            canvas.drawBitmap(colorWheelBitmap, null, colorWheelRect, colorWheelPaint);
            
            // Draw selection circle on color wheel
            float[] pos = getColorWheelPosition();
            canvas.drawCircle(pos[0], pos[1], circleSize, circlePaint);
        }
        
        // Draw brightness slider
        drawBrightnessSlider(canvas);
        
        // Draw brightness slider thumb
        float sliderX = brightnessSliderRect.left + 
            (brightnessSliderRect.width() * colorHSV[2]);
        float sliderY = brightnessSliderRect.centerY();
        
        circlePaint.setColor(Color.WHITE);
        circlePaint.setShadowLayer(dp(4), 0, dp(2), 0x80000000);
        canvas.drawCircle(sliderX, sliderY, dp(8), circlePaint);
        circlePaint.clearShadowLayer();
    }
    
    private void drawBrightnessSlider(Canvas canvas) {
        int currentColorWithMaxBrightness = Color.HSVToColor(new float[]{colorHSV[0], colorHSV[1], 1f});
        
        LinearGradient gradient = new LinearGradient(
            brightnessSliderRect.left, 0,
            brightnessSliderRect.right, 0,
            Color.BLACK, currentColorWithMaxBrightness,
            Shader.TileMode.CLAMP
        );
        
        brightnessSliderPaint.setShader(gradient);
        canvas.drawRoundRect(brightnessSliderRect, dp(4), dp(4), brightnessSliderPaint);
    }
    
    private float[] getColorWheelPosition() {
        float angle = (float) Math.toRadians(colorHSV[0]);
        float radius = colorWheelRadius * colorHSV[1];
        
        float x = centerX + radius * (float) Math.cos(angle);
        float y = centerY + radius * (float) Math.sin(angle);
        
        return new float[]{x, y};
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isInColorWheel(x, y)) {
                    colorWheelPressed = true;
                    updateColorFromWheel(x, y);
                    return true;
                } else if (brightnessSliderRect.contains(x, y)) {
                    brightnessSliderPressed = true;
                    updateBrightnessFromSlider(x);
                    return true;
                }
                break;
                
            case MotionEvent.ACTION_MOVE:
                if (colorWheelPressed) {
                    updateColorFromWheel(x, y);
                    return true;
                } else if (brightnessSliderPressed) {
                    updateBrightnessFromSlider(x);
                    return true;
                }
                break;
                
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                colorWheelPressed = false;
                brightnessSliderPressed = false;
                break;
        }
        
        return super.onTouchEvent(event);
    }
    
    private boolean isInColorWheel(float x, float y) {
        float dx = x - centerX;
        float dy = y - centerY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        return distance <= colorWheelRadius;
    }
    
    private void updateColorFromWheel(float x, float y) {
        float dx = x - centerX;
        float dy = y - centerY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        
        // Calculate angle (hue)
        float angle = (float) Math.toDegrees(Math.atan2(dy, dx));
        if (angle < 0) angle += 360;
        
        // Calculate saturation
        float saturation = Math.min(distance / colorWheelRadius, 1f);
        
        colorHSV[0] = angle;
        colorHSV[1] = saturation;
        
        updateColor();
    }
    
    private void updateBrightnessFromSlider(float x) {
        float value = (x - brightnessSliderRect.left) / brightnessSliderRect.width();
        value = Math.max(0f, Math.min(1f, value));
        
        colorHSV[2] = value;
        updateColor();
    }
    
    private void updateColor() {
        currentColor = Color.HSVToColor(colorHSV);
        
        // Update hex field
        hexEditText.setText(String.format("%06X", (0xFFFFFF & currentColor)));
        hexEditText.setSelection(hexEditText.length());
        
        // Update preview
        colorPreview.setBackgroundColor(currentColor);
        
        invalidate();
        
        if (listener != null) {
            listener.onColorChanged(currentColor);
        }
    }
    
    public void setColor(int color) {
        setColorFromInt(color, true);
    }
    
    private void setColorFromInt(int color, boolean updateHex) {
        Color.colorToHSV(color, colorHSV);
        currentColor = color;
        
        if (updateHex) {
            hexEditText.setText(String.format("%06X", (0xFFFFFF & color)));
        }
        
        colorPreview.setBackgroundColor(color);
        invalidate();
        
        if (listener != null) {
            listener.onColorChanged(color);
        }
    }
    
    public int getColor() {
        return currentColor;
    }
    
    public void setOnColorChangeListener(OnColorChangeListener listener) {
        this.listener = listener;
    }
    
    private int dp(float dp) {
        return (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp,
            getContext().getResources().getDisplayMetrics()
        );
    }
}
