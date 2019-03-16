package team30.personalbest.messeging;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

import androidx.annotation.RequiresApi;
import team30.personalbest.R;

/* Database Schema
 *
 * Collection contacts
 *      Document User_id
 *          Collections user_contacts
 *              Document User_id
 *              Document User_id
 *              .....
 * Collection emails
 *      Document email_addresses
 *          User{ email, userId, userName }
 *          User{ email, userId, userName }
 *          ....
 *
 * Collection User
 *       Document User_id
 *            User{ email, userId, userName }
 *
 * Collection Rooms
 *       Document chat_id
 *            Collection users
 *                  Document UserID
 *
 * Collection Messages
 *       Document chat_id
 *            Collection messages
 *                  Message{ to, from, timestamp }
 *                  Message{ to, from, timestamp }
 *
 *
 *  Update Contacts list periodically
 *  Update friend addition
 */

public class MessageActivity extends AppCompatActivity {
    public static final String TAG = "MessageActivity";
    private final int RC_SIGN_IN = 123;
    public MyUser thisUser;
    private Messager messager;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_page);

        this.firebaseAuthWithGoogle(GoogleSignIn.getLastSignedInAccount(this));
        //subscribeToMessageNotifications();
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());


        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Log.e(TAG, "Unable to retrieve Firebase User ");
            return;
        }

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        DocumentReference userRef = firestore.collection("user")
                .document(user.getUid());

        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot userDoc = task.getResult();
                    if (!userDoc.exists()) {
                        userRef.set(MessageActivity.this.thisUser = new MyUser(user.getUid(), user.getDisplayName(), user.getEmail(), new HashMap<String, Boolean>()));
                        firestore.document("emails/" + MessageActivity.this.thisUser.getUser_email())
                                .set(MessageActivity.this.thisUser);
                        Intent myIntent = new Intent(MessageActivity.this, ContactsActivity.class);
                        myIntent.putExtra("currentUser", MessageActivity.this.thisUser);
                        finish();
                        startActivity(myIntent);

                    } else if (userDoc.exists()) {
                        Log.d(TAG, "Found User in database. Retrieving data...");
                        MessageActivity.this.thisUser = userDoc.toObject(MyUser.class);
                        Intent myIntent = new Intent(MessageActivity.this, ContactsActivity.class);
                        myIntent.putExtra("currentUser", MessageActivity.this.thisUser);
                        finish();
                        startActivity(myIntent);
                    }
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.finish();

    }

    private void subscribeToMessageNotifications() {
        FirebaseMessaging.getInstance().subscribeToTopic("messaging")
                .addOnCompleteListener(task -> {
                            String msg = "Subscribed to notifications";
                            if (!task.isSuccessful()) {
                                msg = "Subscribe to notifications failed";
                            }
                            Log.d(TAG, msg);
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                        }
                );
    }
}
