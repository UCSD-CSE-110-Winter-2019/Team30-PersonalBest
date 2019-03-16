package team30.personalbest;

import android.content.Intent;

import android.widget.Button;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import org.robolectric.shadows.ShadowIntent;


import team30.personalbest.framework.mock.MockFitnessAdapter;

import static junit.framework.TestCase.assertEquals;

import static org.robolectric.Shadows.shadowOf;


@RunWith(RobolectricTestRunner.class)
public class MonthlyStatsActivityTest {
    private MainActivity mainActivity;
    private MonthlyStatsActivity monthlyStatsActivity;
    Button monthlyActivityButton;

    @Before
    public void setup() {
        MainActivity.SERVICE_MANAGER_FACTORY.put("mock", () -> new MockFitnessAdapter());
        MainActivity.SERVICE_MANAGER_KEY = "mock";

        this.mainActivity = Robolectric.setupActivity(MainActivity.class);
        monthlyStatsActivity = new MonthlyStatsActivity();
        monthlyActivityButton = mainActivity.findViewById(R.id.btn_monthly_stats);
    }

    @Test
    public void testLaunchMonthlyStats() {
        monthlyActivityButton.callOnClick();

        Intent startedIntent = shadowOf(monthlyStatsActivity).getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertEquals(MonthlyStatsActivity.class, shadowIntent.getIntentClass());
    }
}