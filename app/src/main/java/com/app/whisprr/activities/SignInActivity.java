package com.app.whisprr.activities;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.Window;
import android.widget.Toast;

import com.app.whisprr.R;
import com.app.whisprr.databinding.ActivityWelcomeStartUpBinding;
import com.app.whisprr.utilities.PreferenceManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.app.whisprr.databinding.ActivitySignInBinding;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Objects;


public class SignInActivity extends AppCompatActivity {
    ActivitySignInBinding binding;
    FirebaseFirestore fireStore;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseAuth mAuth;
    PreferenceManager preferenceManager;


    String userID;

    Dialog googleAuthDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setFirebaseInstances();
        setListeners();
    }

    private void setListeners() {
        binding.btnContinueWithGoogle.setOnClickListener(v ->
                GoogleAuthSignIn()
        );


        binding.registerbtn.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, CreateProfileActivity.class);
            startActivity(intent);
        });

        binding.btnSignIn.setOnClickListener(v -> {
            signInValid();
        });
    }
    private void setFirebaseInstances() {
        fireStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    private void signInValid(){
        String email = binding.inputEmail.getText().toString().trim();
        String password = binding.inputPassword.getText().toString().trim();

        // use TextUtils to ensure handle emptry stirng and null string cases
        if (TextUtils.isEmpty(email)) {
            binding.inputEmail.setError("Please enter your email");
            return;
        }

        if (password.isEmpty()) {
            binding.inputPassword.setError("Please enter your password");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>(){
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(), "Login Accepted", Toast.LENGTH_SHORT).show();

                    // get userid from firebase for user
                    userID = mAuth.getCurrentUser().getUid();

                    FirebaseFirestore database = FirebaseFirestore.getInstance();
                    database.collection(Constants.KEY_COLLECTION_USERS)
                            .document(userID)
                            .get()
                            .addOnCompleteListener(task2 -> {
                                if (task2.isSuccessful() && task2.getResult() != null) {
                                    DocumentSnapshot documentSnapshot = task2.getResult();
                                    if (documentSnapshot.exists()) {
                                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                                        preferenceManager.putString(Constants.KEY_USER_ID, userID);
                                        preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                                        preferenceManager.putString(Constants.KEY_EMAIL, documentSnapshot.getString(Constants.KEY_EMAIL));
                                        preferenceManager.putString(Constants.KEY_USERNAME, documentSnapshot.getString(Constants.KEY_USERNAME));
                                        preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                                        preferenceManager.putString(Constants.KEY_NUMBER, documentSnapshot.getString(Constants.KEY_IMAGE));

                                        preferenceManager.putBoolean(Constants.KEY_ISSYNCED_CONTACTS,false);

                                        Intent intent = new Intent(getApplicationContext(),  AddContactsActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    } else {

                                        Toast.makeText(getApplicationContext(), "Cannot be signed in1! ", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), "Cannot be signed in2! ", Toast.LENGTH_SHORT).show();
                                }
                            });





                   /* Intent intent = new Intent(getApplicationContext(), AddContactsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent); */



                }else{
                    Toast.makeText(getApplicationContext(), "Cannot be signed in! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }



            }

        });





}

    private void GoogleAuthSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // default_web_client_id is the web client id generated automatically by Firebase
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleAuthDialog = new Dialog(SignInActivity.this);
        googleAuthDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        googleAuthDialog.setContentView(R.layout.dialog_googleauth);
        googleAuthDialog.setCanceledOnTouchOutside(false);

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }


    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        try {
                            // Google Sign In was successful, check if email exists in Firebase
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            String googleIdToken = account.getIdToken();
                            String email = account.getEmail();
                            checkEmailExists(email, googleIdToken);
                        } catch (ApiException e) {
                            // Google Sign In failed, update UI appropriately
                            Log.w(TAG, "Google sign in failed", e);
                            googleAuthDialog.dismiss();
                            // ...
                        }
                    }
                }
            }
    );

    private void checkEmailExists(String email, String googleIdToken) {
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        SignInMethodQueryResult result = task.getResult();
                        if (result.getSignInMethods().isEmpty()) {
                            // Email does not exist, create a new account
                            // Proceed with desired actions for a new account creation
                            // For example, show a registration form or navigate to the registration activity
                            // ...

                            // After creating the account, sign in with Google
                            signInWithGoogle(googleIdToken);
                        } else {
                            // Email exists, proceed with sign-in using Google
                            signInWithGoogle(googleIdToken);
                        }
                    } else {
                        // Failed to check if email exists
                        Toast.makeText(SignInActivity.this, "Failed to check if email exists.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void signInWithGoogle(String googleIdToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(googleIdToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign-in with Google successful
                            FirebaseUser user = mAuth.getCurrentUser();
                            // Proceed with desired actions for a signed-in user
                            // For example, navigate to the home activity or update UI

                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);

                        } else {
                            // Sign-in with Google failed
                            Toast.makeText(SignInActivity.this, "Google sign-in failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void firebaseAuthWithGoogle(String idToken){
        AuthCredential credential = GoogleAuthProvider.getCredential( idToken,null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, dismiss dialog and update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");







                    }else{
                        Toast.makeText(getApplicationContext(), "Cannot be signed in! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();



                    }
                });


    }

}