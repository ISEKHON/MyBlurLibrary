package xyz.demo.myblurlibrary;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import xyz.code.blur3.DownscaleScrollableNoiseSuppressor;

/**
 * MiniPlayer with blur effect that slides up from behind NavigationBar
 */
public class MiniPlayer extends FrameLayout {
    
    private DownscaleScrollableNoiseSuppressor noiseSuppressor;
    private android.graphics.Paint paintStrokeTop;
    private android.graphics.Paint paintStrokeBottom;
    private float[] radii = new float[8];
    
    private int backgroundColor = 0x80000000;
    private float cornerRadius = 25f;
    private boolean blurEnabled = true;
    
    private TextView songTitle;
    private TextView artistName;
    private ImageView albumArt;
    private ImageView playPauseButton;
    private ImageView previousButton;
    private ImageView nextButton;
    
    private boolean isPlaying = false;
    private OnMiniPlayerListener listener;
    
    public interface OnMiniPlayerListener {
        void onPlayPause(boolean isPlaying);
        void onPrevious();
        void onNext();
        void onMiniPlayerClick();
    }
    
    public MiniPlayer(Context context) {
        super(context);
        init(context);
    }
    
    public MiniPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    @RequiresApi(api = Build.VERSION_CODES.S)
    private void init(Context context) {
        // Initialize blur components
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            noiseSuppressor = new DownscaleScrollableNoiseSuppressor();
            
            paintStrokeTop = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            paintStrokeTop.setStyle(android.graphics.Paint.Style.STROKE);
            paintStrokeTop.setStrokeWidth(dpf(1));
            paintStrokeTop.setColor(0x40FFFFFF);
            
            paintStrokeBottom = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            paintStrokeBottom.setStyle(android.graphics.Paint.Style.STROKE);
            paintStrokeBottom.setStrokeWidth(dpf(0.67f));
            paintStrokeBottom.setColor(0x30FFFFFF);
            
            java.util.Arrays.fill(radii, dp(cornerRadius));
        }
        
        createUI(context);
        
