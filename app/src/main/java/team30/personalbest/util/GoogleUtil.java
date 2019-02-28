package team30.personalbest.util;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class GoogleUtil
{
	public static final String TAG = "GoogleUtil";

	public static GoogleSignInAccount getUserAccount(Context context)
	{
		GoogleSignInAccount result = GoogleSignIn.getLastSignedInAccount(context);
		if (result == null)
		{
			Log.w(TAG, "Unable to get google account");
		}
		return result;
	}
}
