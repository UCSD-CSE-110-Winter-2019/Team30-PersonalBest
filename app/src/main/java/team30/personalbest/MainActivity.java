package team30.personalbest;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    GoogleFitLayer layer;
    Button updateDailyStep_btn;
    TextView dailyStep_textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        layer = new GoogleFitLayer(this);

        setContentView(R.layout.activity_main);

        updateDailyStep_btn = findViewById( R.id.getDailyStepCount );
        dailyStep_textView = findViewById( R.id.StepCountTemp );

        updateDailyStep_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layer.readDailyStepCount();
            }
        });

    }


    public void setDailySteps( long steps ) {

        if( layer != null ) {
            dailyStep_textView.setText( String.valueOf( steps ));
        } else {
            dailyStep_textView.setText( "Layer is null");
        }

    }
}
