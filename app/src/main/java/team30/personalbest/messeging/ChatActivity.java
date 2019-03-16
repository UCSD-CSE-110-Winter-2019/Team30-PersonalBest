package team30.personalbest.messeging;


import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.RequiresApi;
import team30.personalbest.R;

public class ChatActivity extends AppCompatActivity {

    private final String LOG_TAG = "ChatActivity";

    private FirebaseFirestore firestore;
    private MyUser fromUser;
    private MyUser toUser;
    private String roomId;
    private final String FROM_KEY = "from";
    private final String TEXT_KEY = "text";
    private final String TIMESTAMP_KEY = "timestamp";
    private String from;
    private CollectionReference chat;

    private final int NEEDS_REFRESH = 1;
    private final int NO_REFRESH = 2;
    public int resultCode = NEEDS_REFRESH;



    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_message_acitvity );

        firestore = FirebaseFirestore.getInstance();
        fromUser = (MyUser) getIntent().getExtras().get("fromUser");
        toUser = (MyUser) getIntent().getExtras().get("toUser");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true );

        Log.d(LOG_TAG, toUser.getChatRooms().toString());
        Log.d( LOG_TAG, fromUser.getChatRooms().toString());

        from = fromUser.getUser_name();
        EditText nameView = findViewById((R.id.user_name));
        nameView.setText(from);


        resolveRoomID( fromUser, toUser );
    }


    /*
     * Attempts to find RoomtID between both users.
     * If none found ( no conversation exists between the two )
     * creates a new RoomID along with a chatroom for both users
     */
    private void resolveRoomID( MyUser fromUser, MyUser toUser ) {

        for( String fromUser_chatRoomId : fromUser.getChatRooms().keySet() ) {

            /* If there exists a shared roomID between users */
            if ( toUser.getChatRooms().containsKey(fromUser_chatRoomId) ) {

                Log.d( LOG_TAG, "Found exisiting conversation");
                this.roomId = fromUser_chatRoomId;
                initMessageUpdateListener();
                return;
            }
        }

        Log.d( LOG_TAG, "No existing conversations found. Creating new conversations");

            /* TODO: Create new chatID
             *      Set ID to both maps
             *      Create a new chat activity, showing such conversation
             */

        DocumentReference newChatRoom = firestore.collection("chatRooms").document();
        this.roomId = newChatRoom.getId();

        fromUser.getChatRooms().put( newChatRoom.getId(), true );
        toUser.getChatRooms().put( newChatRoom.getId(), true );

        firestore.document("chatRooms/" + newChatRoom.getId() + "/chatUsers/"+fromUser.getUser_id())
                .set( fromUser )
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d( LOG_TAG, "Successfully added you to chat");
                        Log.d( LOG_TAG, "Updating User objects...");

                    }
                });

        firestore.document("chatRooms/" + newChatRoom.getId() + "/chatUsers/"+toUser.getUser_id())
                .set( toUser )
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d( LOG_TAG, "Successfully added your friend to chat");
                        Log.d( LOG_TAG, "Updating User objects...");


                    }
                });

        firestore.document("user/"+fromUser.getUser_id()).set( fromUser, SetOptions.merge() );
        firestore.document( "user/"+toUser.getUser_id() ).set( toUser, SetOptions.merge() );

        firestore.document("emails/"+fromUser.getUser_email()).set( fromUser, SetOptions.merge() );
        firestore.document( "emails/"+toUser.getUser_email() ).set( toUser, SetOptions.merge() );

        firestore.document( "contacts/"+ toUser.getUser_id() + "/user_contacts/"+ fromUser.getUser_id() )
                .set( fromUser, SetOptions.merge() );
        firestore.document( "contacts/"+ fromUser.getUser_id() + "/user_contacts/"+ toUser.getUser_id() )
                .set( toUser, SetOptions.merge() );

        initMessageUpdateListener();
    }





    private void sendMessage() {
        if (from == null || from.isEmpty() || chat == null ) {
            Toast.makeText(this, "Enter your name", Toast.LENGTH_SHORT).show();
            return;
        }

        EditText messageView = this.findViewById(R.id.text_message);

        Map<String, String> newMessage = new HashMap<>();
        newMessage.put(FROM_KEY, from);
        newMessage.put(TEXT_KEY, messageView.getText().toString());

        chat.add(newMessage).addOnSuccessListener(result -> {
            messageView.setText("");
        }).addOnFailureListener(error -> {
            Log.e(LOG_TAG, error.getLocalizedMessage());
        });
    }


    private void initMessageUpdateListener() {
        chat = firestore.collection("messages/"+roomId+"/chat_messages");
        findViewById(R.id.btn_send).setOnClickListener(view -> sendMessage());

        chat.orderBy(TIMESTAMP_KEY, Query.Direction.ASCENDING )
                .addSnapshotListener((newChatSnapShot, error) -> {
                    if (error != null) {
                        Log.e(LOG_TAG, error.getLocalizedMessage());
                        return;
                    }

                    if (newChatSnapShot != null && !newChatSnapShot.isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        List<DocumentChange> documentChanges = newChatSnapShot.getDocumentChanges();
                        for( DocumentChange change : documentChanges ) {
                            QueryDocumentSnapshot document = change.getDocument();
                            sb.append(document.get(FROM_KEY));
                            sb.append(":\n");
                            sb.append(document.get(TEXT_KEY));
                            sb.append("\n");
                            sb.append("---\n");
                        }


                        TextView chatView = findViewById(R.id.chat);
                        chatView.append(sb.toString());
                    }
                });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch ( item.getItemId() ) {
            case android.R.id.home:
                Log.i("ChatsActivity", "Result code: " + resultCode );

                setResult( this.resultCode );
                finish();

        }
        return  super.onOptionsItemSelected(item);

    }
}
