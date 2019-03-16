package team30.personalbest.framework.mock;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import team30.personalbest.framework.IFitnessAdapter;
import team30.personalbest.framework.google.IGoogleService;
import team30.personalbest.framework.user.IGoogleFitnessUser;
import team30.personalbest.framework.user.MockFitnessUser;
import team30.personalbest.util.Callback;

public class MockFitnessAdapter implements IFitnessAdapter
{
	public static final String TAG = "MockFitnessAdapter";

	private final List<IGoogleService> googleServices = new ArrayList<>();
	private final IGoogleFitnessUser user;

	private Activity activity;

	public MockFitnessAdapter()
	{
		this.user = new MockFitnessUser(this);
	}

	@Override
	public IFitnessAdapter addGoogleService(IGoogleService googleService)
	{
		this.googleServices.add(googleService);
		return this;
	}

	@Nullable
	@Override
	public void onActivityCreate(Activity activity, Bundle savedInstanceState)
	{
		this.activity = activity;

		Log.w(TAG, "Initializing registered services...");
		//this.initializeGoogleServices();
	}

	@Nullable
	@Override
	public void onActivityResult(Activity activity, int requestCode, int resultCode, @Nullable Intent data)
	{

	}

	private Callback<IFitnessAdapter> initializeGoogleServices()
	{
		final Callback<IFitnessAdapter> callback = new Callback<>();
		{
			this.initializeRemainingGoogleServices(this.googleServices.iterator(), callback);
		}
		return callback;
	}

	private void initializeRemainingGoogleServices(Iterator<IGoogleService> iterator, Callback<IFitnessAdapter> callback)
	{
		if (iterator.hasNext())
		{
			iterator.next()
					.initialize(this)
					.onResult(service -> {
						Log.d(TAG, "" + service);
						this.initializeRemainingGoogleServices(iterator, callback);
					});
		}
		else
		{
			callback.resolve(this);
		}
	}

	@Override
	public Activity getActivity()
	{
		return this.activity;
	}

	@Override
	public IGoogleFitnessUser getFitnessUser()
	{
		return this.user;
	}
}