        setWillNotDraw(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurEnabled) {
            setLayerType(LAYER_TYPE_HARDWARE, null);
        }
    }
    
    private void createUI(Context context) {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER_VERTICAL);
        container.setPadding(dp(20), dp(14), dp(20), dp(14));
        
        // Album art
        albumArt = new ImageView(context);
        albumArt.setScaleType(ImageView.ScaleType.CENTER_CROP);
        android.graphics.drawable.GradientDrawable artBg = new android.graphics.drawable.GradientDrawable();
        artBg.setColor(0xFF1DB954);
        artBg.setCornerRadius(dp(25)); // Fully rounded (50dp / 2)
        albumArt.setBackground(artBg);
        LinearLayout.LayoutParams artParams = new LinearLayout.LayoutParams(dp(50), dp(50));
        artParams.setMargins(0, 0, dp(14), 0);
        container.addView(albumArt, artParams);
        
        // Song info container
        LinearLayout infoContainer = new LinearLayout(context);
        infoContainer.setOrientation(LinearLayout.VERTICAL);
        infoContainer.setGravity(Gravity.CENTER_VERTICAL);
        
        songTitle = new TextView(context);
        songTitle.setText("Song Title");
        songTitle.setTextSize(15);
        songTitle.setTextColor(Color.WHITE);
        songTitle.setSingleLine(true);
        songTitle.setEllipsize(android.text.TextUtils.TruncateAt.END);
        try {
            android.graphics.Typeface typeface = androidx.core.content.res.ResourcesCompat.getFont(context, R.font.opensans_semibold);
            songTitle.setTypeface(typeface);
        } catch (Exception e) {
            e.printStackTrace();
        }
        infoContainer.addView(songTitle);
        
        artistName = new TextView(context);
        artistName.setText("Artist Name");
        artistName.setTextSize(13);
        artistName.setTextColor(0xFFAAAAAA);
        artistName.setSingleLine(true);
        artistName.setEllipsize(android.text.TextUtils.TruncateAt.END);
        try {
            android.graphics.Typeface typeface = androidx.core.content.res.ResourcesCompat.getFont(context, R.font.opensans_regular);
            artistName.setTypeface(typeface);
        } catch (Exception e) {
            e.printStackTrace();
        }
        infoContainer.addView(artistName);
        
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
        infoParams.weight = 1;
        container.addView(infoContainer, infoParams);
        
        // Control buttons container
        LinearLayout controlsContainer = new LinearLayout(context);
        controlsContainer.setOrientation(LinearLayout.HORIZONTAL);
        controlsContainer.setGravity(Gravity.CENTER_VERTICAL);
        
        // Previous button
        previousButton = new ImageView(context);
        previousButton.setImageResource(android.R.drawable.ic_media_previous);
        previousButton.setColorFilter(Color.WHITE);
        previousButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
        previousButton.setPadding(dp(8), dp(8), dp(8), dp(8));
        previousButton.setOnClickListener(v -> {
            if (listener != null) listener.onPrevious();
        });
        LinearLayout.LayoutParams prevParams = new LinearLayout.LayoutParams(dp(40), dp(40));
        prevParams.setMargins(dp(4), 0, dp(4), 0);
        controlsContainer.addView(previousButton, prevParams);
        
        // Play/Pause button
        playPauseButton = new ImageView(context);
        playPauseButton.setImageResource(android.R.drawable.ic_media_play);
        playPauseButton.setColorFilter(Color.WHITE);
        playPauseButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
        playPauseButton.setPadding(dp(8), dp(8), dp(8), dp(8));
        playPauseButton.setOnClickListener(v -> togglePlayPause());
        LinearLayout.LayoutParams playParams = new LinearLayout.LayoutParams(dp(40), dp(40));
        playParams.setMargins(dp(4), 0, dp(4), 0);
        controlsContainer.addView(playPauseButton, playParams);
        
        // Next button
        nextButton = new ImageView(context);
        nextButton.setImageResource(android.R.drawable.ic_media_next);
        nextButton.setColorFilter(Color.WHITE);
        nextButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
        nextButton.setPadding(dp(8), dp(8), dp(8), dp(8));
        nextButton.setOnClickListener(v -> {
            if (listener != null) listener.onNext();
        });
        LinearLayout.LayoutParams nextParams = new LinearLayout.LayoutParams(dp(40), dp(40));
        nextParams.setMargins(dp(4), 0, dp(4), 0);
        controlsContainer.addView(nextButton, nextParams);
        
        container.addView(controlsContainer);
        
        // Make entire mini player clickable
        container.setOnClickListener(v -> {
            if (listener != null) listener.onMiniPlayerClick();
        });
        
        addView(container, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ));
    }
    
    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurEnabled && noiseSuppressor != null) {
            drawBlurBackground(canvas);
        } else {
            // Fallback for older versions or when blur is disabled
            android.graphics.Path path = new android.graphics.Path();
            android.graphics.RectF rect = new android.graphics.RectF(0, 0, getWidth(), getHeight());
            path.addRoundRect(rect, dp(cornerRadius), dp(cornerRadius), android.graphics.Path.Direction.CW);
            canvas.save();
            canvas.clipPath(path);
            canvas.drawColor(backgroundColor);
            canvas.restore();
        }
        
        super.dispatchDraw(canvas);
    }
    
    @RequiresApi(api = Build.VERSION_CODES.S)
    private void drawBlurBackground(Canvas canvas) {
        if (!canvas.isHardwareAccelerated()) {
            return;
        }
        
        // Setup render nodes if needed
        if (noiseSuppressor.getRenderNodesCount() == 0) {
            java.util.ArrayList<android.graphics.RectF> positions = new java.util.ArrayList<>();
            positions.add(new android.graphics.RectF(0, 0, getWidth(), getHeight()));
            noiseSuppressor.setupRenderNodes(positions, 1);
        }
        
        // Record content to blur
        try {
            android.graphics.RecordingCanvas recordingCanvas = noiseSuppressor.beginRecordingRect(0);
            recordingCanvas.save();
            recordingCanvas.translate(-getLeft() - getTranslationX(), -getTop() - getTranslationY());
            
            View parent = (View) getParent();
            if (parent != null) {
                int oldVisibility = getVisibility();
                setVisibility(View.INVISIBLE);
                parent.draw(recordingCanvas);
                setVisibility(oldVisibility);
            }
            
            recordingCanvas.restore();
            noiseSuppressor.endRecordingRect();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        
        // Clip to rounded rect
        android.graphics.Path clipPath = new android.graphics.Path();
        android.graphics.RectF clipRect = new android.graphics.RectF(0, 0, getWidth(), getHeight());
        clipPath.addRoundRect(clipRect, radii, android.graphics.Path.Direction.CW);
        canvas.save();
        canvas.clipPath(clipPath);
        
        // Draw blurred background
        noiseSuppressor.invalidateResultRenderNodes(getWidth(), getHeight());
        noiseSuppressor.draw(canvas, DownscaleScrollableNoiseSuppressor.DRAW_FROSTED_GLASS);
        
        // Draw overlay
        canvas.drawColor(backgroundColor);
        
        // Draw strokes
        xyz.code.blur3.drawable.BlurredBackgroundDrawable.drawStroke(
            canvas, 0, 0, getWidth(), getHeight(), radii,
            dpf(1), true, paintStrokeTop
        );
        
        xyz.code.blur3.drawable.BlurredBackgroundDrawable.drawStroke(
            canvas, 0, 0, getWidth(), getHeight(), radii,
            dpf(0.67f), false, paintStrokeBottom
        );
        
        canvas.restore();
    }
    
    private void togglePlayPause() {
        isPlaying = !isPlaying;
        playPauseButton.setImageResource(isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
        if (listener != null) {
            listener.onPlayPause(isPlaying);
        }
    }
    
    public void setSongInfo(String title, String artist) {
        songTitle.setText(title);
        artistName.setText(artist);
    }
    
    public void setAlbumArtColor(int color) {
        android.graphics.drawable.GradientDrawable artBg = new android.graphics.drawable.GradientDrawable();
        artBg.setColor(color);
        artBg.setCornerRadius(dp(25)); // Fully rounded
        albumArt.setBackground(artBg);
    }
    
    public void setPlaying(boolean playing) {
        isPlaying = playing;
        playPauseButton.setImageResource(isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
    }
    
    public boolean isPlaying() {
        return isPlaying;
    }
    
    public void setOnMiniPlayerListener(OnMiniPlayerListener listener) {
        this.listener = listener;
    }
    
    public void setBlurEnabled(boolean enabled) {
        this.blurEnabled = enabled;
        invalidate();
    }
    
    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
        invalidate();
    }
    
    public void setCornerRadius(float radius) {
        this.cornerRadius = radius;
        java.util.Arrays.fill(radii, dp(radius));
        invalidate();
    }
    
    public void show() {
        if (getVisibility() == View.VISIBLE) return;
        
        setVisibility(View.VISIBLE);
        setTranslationY(getHeight());
        
        animate()
            .translationY(0)
            .setDuration(300)
            .setInterpolator(new android.view.animation.DecelerateInterpolator())
            .start();
    }
    
    public void hide() {
        if (getVisibility() != View.VISIBLE) return;
        
        animate()
            .translationY(getHeight())
            .setDuration(300)
            .setInterpolator(new android.view.animation.AccelerateInterpolator())
            .withEndAction(() -> setVisibility(View.GONE))
            .start();
    }
    
    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
    
    private float dpf(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
