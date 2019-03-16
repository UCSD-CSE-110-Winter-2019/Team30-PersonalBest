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
public class WeeklyStatsActivityTest {
    private MainActivity mainActivity;
    private GraphActivity graphActivity;
    Button weeklyActivityButton;

    @Before
    public void setup() {
        MainActivity.SERVICE_MANAGER_FACTORY.put("mock", () -> new MockFitnessAdapter());
        MainActivity.SERVICE_MANAGER_KEY = "mock";

        this.mainActivity = Robolectric.setupActivity(MainActivity.class);
        graphActivity = new GraphActivity();
        weeklyActivityButton = mainActivity.findViewById(R.id.btn_weekly_stats);
    }

    //This test is to make sure that Weekly Stats activity launches after pressing on button
    @Test
    public void testLaunchWeeklyStats() {
        weeklyActivityButton.callOnClick();

        Intent startedIntent = shadowOf(graphActivity).getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertEquals(GraphActivity.class, shadowIntent.getIntentClass());
    }
}

