package team30.personalbest;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.util.Consumer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import team30.personalbest.fitness.GoogleFitAdapter;
import team30.personalbest.fitness.service.ActiveFitnessService;
import team30.personalbest.fitness.service.FitnessService;
import team30.personalbest.fitness.snapshot.IFitnessSnapshot;
import team30.personalbest.goal.CustomStepGoal;
import team30.personalbest.goal.StepGoal;

public class MainActivity extends AppCompatActivity {
    private GoogleFitAdapter googleFitAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView textView = findViewById(R.id.hello_world);
        final Button button = findViewById(R.id.button);

        this.googleFitAdapter = new GoogleFitAdapter();

        this.googleFitAdapter.setOnReadyListener(new Consumer<GoogleFitAdapter>() {
            @Override
            public void accept(GoogleFitAdapter googleFitAdapter) {
                final FitnessService fitnessService = new FitnessService(googleFitAdapter);
                final ActiveFitnessService activeFitnessService = new ActiveFitnessService(googleFitAdapter);

                fitnessService.getFitnessSnapshot().onResult(new Consumer<IFitnessSnapshot>() {
                    @Override
                    public void accept(IFitnessSnapshot iFitnessSnapshot) {
                        final String string = iFitnessSnapshot.getStartTime()
                                + ": "
                                + iFitnessSnapshot.getTotalSteps();

                        textView.setText(string);
                    }
                });

                final StepGoal customStepGoal = new CustomStepGoal(null, googleFitAdapter);
            }
        });

        this.googleFitAdapter.onActivityCreate(this, savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        this.googleFitAdapter.onActivityResult(this, requestCode, resultCode, data);
    }
}
