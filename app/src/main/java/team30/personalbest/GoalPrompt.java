package team30.personalbest;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

import team30.personalbest.service.goal.GoalService;
import team30.personalbest.util.Callback;

public final class GoalPrompt
{
	public static final String TAG = "GoalPrompt";

	private GoalPrompt() {}

	public static Callback<Integer> show(Context context, GoalService service, boolean forceUpdate)
	{
		return GoalPrompt.show(context, service, forceUpdate, true);
	}

	public static Callback<Integer> show(Context context, GoalService service, boolean forceUpdate, boolean cancelable)
	{
		final Callback<Integer> callback = new Callback<>();
		final EditText input = new EditText(context);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		final AlertDialog.Builder builder = new AlertDialog.Builder(context)
				.setTitle(context.getString(R.string.title_stepgoal_prompt))
				.setView(input)
				.setPositiveButton(context.getString(R.string.prompt_confirm), (dialog, which) -> {
					try
					{
						final String goalString = input.getText().toString();
						final int goalInteger = Integer.parseInt(goalString);
						service.setCurrentGoal(goalInteger);

						Log.i(TAG, "Successfully processed step goal");
					}
					catch (Exception e)
					{
						Log.w(TAG, "Failed to process step goal", e);

						if (!cancelable) GoalPrompt.show(context, service, forceUpdate, false);
					}
				})
				.setNegativeButton(context.getString(R.string.prompt_cancel), (dialog, which) -> {
					if (!cancelable) GoalPrompt.show(context, service, forceUpdate, false);
					else if (forceUpdate) service.setCurrentGoal(Integer.MAX_VALUE);
					dialog.cancel();
				});
		builder.show();
		return callback;
	}
}
