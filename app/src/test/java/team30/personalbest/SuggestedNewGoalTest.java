package team30.personalbest;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.tools.ant.Main;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowDialog;

import team30.personalbest.framework.IFitnessAdapter;
import team30.personalbest.framework.IServiceManagerBuilder;
import team30.personalbest.framework.mock.MockFitnessAdapter;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.robolectric.RuntimeEnvironment.application;
import static org.robolectric.Shadows.shadowOf;


@RunWith(RobolectricTestRunner.class)
public class SuggestedNewGoalTest {
    private MainActivity mainActivity;
    String goal = "Your Step Goal: 500 steps";
    Button newGoal;
    TextView newGoalText;

    @Before
    public void setup() {
        MainActivity.SERVICE_MANAGER_FACTORY.put("mock", () -> new MockFitnessAdapter());
        MainActivity.SERVICE_MANAGER_KEY = "mock";

        this.mainActivity = Robolectric.setupActivity(MainActivity.class);
        newGoal = mainActivity.findViewById(R.id.btn_stepgoal_new);
        newGoalText = mainActivity.findViewById(R.id.display_stepgoal);

        //System.out.println(newGoalText.getText());
        newGoalText.setText(goal);
        //System.out.println(newGoalText.getText());
    }

    @Test
    public void newGoalOnClickTest(){
        newGoal.performClick();
        assertTrue(!ShadowAlertDialog.getShownDialogs().isEmpty());
    }

    @Test
    public void getLatestAlertDialogTest() {
        assertEquals(null, ShadowAlertDialog.getLatestAlertDialog());
        AlertDialog dialog = new AlertDialog.Builder(mainActivity).show();
        assertEquals(dialog, ShadowAlertDialog.getLatestAlertDialog());
    }


    @Test
    public void shouldSetViewTest() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        EditText view = new EditText(mainActivity);
        builder.setView(view);

        AlertDialog alert = builder.create();
        assertEquals(view, shadowOf(alert).getView());
    }

    @Test
    public void clickingDismissesOkDialogTest() {
        AlertDialog alert = new AlertDialog.Builder(mainActivity)
                .setPositiveButton("Ok", null).create();
        alert.show();

        assertTrue(alert.isShowing());
        alert.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        assertFalse(alert.isShowing());
    }

    @Test
    public void clickingDismissesCancelDialogTest() {
        AlertDialog alert = new AlertDialog.Builder(mainActivity)
                .setNegativeButton("Cancel", null).create();
        alert.show();

        assertTrue(alert.isShowing());
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).performClick();
        assertFalse(alert.isShowing());
    }

   @Test
    public void onClickUpdateNewStepGoalPositiveButtonTest() {
        newGoal.performClick();
        assertEquals("Your Step Goal: 500 steps", newGoalText.getText());
    }
}
