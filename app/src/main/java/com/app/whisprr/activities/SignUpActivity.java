package com.app.whisprr.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.app.whisprr.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;


    FirebaseFirestore fireStore;
    FirebaseAuth mAuth;
    String userID;

    private String encodedImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        fireStore = FirebaseFirestore.getInstance();
        setListeners();
    }
    private final ActivityResultLauncher<Intent> galleryResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK){
                    Intent data = result.getData();
                    if(data != null){
                        Uri imageUri = data.getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageProfile.setImageBitmap(bitmap);
                            binding.textAddImage.setVisibility(View.GONE);
                            encodedImage = encodeImage(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private void setListeners() {
        binding.btnSignUp.setOnClickListener(v ->
               signUpValid());


        binding.imageProfile.setOnClickListener(v -> {
            //https://hamzaasif-mobileml.medium.com/android-capturing-images-from-camera-or-gallery-as-bitmaps-using-activityresultlauncher-ad59d3a075e1
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            galleryResultLauncher.launch(intent);
        });
    }
    private String encodeImage(Bitmap bitmap){
        int previewWidth = 150;

        //calculate the height of a preview image based on the aspect ratio of a bitmap image.
        // scale image down while keeping aspect ratio
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
    private void loading(Boolean isLoading){
        if(isLoading){
            binding.btnSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.btnSignUp.setVisibility(View.VISIBLE);

        }
    }

    private void signUpValid(){
            String email = binding.inputEmail.getText().toString().trim();
            String password = binding.inputPassword.getText().toString().trim();
            String confirmPassword =  binding.inputConfirmPassword.getText().toString().trim();
            String number = binding.inputNumber.getText().toString().trim();
            String name = binding.inputName.getText().toString().trim();
            String username = binding.inputUsername.getText().toString().trim();
            if(encodedImage == null){
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
               // return false;
            }else if(TextUtils.isEmpty(name)){
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
                //return false;
            }else if(TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
                //return false;
            }
            else if(TextUtils.isEmpty(number)) {
                Toast.makeText(this, "Please enter your number", Toast.LENGTH_SHORT).show();
                //return false;
            }
            else if(TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
               // return false;
            }
            else if(TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(this, "Please confirm your password", Toast.LENGTH_SHORT).show();
               // return false;
            }
            else if(!password.equals(confirmPassword)) {
                Toast.makeText(this, "Password & Confirm Password must be same", Toast.LENGTH_SHORT).show();
                //return false;
            }
            else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                //return false;
            }else if(!password.equals(confirmPassword)){
                Toast.makeText(this, "Password & Confirm Password must be same", Toast.LENGTH_SHORT).show();
                //return false;
            }
            else if (TextUtils.isEmpty(username)){
                Toast.makeText(this, "Please enter your username", Toast.LENGTH_SHORT).show();
                //return false;
            }

        else{
            SignUp(email,password,number,name,username);
            //return true;
        }
    }

    private void SignUp(String email, String password,String number,String name,String username){
        mAuth.createUserWithEmailAndPassword(email,password)
                //handle result of the authentication task and we want to handle it when the task completes.
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser NewUser = mAuth.getCurrentUser();

                            // send email verifacation for new user
                            NewUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>(){
                                @Override
                                public void onSuccess(Void aVoid){
                                    Toast.makeText(getApplicationContext(), "Please check email for verifacation", Toast.LENGTH_SHORT).show();


                                }
                            }).addOnFailureListener(new OnFailureListener(){
                                @Override
                                public void onFailure(@NonNull Exception e){
                                    Toast.makeText(getApplicationContext(), "Email could not be not sent " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                            // get userid from firebase for user
                            userID = mAuth.getCurrentUser().getUid();

                            FirebaseFirestore db = FirebaseFirestore.getInstance();

                            //Firebase add user
                            final String TAG = "Sample";
                            HashMap<String, String> data = new HashMap<>();
                            if (userID .length() > 0) {  //if (userName.length() > 0 && email.lengh() > 0 && phoneNumber.length() > 0)
                                data.put("Email", email);
                                data.put("Number", number);
                                data.put("Name", name);
                                data.put("Username", username);
                                data.put("Image", encodedImage);
                                db.collection("users")
                                        .document(userID )
                                        .set(data)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Log.d(TAG, "Data has been added successfully!");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                Log.d(TAG, "Data not be added!" + e.toString());
                                            }
                                        });
                            }






                        }else{
                            Toast.makeText(getApplicationContext(), "Error unable create new user " , Toast.LENGTH_SHORT).show();
                        }

                    }
                });






    }




}