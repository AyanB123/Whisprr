package com.app.whisprr.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app.whisprr.R;
import com.app.whisprr.activities.Constants;
import com.app.whisprr.databinding.FragmentCreateProfileStartBinding;
import com.app.whisprr.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateProfileStartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateProfileStartFragment extends Fragment {
    private DocumentReference documentReference;
    PreferenceManager preferenceManager;

    private FragmentCreateProfileStartBinding binding;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private  String verifiedSignUpEmail;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CreateProfileStartFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CreateProfileStartFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CreateProfileStartFragment newInstance(String param1, String param2) {
        CreateProfileStartFragment fragment = new CreateProfileStartFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentCreateProfileStartBinding.inflate(inflater, container, false);
        preferenceManager = new PreferenceManager(requireContext());
        setUserSignUpEmail();
        setListeners();
        return binding.getRoot();

    }
    public void setUserSignUpEmail(){
        verifiedSignUpEmail = preferenceManager.getString("SIGNUP_EMAIL");
        binding.profilesignupemailText.setText(verifiedSignUpEmail);
    }

    private void setListeners(){
        binding.btnSetupprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Constants.BROADCAST_ACTION_NEXT_PAGE);
                requireActivity().sendBroadcast(intent);
            }
        });
    }
}