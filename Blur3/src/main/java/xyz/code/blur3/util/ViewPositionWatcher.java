package xyz.code.blur3.util;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import java.util.HashMap;
import java.util.Map;

/**
 * Watches view positions and notifies listeners when they change
 */
public class ViewPositionWatcher {
    
    public interface Listener {
        void onPositionChanged(View view, Rect position);
    }
    
    private final Map<View, PositionListener> listeners = new HashMap<>();
    
    public void subscribe(View view, ViewGroup parent, Listener listener) {
        if (view == null || parent == null || listener == null) {
            return;
        }
        
        PositionListener positionListener = new PositionListener(view, parent, listener);
        listeners.put(view, positionListener);
        
        view.getViewTreeObserver().addOnPreDrawListener(positionListener);
    }
    
    public void unsubscribe(View view) {
        PositionListener listener = listeners.remove(view);
        if (listener != null && view.getViewTreeObserver().isAlive()) {
            view.getViewTreeObserver().removeOnPreDrawListener(listener);
        }
    }
    
    public void clear() {
        for (Map.Entry<View, PositionListener> entry : listeners.entrySet()) {
            View view = entry.getKey();
            PositionListener listener = entry.getValue();
            if (view != null && view.getViewTreeObserver().isAlive()) {
                view.getViewTreeObserver().removeOnPreDrawListener(listener);
            }
        }
        listeners.clear();
    }
    
    private static class PositionListener implements ViewTreeObserver.OnPreDrawListener {
        private final View view;
        private final ViewGroup parent;
        private final Listener listener;
        private final Rect lastPosition = new Rect();
        private final int[] location = new int[2];
        
        PositionListener(View view, ViewGroup parent, Listener listener) {
            this.view = view;
            this.parent = parent;
            this.listener = listener;
        }
        
        @Override
        public boolean onPreDraw() {
            view.getLocationInWindow(location);
            Rect currentPosition = new Rect(
                location[0],
                location[1],
                location[0] + view.getWidth(),
                location[1] + view.getHeight()
            );
            
            if (!currentPosition.equals(lastPosition)) {
                lastPosition.set(currentPosition);
                listener.onPositionChanged(view, currentPosition);
            }
            
            return true;
        }
    }
}
