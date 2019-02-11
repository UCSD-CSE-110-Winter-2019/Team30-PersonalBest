package team30.personalbest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class HeightPrompt extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SharedPreferences sharedPreferences = getSharedPreferences("userheight", MODE_PRIVATE);

        if(sharedPreferences.contains("height")){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_height_prompt);

        final EditText heightvalue = (EditText)findViewById(R.id.heightbox);
        Button heightbutton = (Button)findViewById(R.id.heightbutton);

        heightbutton.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View view)
                    {
                        String height;
                        height = heightvalue.getText().toString();

                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        editor.putString("height", height);

                        editor.apply();
                        Toast.makeText(HeightPrompt.this, "HEIGHT ENTERED", Toast.LENGTH_SHORT).show();

                        launchMainActivity();
                    }
                });
    }

    public void launchMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
