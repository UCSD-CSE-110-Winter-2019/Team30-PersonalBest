package team30.personalbest.messeging;


import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
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

    private boolean toUserReady;
    private boolean fromUserReady;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_message_acitvity );

        toUserReady = false;
        fromUserReady = false;
        firestore = FirebaseFirestore.getInstance();
        fromUser = (MyUser) getIntent().getExtras().get("fromUser");
        toUser = (MyUser) getIntent().getExtras().get("toUser");

        from = fromUser.getUser_name();

        getRoomID( fromUser, toUser );




    }


    /*
     * Attempts to find RoomtID between both users.
     * If none found ( no conversation exists between the two )
     * creates a new RoomID along with a chatroom for both users
     */
    private void getRoomID( MyUser fromUser, MyUser toUser ) {

        for( String fromUser_chatRoomId : fromUser.getChatRooms().keySet() ) {

            /* If there exists a shared roomID between users */
            if (toUser.getChatRooms().containsKey(fromUser_chatRoomId)) {
                toUserReady = true;
                fromUserReady = true;
                startConversation();
            }
        }

            /* TODO: Create new chatID
             *      Set ID to both maps
             *      craete a new chat activity, showing such conversation
             */
            firestore.collection("chatRooms").add( fromUser ).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {

                    Log.d("ChatActivity", "Successfully craeted new chatroom");

                    if( ChatActivity.this.fromUser == null || ChatActivity.this.toUser == null ) {
                        Log.e( LOG_TAG, "Error: One or more users are null");
                        return;
                    }

                    ChatActivity.this.fromUser.getChatRooms().put( documentReference.getId(), true );
                    ChatActivity.this.toUser.getChatRooms().put( documentReference.getId(), true );

                    ChatActivity.this.roomId = documentReference.getId();

                    fromUserReady = true;

                    /* Add fromUser to the chatRoom */
                    firestore.document("chatRooms/"+documentReference.getId())
                            .set( ChatActivity.this.toUser, SetOptions.merge() )
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d( LOG_TAG, "Both users sucessfully added to chatRoom");
                                    toUserReady = true;
                                    startConversation();
                                    initMessageUpdateListener();

                                }
                            })

                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e( LOG_TAG, "There was a problem adding a user to the chatroom");
                                }
                            });

                }
            });

    }

    private void startConversation( ) {

        chat = firestore.collection("messages/"+roomId+"/chat_messages");

        chat.addSnapshotListener((newChatSnapShot, error) -> {
            if (error != null) {
                Log.e(LOG_TAG, error.getLocalizedMessage());
                return;
            }

            if (newChatSnapShot != null && !newChatSnapShot.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                List<DocumentChange> documentChanges = newChatSnapShot.getDocumentChanges();
                documentChanges.forEach(change -> {
                    QueryDocumentSnapshot document = change.getDocument();
                    sb.append(document.get(FROM_KEY));
                    sb.append(":\n");
                    sb.append(document.get(TEXT_KEY));
                    sb.append("\n");
                    sb.append("---\n");
                });

                TextView chatView = findViewById(R.id.chat);
                chatView.append(sb.toString());
            }
        });

        findViewById(R.id.btn_send).setOnClickListener(view -> sendMessage());

        EditText nameView = findViewById((R.id.user_name));
        nameView.setText(from);
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

    private void subscribeToNotificationsTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic("bullshit")
                .addOnCompleteListener(task -> {
                            String msg = "Subscribed to notifications";
                            if (!task.isSuccessful()) {
                                msg = "Subscribe to notifications failed";
                            }
                            Log.d(LOG_TAG, msg);
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                        }
                );
    }
}
