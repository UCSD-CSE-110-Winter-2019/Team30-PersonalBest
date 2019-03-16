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
import team30.personalbest.messeging.ContactsActivity;
import team30.personalbest.messeging.MessageActivity;

import static junit.framework.TestCase.assertEquals;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class FriendsListTest {
    private MainActivity mainActivity;
    private ContactsActivity contactsActivity;
    Button friendslistbutton;

    @Before
    public void setup() {
        MainActivity.SERVICE_MANAGER_FACTORY.put("mock", () -> new MockFitnessAdapter());
        MainActivity.SERVICE_MANAGER_KEY = "mock";

        this.mainActivity = Robolectric.setupActivity(MainActivity.class);
        contactsActivity = new ContactsActivity();
        friendslistbutton = mainActivity.findViewById(R.id.btn_friends);
    }

    @Test
    public void testShowList() {
        friendslistbutton.callOnClick();

        Intent startedIntent = shadowOf(contactsActivity).getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertEquals(MessageActivity.class, shadowIntent.getIntentClass());
    }
}
