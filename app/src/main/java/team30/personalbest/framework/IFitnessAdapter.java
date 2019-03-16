package team30.personalbest.framework;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import team30.personalbest.framework.google.IGoogleService;
import team30.personalbest.framework.user.IGoogleFitnessUser;

public interface IFitnessAdapter {
    IFitnessAdapter addGoogleService(IGoogleService googleService);

    @Nullable
    void onActivityCreate(Activity activity, Bundle savedInstanceState);

    @Nullable
    void onActivityResult(Activity activity, int requestCode, int resultCode, @Nullable Intent data);

    Activity getActivity();

    IGoogleFitnessUser getFitnessUser();
}
