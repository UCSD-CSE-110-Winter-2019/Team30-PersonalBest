package team30.personalbest.framework.google;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import team30.personalbest.R;
import team30.personalbest.messeging.ConversationsPageActivity;
import team30.personalbest.messeging.MessageActivity;
import team30.personalbest.messeging.MyUser;
import team30.personalbest.util.Callback;

public class GoogleFitnessAdapter
{
	public static final String TAG = "GoogleFitnessAdapter";

	public static final String RECORDING_SESSION_ID = "PersonalBestRun";
	public static final String RECORDING_SESSION_NAME = "Personal Best Run";
	public static final String RECORDING_SESSION_DESCRIPTION = "Doing a run";
	public static final int RECORDER_SAMPLING_RATE = 1;
	public static final int REQUEST_OAUTH_REQUEST_CODE = 0x1001;
	public static final int RC_SIGN_IN = 0x1002;

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

		List<Scope> scopes = fitnessOptions.getImpliedScopes();
		Scope defaultScope = scopes.get(0);
		scopes.remove(0);
		final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
				.requestIdToken(activity.getString(R.string.web_client_id))
				.requestEmail()
				.requestScopes(defaultScope, scopes.toArray(new Scope[0]))
				.build();
		GoogleSignInClient client = GoogleSignIn.getClient(activity, gso);

		Intent signInIntent = client.getSignInIntent();
		activity.startActivityForResult(signInIntent, RC_SIGN_IN);
	}

	@Nullable
	public void onActivityResult(Activity activity, int requestCode, int resultCode, @Nullable Intent data)
	{
		this.activity = activity;

		if (requestCode == RC_SIGN_IN)
		{
			Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
			try
			{
				Log.d(TAG, "Google sign succeeded.");
				final GoogleSignInAccount account = task.getResult(ApiException.class);

				firebaseAuthWithGoogle( account );

				Log.d( TAG, "Initialzing Firebase");

				final FitnessOptions fitnessOptions = FitnessOptions.builder()
						.addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
						.addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
						.addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE, FitnessOptions.ACCESS_WRITE)
						.addDataType(DataType.TYPE_HEIGHT, FitnessOptions.ACCESS_WRITE)
						.build();

				if (!GoogleSignIn.hasPermissions(
						account,
						fitnessOptions))
				{
					GoogleSignIn.requestPermissions(
							activity,
							REQUEST_OAUTH_REQUEST_CODE,
							account,
							fitnessOptions
					);
				}
				else
				{
					Log.w(TAG, "Initializing registered services...");
					this.initializeGoogleServices();
				}
			}
			catch (ApiException e)
			{
				Log.w(TAG, "Google sign in failed.", e);
			}
		}
		else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_OAUTH_REQUEST_CODE)
		{
			Log.w(TAG, "Initializing registered services (restarted)...");
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


	private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
		Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

		FirebaseAuth mAuth = FirebaseAuth.getInstance();
		AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
		mAuth.signInWithCredential(credential)
				.addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						if (task.isSuccessful()) {
							// Sign in success, update UI with the signed-in user's information
							Log.d(TAG, "signInWithCredential:success");
							FirebaseUser user = mAuth.getCurrentUser();

							FirebaseFirestore firestore = FirebaseFirestore.getInstance();
						} else {
							// If sign in fails, display a message to the user.
							Log.w(TAG, "signInWithCredential:failure", task.getException());
						}

						// ...
					}
				});
	}
}
