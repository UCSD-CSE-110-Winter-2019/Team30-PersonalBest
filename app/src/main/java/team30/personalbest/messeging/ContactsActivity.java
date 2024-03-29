package team30.personalbest.messeging;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;

import team30.personalbest.FriendlyActivity;
import team30.personalbest.R;

public class ContactsActivity extends AppCompatActivity {

    public static String TO_USER = "toUser";
    public static String FROM_USER = "fromUser";
    private final String LOG_TAG = "ContactsActivity";
    ListView listView;
    private ArrayList<String> contactsList;
    private ArrayList<MyUser> contactsListObject;
    private MyUser thisUser;
    private boolean initialContactsQuery;
    private boolean initialUserQuery;
    private ListenerRegistration contactsListener;
    private ListenerRegistration userListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_page);

        contactsListObject = new ArrayList<>();

        thisUser = (MyUser) this.getIntent().getExtras().get("currentUser");

        if (thisUser == null) {
            Log.d("ContactsActivity", "ThisUser is null");
        }

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String user_id = user.getUid();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CollectionReference contacts = firestore.collection("contacts/" + user_id + "/user_contacts");


        contactsList = new ArrayList<>();

        /*
         * https://stackoverflow.com/questions/2468100/how-to-handle-listview-click-in-android
         */

        contacts.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        Log.i("Contacts Query", "Retrieved Data: " + doc.toString());
                        MyUser contact = doc.toObject(MyUser.class);
                        contactsListObject.add(contact);
                        contactsList.add(contact.getUser_name());
                    }

                    ContactsActivity.this.displayContacts(contactsList);
                    ContactsActivity.this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            Log.d("Row Clicked", "" + id);

                            Intent friendGraphIntent = new Intent(ContactsActivity.this, FriendlyActivity.class);
                            friendGraphIntent.putExtra("fromUser", ContactsActivity.this.thisUser);
                            friendGraphIntent.putExtra("toUser", contactsListObject.get((int) id));
                            userListener.remove();
                            contactsListener.remove();
                            Log.d(LOG_TAG, "Launching friendlyGraph");
                            startActivityForResult(friendGraphIntent, 1);


                            Intent startConvIntent = new Intent(ContactsActivity.this, ChatActivity.class);
                            startConvIntent.putExtra("fromUser", ContactsActivity.this.thisUser);
                            startConvIntent.putExtra("toUser", contactsListObject.get((int) id));


                            //startActivityForResult( startConvIntent , 1);
                        }
                    });
                } else {
                    Log.d("Contacts Query", "COuldn't retrieve contacts");
                }
            }
        });

        initialContactsQuery = true;
        contactsListener = contacts.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    Log.e(LOG_TAG, "Error listening");
                    return;
                }

                if (initialContactsQuery) {
                    Log.d(LOG_TAG, "Initial Query. Skipping...");
                    initialContactsQuery = false;
                    return;
                }
                boolean contactAdded = false;

                for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                    switch (dc.getType()) {
                        case ADDED:
                            Log.d(LOG_TAG, "New user: " + dc.getDocument().getData());
                            contactAdded = true;
                            break;
                        case MODIFIED:
                            Log.d(LOG_TAG, "Modified user: " + dc.getDocument().getData());
                            contactAdded = true;
                            break;
                        case REMOVED:
                            Log.d(LOG_TAG, "Removed user: " + dc.getDocument().getData());
                            break;
                    }
                }

                if (contactAdded) {
                    updateUser();
                }
            }
        });

        initialUserQuery = true;
        userListener = firestore.document("user/" + thisUser.getUser_id())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.e(LOG_TAG, "Error listening");
                            return;
                        }

                        if (initialUserQuery) {
                            Log.d(LOG_TAG, "Initial Query. Skipping...");
                            initialUserQuery = false;
                            return;
                        }

                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            Log.d(LOG_TAG, "Current data: " + documentSnapshot.getData());
                            updateUser();
                        } else {
                            Log.d(LOG_TAG, "Current data: null");
                        }


                    }
                });

        if (thisUser != null) {
            for (String chatId : thisUser.getChatRooms().keySet()) {
                subscribeToNotificationsTopic(chatId);
            }
        }
    }

    public void displayContacts(ArrayList<String> contactsList) {
        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                contactsList);

        listView = findViewById(R.id.list_view);
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
                aboutIntent.putExtra("currentUser", thisUser);
                userListener.remove();
                contactsListener.remove();
                startActivityForResult(aboutIntent, 1);
                break;
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 1) {
            Log.d("onActivityResult", "Restarting Activity");
            updateUser();
        } else {
            Log.d("onActivityResult", "Something went wrong");
        }
    }

    private void updateUser() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        userListener.remove();
        contactsListener.remove();

        firestore.collection("user")
                .document(thisUser.getUser_id())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot userDoc = task.getResult();

                            if (userDoc.exists()) {
                                Log.d("MessageActivity", "Found User in database. Retrieving data...");
                                MyUser thisUser = userDoc.toObject(MyUser.class);
                                ContactsActivity.this.getIntent().putExtra("currentUser", thisUser);
                            } else {
                                Log.d("ContactsActivity", "Could not update user");
                            }
                        }
                        restartActivity();
                    }
                });
    }

    private void subscribeToNotificationsTopic(String topic) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnCompleteListener(task -> {
                            String msg = "Subscribed to notifications with topic = " + topic;
                            if (!task.isSuccessful()) {
                                msg = "Subscribe to notifications failed";
                            }
                            Log.d("ContactsActivity", msg);
                        }
                );
    }

    private void restartActivity() {
        finish();
        startActivity(getIntent());
    }
}
