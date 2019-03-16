package team30.personalbest;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

import team30.personalbest.framework.clock.IFitnessClock;
import team30.personalbest.framework.google.HeightService;
import team30.personalbest.framework.user.IFitnessUser;
import team30.personalbest.util.Callback;

public final class HeightPrompt
{
	public static final String TAG = "HeightPrompt";

	private HeightPrompt() {}

	public static Callback<Float> show(Context context, HeightService heightService, IFitnessUser user, IFitnessClock clock)
	{
		return HeightPrompt.show(context, heightService, user, clock, false);
	}

	public static Callback<Float> show(Context context, HeightService heightService, IFitnessUser user, IFitnessClock clock, boolean cancelable)
	{
		final Callback<Float> callback = new Callback<>();
		final EditText input = new EditText(context);
		input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		final AlertDialog.Builder builder = new AlertDialog.Builder(context)
				.setTitle(context.getString(R.string.title_height_prompt))
				.setView(input)
				.setPositiveButton(context.getString(R.string.prompt_confirm), (dialog, which) -> {
					try
					{
						final String heightString = input.getText().toString();
						final float heightFloat = Float.parseFloat(heightString);

						heightService.setHeight(user, clock, heightFloat).onResult(aFloat -> {
							if (aFloat == null)
								throw new IllegalArgumentException("Unable to set height for google services");

							Log.i(TAG, "Successfully processed height");
							callback.resolve(aFloat);
						});
					}
					catch (Exception e)
					{
						Log.w(TAG, "Failed to process height", e);

						if (!cancelable)
							HeightPrompt.show(context, heightService, user, clock, false);
					}
				})
				.setNegativeButton(context.getString(R.string.prompt_cancel), (dialog, which) -> {
					dialog.cancel();

					if (!cancelable) HeightPrompt.show(context, heightService, user, clock, false);
				});
		builder.show();
		return callback;
	}
}
