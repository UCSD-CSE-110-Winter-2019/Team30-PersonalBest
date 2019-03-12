package team30.personalbest;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.HashMap;

import team30.personalbest.messeging.ChatActivity;
import team30.personalbest.messeging.ContactsActivity;
import team30.personalbest.messeging.ConversationsPageActivity;
import team30.personalbest.messeging.MessageActivity;
import team30.personalbest.messeging.MyUser;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static final String TAG = "FireMessagingService";

    private MyUser currentUser;
    private MyUser msgSender;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String msgSenderName = remoteMessage.getData().get("msgSender");
        this.goToConversation(GoogleSignIn.getLastSignedInAccount(this), msgSenderName);
    }

    private void goToConversation(GoogleSignInAccount acct, String msgSenderName) {
        Log.d(TAG, "going to conversation");

        // search through contact and find the user who matches msg sender's name
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String user_id = user.getUid();
        CollectionReference contacts = firestore.collection("contacts/" + user_id + "/user_contacts");
        contacts.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        Log.i("Contacts Query", "Retrieved Data: " + doc.toString());
                        MyUser contact = doc.toObject(MyUser.class);
                        if (contact.getUser_name() == msgSenderName) {
                            msgSender = contact;
                        }
                    }
                } else {
                    Log.d("Contacts Query", "COuldn't retrieve contacts");
                }
            }
        });

        // get current user then go to convo activity
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                            DocumentReference userRef = firestore.collection("user")
                                    .document( user.getUid() );

                            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if( task.isSuccessful() ) {
                                        DocumentSnapshot userDoc = task.getResult();
                                        if( userDoc.exists() ){
                                            Log.d("MessageActivity", "Found User in database. Retrieving data...");
                                            currentUser = userDoc.toObject( MyUser.class );

                                            Intent startConvIntent = new Intent( null, ChatActivity.class );
                                            startConvIntent.putExtra("fromUser", currentUser);
                                            startConvIntent.putExtra( "toUser", msgSender);
                                            startActivity(startConvIntent);
                                        }
                                    }
                                }
                            });
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }
}
