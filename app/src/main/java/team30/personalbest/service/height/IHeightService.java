package team30.personalbest.service.height;

import android.app.Activity;

import team30.personalbest.service.IService;
import team30.personalbest.util.Callback;

public interface IHeightService extends IService
{
	Callback<? extends IService> initialize(Activity activity);
	Callback<Float> setHeight(float value);
	Callback<Float> getHeight();
}
