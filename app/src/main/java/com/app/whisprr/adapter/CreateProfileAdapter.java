package com.app.whisprr.adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.app.whisprr.fragments.CreateProfileDisplayInfoSetupFragment;
import com.app.whisprr.fragments.CreateProfilePhoneSetupFragment;
import com.app.whisprr.fragments.CreateProfileStartFragment;
import com.app.whisprr.fragments.CreateProfileUsernameSetupFragment;
import com.app.whisprr.interfaces.CreateProfileNextPageButtonClickListener;

public class CreateProfileAdapter extends FragmentStateAdapter {
    private CreateProfileNextPageButtonClickListener nextPageButtonClickListener;

    public CreateProfileAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
            if(position ==0){
                return new CreateProfileStartFragment();
            }
            else if(position ==1){
                return new CreateProfilePhoneSetupFragment();
            }
            else if(position ==2){
                return new CreateProfileUsernameSetupFragment();
            }
            else{
                return new CreateProfileDisplayInfoSetupFragment();
            }

    }

    public void setNextPageClickListener(CreateProfileNextPageButtonClickListener listener) {
        nextPageButtonClickListener = listener;
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
