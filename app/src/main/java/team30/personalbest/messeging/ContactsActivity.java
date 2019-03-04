package team30.personalbest.messeging;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import team30.personalbest.R;

public class ContactsActivity extends AppCompatActivity {

    ListView listView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_contacts_page );

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String user_id = user.getUid();


        CollectionReference contacts = firestore.collection("contacts/"+user_id+"/userContacts");
        ArrayList<String> contactsList = new ArrayList<>();

        contacts.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if( task.isSuccessful() ) {


                    for(QueryDocumentSnapshot doc : task.getResult() ) {
                        MyUser contact = doc.toObject( MyUser.class );
                        contactsList.add( contact.getUser_name() );
                    }
                }
                else {
                    Log.d( "Contacts Query", "COuldn't retrieve contacts");
                }
            }
        });

        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                contactsList);

        listView = findViewById( R.id.list_view );
        listView.setAdapter(adapter);

    }
}
