package team30.personalbest;

import android.widget.Button;
import android.widget.EditText;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowToast;


import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class MainActivityRobolectricTest {
    private static final String TEST_SERVICE = "TEST_SERVICE";

    private MainActivity activity;
    private EditText currTimeTexts;
    private Button submitTime;
    private long currTime;

    @Before
    public void setUp() throws Exception {

        activity = Robolectric.setupActivity(MainActivity.class);

        currTimeTexts = activity.findViewById(R.id.timeText);
        submitTime = activity.findViewById(R.id.subTime);
        currTimeTexts.setText("72000000");
    }

    @Test
    public void testImprovementToastEncouragement() {
        submitTime.performClick();
        assertEquals("Good job you significantly improved your steps from yesterday!", ShadowToast.getTextOfLatestToast());
    }

    @Test
    public void testGoalToastEncouragement() {
        activity.onGoalAchievement(null);
        assertEquals("Achievement get!", ShadowToast.getTextOfLatestToast());
    }

}
