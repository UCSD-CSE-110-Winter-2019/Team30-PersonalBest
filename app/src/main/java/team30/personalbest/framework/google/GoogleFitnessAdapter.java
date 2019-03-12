package team30.personalbest.framework.google;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import team30.personalbest.util.Callback;

public class GoogleFitnessAdapter
{
	public static final String TAG = "GoogleFitnessAdapter";

	public static final String RECORDING_SESSION_ID = "PersonalBestRun";
	public static final String RECORDING_SESSION_NAME = "Personal Best Run";
	public static final String RECORDING_SESSION_DESCRIPTION = "Doing a run";
	public static final int RECORDER_SAMPLING_RATE = 1;
	public static final int REQUEST_OAUTH_REQUEST_CODE = 0x1001;

	private final List<IGoogleService> googleServices = new ArrayList<>();

	private Activity activity;

	public GoogleFitnessAdapter addGoogleService(IGoogleService googleService)
	{
		this.googleServices.add(googleService);
		return this;
	}

	@Nullable
	public void onActivityCreate(Activity activity, Bundle savedInstanceState)
	{
		this.activity = activity;

		final FitnessOptions fitnessOptions = FitnessOptions.builder()
				.addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
				.addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
				.addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE, FitnessOptions.ACCESS_WRITE)
				.addDataType(DataType.TYPE_HEIGHT, FitnessOptions.ACCESS_WRITE)
				.build();

		if (!GoogleSignIn.hasPermissions(
				GoogleSignIn.getLastSignedInAccount(activity),
				fitnessOptions))
		{
			GoogleSignIn.requestPermissions(
					activity,
					REQUEST_OAUTH_REQUEST_CODE,
					GoogleSignIn.getLastSignedInAccount(activity),
					fitnessOptions
			);
		}
		else
		{
			this.initializeGoogleServices();
		}
	}

	@Nullable
	public void onActivityResult(Activity activity, int requestCode, int resultCode, @Nullable Intent data)
	{
		this.activity = activity;

		if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_OAUTH_REQUEST_CODE)
		{
			this.initializeGoogleServices();
		}
	}

	private Callback<GoogleFitnessAdapter> initializeGoogleServices()
	{
		final Callback<GoogleFitnessAdapter> callback = new Callback<>();
		{
			this.initializeRemainingGoogleServices(this.googleServices.iterator(), callback);
		}
		return callback;
	}

	private void initializeRemainingGoogleServices(Iterator<IGoogleService> iterator, Callback<GoogleFitnessAdapter> callback)
	{
		if (iterator.hasNext())
		{
			iterator.next()
					.initialize(this)
					.onResult(service -> this.initializeRemainingGoogleServices(iterator, callback));
		}
		else
		{
			callback.resolve(this);
		}
	}

	public Callback<GoogleSignInAccount> getCurrentGoogleAccount()
	{
		final Callback<GoogleSignInAccount> callback = new Callback<>();
		final GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(this.activity);
		if (lastSignedInAccount == null)
		{
			Log.w(TAG, "Could not find signed-in google account.");
			callback.reject();
		}
		else
		{
			callback.resolve(lastSignedInAccount);
		}
		return callback;
	}

	public Activity getActivity()
	{
		return this.activity;
	}
}
