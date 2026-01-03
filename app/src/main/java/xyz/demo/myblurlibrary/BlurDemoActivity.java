package xyz.demo.myblurlibrary;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.InputStream;
import java.util.ArrayList;

import xyz.code.blur3.DownscaleScrollableNoiseSuppressor;
import xyz.code.blur3.util.AndroidUtilities;

/**
 * Demo Activity showcasing the Blur3 library effects
 */
public class BlurDemoActivity extends Activity {
    
    private static final int PICK_IMAGE_REQUEST = 1;

    private ListView listView;
    private LinearLayout messageContainer;
    private DemoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Detect dark mode
        boolean isDarkMode = (getResources().getConfiguration().uiMode & 
            android.content.res.Configuration.UI_MODE_NIGHT_MASK) == 
            android.content.res.Configuration.UI_MODE_NIGHT_YES;

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(isDarkMode ? Color.parseColor("#121212") : Color.parseColor("#FFFFFF"));

        // Create scrollable content
        adapter = new DemoAdapter();
        listView = new ListView(this);
        listView.setAdapter(adapter);
        listView.setPadding(0, 0, 0, dp(100));
        listView.setClipToPadding(false);
        root.addView(listView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        // Create floating message container
        messageContainer = createMessageContainer();

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM
        );
        params.setMargins(dp(16), 0, dp(16), dp(16));
        root.addView(messageContainer, params);

