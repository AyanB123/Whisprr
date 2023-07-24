package com.app.whisprr.activities;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.app.whisprr.R;
import com.app.whisprr.databinding.ActivityVerifyEmailBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class VerifyEmailActivity extends AppCompatActivity {
    ActivityVerifyEmailBinding binding;
    private  FirebaseFirestore database;
    FirebaseAuth mAuth;
    private Handler verifiedhandler;
    private Runnable verifiedrunnable;
    private String verifiedEmail;


    public HashMap<String, String> usedUserEmails = new HashMap<>();

    // index 0 for emailvalid, index 1 for emailexist
    public List<Boolean> isEmailValidUsed =  Arrays.asList(false, false);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVerifyEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initialSetup();
        setListeners();
        getUsedEmail();
        listenForExistingEmails();


    }
    private void initialSetup() {
        database = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    private void getUsedEmail(){
        //get all list emails for all users signed up in app

        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null){
                        for(QueryDocumentSnapshot document : task.getResult()){

                            // ensure user has email field and it is not null, store documentid and email easy update list for changes in users in app
                            if(document.contains(Constants.KEY_EMAIL) ){
                                String documentId = document.getId();
                                String email = document.getString(Constants.KEY_EMAIL);
                                usedUserEmails.put(documentId, email);

                            }


                        }


                    }else{
                        Toast.makeText(getApplicationContext(), "Could not get existing users", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    public void listenForExistingEmails(){

        // listener handle user creating or deleting accounts which need to update our list of email being used for app
        database.collection(Constants.KEY_COLLECTION_USERS)
                .addSnapshotListener(VerifyEmailActivity.this,(value,error) -> {
                    if(error != null){
                        return;
                    }
                    if(value != null){
                        for(DocumentChange documentChange : value.getDocumentChanges()){

                            String documentId = documentChange.getDocument().getId();

                            // handle new user account create which need add new email to our list
                            if(documentChange.getType() == DocumentChange.Type.ADDED){
                                // ensure email not been already added to list
                                if(!usedUserEmails.containsKey(documentId)){
                                    // ensure doucment contain email feild and field is not null
                                    if(documentChange.getDocument().contains(Constants.KEY_EMAIL) && documentChange.getDocument().get(Constants.KEY_EMAIL) != null){
                                        String email = documentChange.getDocument().getString(Constants.KEY_EMAIL);
                                        usedUserEmails.put(documentId, email);
                                    }
                                }else if(documentChange.getType() == DocumentChange.Type.REMOVED){
                                    // handle deleted user account delete which need remove email from our list
                                    if(usedUserEmails.containsKey(documentId)){
                                        usedUserEmails.remove(documentId);
                                    }
                                }

                            }
                        }
                        // Call checkEmailAvailable after the email list is updated
                        checkEmailAvailable(binding.inputEmail.getText().toString());
                    }


                });

    }
    private void setListeners() {



        binding.inputEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkinputEmailValid(s.toString());

                // Check if email is already used
                checkEmailAvailable(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.imageBack.setOnClickListener(v -> {
            deleteAnonymousAccount();
            onBackPressed();
        });
        binding.btnEmailAlreadyver.setOnClickListener(v -> {
            startProfileSignUpActivity();
        });
        binding.btnEnterEmail.setOnClickListener(v ->
                finalverifyEmail());
    }
    @Override
    protected void onResume() {
        super.onResume();
        getUsedEmail();
    }

    private boolean isEmailUsed(String email) {
        return usedUserEmails.containsValue(email);
    }

    private void checkinputEmailAvailability(String email) {
        if (isEmailUsed(email)) {
            binding.inputEmail.setError("Email already in use");
        }
    }

    private Boolean issendVerificationCodeSuccess(String email){
        /**** Citation Start ********************************
         * How to send verification email wihout creating accoutn for that email
         * Link: https://stackoverflow.com/questions/48464230/firebase-email-verification-without-creating-and-account
         * Username: NickPr
         * User Profile Link: https://stackoverflow.com/users/5457692/nickpr
         */
        final boolean[] isverEmailSent = {false};

        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInAnonymously:success");
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            if (currentUser != null && !currentUser.isEmailVerified()) {
                                mAuth.getCurrentUser().updateEmail(email);
                                mAuth.getCurrentUser().sendEmailVerification();
                                isverEmailSent[0] = true;
                                startEmailBeenVerified();
                                Log.e(TAG, "mail sent.....................................");
                            }else{
                                Log.d(TAG, "signInAnonymously:delveremaillmao");
                                startEmailAlreadyBeenVerified();
                            }

                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInAnonymously:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });


        /****** Citation End********/
        return isverEmailSent[0];


    }
    private void finalverifyEmail(){
        String enteredEmail = binding.inputEmail.getText().toString().trim();
        loading(true,binding.progressBarEnteremail,binding.btnEnterEmail);
        if(Patterns.EMAIL_ADDRESS.matcher(enteredEmail).matches() && !isEmailUsed(enteredEmail)){
            Log.w(TAG, "cehckpoint1");
            // final detailed verification of email where we go query in actual database for email address
            database.collection(Constants.KEY_COLLECTION_USERS)
                    .whereEqualTo(Constants.KEY_EMAIL, enteredEmail)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.w(TAG, "cehckpoint2");
                            if (task.getResult().isEmpty() || task.getResult() == null) {
                                // Email not found, good and we can go to nest screeen
                                // ...
                                if(issendVerificationCodeSuccess(enteredEmail)){
                                    Log.w(TAG, "cehckpoint3");
                                    verifiedEmail = enteredEmail;
                                    startEmailBeenVerified();
                                }



                            } else {
                                loading(false,binding.progressBarEnteremail,binding.btnEnterEmail);
                                Toast.makeText(getApplicationContext(), "Email in Use or Not Valid", Toast.LENGTH_SHORT).show();
                                binding.inputEmail.setText("");
                                hideKeyboard(VerifyEmailActivity.this);

                                                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Could not get existing users", Toast.LENGTH_SHORT).show();
                            binding.inputEmail.setText("");
                            hideKeyboard(VerifyEmailActivity.this);

                        }
                    });


        }else{
            loading(false,binding.progressBarEnteremail,binding.btnEnterEmail);
            Toast.makeText(getApplicationContext(), "Error check if Email used try again", Toast.LENGTH_SHORT).show();
            binding.inputEmail.setText("");
            hideKeyboard(VerifyEmailActivity.this);


        }





    }
    private void   startEmailAlreadyBeenVerified(){
        binding.enterEmailContainer.setVisibility(View.GONE);
        binding.emailAlreadyverContainer.setVisibility(View.VISIBLE);
        deleteAnonymousAccount();
        Intent intent = new Intent(getApplicationContext(), CreateProfileActivity.class);
        intent.putExtra("email", verifiedEmail); // Pass the email string with the intent
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void  startEmailBeenVerified(){
        Log.w(TAG, "cehckpoint4");
        binding.enterEmailContainer.setVisibility(View.GONE);
        binding.verifyEmailContainer.setVisibility(View.VISIBLE);
        binding.progressBarVerifyemail.setVisibility(View.VISIBLE);
        startEmailVerificationCheck();


    }
        private void startEmailVerificationCheck() {
            verifiedhandler = new Handler();
            verifiedrunnable = new Runnable() {
                @Override
                public void run() {
                    // Refresh the user object
                    mAuth.getCurrentUser().reload()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    if(mAuth.getCurrentUser().isEmailVerified()){
                                        Log.w(TAG, "yayyyyyy");
                                        binding.progressBarVerifyemail.setVisibility(View.GONE);
                                        binding.emailverifedAnimation.addAnimatorListener(new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationStart(@NonNull Animator animation, boolean isReverse) {
                                                super.onAnimationStart(animation, isReverse);

                                            }

                                            @Override
                                            public void onAnimationEnd(@NonNull Animator animation, boolean isReverse) {
                                                super.onAnimationEnd(animation, isReverse);
                                                startProfileSignUpActivity();
                                            }
                                        });
                                        binding.emailverifedAnimation.setVisibility(View.VISIBLE);
                                        binding.emailverifedAnimation.setAnimation("check_animation.json");
                                        binding.emailverifedAnimation.setSpeed(0.75f);
                                        binding.emailverifedAnimation.setRepeatCount(0); // Set the number of animation repetitions (0 for no loop)
                                        binding.emailverifedAnimation.playAnimation();



                                    }else{
                                        Log.w(TAG, "nooooooo");
                                        verifiedhandler.postDelayed(verifiedrunnable, 1000); // Repeat the check after 1 second

                                    }
                                }
                            });

                }
            };

            // Start the initial check
            verifiedhandler.postDelayed(verifiedrunnable, 1000); // Start checking after 1 second
        }

    // check if  inputemail is valid email, set correct states of its rule property
    private void checkinputEmailValid(String email) {
        if (TextUtils.isEmpty(email)) {
            // Set the icon and text view to the off state if they are not already set
            if (binding.validIcon.getDrawable() != ContextCompat.getDrawable(this, R.drawable.ic_lock)) {
                binding.validIcon.setImageResource(R.drawable.ic_lock);
            }
            if (binding.validText.getCurrentTextColor() != ContextCompat.getColor(this, R.color.secondary_white)) {
                binding.validText.setTextColor(ContextCompat.getColor(this, R.color.secondary_white));
            }
            isEmailValidUsed.set(0, false);
        } else if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // Set the icon and text view to the valid state if they are not already set
            if (binding.validIcon.getDrawable() != ContextCompat.getDrawable(this, R.drawable.ic_check)) {
                binding.validIcon.setImageResource(R.drawable.ic_check);
            }
            if (binding.validText.getCurrentTextColor() != ContextCompat.getColor(this, R.color.valid)) {
                binding.validText.setTextColor(ContextCompat.getColor(this, R.color.valid));
            }
            isEmailValidUsed.set(0, true);
        } else {
            // Set the icon and text view to the invalid state if they are not already set
            if (binding.validIcon.getDrawable() != ContextCompat.getDrawable(this, R.drawable.ic_error)) {
                binding.validIcon.setImageResource(R.drawable.ic_error);
            }
            if (binding.validText.getCurrentTextColor() != ContextCompat.getColor(this, R.color.invalid)) {
                binding.validText.setTextColor(ContextCompat.getColor(this, R.color.invalid));
            }
            isEmailValidUsed.set(0, false);
        }
        // Set the button state
        setButtonEnterEmailState();
    }

    private void  deleteAnonymousAccount(){
        FirebaseUser delCurAnomUser = mAuth.getCurrentUser();
        if (delCurAnomUser != null && delCurAnomUser.isAnonymous()) {
            // Delete the anonymous account
            delCurAnomUser.delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Account deletion successful
                            // You can perform any necessary actions after deleting the account
                            Log.d(TAG, "anomuser:deleted");
                        } else {
                            // Account deletion failed
                            Exception exception = task.getException();
                            Log.d(TAG, "anomuser:failed-deleted");
                            // Handle the exception accordingly
                        }
                    });
        } else {
            // User is not anonymous or currentUser is null
            // Handle the case accordingly
        }

    }

    private void startProfileSignUpActivity(){
        deleteAnonymousAccount();
        Intent intent = new Intent(getApplicationContext(), CreateProfileActivity.class);
        intent.putExtra("email", verifiedEmail); // Pass the email string with the intent
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
    public void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }




    private void checkEmailAvailable(String email) {
        if (TextUtils.isEmpty(email)) {
            // Set the icon and text view to the off state if they are not already set
            if (binding.notinuseIcon.getDrawable() != ContextCompat.getDrawable(this, R.drawable.ic_lock)) {
                binding.notinuseIcon.setImageResource(R.drawable.ic_lock);
            }
            if (binding.notinuseText.getCurrentTextColor() != ContextCompat.getColor(this, R.color.secondary_white)) {
                binding.notinuseText.setTextColor(ContextCompat.getColor(this, R.color.secondary_white));
            }
            isEmailValidUsed.set(1, false);
        } else if (isEmailUsed(email)) {
            // Set the icon and text view to the invalid state if they are not already set
            if (binding.notinuseIcon.getDrawable() != ContextCompat.getDrawable(this, R.drawable.ic_error)) {
                binding.notinuseIcon.setImageResource(R.drawable.ic_error);
            }
            if (binding.notinuseText.getCurrentTextColor() != ContextCompat.getColor(this, R.color.invalid)) {
                binding.notinuseText.setTextColor(ContextCompat.getColor(this, R.color.invalid));
            }
            isEmailValidUsed.set(1, false);
        } else {
            // Set the icon and text view to the valid state if they are not already set
            if (binding.notinuseIcon.getDrawable() != ContextCompat.getDrawable(this, R.drawable.ic_check)) {
                binding.notinuseIcon.setImageResource(R.drawable.ic_check);
            }
            if (binding.notinuseText.getCurrentTextColor() != ContextCompat.getColor(this, R.color.valid)) {
                binding.notinuseText.setTextColor(ContextCompat.getColor(this, R.color.valid));
            }
            isEmailValidUsed.set(1, true);
        }

        setButtonEnterEmailState();
    }

    private void loading(Boolean isLoading, ProgressBar loadingBar, MaterialButton pressedButton){
        if(isLoading){
            pressedButton.setVisibility(View.INVISIBLE);
            loadingBar.setVisibility(View.VISIBLE);
        }else{
            loadingBar.setVisibility(View.INVISIBLE);
            pressedButton.setVisibility(View.VISIBLE);

        }
    }


    // set button state based on the state of the email input and validity for first screen when user enter their email
    private void setButtonEnterEmailState() {
        boolean isButtonEnabled = isEmailValidUsed.get(0) && isEmailValidUsed.get(1);

        if (isButtonEnabled) {
            binding.btnEnterEmail.setEnabled(true);
            binding.btnEnterEmail.setTextColor(ContextCompat.getColor(this, R.color.primary_purple));
            binding.btnEnterEmail.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary_purple)));
        } else {
            binding.btnEnterEmail.setEnabled(false);
            binding.btnEnterEmail.setTextColor(ContextCompat.getColor(this, R.color.secondary_white));
            binding.btnEnterEmail.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.secondary_white)));
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the recurring check when the activity is destroyed
        if (verifiedhandler != null && verifiedrunnable != null) {
            verifiedhandler.removeCallbacks(verifiedrunnable);
        }
    }




    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {

        if(error != null){
            return;
        }
        if(value != null){
            int count = usedUserEmails.size();
        }
    };
}