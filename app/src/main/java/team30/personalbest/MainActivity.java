package team30.personalbest;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.util.Consumer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import team30.personalbest.fitness.GoogleFitAdapter;
import team30.personalbest.fitness.OnGoogleFitReadyListener;
import team30.personalbest.fitness.service.ActiveFitnessService;
import team30.personalbest.fitness.service.FitnessService;
import team30.personalbest.fitness.snapshot.IFitnessSnapshot;

public class MainActivity extends AppCompatActivity implements OnGoogleFitReadyListener
{
    private GoogleFitAdapter googleFitAdapter;
    private FitnessService fitnessService;
    private ActiveFitnessService activeFitnessService;

    private Button updateDailyStep_btn;
    private TextView dailyStep_textView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.googleFitAdapter = new GoogleFitAdapter(this);

        this.fitnessService = new FitnessService(this.googleFitAdapter);
        this.activeFitnessService = new ActiveFitnessService(this.googleFitAdapter);

        this.updateDailyStep_btn = (Button) findViewById(R.id.getDailyStepCount);
        this.dailyStep_textView = (TextView) findViewById(R.id.StepCountTemp);

        this.googleFitAdapter.addOnReadyListener(this);
        this.googleFitAdapter.onActivityCreate(this, savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        this.googleFitAdapter.onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public void onGoogleFitReady(final GoogleFitAdapter googleFitAdapter)
    {
        this.updateDailyStep_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.fitnessService.getFitnessSnapshot()
                        .onResult(new Consumer<IFitnessSnapshot>() {
                            @Override
                            public void accept(IFitnessSnapshot iFitnessSnapshot) {
                                String totalSteps = "" + iFitnessSnapshot.getTotalSteps();
                                dailyStep_textView.setText(totalSteps);
                            }
                        });
            }
        });
    }
}
