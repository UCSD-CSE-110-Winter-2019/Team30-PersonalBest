package team30.personalbest;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import androidx.annotation.RequiresApi;

public class MessageActivity extends AppCompatActivity {

    private Messager messager;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_acitvity);

        this.messager = new Messager(this);
    }
}
