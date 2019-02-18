package team30.personalbest.service.height;

import team30.personalbest.service.IService;
import team30.personalbest.util.Callback;

public interface IHeightService extends IService
{
	Callback<Float> setHeight(float value);

	Callback<Float> getHeight();
}
