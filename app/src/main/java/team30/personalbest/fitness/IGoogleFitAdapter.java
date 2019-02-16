package team30.personalbest.fitness;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public interface IGoogleFitAdapter
{
    GoogleFitAdapter addOnReadyListener(OnGoogleFitReadyListener listener);
    void removeOnReadyListener(OnGoogleFitReadyListener listener);
    void onActivityCreate(Activity activity, Bundle savedInstanceState);
    void onActivityResult(Activity activity,
                          int requestCode,
                          int resultCode,
                          @Nullable Intent data);
    GoogleSignInAccount getCurrentGoogleAccount();
    Activity getActivity();
    long getCurrentTime();
    boolean isReady();
}
