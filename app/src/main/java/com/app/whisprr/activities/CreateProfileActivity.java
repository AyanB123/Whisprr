package com.app.whisprr.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.app.whisprr.R;
import com.app.whisprr.adapter.CreateProfileAdapter;
import com.app.whisprr.databinding.ActivityCreateProfileBinding;
import com.app.whisprr.interfaces.CreateProfileNextPageButtonClickListener;
import com.app.whisprr.utilities.PreferenceManager;
import com.google.firebase.firestore.auth.User;

public class CreateProfileActivity extends AppCompatActivity implements CreateProfileNextPageButtonClickListener {
    ActivityCreateProfileBinding binding;
    private PreferenceManager preferenceManager;
    private String verifiedSignUpEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Register the broadcast receiver
        IntentFilter filter = new IntentFilter(Constants.BROADCAST_ACTION_NEXT_PAGE);
        registerReceiver(nextPageReceiver, filter);


        preferenceManager = new PreferenceManager(getApplicationContext());
        loadVerEmailDetails();
        setPagerAndAdapter();
    }
    private void setPagerAndAdapter() {
        CreateProfileAdapter adapter = new CreateProfileAdapter(getSupportFragmentManager(), getLifecycle());
        adapter.setNextPageClickListener(this);
        binding.viewpagermain.setAdapter(adapter);
    }

    private void loadVerEmailDetails() {
        verifiedSignUpEmail = getIntent().getStringExtra("email");
        preferenceManager.putString(Constants.KEY_SIGNUPEMAIL,   verifiedSignUpEmail);


    }

    private BroadcastReceiver nextPageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.BROADCAST_ACTION_NEXT_PAGE)) {
                // Navigate to the next page in the ViewPager
                int nextPage = binding.viewpagermain.getCurrentItem() + 1;
                binding.viewpagermain.setCurrentItem(nextPage, true);
            }
        }
    };



    @Override
    public void GoToNextPage() {
        ViewPager2 viewPager = binding.viewpagermain;
        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(nextPageReceiver);
    }
}
