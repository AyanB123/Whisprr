package com.app.whisprr.activities;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.app.whisprr.adapter.UserContactsAdapter;
import com.app.whisprr.databinding.ActivityAddContactsBinding;
import com.app.whisprr.databinding.ActivityMainBinding;
import com.app.whisprr.interfaces.OnRegistrationNumberLoadedListener;
import com.app.whisprr.model.Contact;
import com.app.whisprr.model.User;
import com.app.whisprr.utilities.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;


public class AddContactsActivity extends AppCompatActivity {
    private ActivityAddContactsBinding binding;
    FirebaseFirestore fireStore;
    FirebaseAuth mAuth;
    String loggedInuserID;

    FirebaseUser loggedInUser;
    List<Contact> contactList;

    FirebaseFirestore database;

    private ActivityResultLauncher<Intent> activityLauncher;
    private PreferenceManager preferenceManager;




    public HashMap<String, String> allNumberContacts= new HashMap<>();
    UserContactsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddContactsBinding.inflate(getLayoutInflater()); // inflate layout
        setContentView(binding.getRoot()); // set layout as activity content
        init();
        setListeners();
    }

    private void init(){
        contactList = new ArrayList<>();
        allNumberContacts =  new HashMap<>();
        preferenceManager = new PreferenceManager(getApplicationContext());
        adapter = new UserContactsAdapter(contactList,getApplicationContext());
        binding.conversationsRecylerView.setAdapter(adapter);
        FirebaseFirestore.getInstance();
    }

    private void setListeners() {
        binding.btnYescontactaccess.setOnClickListener(v -> getUserContacts());
        binding.btnConnotactaccess.setOnClickListener(v -> startMainActivity());
    }

    private void uploadContactsToFirebase() {
        database = FirebaseFirestore.getInstance();

        // Replace the code to get a specific user document instead of iterating over all documents
        DocumentReference userDocumentRef = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));

        userDocumentRef.get().addOnCompleteListener(userTask -> {
            if (userTask.isSuccessful() && userTask.getResult() != null) {
                DocumentSnapshot userDocument = userTask.getResult();

                // Check if the "Contacts" subcollection exists
                CollectionReference contactsCollection = userDocumentRef
                        .collection(Constants.KEY_COLLECTION_CONTACTS);

                contactsCollection.get().addOnCompleteListener(contactsTask -> {
                    if (contactsTask.isSuccessful() && contactsTask.getResult() != null) {
                        if (contactsTask.getResult().isEmpty()) {
                            // Create the "Contacts" subcollection if it does not exist
                            contactsCollection.document().set(new HashMap<>());
                        }

                        // Loop over the contactList and add each item as a document inside "Contacts" subcollection
                        for (Contact contact : contactList) {
                            Map<String, Object> contactData = new HashMap<>();
                            contactData.put("Name", contact.uName);
                            contactData.put("Username", contact.uUsername);
                            contactData.put("Number", contact.unumber);
                            contactData.put("Photo", contact.uphoto);
                            contactData.put("DisplayName", contact.udisplayname);
                            contactData.put("OnWhisprr", contact.isOnWhisprr);

                            // Create a new document with autogenerated ID inside "Contacts" subcollection
                            contactsCollection.document().set(contactData);
                        }
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), "Could not get existing user", Toast.LENGTH_SHORT).show();
            }

            // Call the interface method after the numbers are loaded

        });
    }






    private void getUserContacts() {
        binding.allowaccesscontainer.setVisibility(View.GONE);
        binding.usercontactscontainer.setVisibility(View.VISIBLE);

        Dexter.withContext(this)
                .withPermissions(android.Manifest.permission.READ_CONTACTS)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            getAllRegistereNumbers(new OnRegistrationNumberLoadedListener() {
                                @Override
                                public void onNumbersLoaded() {
                                    getContacts();

                                }
                            });
                        } else {
                            Toast.makeText(AddContactsActivity.this, "Permission should be granted", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void performAdditionalActions() {
        // Code for additional actions after both getAllRegistereNumbers() and getContacts() are completed
        // Example:
        // Update UI, process data, etc.
    }


    private void getContacts() {
        Cursor phones = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        if (phones != null) {
            database = FirebaseFirestore.getInstance();
            int nameIndex = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY);
            int phoneIndex = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER);
            while (phones.moveToNext()) {
                if (nameIndex != -1 && phoneIndex != -1) {
                    String displayname = phones.getString(nameIndex);
                    String phone = phones.getString(phoneIndex);
                    Log.d(TAG, "Local Phone number" + phone);



                    if(isNumberRegistered(phone)){
                        String docId = allNumberContacts.get(phone);
                        database.collection(Constants.KEY_COLLECTION_USERS)
                                .document(docId)
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful() && task.getResult() != null) {
                                        DocumentSnapshot documentSnapshot = task.getResult();
                                        if (documentSnapshot.exists()) {
                                            // Access the document data here and perform any desired actions

                                            Contact contact = new Contact();
                                            contact.uUsername = documentSnapshot.getString(Constants.KEY_USERNAME);
                                            contact.uName= documentSnapshot.getString(Constants.KEY_NAME);
                                            contact.unumber= phone;
                                            contact.udisplayname= displayname;
                                            contact.uphoto= documentSnapshot.getString(Constants.KEY_IMAGE);
                                            contact.isOnWhisprr= true;
                                            contactList.add(contact);
                                            adapter.notifyDataSetChanged();



                                            // Do something with the email
                                        } else {
                                            // Handle the case where the document doesn't exist
                                            Toast.makeText(getApplicationContext(), "Document does not exist for ID: " + docId, Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        // Handle the error if the document retrieval fails
                                        Toast.makeText(getApplicationContext(), "Could not retrieve document with ID: " + docId, Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                    else{
                        Contact contact = new Contact();
                        contact.unumber= phone;
                        contact.udisplayname= displayname;
                        contact.isOnWhisprr= false;
                        contactList.add(contact);
                        adapter.notifyDataSetChanged();


                    }

                }


            }
            phones.close(); // Close the cursor when finished
        }
        //uploadContactsToFirebase();
        //upload all contact to firebase code

        // Replace the code to get a specific user document instead of iterating over all documents
        DocumentReference userDocumentRef = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(loggedInuserID);

        userDocumentRef.get().addOnCompleteListener(userTask -> {
            if (userTask.isSuccessful() && userTask.getResult() != null) {
                DocumentSnapshot userDocument = userTask.getResult();

                // Check if the "Contacts" subcollection exists
                CollectionReference contactsCollection = userDocumentRef
                        .collection(Constants.KEY_COLLECTION_CONTACTS);

                contactsCollection.get().addOnCompleteListener(contactsTask -> {
                    if (contactsTask.isSuccessful() && contactsTask.getResult() != null) {
                        if (contactsTask.getResult().isEmpty()) {
                            // Create the "Contacts" subcollection if it does not exist
                            contactsCollection.document().set(new HashMap<>());
                        }

                        // Loop over the contactList and add each item as a document inside "Contacts" subcollection
                        for (Contact contact : contactList) {
                            Map<String, Object> contactData = new HashMap<>();
                            contactData.put("Name", contact.uName);
                            contactData.put("Username", contact.uUsername);
                            contactData.put("Number", contact.unumber);
                            contactData.put("Photo", contact.uphoto);
                            contactData.put("DisplayName", contact.udisplayname);
                            contactData.put("OnWhisprr", contact.isOnWhisprr);

                            // Create a new document with autogenerated ID inside "Contacts" subcollection
                            contactsCollection.document().set(contactData);
                        }
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), "Could not get existing user", Toast.LENGTH_SHORT).show();
            }

            // Call the interface method after the numbers are loaded

        });
    }


    private void getAllRegistereNumbers(OnRegistrationNumberLoadedListener listener) {
        database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document.contains(Constants.KEY_NUMBER)) {
                                String documentId = document.getId();
                                String number = document.getString(Constants.KEY_NUMBER);

                                allNumberContacts.put(number, documentId);
                            }
                        }

                        String keysString = TextUtils.join(", ", allNumberContacts.keySet());
                        Log.d("Databse number", keysString);
                    } else {
                        Toast.makeText(getApplicationContext(), "Could not get existing users", Toast.LENGTH_SHORT).show();
                    }

                    // Call the interface method after the numbers are loaded
                    listener.onNumbersLoaded();
                });
    }


    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        if(encodedImage != null){
            byte[] bytes= Base64.decode(encodedImage,Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        }else {
            return null;
        }

    }
    public Boolean isNumberRegistered(String phoneNumber){
        Log.d(TAG, "isNumberRegistered: " + phoneNumber);

        if(allNumberContacts.containsKey(phoneNumber)){
            Log.d(TAG, "matchhhhhhhhhhh");
        }
        List<String> phoneNumberList = new ArrayList<>(allNumberContacts.keySet());
        for (String number : phoneNumberList) {
            Log.d(TAG, " compareto: " + number);
            if (number.equals(phoneNumber)) {
                return true;
            }
        }
        return false;
    }
    public String formatPhoneNumber(String number) {
        // Remove all non-digit characters
        String formattedNumber = number.replaceAll("[^\\d]", "");

        // Check if the number has a country code
        if (!formattedNumber.startsWith("+")) {
            // Try to add a country code
            formattedNumber = "+" + formattedNumber;
        }

        // Ensure the number is in E.164 format
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(formattedNumber, null);
            formattedNumber = phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException e) {
            System.err.println("Error parsing phone number: " + e);
        }

        return formattedNumber;
    }



    private void startMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


}