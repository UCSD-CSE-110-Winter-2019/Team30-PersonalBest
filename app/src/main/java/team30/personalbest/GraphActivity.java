/*
    Uses MPAndroidChart Library to create bar graph for weekly snapshot
    Source: https://github.com/PhilJay/MPAndroidChart
 */
package team30.personalbest;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.ArrayList;
import java.util.List;

public class GraphActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        // Switch between activities
        Button switchScreen = (Button) findViewById(R.id.button_back);
        switchScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        CombinedChart chart = (CombinedChart) findViewById(R.id.chart);
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
        final String[] xLabels = new String[] {
                "Sun",
                "Mon",
                "Tue",
                "Wed",
                "Thu",
                "Fri",
                "Sat"
        };

        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                if ((int) value < xLabels.length && (int) value >= 0) {
                    return xLabels[(int) value];
                }
                else {
                    return "";
                }
            }
        });


        xAxis.setCenterAxisLabels(true);

        // Add entries for intentional steps
        List<BarEntry> intentStepEntries = new ArrayList<>();
        intentStepEntries.add(new BarEntry(0,1));
        intentStepEntries.add(new BarEntry(1, 2));
        intentStepEntries.add(new BarEntry(2,4));
        intentStepEntries.add(new BarEntry(3, 4));
        intentStepEntries.add(new BarEntry(4, 5));
        intentStepEntries.add(new BarEntry(5, 6));
        intentStepEntries.add(new BarEntry(6, 7));

        BarDataSet intentDataSet = new BarDataSet(intentStepEntries, "Intentional Steps");
        intentDataSet.setColor(Color.CYAN);

        // Add entries for intentional steps (dummy data for now..)
        List<BarEntry> incidentStepEntries = new ArrayList<>();
        incidentStepEntries.add(new BarEntry(0,2));
        incidentStepEntries.add(new BarEntry(1,5));
        incidentStepEntries.add(new BarEntry(2,5));
        incidentStepEntries.add(new BarEntry(3,5));
        incidentStepEntries.add(new BarEntry(4,5));
        incidentStepEntries.add(new BarEntry(5,5));
        incidentStepEntries.add(new BarEntry(6,5));

        BarDataSet incidentDataSet = new BarDataSet(incidentStepEntries, "Incidental Steps");
        incidentDataSet.setColor(Color.LTGRAY);

        xAxis.setLabelCount(incidentStepEntries.size());
        float groupSpace = 0.12f;
        float barSpace = 0.02f;
        float barWidth = 0.42f;

        // Graph lines for step goal & intentional walk stats
        List<Entry> stepGoalEntries = new ArrayList<>();
        stepGoalEntries.add(new Entry(.5f,0));
        stepGoalEntries.add(new Entry(1.5f,5));
        stepGoalEntries.add(new Entry(2.5f,5));
        stepGoalEntries.add(new Entry(3.5f,5));
        stepGoalEntries.add(new Entry(4.5f,5));
        stepGoalEntries.add(new Entry(5.5f,5));
        stepGoalEntries.add(new Entry(6.5f,7));

        LineDataSet stepGoalDataSet = new LineDataSet(stepGoalEntries, "Step Goal");
        LineData stepGoalData = new LineData();
        stepGoalDataSet.setColor(Color.RED);
        stepGoalDataSet.setLineWidth(6f);
        stepGoalData.addDataSet(stepGoalDataSet);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(intentDataSet);
        dataSets.add(incidentDataSet);

        BarData barData = new BarData(dataSets);
        barData.setBarWidth(barWidth);
        barData.groupBars(0,groupSpace,barSpace);

        CombinedData combinedData = new CombinedData();
        combinedData.setData(barData);
        combinedData.setData(stepGoalData);


        chart.setData(combinedData);
        chart.invalidate();

    }
}
