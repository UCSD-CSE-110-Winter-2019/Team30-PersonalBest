package team30.personalbest;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowDialog;
import org.robolectric.shadows.ShadowToast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class MainActivityRobolectricTest {

    private MainActivity activity;
    private EditText currTimeTexts;
    private TextView newGoalText;
    private Button submitTime;

    @Before
    public void setUp() throws Exception {

        activity = Robolectric.setupActivity(MainActivity.class);

        currTimeTexts = activity.findViewById(R.id.timeText);
        submitTime = activity.findViewById(R.id.subTime);
        newGoalText = activity.findViewById(R.id.steps_goal);
    }

    @Test
    public void testImprovementToastEncouragement() {
        currTimeTexts.setText("72000000");
        submitTime.performClick();
        assertEquals("Good job you significantly improved your steps from yesterday!", ShadowToast.getTextOfLatestToast());
    }

    /**
     * Testing toast message when goal is achieved
     *
     * Testing if dialog prompt to set up new goal is shown
     */

    @Test
    public void testGoalToastEncouragement() {
        activity.onGoalAchievement(null);
        assertEquals("Achievement get!", ShadowToast.getTextOfLatestToast());
        assertTrue(ShadowDialog.getLatestDialog().isShowing());
    }

    /**
     * Tests that there is not a dialog box that shows up when you don't reach step goal
     */
    @Test
    public void testNoNewDialogAlert() {
        assertTrue(ShadowDialog.getLatestDialog() ==  null);
    }

}
