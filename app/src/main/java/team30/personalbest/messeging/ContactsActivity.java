package team30.personalbest.messeging;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

    private MyUser thisUser;

    ListView listView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_contacts_page );


        thisUser = (MyUser) this.getIntent().getExtras().get("currentUser");

        if( thisUser == null ) {
            Log.d("ContactsActivity", "ThisUser is null");
        }

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String user_id = user.getUid();


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_friend:
                Intent aboutIntent = new Intent(this, AddContactActivity.class);
                aboutIntent.putExtra("currentUser", thisUser  );
                startActivity(aboutIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
