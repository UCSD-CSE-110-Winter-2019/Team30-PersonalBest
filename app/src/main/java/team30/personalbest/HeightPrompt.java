package team30.personalbest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.util.Consumer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import team30.personalbest.fitness.Callback;
import team30.personalbest.fitness.GoogleFitAdapter;
import team30.personalbest.fitness.OnGoogleFitReadyListener;
import team30.personalbest.fitness.service.HeightService;

public class HeightPrompt extends AppCompatActivity implements OnGoogleFitReadyListener {
    public static final String TAG = "HeightPrompt";

    private GoogleFitAdapter googleFitAdapter;
    private HeightService heightService;

    private EditText heightValue;
    private Button heightButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_height_prompt);

        this.googleFitAdapter = new GoogleFitAdapter(this);

        this.heightService = new HeightService(this.googleFitAdapter);

        this.heightValue = (EditText) findViewById(R.id.heightbox);
        this.heightButton = (Button) findViewById(R.id.heightbutton);

        heightButton.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View view)
                    {
                        try
                        {
                            String heightString = heightValue.getText().toString();
                            float heightFloat = Float.parseFloat(heightString);

                            HeightPrompt.this.heightService.setHeight(heightFloat).onResult(new Consumer<Float>() {
                                @Override
                                public void accept(Float aFloat) {
                                    if (aFloat == null) throw new IllegalArgumentException("Unable to set height for google services");

                                    Log.i(TAG, "Successfully processed height");

                                    HeightPrompt.this.launchMainActivity();
                                }
                            });
                        }
                        catch (Exception e)
                        {
                            Log.w(TAG, "Failed to process height", e);

                            HeightPrompt.this.launchHeightPrompt();
                        }
                    }
                });

        this.googleFitAdapter.onActivityCreate(this, savedInstanceState);
    }

    @Override
    public void onGoogleFitReady(GoogleFitAdapter googleFitAdapter)
    {
        /*
        this.heightService.getHeight().onResult(new Consumer<Float>() {
            @Override
            public void accept(Float aFloat) {
                //Just continue if it has the height
                if (aFloat != null)
                {
                    HeightPrompt.this.launchMainActivity();
                }
                else
                {
                    heightButton.setEnabled(true);
                }
            }
        });
        */
    }

    private void launchMainActivity()
    {
        Intent intent = new Intent(this, MainActivity.class);
        this.startActivity(intent);
        this.finish();
    }

    private void launchHeightPrompt()
    {
        this.recreate();
    }
}
