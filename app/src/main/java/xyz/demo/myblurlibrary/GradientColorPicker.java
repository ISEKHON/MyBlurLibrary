package xyz.demo.myblurlibrary;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Color picker component for selecting gradient colors
 */
public class GradientColorPicker extends LinearLayout {

    public interface ColorChangeListener {
        void onColorChanged(int[] colors);
    }

    private ColorChangeListener listener;
    private ColorButton[] colorButtons = new ColorButton[4];
    private EditText[] colorEditTexts = new EditText[4];
    private ImageView addButton;
    private ImageView removeButton;
    private int selectedColorIndex = 0;
    private int colorCount = 2;
    private int maxColorCount = 4;

    public GradientColorPicker(Context context) {
        super(context);
        setOrientation(VERTICAL);
        init(context);
    }

    private void init(Context context) {
        // Color buttons container
        LinearLayout buttonsContainer = new LinearLayout(context);
        buttonsContainer.setOrientation(HORIZONTAL);
        buttonsContainer.setGravity(Gravity.CENTER);
        buttonsContainer.setPadding(dp(16), dp(16), dp(16), dp(8));

        // Create color buttons
        for (int i = 0; i < 4; i++) {
            final int index = i;
            colorButtons[i] = new ColorButton(context);
            colorButtons[i].setOnClickListener(v -> selectColor(index));
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(40), dp(40));
            if (i > 0) {
                params.leftMargin = dp(12);
            }
            buttonsContainer.addView(colorButtons[i], params);
            
            if (i >= 2) {
                colorButtons[i].setVisibility(GONE);
            }
        }

        // Add button
        addButton = new ImageView(context);
        addButton.setImageResource(android.R.drawable.ic_input_add);
        addButton.setScaleType(ImageView.ScaleType.CENTER);
        addButton.setBackgroundResource(android.R.drawable.btn_default);
        addButton.setOnClickListener(v -> addColor());
        LinearLayout.LayoutParams addParams = new LinearLayout.LayoutParams(dp(40), dp(40));
        addParams.leftMargin = dp(12);
        buttonsContainer.addView(addButton, addParams);

        // Remove button
        removeButton = new ImageView(context);
        removeButton.setImageResource(android.R.drawable.ic_delete);
        removeButton.setScaleType(ImageView.ScaleType.CENTER);
        removeButton.setBackgroundResource(android.R.drawable.btn_default);
        removeButton.setOnClickListener(v -> removeColor());
        LinearLayout.LayoutParams removeParams = new LinearLayout.LayoutParams(dp(40), dp(40));
        removeParams.leftMargin = dp(12);
        removeButton.setVisibility(GONE);
        buttonsContainer.addView(removeButton, removeParams);

        addView(buttonsContainer);

        // Color input fields
        LinearLayout inputsContainer = new LinearLayout(context);
        inputsContainer.setOrientation(HORIZONTAL);
        inputsContainer.setPadding(dp(16), 0, dp(16), dp(16));
        inputsContainer.setGravity(Gravity.CENTER);

