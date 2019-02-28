package team30.personalbest.service;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import team30.personalbest.util.Callback;

public interface IService
{
	@Nullable Callback<IService> onActivityCreate(ServiceInitializer serviceInitializer, Activity activity, Bundle savedInstanceState);

	@Nullable Callback<IService> onActivityResult(ServiceInitializer serviceInitializer, Activity activity, int requestCode, int resultCode, @Nullable Intent data);
}
