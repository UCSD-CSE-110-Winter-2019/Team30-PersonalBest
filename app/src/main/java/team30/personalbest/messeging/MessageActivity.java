package team30.personalbest.messeging;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.RequiresApi;
import team30.personalbest.R;

public class MessageActivity extends AppCompatActivity {

    public MyUser thisUser;
    private Messager messager;
    private final int RC_SIGN_IN = 123;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_acitvity);



        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(), RC_SIGN_IN);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

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




                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }
}
