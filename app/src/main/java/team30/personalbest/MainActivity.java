package team30.personalbest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        GoogleFitLayer layer = new GoogleFitLayer(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