        for (int i = 0; i < 4; i++) {
            final int index = i;
            
            LinearLayout inputWrapper = new LinearLayout(context);
            inputWrapper.setOrientation(VERTICAL);
            inputWrapper.setGravity(Gravity.CENTER);
            
            TextView label = new TextView(context);
            label.setText("Color " + (i + 1));
            label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            label.setGravity(Gravity.CENTER);
            inputWrapper.addView(label);
            
            colorEditTexts[i] = new EditText(context);
            colorEditTexts[i].setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            colorEditTexts[i].setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
            colorEditTexts[i].setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            colorEditTexts[i].setGravity(Gravity.CENTER);
            colorEditTexts[i].setPadding(dp(8), dp(8), dp(8), dp(8));
            colorEditTexts[i].setImeOptions(EditorInfo.IME_ACTION_DONE);
            
            colorEditTexts[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 6) {
                        try {
                            int color = Color.parseColor("#" + s.toString());
                            colorButtons[index].setColor(color);
                            notifyColorChanged();
                        } catch (Exception e) {
                            // Invalid color
                        }
                    }
                }
            });
            
            LinearLayout.LayoutParams inputWrapperParams = new LinearLayout.LayoutParams(
                0, LayoutParams.WRAP_CONTENT, 1f);
            if (i > 0) {
                inputWrapperParams.leftMargin = dp(8);
            }
            inputWrapper.addView(colorEditTexts[i]);
            inputsContainer.addView(inputWrapper, inputWrapperParams);
            
            if (i >= 2) {
                inputWrapper.setVisibility(GONE);
            }
        }

        addView(inputsContainer);

        // Set default colors
        setColors(0xFF6B73FF, 0xFF000000, 0xFF10D0F6, 0xFFF3558E);
        selectColor(0);
    }

    public void setColors(int c1, int c2, int c3, int c4) {
        colorButtons[0].setColor(c1);
        colorButtons[1].setColor(c2);
        colorButtons[2].setColor(c3);
        colorButtons[3].setColor(c4);
        
        updateColorFields();
        notifyColorChanged();
    }

    public int[] getColors() {
        int[] colors = new int[colorCount];
        for (int i = 0; i < colorCount; i++) {
            colors[i] = colorButtons[i].getColor();
        }
        return colors;
    }

    public int[] getAllColors() {
        return new int[]{
            colorButtons[0].getColor(),
            colorCount > 1 ? colorButtons[1].getColor() : 0,
            colorCount > 2 ? colorButtons[2].getColor() : 0,
            colorCount > 3 ? colorButtons[3].getColor() : 0
        };
    }
    
    public View getColorButton(int index) {
        if (index >= 0 && index < 4) {
            return colorButtons[index];
        }
        return null;
    }

    private void selectColor(int index) {
        if (index >= colorCount) return;
        
        selectedColorIndex = index;
        for (int i = 0; i < 4; i++) {
            colorButtons[i].setSelected(i == index);
        }
    }

    private void addColor() {
        if (colorCount >= maxColorCount) return;
        
        // Generate new color based on existing ones
        int newColor;
        if (colorCount == 2) {
            newColor = GradientUtils.generateThirdColor(colorButtons[0].getColor());
        } else {
            newColor = GradientUtils.generateComplementaryColor(colorButtons[colorCount - 1].getColor());
        }
        
        colorButtons[colorCount].setColor(newColor);
        colorButtons[colorCount].setVisibility(VISIBLE);
        ((View)colorEditTexts[colorCount].getParent()).setVisibility(VISIBLE);
        
        colorCount++;
        updateColorFields();
        selectColor(colorCount - 1);
        notifyColorChanged();
        
        if (colorCount >= maxColorCount) {
            addButton.setVisibility(GONE);
        }
        if (colorCount > 2) {
            removeButton.setVisibility(VISIBLE);
        }
    }

    private void removeColor() {
        if (colorCount <= 2) return;
        
        colorCount--;
        colorButtons[colorCount].setVisibility(GONE);
        ((View)colorEditTexts[colorCount].getParent()).setVisibility(GONE);
        
        if (selectedColorIndex >= colorCount) {
            selectColor(colorCount - 1);
        }
        
        updateColorFields();
        notifyColorChanged();
        
        if (colorCount < maxColorCount) {
            addButton.setVisibility(VISIBLE);
        }
        if (colorCount <= 2) {
            removeButton.setVisibility(GONE);
        }
    }

    private void updateColorFields() {
        for (int i = 0; i < colorCount; i++) {
            int color = colorButtons[i].getColor();
            String hex = String.format("%06X", (0xFFFFFF & color));
            colorEditTexts[i].setText(hex);
        }
    }

    private void notifyColorChanged() {
        if (listener != null) {
            listener.onColorChanged(getAllColors());
        }
    }

    public void setColorChangeListener(ColorChangeListener listener) {
        this.listener = listener;
    }

    private int dp(float dp) {
        return (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, 
            getContext().getResources().getDisplayMetrics());
    }

    /**
     * Color button view
     */
    private static class ColorButton extends View {
        private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private int color = Color.WHITE;
        private boolean isSelected = false;
        private RectF rectF = new RectF();

        public ColorButton(Context context) {
            super(context);
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setStrokeWidth(dp(3));
        }

        public void setColor(int color) {
            this.color = color;
            invalidate();
        }

        public int getColor() {
            return color;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            
            int size = Math.min(getWidth(), getHeight());
            float radius = size / 2f;
            float centerX = getWidth() / 2f;
            float centerY = getHeight() / 2f;

            // Draw color circle
            paint.setColor(color);
            canvas.drawCircle(centerX, centerY, radius - dp(4), paint);

            // Draw selection ring
            if (isSelected) {
                strokePaint.setColor(Color.WHITE);
                canvas.drawCircle(centerX, centerY, radius - dp(2), strokePaint);
            }
            
            // Draw border
            strokePaint.setColor(0x40000000);
            strokePaint.setStrokeWidth(dp(1));
            canvas.drawCircle(centerX, centerY, radius - dp(4), strokePaint);
        }

        private int dp(float dp) {
            return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, 
                getContext().getResources().getDisplayMetrics());
        }
    }
}
