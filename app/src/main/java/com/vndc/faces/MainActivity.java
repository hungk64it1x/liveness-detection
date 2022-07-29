package com.vndc.faces;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.vndc.faces.RealTimeFaceDetection.RealTimeFaceDetectionActivity;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static DrawerLayout drawerLayout;
    private static ActionBarDrawerToggle actionBarDrawerToggle;
    private ImageButton identify;
    private Button btShowGuideline;


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        identify = (ImageButton) findViewById(R.id.Indentify);
        btShowGuideline = (Button) findViewById(R.id.btShowGuideline);
        Animation start_rotate = AnimationUtils.loadAnimation(this, R.anim.anim_rotate);
        identify.startAnimation(start_rotate);
        identify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchActivity(RealTimeFaceDetectionActivity.class);
            }
        });

    }


    private void switchActivity(Class c) {
        Intent intent = new Intent(this, c);
        this.startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }
}