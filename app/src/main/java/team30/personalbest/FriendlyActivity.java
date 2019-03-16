package team30.personalbest;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import team30.personalbest.framework.snapshot.IFitnessSnapshot;
import team30.personalbest.framework.snapshot.IGoalSnapshot;
import team30.personalbest.messeging.ChatActivity;
import team30.personalbest.messeging.ContactsActivity;
import team30.personalbest.messeging.MyUser;

public class FriendlyActivity extends AppCompatActivity {
    private long startTime = 0;

    private String LOG_TAG = "FriendlyActivity";
    private MyUser toUser;
    private MyUser fromUser;

    private SnapshotObject snapshotObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendly);

        FirebaseFirestore fs = FirebaseFirestore.getInstance();

        Log.d(LOG_TAG, "Made it");
        toUser = (MyUser) getIntent().getExtras().get(ContactsActivity.TO_USER);
        fromUser = (MyUser) getIntent().getExtras().get(ContactsActivity.FROM_USER);


        if (toUser == null || fromUser == null) {
            Log.e(LOG_TAG, "One or Both Users Null");
            setResult(99);
            finish();
        }

        fs.document("snapshot/" + fromUser.getUser_id())
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    Log.d(LOG_TAG, documentSnapshot.toString());
                    snapshotObject = documentSnapshot.toObject(SnapshotObject.class);
                    List<IFitnessSnapshot> fitnessSnapshots = new ArrayList<>(snapshotObject.fitnessSnapshots);
                    List<IGoalSnapshot> goalSnapshots = new ArrayList<>(snapshotObject.goalSnapshots);
                    Bundle bundle = GraphBundler.buildBundleForDays(GraphActivity.BUNDLE_MONTH_LENGTH, fitnessSnapshots, goalSnapshots);
                    this.getIntent().getExtras().putBundle(GraphActivity.BUNDLE_WEEKLY_STATS, bundle);
                    createCharts();
                });


        final Button switchScreen = findViewById(R.id.btn_messaging);
        switchScreen.setOnClickListener(view -> this.launchMessaging());


    }

    public void createCharts() {
        this.createWeekChart(R.id.week1_chart, 0);
        this.createWeekChart(R.id.week2_chart, 1);
        this.createWeekChart(R.id.week3_chart, 2);
        this.createWeekChart(R.id.week4_chart, 3);
    }

    private void createWeekChart(int chartID, int weekIndex) {
        CombinedChart chart = findViewById(chartID);
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(10f);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(7f);

        // Creating week labels for x-axis
        final String[] xLabels = GraphActivity.WEEK_DAY_LABELS;
        xAxis.setValueFormatter((value, axis) -> {
            if ((int) value < xLabels.length && (int) value >= 0) {
                return xLabels[(int) value];
            } else {
                return "";
            }
        });
        xAxis.setCenterAxisLabels(true);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle == null) {
            bundle = new Bundle();
        }
        Bundle weeklyBundle = bundle.getBundle(GraphActivity.BUNDLE_WEEKLY_STATS);
        if (weeklyBundle == null) {
            weeklyBundle = new Bundle();
        }

        this.startTime = weeklyBundle.getLong(GraphActivity.BUNDLE_WEEKLY_TIME);
        // Add entries for intentional steps
        List<BarEntry> intentStepEntries = new ArrayList<>();
        // Add entries for intentional steps
        List<BarEntry> incidentStepEntries = new ArrayList<>();
        // Graph lines for step goal
        List<Entry> stepGoalEntries = new ArrayList<>();

        int prevStepGoal = 0;
        int startWeekDay = weekIndex * GraphActivity.BUNDLE_WEEK_LENGTH;
        int stopWeekDay = startWeekDay + GraphActivity.BUNDLE_WEEK_LENGTH;
        for (int i = startWeekDay; i < stopWeekDay; ++i) {
            Bundle dailyBundle = weeklyBundle.getBundle(GraphActivity.BUNDLE_WEEKLY_PREFIX + i);

            int stepCount;
            int activeCount;
            int stepGoal = prevStepGoal;
            if (dailyBundle != null) {
                stepCount = dailyBundle.getInt(GraphActivity.BUNDLE_DAILY_STEPS, 0);
                activeCount = dailyBundle.getInt(GraphActivity.BUNDLE_DAILY_ACTIVE_STEPS, 0);
                prevStepGoal = stepGoal = dailyBundle.getInt(GraphActivity.BUNDLE_DAILY_GOALS, 0);
            } else {
                //TESTING ONLY - PLEASE REMOVE - Randomize it if you can't get anything. Just for visualization purposes.
                stepCount = (int) Math.max(0, Math.floor(10 * Math.random()) - 4);
                activeCount = (int) Math.max(0, Math.floor(10 * Math.random()) - 4);
            }

            intentStepEntries.add(new BarEntry(i, stepCount));
            incidentStepEntries.add(new BarEntry(i, activeCount));
            stepGoalEntries.add(new Entry(i + 0.5F, stepGoal));
        }

        BarDataSet intentDataSet = new BarDataSet(intentStepEntries, GraphActivity.LABEL_INTENTIONAL_STEPS);
        intentDataSet.setColor(Color.CYAN);
        BarDataSet incidentDataSet = new BarDataSet(incidentStepEntries, GraphActivity.LABEL_INCIDENTAL_STEPS);
        incidentDataSet.setColor(Color.LTGRAY);

        xAxis.setLabelCount(incidentStepEntries.size());
        float groupSpace = 0.12f;
        float barSpace = 0.02f;
        float barWidth = 0.42f;

        LineDataSet stepGoalDataSet = new LineDataSet(stepGoalEntries, GraphActivity.LABEL_STEP_GOAL);
        LineData stepGoalData = new LineData();
        stepGoalDataSet.setColor(Color.RED);
        stepGoalDataSet.setLineWidth(6f);
        stepGoalData.addDataSet(stepGoalDataSet);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(intentDataSet);
        dataSets.add(incidentDataSet);

        BarData barData = new BarData(dataSets);
        barData.setBarWidth(barWidth);
        barData.groupBars(0, groupSpace, barSpace);

        CombinedData combinedData = new CombinedData();
        combinedData.setData(barData);
        combinedData.setData(stepGoalData);

        chart.setData(combinedData);
        chart.invalidate();
    }

    private void launchMessaging() {
        Log.d(LOG_TAG, "Launching conversation");
        Intent startConvIntent = new Intent(this, ChatActivity.class);
        startConvIntent.putExtra(ContactsActivity.FROM_USER, fromUser);
        startConvIntent.putExtra(ContactsActivity.TO_USER, toUser);
        startActivityForResult(startConvIntent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        Log.d(LOG_TAG, "Returning to Contacts Page");
        super.onActivityResult(requestCode, resultCode, data);
        setResult(1);

        finish();


    }
}
