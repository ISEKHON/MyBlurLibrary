package xyz.demo.myblurlibrary;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import xyz.code.blur3.util.AndroidUtilities;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidUtilities.checkDisplaySize(this);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);
        
        Button button = new Button(this);
        button.setText("Open Blur Demo");
        button.setOnClickListener(v -> {
            startActivity(new Intent(this, BlurDemoActivity.class));
        });
        
        Button navButton = new Button(this);
        navButton.setText("Open Navigation Bar Demo");
        navButton.setOnClickListener(v -> {
            startActivity(new Intent(this, NavDemoActivity.class));
        });
        
        Button xmlButton = new Button(this);
        xmlButton.setText("Open XML Demo");
        xmlButton.setOnClickListener(v -> {
            startActivity(new Intent(this, XmlDemoActivity.class));
        });
        
        Button musicButton = new Button(this);
        musicButton.setText("Open Music Demo");
        musicButton.setOnClickListener(v -> {
            startActivity(new Intent(this, DemoMusicActivity.class));
        });
        
        Button gradientButton = new Button(this);
        gradientButton.setText("Open Animated Gradient Demo");
        gradientButton.setOnClickListener(v -> {
            startActivity(new Intent(this, AnimatedGradientActivity.class));
        });
        
        layout.addView(button);
        layout.addView(navButton);
        layout.addView(xmlButton);
        layout.addView(musicButton);
        layout.addView(gradientButton);
        setContentView(layout);
    }
}
