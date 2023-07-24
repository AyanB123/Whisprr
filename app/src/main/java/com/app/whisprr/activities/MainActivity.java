package com.app.whisprr.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.app.whisprr.R;
import com.app.whisprr.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    FirebaseFirestore fireStore;
    FirebaseAuth mAuth;
    String loggedInuserID;

    FirebaseUser loggedInUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater()); // inflate layout
        setContentView(binding.getRoot()); // set layout as activity content
        getUserData();
        setFirebaseData();
        checkUserEmailVerification();

    }

    private void setFirebaseData() {
        // Initialize Firebase Auth and Firestore instance
        mAuth = FirebaseAuth.getInstance();
        fireStore = FirebaseFirestore.getInstance();


        //get current user and user id
        loggedInuserID = mAuth.getCurrentUser().getUid();
        loggedInUser = mAuth.getCurrentUser();

    }

    private void checkUserEmailVerification() {
        if (!loggedInUser.isEmailVerified()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            builder.setTitle("Email Not Verified!");

            builder.setMessage("Please verify your email address to continue using the app. Press Button Below to send verification email again.");

            builder.setNegativeButton("Resend", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    loggedInUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(MainActivity.this, "Verification Email has been sent, check inbox please", Toast.LENGTH_SHORT).show();
                            if (loggedInUser.isEmailVerified()) {
                                Toast.makeText(MainActivity.this, "Email Verified, now use app", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }

                    }).addOnFailureListener(new OnFailureListener() {

                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, "Verification Email could not be  sent, check inbox please", Toast.LENGTH_SHORT).show();

                        }
                    });
                }


            });


            AlertDialog diag = builder.create();

            //Display the message!
            diag.show();
        }


    }

    public void getUserData() {
        mAuth = FirebaseAuth.getInstance();
        fireStore = FirebaseFirestore.getInstance();


        //get current user and user id
        loggedInuserID = mAuth.getCurrentUser().getUid();
        loggedInUser = mAuth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Firebase add user
        final String TAG = "Sample";
        db.collection("users")
                .document(loggedInuserID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String email = documentSnapshot.getString("email");
                            String number = documentSnapshot.getString("number");
                            String name = documentSnapshot.getString("name");
                            String username = documentSnapshot.getString("username");
                            String profileImage = documentSnapshot.getString("profileimage");

                            // Use the retrieved values as needed
                            Log.d(TAG, "Email: " + email);
                            Log.d(TAG, "Number: " + number);
                            Log.d(TAG, "Name: " + name);
                            Log.d(TAG, "Username: " + username);
                            Log.d(TAG, "Profile Image: " + profileImage);
                            binding.textName.setText(name);
                           // byte[] bytes = Base64.decode(profileImage, Base64.DEFAULT);
                            //Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            //binding.imageProfile.setImageBitmap(bitmap);
                        } else {
                            Log.d(TAG, "Document does not exist!");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Failed to retrieve data: " + e.toString());
                    }
                });


    }



}