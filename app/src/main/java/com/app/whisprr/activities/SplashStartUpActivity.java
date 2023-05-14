package com.app.whisprr.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.app.whisprr.R;
import com.app.whisprr.databinding.SplashstartupActivityBinding;

public class SplashStartUpActivity extends AppCompatActivity {
    private SplashstartupActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = SplashstartupActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Show Splash Screen for 5 seconds
        showSplashScreen();
    }

    private void showSplashScreen() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), WelcomeStartUpActivity.class);
                startActivity(intent);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }, 3 * 1000); // Wait for 3 seconds
    }
}
