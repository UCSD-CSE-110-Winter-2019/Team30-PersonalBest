package team30.personalbest.messeging;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import team30.personalbest.R;



public class AddContactActivity extends AppCompatActivity {

    private final String LOG_TAG = "AddContactActivity";
    private String friend_email;
    private FirebaseFirestore firestore;
    private FirebaseUser user;
    private MyUser thisUser;
    private final int NEEDS_REFRESH = 1;
    private final int NO_REFRESH = 2;
    public int resultCode = NO_REFRESH;
    private Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        thisUser =  ( MyUser) getIntent().getExtras().get("currentUser");
        if( thisUser == null ) {
            Log.d("AddContactActivity", "ThisUser is null");
        }

        firestore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        setContentView(R.layout.activity_add_contact);

        Button submit_btn = findViewById( R.id.add_friend_btn );
        //Button back = findViewById( R.id. );
        EditText email = findViewById( R.id.editText_email );

        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friend_email = email.getText().toString();

                if(friend_email.equals(thisUser.getUser_email() ) ) {
                    Toast.makeText(AddContactActivity.this, "I wish this was possible to. :( \n Add someone other than yourself.", Toast.LENGTH_LONG).show();
                } else {
                    addFriend( friend_email );
                }



            }
        });






    }

    private void addFriend( String friend ) {


       // Check if already friends



        DocumentReference friendDoc = firestore.document( "emails/"+friend );

        friendDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if( task.isSuccessful() ) {
                    DocumentSnapshot doc = task.getResult();

                    if( !doc.exists() ) {
                        Toast.makeText(AddContactActivity.this, "User Not Found", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        MyUser friend = doc.toObject( MyUser.class );

                        FirebaseFirestore fs = FirebaseFirestore.getInstance();
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        fs.document("contacts/"+friend.getUser_id()+"/user_contacts/"+user.getUid() )
                                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                if( task.isSuccessful() ) {
                                    DocumentSnapshot doc = task.getResult();

                                    if( doc.exists() ) {
                                        Log.d( LOG_TAG,"Already Friends");
                                        Toast.makeText( AddContactActivity.this , "Already friends with " + friend.getUser_name(), Toast.LENGTH_LONG ).show();
                                    } else {
                                        Log.d( LOG_TAG, "Resolving Friend Request");
                                        resolvePendingFriend( friend );
                                    }
                                }
                            }
                        });




                    }
                }
            }
        });

    }

    /* Checks if there is a pending friend request.
     * If not, makes a pending friends request,
     * Otherwise deleted friend request and adds friends
     */
    private void resolvePendingFriend( MyUser friend ) {

        FirebaseFirestore fs = FirebaseFirestore.getInstance();

        fs.document( "friend_requests/" + friend.getUser_id()+ "/pending/"+thisUser.getUser_id() )
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if( task.isSuccessful() ) {
                            DocumentSnapshot doc = task.getResult();

                            if( doc.exists() ) {

                                fs.document("contacts/"+friend.getUser_id()+"/user_contacts/"+ thisUser.getUser_id()  )
                                        .set( thisUser, SetOptions.merge() );
                                fs.document("contacts/"+thisUser.getUser_id()+"/user_contacts/"+friend.getUser_id())
                                        .set( friend, SetOptions.merge() );

                                Toast.makeText(AddContactActivity.this, "Friend Successfully added", Toast.LENGTH_SHORT).show();
                                AddContactActivity.this.resultCode = NEEDS_REFRESH;
                                fs.document( "friend_requests/" + friend.getUser_id() + "/pending/"+thisUser.getUser_id() ).delete();

                            } else {

                                fs.document( "friend_requests/" + thisUser.getUser_id() +"/pending/"+friend.getUser_id() ).set( friend );
                                Toast.makeText(AddContactActivity.this, "Friend Request Pending Approval.", Toast.LENGTH_SHORT).show();

                            }
                        }
                    }
                });

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch ( item.getItemId() ) {
            case android.R.id.home:
            Log.i("AddContactsActivity", "Result code: " + resultCode );

            setResult( this.resultCode );
            finish();

        }

        return super.onOptionsItemSelected(item);
    }

}
