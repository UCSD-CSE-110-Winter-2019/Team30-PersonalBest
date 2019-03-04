package team30.personalbest.messeging;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

import team30.personalbest.R;



public class AddContactActivity extends AppCompatActivity {

    private String friend_email;
    private FirebaseFirestore firestore;
    private FirebaseUser user;
    private MyUser thisUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        thisUser =  ( MyUser) getIntent().getExtras().get("currentUser");
        if( thisUser == null ) {
            Log.d("AddContactActivity", "ThisUser is null");
        }

        firestore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        setContentView(R.layout.activity_add_contact);

        Button submit_btn = findViewById( R.id.add_friend_btn );
        EditText email = findViewById( R.id.editText_email );

        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friend_email = email.getText().toString();
                addFriend( friend_email );


            }
        });


    }

    private void addFriend( String friend ) {


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
                                .set( thisUser );
                        fs.document("contacts/"+thisUser.getUser_id()+"/user_contacts/"+friend.getUser_id())
                                .set( friend );

                        Toast.makeText(AddContactActivity.this, "Friend Successfully added", Toast.LENGTH_SHORT).show();


                    }
                }
            }
        });

    }
}