        // Apply blur effect if supported
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            root.post(() -> applyBlurEffect(messageContainer));
        }

        setContentView(root);
    }

    private LinearLayout createMessageContainer() {
        boolean isDarkMode = (getResources().getConfiguration().uiMode & 
            android.content.res.Configuration.UI_MODE_NIGHT_MASK) == 
            android.content.res.Configuration.UI_MODE_NIGHT_YES;

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setBackgroundColor(Color.TRANSPARENT);
        container.setPadding(dp(16), dp(12), dp(16), dp(12));
        container.setGravity(Gravity.CENTER_VERTICAL);

        // Add image picker button
        Button pickImageButton = new Button(this);
        pickImageButton.setText("📷");
        pickImageButton.setTextSize(18);
        pickImageButton.setPadding(dp(12), dp(8), dp(12), dp(8));
        pickImageButton.setBackground(null);
        pickImageButton.setOnClickListener(v -> openImagePicker());
        
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonParams.setMargins(0, 0, dp(8), 0);
        container.addView(pickImageButton, buttonParams);

        TextView textView = new TextView(this);
        textView.setText("Tap 📷 to add images");
        textView.setTextColor(isDarkMode ? Color.WHITE : Color.BLACK);
        textView.setTextSize(16);
        container.addView(textView, new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
        ));

        return container;
    }
    
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    if (bitmap != null) {
                        adapter.addImage(bitmap);
                        Toast.makeText(this, "Image added!", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void applyBlurEffect(LinearLayout container) {
        // Detect dark mode
        boolean isDarkMode = (getResources().getConfiguration().uiMode & 
            android.content.res.Configuration.UI_MODE_NIGHT_MASK) == 
            android.content.res.Configuration.UI_MODE_NIGHT_YES;

        // Create the downscale suppressor for smooth blur (like Telegram uses)
        final DownscaleScrollableNoiseSuppressor noiseSuppressor =
            new DownscaleScrollableNoiseSuppressor();
        
        // Create paints for glass strokes
        final android.graphics.Paint paintStrokeTop = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        paintStrokeTop.setStyle(android.graphics.Paint.Style.STROKE);
        paintStrokeTop.setStrokeWidth(dpf(1));
        paintStrokeTop.setColor(isDarkMode ? 0x60FFFFFF : 0x60FFFFFF); // White highlight
        
        final android.graphics.Paint paintStrokeBottom = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        paintStrokeBottom.setStyle(android.graphics.Paint.Style.STROKE);
        paintStrokeBottom.setStrokeWidth(dpf(2 / 3f));
        paintStrokeBottom.setColor(isDarkMode ? 0x40FFFFFF : 0x40000000); // Light in dark mode, shadow in light mode
        
        final android.graphics.Paint paintShadow = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        paintShadow.setColor(isDarkMode ? 0x50000000 : 0x30000000);
        
        final float cornerRadius = dp(24);
        final float[] radii = new float[8];
        java.util.Arrays.fill(radii, cornerRadius);
        
        // Set blur as background drawable
        android.graphics.drawable.Drawable blurDrawable = new android.graphics.drawable.Drawable() {
            private boolean isRecording = false;
            private final android.graphics.Path clipPath = new android.graphics.Path();
            private final android.graphics.RectF clipRect = new android.graphics.RectF();
            
            @Override
            public void draw(Canvas canvas) {
                if (!canvas.isHardwareAccelerated()) {
                    return;
                }
                
                if (isRecording) {
                    return;
                }

                // Setup render nodes if needed
                if (noiseSuppressor.getRenderNodesCount() == 0) {
                    java.util.ArrayList<android.graphics.RectF> positions = new java.util.ArrayList<>();
                    android.graphics.Rect bounds = getBounds();
                    positions.add(new android.graphics.RectF(bounds));
                    noiseSuppressor.setupRenderNodes(positions, 1);
                }

                // Record content to blur
                isRecording = true;
                try {
                    android.graphics.RecordingCanvas recordingCanvas = noiseSuppressor.beginRecordingRect(0);
                    recordingCanvas.save();
                    
                    // Translate to capture background behind message container
                    recordingCanvas.translate(-container.getLeft(), -container.getTop());
                    
                    // Draw parent to capture the list content
                    View parentView = (View) container.getParent();
                    if (parentView != null) {
                        int oldVisibility = container.getVisibility();
                        container.setVisibility(View.INVISIBLE);
                        parentView.draw(recordingCanvas);
                        container.setVisibility(oldVisibility);
                    }
                    
                    recordingCanvas.restore();
                    noiseSuppressor.endRecordingRect();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    isRecording = false;
                }

                // Clip canvas to rounded rectangle shape
                android.graphics.Rect bounds = getBounds();
                clipRect.set(bounds);
                clipPath.reset();
                clipPath.addRoundRect(clipRect, radii, android.graphics.Path.Direction.CW);
                canvas.save();
                canvas.clipPath(clipPath);

                // Draw the blurred result
                noiseSuppressor.invalidateResultRenderNodes(bounds.width(), bounds.height());
                noiseSuppressor.draw(canvas, DownscaleScrollableNoiseSuppressor.DRAW_FROSTED_GLASS);

                // Draw semi-transparent background (adapt to theme)
                canvas.drawColor(isDarkMode ? 0x4D1E1E1E : 0x4DFFFFFF);
                
                // Draw glass strokes
                xyz.code.blur3.drawable.BlurredBackgroundDrawable.drawStroke(
                    canvas, 0, 0, bounds.width(), bounds.height(), radii,
                    dpf(1), true, paintStrokeTop
                );
                
                xyz.code.blur3.drawable.BlurredBackgroundDrawable.drawStroke(
                    canvas, 0, 0, bounds.width(), bounds.height(), radii,
                    dpf(2 / 3f), false, paintStrokeBottom
                );

                canvas.restore();
            }
            
            @Override
            public void setAlpha(int alpha) {}
            
            @Override
            public void setColorFilter(android.graphics.ColorFilter colorFilter) {}
            
            @Override
            public int getOpacity() {
                return android.graphics.PixelFormat.TRANSLUCENT;
            }
        };
        
        container.setBackground(blurDrawable);
        container.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        
        // Update blur when scrolling
        listView.setOnScrollListener(new android.widget.AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(android.widget.AbsListView view, int scrollState) {}
            
            @Override
            public void onScroll(android.widget.AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                container.invalidate();
            }
        });
        
        Toast.makeText(this, "Blur with strokes applied!", Toast.LENGTH_SHORT).show();
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private float dpf(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private class DemoAdapter extends android.widget.BaseAdapter {
        private final ArrayList<Object> items = new ArrayList<>();
        
        public DemoAdapter() {
            // Add initial text items
            items.add("Item 1 - Scroll to see blur update");
            items.add("Item 2 - Background blurs behind message");
            items.add("Item 3 - Frosted glass effect");
            items.add("Item 4 - Real-time blur rendering");
            items.add("Item 5 - Hardware accelerated");
            items.add("Item 6 - Android 12+ required");
            items.add("Item 7 - Keep scrolling...");
            items.add("Item 8 - Blur adapts to content");
            items.add("Item 9 - Smooth and performant");
            items.add("Item 10 - Based on Telegram's blur3");
            items.add("Item 11 - Tap camera button to add images");
            items.add("Item 12 - Images blend with blur effect");
            items.add("Item 13 - Optimized for mobile");
            items.add("Item 14 - Professional quality");
            items.add("Item 15 - End of demo items");
        }
        
        public void addImage(Bitmap bitmap) {
            items.add(bitmap);
            notifyDataSetChanged();
            listView.smoothScrollToPosition(items.size() - 1);
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
        
        @Override
        public int getViewTypeCount() {
            return 2; // Text and Image types
        }
        
        @Override
        public int getItemViewType(int position) {
            return items.get(position) instanceof Bitmap ? 1 : 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Object item = items.get(position);
            
            if (item instanceof Bitmap) {
                // Image view
                ImageView imageView;
                if (convertView instanceof ImageView) {
                    imageView = (ImageView) convertView;
                } else {
                    imageView = new ImageView(BlurDemoActivity.this);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    imageView.setPadding(dp(16), dp(16), dp(16), dp(16));
                    imageView.setAdjustViewBounds(true);
                }
                
                Bitmap bitmap = (Bitmap) item;
                // Scale bitmap to fit width while maintaining aspect ratio
                int targetWidth = parent.getWidth() - dp(32);
                int targetHeight = (int) (bitmap.getHeight() * ((float) targetWidth / bitmap.getWidth()));
                
                imageView.setImageBitmap(bitmap);
                imageView.setLayoutParams(new android.widget.AbsListView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        Math.min(targetHeight, dp(300))
                ));
                
                return imageView;
            } else {
                // Text view
                TextView textView;
                if (convertView instanceof TextView) {
                    textView = (TextView) convertView;
                } else {
                    boolean isDarkMode = (BlurDemoActivity.this.getResources().getConfiguration().uiMode & 
                        android.content.res.Configuration.UI_MODE_NIGHT_MASK) == 
                        android.content.res.Configuration.UI_MODE_NIGHT_YES;

                    textView = new TextView(BlurDemoActivity.this);
                    textView.setPadding(dp(16), dp(16), dp(16), dp(16));
                    textView.setTextSize(16);
                    textView.setTextColor(isDarkMode ? Color.WHITE : Color.BLACK);
                }
                textView.setText((String) item);
                return textView;
            }
        }
    }
}
