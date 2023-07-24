package com.app.whisprr.fragments;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.app.whisprr.R;
import com.app.whisprr.activities.Constants;
import com.app.whisprr.databinding.FragmentCreateProfilePhonesetupBinding;
import com.app.whisprr.utilities.PreferenceManager;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateProfilePhoneSetupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateProfilePhoneSetupFragment extends Fragment {
    private FragmentCreateProfilePhonesetupBinding binding;
    PreferenceManager preferenceManager;
    private String inputNumberCountryCode;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CreateProfilePhoneSetupFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CreateProfilePhoneSetup.
     */
    // TODO: Rename and change types and number of parameters
    public static CreateProfilePhoneSetupFragment newInstance(String param1, String param2) {
        CreateProfilePhoneSetupFragment fragment = new CreateProfilePhoneSetupFragment();
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
        binding = FragmentCreateProfilePhonesetupBinding.inflate(inflater, container, false);
        preferenceManager = new PreferenceManager(requireContext());
        setListeners();
        return binding.getRoot();
    }

    public void setListeners() {
        binding.ccp.setOnCountryChangeListener(() -> {
            inputNumberCountryCode = binding.ccp.getSelectedCountryCode();
        });
        binding.btnPhonenumsetup.setOnClickListener(
                v -> {
                   saveNumber();
                }
        );
    }

    public void saveNumber() {
        String inputNumber = binding.inputNumber.getText().toString();
        Log.w(TAG, (inputNumberCountryCode + inputNumber));
        if(inputNumberCountryCode != null && inputNumber != null){
            if(isPhoneNumberValid(inputNumber, inputNumberCountryCode)){
                Toast.makeText(getContext(), "Valid Number", Toast.LENGTH_SHORT).show();

                String fullSignUpPhoneNumber = "+"+inputNumberCountryCode + inputNumber;
                preferenceManager.putString(Constants.KEY_SIGNUPEMAIL, fullSignUpPhoneNumber);

                Intent intent = new Intent(Constants.BROADCAST_ACTION_NEXT_PAGE);
                requireActivity().sendBroadcast(intent);
            } else {
                Toast.makeText(getContext(), "You have entered Invalid number please try again", Toast.LENGTH_SHORT).show();
                binding.inputNumber.setText("");


            }

        } else {
            Toast.makeText(getContext(), "Please ensure entered number and selected country code", Toast.LENGTH_SHORT).show();

        }
    }

    public boolean isPhoneNumberValid(String phoneNumber, String countryCode) {
        // NOTE: This should probably be a member variable.
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

        try {
            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(phoneNumber, countryCode);
            return phoneUtil.isValidNumber(numberProto);
        } catch (NumberParseException e) {
            System.err.println("NumberParseException was thrown: " + e.toString());
        }

        return false;
    }
}