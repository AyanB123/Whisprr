package com.app.whisprr.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.app.whisprr.R;
import com.app.whisprr.databinding.ActivityWelcomeStartUpBinding;
import com.google.firebase.auth.FirebaseAuth;

public class WelcomeStartUpActivity extends AppCompatActivity {
    ActivityWelcomeStartUpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWelcomeStartUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }

    private void setListeners() {
        binding.btnContinue.setOnClickListener(v ->
                progressIndicator()
               );
    }

    private void loading(Boolean isLoading){
        if(isLoading){
            binding.welcomeCardContent.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.welcomeCardContent.setVisibility(View.VISIBLE);

        }

    }

    private void progressIndicator() {
        loading(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                startActivity(new Intent(getApplicationContext(), SignInActivity.class));

            }
        }, 2 * 1000); // Wait for 3 seconds
    }
}