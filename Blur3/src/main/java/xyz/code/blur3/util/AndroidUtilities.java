package xyz.code.blur3.util;

import android.content.Context;
import android.content.res.Resources;

/**
 * Utility class for Android dimension conversions
 */
public class AndroidUtilities {
    private static float density = 1;
    
    public static void checkDisplaySize(Context context) {
        if (context != null) {
            density = context.getResources().getDisplayMetrics().density;
        }
    }
    
    public static int dp(float value) {
        if (value == 0) {
            return 0;
        }
        return (int) Math.ceil(density * value);
    }
    
    public static float dpf2(float value) {
        if (value == 0) {
            return 0;
        }
        return density * value;
    }
    
    public static int statusBarHeight = 0;
    
    public static float computePerceivedBrightness(int color) {
        int r = android.graphics.Color.red(color);
        int g = android.graphics.Color.green(color);
        int b = android.graphics.Color.blue(color);
        return (r * 0.299f + g * 0.587f + b * 0.114f) / 255f;
    }
    
    public static String readRes(Context context, int resourceId) {
        try {
            java.io.InputStream inputStream = context.getResources().openRawResource(resourceId);
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            reader.close();
            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
