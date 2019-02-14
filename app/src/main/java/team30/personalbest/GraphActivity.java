package team30.personalbest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.List;

public class GraphActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        BarChart chart = (BarChart) findViewById(R.id.chart);

        List<BarEntry> entries = new ArrayList<BarEntry>();
        entries.add(new BarEntry(4f,4));
        entries.add(new BarEntry(8f,2));

        BarDataSet dataset = new BarDataSet(entries, "Test 1");

        // Creating week labels for x-axis
        String[] xLabels = new String[] {
                "Sunday",
                "Monday",
                "Tuesday",
                "Wednesday",
                "Thursday",
                "Friday",
                "Saturday"
        };

        dataset.setStackLabels(xLabels);

        BarData data = new BarData(dataset);
        chart.setData(data);



    }
}
