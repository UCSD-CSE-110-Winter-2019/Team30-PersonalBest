/*
    Uses MPAndroidChart Library to create bar graph for weekly snapshot
    Source: https://github.com/PhilJay/MPAndroidChart
 */
package team30.personalbest;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

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
import java.util.Calendar;
import java.util.List;

import team30.personalbest.framework.clock.FitnessClock;
import team30.personalbest.util.Callback;

public class GraphActivity extends AppCompatActivity
{
	public static final String BUNDLE_WEEKLY_STATS = "weekly_stats";
	public static final String BUNDLE_WEEKLY_PREFIX = "weekly_day_";
	public static final String BUNDLE_DAILY_ACTIVE_STEPS = "daily_active_steps";
	public static final String BUNDLE_DAILY_STEPS = "daily_steps";
	public static final String BUNDLE_DAILY_MPH = "daily_mph";
	public static final String BUNDLE_DAILY_TIMES = "daily_times";
	public static final String BUNDLE_DAILY_GOALS = "daily_goals";
	public static final String BUNDLE_WEEKLY_TIME = "weekly_time";
	public static final int BUNDLE_WEEK_LENGTH = 7;

	public static final String[] WEEK_DAY_LABELS = new String[]{
			"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
	};
	public static final String LABEL_INTENTIONAL_STEPS = "Intentional Steps";
	public static final String LABEL_INCIDENTAL_STEPS = "Incidental Steps";
	public static final String LABEL_STEP_GOAL = "Step Goal";

	private long startTime = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_graph);

		// Switch between activities
		Button switchScreen = findViewById(R.id.button_back);
		switchScreen.setOnClickListener(view -> finish());

		//Weekly buttons
		Button prevWeek = findViewById(R.id.button_prev);
		prevWeek.setOnClickListener(view -> {
			//isValidWeek();
			getPrevWeeksData().onResult(bundle -> {
				this.finish();
				final Intent intent = new Intent(this, GraphActivity.class);
				intent.putExtra(BUNDLE_WEEKLY_STATS, bundle);
				this.startActivity(intent);
			});
		});

		Button nextWeek = findViewById(R.id.button_next);
		nextWeek.setOnClickListener(view -> {
			//isValidWeek();
			getNextWeeksData().onResult(bundle -> {
				this.finish();
				final Intent intent = new Intent(this, GraphActivity.class);
				intent.putExtra(BUNDLE_WEEKLY_STATS, bundle);
				this.startActivity(intent);
			});
		});

		CombinedChart chart = findViewById(R.id.chart);
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
		final String[] xLabels = WEEK_DAY_LABELS;
		xAxis.setValueFormatter((value, axis) -> {
			if ((int) value < xLabels.length && (int) value >= 0)
			{
				return xLabels[(int) value];
			}
			else
			{
				return "";
			}
		});
		xAxis.setCenterAxisLabels(true);

		final Bundle bundle = this.getIntent().getExtras();
		if (bundle == null)
		{
			this.finish();
			return;
		}
		final Bundle weeklyBundle = bundle.getBundle(BUNDLE_WEEKLY_STATS);
		if (weeklyBundle == null)
		{
			this.finish();
			return;
		}

		this.startTime = weeklyBundle.getLong(BUNDLE_WEEKLY_TIME);
		// Add entries for intentional steps
		List<BarEntry> intentStepEntries = new ArrayList<>();
		// Add entries for intentional steps
		List<BarEntry> incidentStepEntries = new ArrayList<>();
		// Graph lines for step goal
		List<Entry> stepGoalEntries = new ArrayList<>();

		int prevStepGoal = 0;
		for (int i = 0; i < BUNDLE_WEEK_LENGTH; ++i)
		{
			Bundle dailyBundle = weeklyBundle.getBundle(BUNDLE_WEEKLY_PREFIX + i);

			int stepCount;
			int activeCount;
			int stepGoal = prevStepGoal;
			if (dailyBundle != null)
			{
				stepCount = dailyBundle.getInt(BUNDLE_DAILY_STEPS, 0);
				activeCount = dailyBundle.getInt(BUNDLE_DAILY_ACTIVE_STEPS, 0);
				prevStepGoal = stepGoal = dailyBundle.getInt(BUNDLE_DAILY_GOALS, 0);
			}
			else
			{
				//Randomize it if you can't get anything. Just for visualization purposes.
				stepCount = (int) Math.floor(10 * Math.random());
				activeCount = (int) Math.floor(10 * Math.random());
			}

			intentStepEntries.add(new BarEntry(i, stepCount));
			incidentStepEntries.add(new BarEntry(i, activeCount));
			stepGoalEntries.add(new Entry(i + 0.5F, stepGoal));
		}

		BarDataSet intentDataSet = new BarDataSet(intentStepEntries, LABEL_INTENTIONAL_STEPS);
		intentDataSet.setColor(Color.CYAN);
		BarDataSet incidentDataSet = new BarDataSet(incidentStepEntries, LABEL_INCIDENTAL_STEPS);
		incidentDataSet.setColor(Color.LTGRAY);

		xAxis.setLabelCount(incidentStepEntries.size());
		float groupSpace = 0.12f;
		float barSpace = 0.02f;
		float barWidth = 0.42f;

		LineDataSet stepGoalDataSet = new LineDataSet(stepGoalEntries, LABEL_STEP_GOAL);
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

	private boolean isValidWeek()
	{
		return true;
	}

	private Callback<Bundle> getPrevWeeksData()
	{
		FitnessClock clock = new FitnessClock();
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.setTimeInMillis(this.startTime);
		calendar.add(Calendar.WEEK_OF_YEAR, -1);
		final long nextStartTime = calendar.getTimeInMillis();
		clock.freezeTimeAt(nextStartTime);
		return GraphBundler.makeBundle(clock, MainActivity.LOCAL_USER);
	}

	private Callback<Bundle> getNextWeeksData()
	{
		FitnessClock clock = new FitnessClock();
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.setTimeInMillis(this.startTime);
		calendar.add(Calendar.WEEK_OF_YEAR, 1);
		final long nextStartTime = calendar.getTimeInMillis();
		clock.freezeTimeAt(nextStartTime);
		return GraphBundler.makeBundle(clock, MainActivity.LOCAL_USER);
	}
}
