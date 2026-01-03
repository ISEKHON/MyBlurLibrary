package xyz.code.blur3.drawable.color;

import androidx.core.graphics.ColorUtils;

import xyz.code.blur3.util.AndroidUtilities;
import xyz.code.blur3.util.Theme;

public class BlurredBackgroundColorProviderThemed implements BlurredBackgroundColorProvider {

    private final int backgroundColorId;
    private final float alpha;

    public BlurredBackgroundColorProviderThemed(int backgroundColorId) {
        this(backgroundColorId, true ? 0.84f : 0.76f);
    }

    public BlurredBackgroundColorProviderThemed(int backgroundColorId, float alpha) {
        this.backgroundColorId = backgroundColorId;
        this.alpha = alpha;

        updateColors();
    }

    private int backgroundColor, shadowColor, strokeColorTop, strokeColorBottom;

    public void updateColors() {
        final int color = Theme.getColor(backgroundColorId);
        final boolean isDark = AndroidUtilities.computePerceivedBrightness(color) < .721f;

        backgroundColor = Theme.multAlpha(color, alpha);
        if (isDark) {
            strokeColorTop = 0x28FFFFFF;
            strokeColorBottom = 0x14FFFFFF;
            shadowColor = 0;
        } else {
            strokeColorTop = 0xFFFFFFFF;
            strokeColorBottom = 0xFFFFFFFF;
            shadowColor = 0x20000000; //0x19000000;
        }
    }


    @Override
    public int getShadowColor() {
        return shadowColor;
    }

    @Override
    public int getBackgroundColor() {
        return backgroundColor;
    }

    @Override
    public int getStrokeColorTop() {
        return strokeColorTop;
    }

    @Override
    public int getStrokeColorBottom() {
        return strokeColorBottom;
    }
}

