package xyz.code.blur3;

import android.os.Build;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import xyz.code.blur3.drawable.BlurredBackgroundDrawable;
import xyz.code.blur3.drawable.BlurredBackgroundDrawableRenderNode;
import xyz.code.blur3.drawable.color.BlurredBackgroundColorProvider;
import xyz.code.blur3.source.BlurredBackgroundSource;
import xyz.code.blur3.util.ReferenceList;
import xyz.code.blur3.util.ViewPositionWatcher;

public class BlurredBackgroundDrawableViewFactory {

    private final BlurredBackgroundSource source;

    public BlurredBackgroundDrawableViewFactory(BlurredBackgroundSource source) {
        this.source = source;
    }

    public BlurredBackgroundDrawableViewFactory(ViewPositionWatcher watcher, ViewGroup parent, BlurredBackgroundSource source) {
        this(source);
        setSourceRootView(watcher, parent);
    }

    public void setSourceRootView(ViewPositionWatcher watcher, ViewGroup parent) {
        this.viewPositionWatcher = watcher;
        this.parent = parent;
    }

    private @Nullable ReferenceList<View> linkedViews;
    private @Nullable ViewPositionWatcher viewPositionWatcher;
    private @Nullable ViewGroup parent;

    public void setLinkedViewsRef(@Nullable ReferenceList<View> linkedViews) {
        this.linkedViews = linkedViews;
    }

    private boolean isLiquidGlassEffectAllowed;

    public void setLiquidGlassEffectAllowed(boolean liquidGlassEffectAllowed) {
        isLiquidGlassEffectAllowed = liquidGlassEffectAllowed;
    }

    public BlurredBackgroundDrawable create(View view) {
        return create(view, null);
    }

    public BlurredBackgroundDrawable create(View view, BlurredBackgroundColorProvider provider) {
        final BlurredBackgroundDrawable drawable = source.createDrawable();
        if (isLiquidGlassEffectAllowed && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (drawable instanceof BlurredBackgroundDrawableRenderNode) {
                ((BlurredBackgroundDrawableRenderNode) drawable).setLiquidGlassEffectAllowed();
            }
        }

        drawable.setColorProvider(provider);

        if (linkedViews != null && view != null) {
            linkedViews.add(view);
        }

        if (viewPositionWatcher != null && parent != null) {
            viewPositionWatcher.subscribe(view, parent, (v, pos) -> {
                drawable.setSourceOffset(pos.left, pos.top);
                view.invalidate();
            });
        }

        return drawable;
    }
}
