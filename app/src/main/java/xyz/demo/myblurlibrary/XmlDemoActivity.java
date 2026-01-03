package xyz.demo.myblurlibrary;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class XmlDemoActivity extends AppCompatActivity {

    private NavigationBar navigationBar;
    private TextView statusText;
    private View gradient1, gradient2, gradient3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xml_demo);

        statusText = findViewById(R.id.xmlStatusText);
        navigationBar = findViewById(R.id.navigationBarXml);
        gradient1 = findViewById(R.id.gradient1);
        gradient2 = findViewById(R.id.gradient2);
        gradient3 = findViewById(R.id.gradient3);

        // Tabs are now inflated from menu resource via XML (app:menu attribute)
        // No need for programmatic addTab calls

        // Set badges
        navigationBar.setBadge(1, 3);
        navigationBar.setBadge(2, 12);

        // Set listener
        navigationBar.setOnTabSelectedListener((position, tab) -> {
            statusText.setText("Active Tab: " + tab.label + " (Position: " + position + ")");
            Toast.makeText(this, "Selected: " + tab.label, Toast.LENGTH_SHORT).show();
        });
        
        // Force blur to initialize after layout
        navigationBar.post(() -> {
            navigationBar.invalidate();
            navigationBar.requestLayout();
        });
        
        // Start gradient animations
        startGradientAnimations();
    }
    
    private void startGradientAnimations() {
        // Animate gradient1 - rotation
        ObjectAnimator rotation1 = ObjectAnimator.ofFloat(gradient1, "rotation", 0f, 360f);
        rotation1.setDuration(8000);
        rotation1.setRepeatCount(ValueAnimator.INFINITE);
        rotation1.setInterpolator(new AccelerateDecelerateInterpolator());
        rotation1.start();
        
        // Animate gradient2 - scale pulsing
        ObjectAnimator scaleX2 = ObjectAnimator.ofFloat(gradient2, "scaleX", 1f, 1.3f, 1f);
        ObjectAnimator scaleY2 = ObjectAnimator.ofFloat(gradient2, "scaleY", 1f, 1.3f, 1f);
        scaleX2.setDuration(4000);
        scaleY2.setDuration(4000);
        scaleX2.setRepeatCount(ValueAnimator.INFINITE);
        scaleY2.setRepeatCount(ValueAnimator.INFINITE);
        scaleX2.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY2.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleX2.start();
        scaleY2.start();
        
        // Animate gradient3 - rotation and translation
        ObjectAnimator rotation3 = ObjectAnimator.ofFloat(gradient3, "rotation", 0f, -360f);
        ObjectAnimator translateY3 = ObjectAnimator.ofFloat(gradient3, "translationY", 0f, -30f, 0f);
        rotation3.setDuration(10000);
        translateY3.setDuration(3000);
        rotation3.setRepeatCount(ValueAnimator.INFINITE);
        translateY3.setRepeatCount(ValueAnimator.INFINITE);
        rotation3.setInterpolator(new AccelerateDecelerateInterpolator());
        translateY3.setInterpolator(new AccelerateDecelerateInterpolator());
        rotation3.start();
        translateY3.start();
    }
}
