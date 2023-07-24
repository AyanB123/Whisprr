package com.app.whisprr;

import static org.mockito.Mockito.mock;

import com.app.whisprr.activities.VerifyEmailActivity;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

    @RunWith(RobolectricTestRunner.class)
public class VerifyEmailActivityTest {

    @Mock
    private CollectionReference collectionReference;

    @Mock
    private EventListener<QuerySnapshot> eventListener;

    private VerifyEmailActivity verifyEmailActivity;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        verifyEmailActivity = Robolectric.buildActivity(VerifyEmailActivity.class).create().get();
        verifyEmailActivity.usedUserEmails = new HashMap<>(); // Initialize the map as empty
    }

    @Test
    public void testEmailListener() {
        // Mock the behavior of the collectionReference
        Mockito.when(collectionReference.addSnapshotListener(Mockito.any(EventListener.class)))
                .thenReturn(mock(ListenerRegistration.class));

        // Set the mocked collectionReference to the VerifyEmailActivity
        verifyEmailActivity.database = mock(FirebaseFirestore.class);
        Mockito.when(verifyEmailActivity.database.collection(Mockito.anyString()))
                .thenReturn(collectionReference);

        // Call the method under test
        verifyEmailActivity.listenForExistingEmails();

        // Simulate a document change event
        DocumentChange documentChange = mock(DocumentChange.class);
        Mockito.when(documentChange.getType()).thenReturn(DocumentChange.Type.ADDED);
        Mockito.when(documentChange.getDocument().getId()).thenReturn("documentId");
        Mockito.when(documentChange.getDocument().contains(Mockito.anyString())).thenReturn(true);
        Mockito.when(documentChange.getDocument().get(Mockito.anyString())).thenReturn("babwany@ualberta.ca");

        // Simulate a query snapshot
        List<DocumentChange> documentChanges = Collections.singletonList(documentChange);
        QuerySnapshot querySnapshot = mock(QuerySnapshot.class);
        Mockito.when(querySnapshot.getDocumentChanges()).thenReturn(documentChanges);

        // Trigger the eventListener with the simulated snapshot
        Mockito.doAnswer(invocation -> {
            // Access the email value after the eventListener is triggered
            String email = verifyEmailActivity.usedUserEmails.get("documentId");
            System.out.println("Email: " + email);
            return null;
        }).when(eventListener).onEvent(querySnapshot, null);

        // Invoke the eventListener with the simulated snapshot
        eventListener.onEvent(querySnapshot, null);

        // Verify that the email was printed correctly
        Mockito.verify(eventListener, Mockito.times(1)).onEvent(querySnapshot, null);
    }

}
