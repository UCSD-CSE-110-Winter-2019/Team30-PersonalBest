package team30.personalbest.messeging;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
 */

public class MessageActivity extends AppCompatActivity {
    public static final String TAG = "MessageActivity";
    public MyUser thisUser;
    private Messager messager;
    private final int RC_SIGN_IN = 123;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_acitvity);

        this.firebaseAuthWithGoogle(GoogleSignIn.getLastSignedInAccount(this));
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
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
                                        if( !userDoc.exists() ) {
                                            userRef.set( MessageActivity.this.thisUser = new MyUser( user.getUid(), user.getDisplayName(), user.getEmail(), new HashMap<String, Boolean>() ));
                                            firestore.document("emails/"+MessageActivity.this.thisUser.getUser_email() )
                                                    .set( MessageActivity.this.thisUser );
                                            Intent myIntent = new Intent(MessageActivity.this, ConversationsPageActivity.class);
                                            myIntent.putExtra("currentUser", MessageActivity.this.thisUser  );
                                            startActivity(myIntent);

                                        }
                                        else if( userDoc.exists() ){
                                            Log.d("MessageActivity", "Found User in database. Retrieving data...");
                                            MessageActivity.this.thisUser = userDoc.toObject( MyUser.class );
                                            Intent myIntent = new Intent(MessageActivity.this, ConversationsPageActivity.class);
                                            myIntent.putExtra("currentUser", MessageActivity.this.thisUser  );
                                            startActivity(myIntent);
                                        }
                                    }
                                }
                            });
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                        }

                        // ...
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }
}
