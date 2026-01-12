package xyz.demo.myblurlibrary;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import java.lang.ref.WeakReference;

/**
 * Animated gradient background drawable with support for up to 4 colors.
 * Based on Telegram's MotionBackgroundDrawable implementation.
 */
public class MotionBackgroundDrawable extends Drawable {

    private static final int ANIMATION_CACHE_BITMAPS_COUNT = 3;
    
    private int[] colors = new int[]{
            0xff6B73FF,
            0xff000000,
            0xff10D0F6,
            0xffF3558E
    };

    private long lastUpdateTime;
    private WeakReference<View> parentView;

    private final CubicBezierInterpolator interpolator = new CubicBezierInterpolator(0.33f, 0.0f, 0.0f, 1.0f);

    public float posAnimationProgress = 1.0f;
    private int phase;

    private RectF rect = new RectF();
    private Bitmap currentBitmap;
    private Bitmap gradientFromBitmap;
    private Bitmap[] gradientToBitmap = new Bitmap[ANIMATION_CACHE_BITMAPS_COUNT];
    private Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);

    private int bitmapWidth = 60;
    private int bitmapHeight = 80;

    private boolean rotationBack;
    private boolean fastAnimation;
    private float indeterminateSpeedScale = 1f;
    private boolean isIndeterminateAnimation;

    private Runnable updateAnimationRunnable = () -> updateAnimation(true);

    public MotionBackgroundDrawable() {
        super();
        init();
    }

    public MotionBackgroundDrawable(int c1, int c2, int c3, int c4) {
        super();
        setColors(c1, c2, c3, c4, false);
        init();
    }

    private void init() {
        currentBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
        for (int i = 0; i < ANIMATION_CACHE_BITMAPS_COUNT; i++) {
            gradientToBitmap[i] = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
        }

        gradientFromBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);

        GradientUtils.generateGradient(currentBitmap, phase, 
                interpolator.getInterpolation(posAnimationProgress), colors);
    }

    public void setParentView(View view) {
        parentView = new WeakReference<>(view);
    }

    public void setColors(int c1, int c2, int c3, int c4) {
        setColors(c1, c2, c3, c4, true);
    }

    public void setColors(int c1, int c2, int c3, int c4, boolean invalidate) {
        if (colors[0] == c1 && colors[1] == c2 && colors[2] == c3 && colors[3] == c4) {
            return;
        }
        colors[0] = c1;
        colors[1] = c2;
        colors[2] = c3;
        colors[3] = c4;
        if (currentBitmap != null) {
            GradientUtils.generateGradient(currentBitmap, phase, 
                    interpolator.getInterpolation(posAnimationProgress), colors);
            if (invalidate) {
                invalidateParent();
            }
        }
    }

    public int[] getColors() {
        return colors;
    }

    public void setPhase(int value) {
        phase = value;
        if (phase < 0) {
            phase = 0;
        } else if (phase > 7) {
            phase = 7;
        }
        GradientUtils.generateGradient(currentBitmap, phase, 
                interpolator.getInterpolation(posAnimationProgress), colors);
    }

    public int getPhase() {
        return phase;
    }

    public void startAnimation() {
        if (isIndeterminateAnimation) {
            return;
        }
        isIndeterminateAnimation = true;
        posAnimationProgress = 0f;
        lastUpdateTime = SystemClock.elapsedRealtime();
        invalidateParent();
    }

    public void stopAnimation() {
        isIndeterminateAnimation = false;
    }

    public void setIndeterminateSpeedScale(float scale) {
        this.indeterminateSpeedScale = scale;
    }

    public void switchToNextPosition() {
        switchToNextPosition(false);
    }

    public void switchToNextPosition(boolean fast) {
        if (posAnimationProgress < 1.0f) {
            return;
        }
        rotationBack = false;
        fastAnimation = fast;
        posAnimationProgress = 0.0f;
        phase--;
        if (phase < 0) {
            phase = 7;
        }
        invalidateParent();
        Canvas canvas = new Canvas(gradientFromBitmap);
        canvas.drawBitmap(currentBitmap, 0, 0, null);
        generateNextGradient();
    }

    private void generateNextGradient() {
        for (int i = -1; i < ANIMATION_CACHE_BITMAPS_COUNT; i++) {
            float p = (i + 1) / (float) ANIMATION_CACHE_BITMAPS_COUNT;
            GradientUtils.generateGradient(
                    i < 0 ? gradientFromBitmap : gradientToBitmap[i], 
                    phase, p, colors);
        }
    }

    private void invalidateParent() {
        invalidateSelf();
        if (parentView != null && parentView.get() != null) {
            parentView.get().postInvalidate();
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();
        int bitmapWidth = currentBitmap.getWidth();
        int bitmapHeight = currentBitmap.getHeight();
        float w = bounds.width();
        float h = bounds.height();
        float maxScale = Math.max(w / bitmapWidth, h / bitmapHeight);
        float width = bitmapWidth * maxScale;
        float height = bitmapHeight * maxScale;
        float x = (w - width) / 2;
        float y = (h - height) / 2;

        rect.set(x, y, x + width, y + height);
        canvas.drawBitmap(currentBitmap, null, rect, paint);

        if (isIndeterminateAnimation) {
            updateAnimation(true);
        }
    }

    public void updateAnimation(boolean invalidate) {
        long newTime = SystemClock.elapsedRealtime();
        long dt = newTime - lastUpdateTime;
        if (dt > 20) {
            dt = 17;
        }
        lastUpdateTime = newTime;
        if (dt <= 1) {
            return;
        }

        if (isIndeterminateAnimation && posAnimationProgress >= 1.0f) {
            posAnimationProgress = 0f;
            phase--;
            if (phase < 0) {
                phase = 7;
            }
            Canvas canvas = new Canvas(gradientFromBitmap);
            canvas.drawBitmap(currentBitmap, 0, 0, null);
            generateNextGradient();
        }

        if (posAnimationProgress < 1.0f || isIndeterminateAnimation) {
            float progress;
            boolean isNeedGenerateGradient = isIndeterminateAnimation;

            if (isIndeterminateAnimation) {
                posAnimationProgress += (dt / 8000f) * indeterminateSpeedScale;
                if (posAnimationProgress >= 1.0f) {
                    posAnimationProgress = 1.0f;
                }
                progress = interpolator.getInterpolation(posAnimationProgress);
                isNeedGenerateGradient = true;
            } else {
                posAnimationProgress += dt / (fastAnimation ? 300.0f : 500.0f);
                if (posAnimationProgress > 1.0f) {
                    posAnimationProgress = 1.0f;
                }
                progress = interpolator.getInterpolation(posAnimationProgress);
                if (rotationBack) {
                    progress = 1.0f - progress;
                    if (posAnimationProgress >= 1.0f) {
                        phase++;
                        if (phase > 7) {
                            phase = 0;
                        }
                        progress = 1.0f;
                    }
                }
                isNeedGenerateGradient = true;
            }

            if (isNeedGenerateGradient) {
                // Interpolate between cached bitmaps for smooth animation
                if (progress < 1f && gradientFromBitmap != null) {
                    int index = (int) (progress * ANIMATION_CACHE_BITMAPS_COUNT);
                    if (index >= ANIMATION_CACHE_BITMAPS_COUNT) {
                        index = ANIMATION_CACHE_BITMAPS_COUNT - 1;
                    }
                    float localProgress = (progress * ANIMATION_CACHE_BITMAPS_COUNT) - index;
                    
                    Bitmap bitmap1 = index == 0 ? gradientFromBitmap : gradientToBitmap[index - 1];
                    Bitmap bitmap2 = gradientToBitmap[index];
                    
                    blendBitmaps(currentBitmap, bitmap1, bitmap2, localProgress);
                } else {
                    GradientUtils.generateGradient(currentBitmap, phase, progress, colors);
                }
            }

            if (invalidate) {
                invalidateParent();
            }
        }
    }

    private void blendBitmaps(Bitmap dest, Bitmap src1, Bitmap src2, float progress) {
        int[] pixels1 = new int[src1.getWidth() * src1.getHeight()];
        int[] pixels2 = new int[src2.getWidth() * src2.getHeight()];
        int[] pixelsDest = new int[dest.getWidth() * dest.getHeight()];

        src1.getPixels(pixels1, 0, src1.getWidth(), 0, 0, src1.getWidth(), src1.getHeight());
        src2.getPixels(pixels2, 0, src2.getWidth(), 0, 0, src2.getWidth(), src2.getHeight());

        for (int i = 0; i < pixelsDest.length; i++) {
            pixelsDest[i] = ColorUtils.blendARGB(pixels1[i], pixels2[i], progress);
        }

        dest.setPixels(pixelsDest, 0, dest.getWidth(), 0, 0, dest.getWidth(), dest.getHeight());
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    /**
     * Cubic Bezier interpolator for smooth animations
     */
    private static class CubicBezierInterpolator {
        private final float mX1, mY1, mX2, mY2;

        public CubicBezierInterpolator(float x1, float y1, float x2, float y2) {
            mX1 = x1;
            mY1 = y1;
            mX2 = x2;
            mY2 = y2;
        }

        public float getInterpolation(float input) {
            return getBezierCoordinateY(getXForTime(input));
        }

        private float getBezierCoordinateY(float time) {
            float c = 3 * mY1;
            float b = 3 * (mY2 - mY1) - c;
            float a = 1 - c - b;
            return time * (c + time * (b + time * a));
        }

        private float getXForTime(float time) {
            float x = time;
            float z;
            for (int i = 1; i < 14; i++) {
                z = getBezierCoordinateX(x) - time;
                if (Math.abs(z) < 1e-3) {
                    break;
                }
                x -= z / getXDerivate(x);
            }
            return x;
        }

        private float getBezierCoordinateX(float time) {
            float c = 3 * mX1;
            float b = 3 * (mX2 - mX1) - c;
            float a = 1 - c - b;
            return time * (c + time * (b + time * a));
        }

        private float getXDerivate(float t) {
            float c = 3 * mX1;
            float b = 3 * (mX2 - mX1) - c;
            float a = 1 - c - b;
            return c + t * (2 * b + 3 * a * t);
        }
    }
}
