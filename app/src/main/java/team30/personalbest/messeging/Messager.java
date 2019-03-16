package team30.personalbest.messeging;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.RequiresApi;
import team30.personalbest.R;


public class Messager {
    String TAG = MessageActivity.class.getSimpleName();

    String COLLECTION_KEY = "chats";
    String DOCUMENT_KEY = "chat1";
    String MESSAGES_KEY = "messages";
    String FROM_KEY = "from";
    String TEXT_KEY = "text";
    String TIMESTAMP_KEY = "timestamp";

    AppCompatActivity activity;
    CollectionReference chat;
    String from;

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

    @RequiresApi(api = Build.VERSION_CODES.N)
    public Messager(AppCompatActivity activity) {
        this.activity = activity;
        FirebaseApp.initializeApp(activity);

        SharedPreferences sharedpreferences = this.activity.getSharedPreferences("FirebaseLabApp", Context.MODE_PRIVATE);
        from = sharedpreferences.getString(FROM_KEY, null);

        chat = FirebaseFirestore.getInstance()
                .collection(COLLECTION_KEY)
                .document(DOCUMENT_KEY)
                .collection(MESSAGES_KEY);

        chat.addSnapshotListener((newChatSnapShot, error) -> {
            if (error != null) {
                Log.e(TAG, error.getLocalizedMessage());
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

                TextView chatView = activity.findViewById(R.id.chat);
                chatView.append(sb.toString());
            }
        });

        this.activity.findViewById(R.id.btn_send).setOnClickListener(view -> sendMessage());

        EditText nameView = this.activity.findViewById((R.id.user_name));
        nameView.setText(from);
        nameView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                from = s.toString();
                sharedpreferences.edit().putString(FROM_KEY, from).apply();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void sendMessage() {
        if (from == null || from.isEmpty()) {
            Toast.makeText(this.activity, "Enter your name", Toast.LENGTH_SHORT).show();
            return;
        }

        EditText messageView = this.activity.findViewById(R.id.text_message);

        Map<String, String> newMessage = new HashMap<>();
        newMessage.put(FROM_KEY, from);
        newMessage.put(TEXT_KEY, messageView.getText().toString());

        chat.add(newMessage).addOnSuccessListener(result -> {
            messageView.setText("");
        }).addOnFailureListener(error -> {
            Log.e(TAG, error.getLocalizedMessage());
        });
    }


}
