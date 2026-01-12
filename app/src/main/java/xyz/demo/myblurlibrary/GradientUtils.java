package xyz.demo.myblurlibrary;

import android.graphics.Bitmap;
import android.graphics.Color;
import androidx.core.graphics.ColorUtils;

/**
 * Utility class for generating animated gradients.
 * Implements 8-phase gradient generation algorithm inspired by Telegram.
 */
public class GradientUtils {

    /**
     * Generates a multi-color gradient with animation support
     *
     * @param bitmap The bitmap to draw the gradient into
     * @param phase Animation phase (0-7)
     * @param progress Progress within the current phase (0.0-1.0)
     * @param colors Array of colors (supports 1-4 colors)
     */
    public static void generateGradient(Bitmap bitmap, int phase, float progress, int[] colors) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        // Normalize phase to 0-7
        phase = phase % 8;
        if (phase < 0) phase += 8;

        // Get positions for the 4 color points based on phase and progress
        PointF[] positions = getGradientPositions(phase, progress, width, height);

        // Generate the gradient
        int[] pixels = new int[width * height];
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                pixels[index] = calculatePixelColor(x, y, width, height, positions, colors);
            }
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
    }

    /**
     * Gets the positions of color points based on animation phase and progress
     */
    private static PointF[] getGradientPositions(int phase, float progress, int width, int height) {
        PointF[] positions = new PointF[4];
        
        // Define 8 different configurations for smooth animation
        float[][] phaseConfigs = {
            // Phase 0: Colors spread from corners
            {0.25f, 0.25f,  0.75f, 0.25f,  0.25f, 0.75f,  0.75f, 0.75f},
            // Phase 1: Rotating clockwise
            {0.5f, 0.2f,   0.8f, 0.5f,   0.5f, 0.8f,   0.2f, 0.5f},
            // Phase 2: Diagonal spread
            {0.2f, 0.2f,   0.8f, 0.4f,   0.4f, 0.8f,   0.6f, 0.6f},
            // Phase 3: Vertical emphasis
            {0.3f, 0.1f,   0.7f, 0.3f,   0.3f, 0.7f,   0.7f, 0.9f},
            // Phase 4: Center spread
            {0.5f, 0.5f,   0.8f, 0.2f,   0.2f, 0.8f,   0.8f, 0.8f},
            // Phase 5: Rotating counter-clockwise
            {0.5f, 0.8f,   0.2f, 0.5f,   0.5f, 0.2f,   0.8f, 0.5f},
            // Phase 6: Horizontal emphasis
            {0.1f, 0.3f,   0.3f, 0.7f,   0.7f, 0.3f,   0.9f, 0.7f},
            // Phase 7: Diagonal opposite
            {0.8f, 0.2f,   0.6f, 0.6f,   0.4f, 0.4f,   0.2f, 0.8f}
        };

        float[] currentConfig = phaseConfigs[phase];
        float[] nextConfig = phaseConfigs[(phase + 1) % 8];

        // Interpolate between current and next phase
        for (int i = 0; i < 4; i++) {
            float x = lerp(currentConfig[i * 2] * width, nextConfig[i * 2] * width, progress);
            float y = lerp(currentConfig[i * 2 + 1] * height, nextConfig[i * 2 + 1] * height, progress);
            positions[i] = new PointF(x, y);
        }

        return positions;
    }

    /**
     * Calculates the color for a specific pixel based on distance to color points
     */
    private static int calculatePixelColor(int x, int y, int width, int height, 
                                          PointF[] positions, int[] colors) {
        // Calculate distances to each color point
        float[] distances = new float[4];
        float totalDistance = 0;
        
        for (int i = 0; i < 4; i++) {
            if (i >= colors.length || colors[i] == 0) {
                distances[i] = Float.MAX_VALUE;
                continue;
            }
            
            float dx = x - positions[i].x;
            float dy = y - positions[i].y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            
            // Apply smoothing function for better blending
            distance = (float) Math.pow(distance, 0.8);
            distances[i] = distance;
            totalDistance += 1f / (distance + 1f);
        }

        // Blend colors based on inverse distance weighting
        int resultR = 0, resultG = 0, resultB = 0;
        
        for (int i = 0; i < 4; i++) {
            if (i >= colors.length || colors[i] == 0 || distances[i] == Float.MAX_VALUE) {
                continue;
            }
            
            float weight = (1f / (distances[i] + 1f)) / totalDistance;
            
            resultR += Color.red(colors[i]) * weight;
            resultG += Color.green(colors[i]) * weight;
            resultB += Color.blue(colors[i]) * weight;
        }

        return Color.rgb(
            Math.min(255, Math.max(0, resultR)),
            Math.min(255, Math.max(0, resultG)),
            Math.min(255, Math.max(0, resultB))
        );
    }

    /**
     * Linear interpolation
     */
    private static float lerp(float start, float end, float progress) {
        return start + (end - start) * progress;
    }

    /**
     * Generates a complementary gradient color from a base color
     */
    public static int generateComplementaryColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        
        // Adjust saturation
        if (hsv[1] > 0.5f) {
            hsv[1] -= 0.15f;
        } else {
            hsv[1] += 0.15f;
        }
        
        // Shift hue
        if (hsv[0] > 180) {
            hsv[0] -= 20;
        } else {
            hsv[0] += 20;
        }
        
        return Color.HSVToColor(255, hsv);
    }

    /**
     * Generates a color for third position
     */
    public static int generateThirdColor(int baseColor) {
        float[] hsv = new float[3];
        Color.colorToHSV(baseColor, hsv);
        
        // Significant hue shift
        if (hsv[0] > 180) {
            hsv[0] -= 60;
        } else {
            hsv[0] += 60;
        }
        
        // Adjust brightness
        hsv[2] = Math.min(1f, hsv[2] + 0.1f);
        
        return Color.HSVToColor(255, hsv);
    }

    /**
     * Simple 2D point class
     */
    private static class PointF {
        float x, y;

        PointF(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    /**
     * Checks if the gradient would be dark
     */
    public static boolean isDarkGradient(int color1, int color2, int color3, int color4) {
        int averageColor = ColorUtils.blendARGB(color1, color2, 0.5f);
        if (color3 != 0) {
            averageColor = ColorUtils.blendARGB(averageColor, color3, 0.5f);
        }
        if (color4 != 0) {
            averageColor = ColorUtils.blendARGB(averageColor, color4, 0.5f);
        }
        
        float[] hsv = new float[3];
        Color.colorToHSV(averageColor, hsv);
        return hsv[2] < 0.3f;
    }
}
