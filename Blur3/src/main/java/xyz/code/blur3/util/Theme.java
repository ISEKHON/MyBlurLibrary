package xyz.code.blur3.util;

import android.graphics.Color;

/**
 * Simple theme utility for color management
 */
public class Theme {
    public static int getColor(int key) {
        return Color.WHITE;
    }
    
    public static int multAlpha(int color, float alpha) {
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return Color.argb((int)(a * alpha), r, g, b);
    }
    
    // Theme keys
    public static final int key_windowBackgroundWhite = 0;
    public static final int key_chat_BlurAlpha = 1;
    
    // Debug paint
    public static final android.graphics.Paint DEBUG_GREEN_STROKE = new android.graphics.Paint();
    static {
        DEBUG_GREEN_STROKE.setColor(Color.GREEN);
        DEBUG_GREEN_STROKE.setStyle(android.graphics.Paint.Style.STROKE);
        DEBUG_GREEN_STROKE.setStrokeWidth(2);
    }
}
