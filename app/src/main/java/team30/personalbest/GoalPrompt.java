package team30.personalbest;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

import team30.personalbest.framework.clock.IFitnessClock;
import team30.personalbest.framework.google.GoalService;
import team30.personalbest.framework.user.IFitnessUser;
import team30.personalbest.util.Callback;

public final class GoalPrompt
{
	public static final String TAG = "GoalPrompt";

	public static final int DEFAULT_GOAL_VALUE = 500;
	public static final int SUGGESTED_NEXT_GOAL = 500;

	private GoalPrompt() {}

	public static Callback<Integer> show(Context context, GoalService service, IFitnessUser user, IFitnessClock clock, boolean forceUpdate)
	{
		return GoalPrompt.show(context, service, user, clock, forceUpdate, true);
	}

	public static Callback<Integer> show(Context context, GoalService service, IFitnessUser user, IFitnessClock clock, boolean forceUpdate, boolean cancelable)
	{
		final Callback<Integer> callback = new Callback<>();
		service.getGoalSnapshot(user, clock).onResult(iGoalSnapshot -> {
			final EditText input = new EditText(context);
			input.setInputType(InputType.TYPE_CLASS_TEXT);
			int stepGoal = DEFAULT_GOAL_VALUE;
			if (iGoalSnapshot != null)
			{
				stepGoal = iGoalSnapshot.getGoalValue();

				if (stepGoal >= Integer.MAX_VALUE)
				{
					stepGoal = DEFAULT_GOAL_VALUE;
				}
				else
				{
					stepGoal = stepGoal + SUGGESTED_NEXT_GOAL;
				}
			}
			input.setText("" + stepGoal);
			final AlertDialog.Builder builder = new AlertDialog.Builder(context)
					.setTitle(context.getString(R.string.title_stepgoal_prompt))
					.setView(input)
					.setPositiveButton(context.getString(R.string.prompt_confirm), (dialog, which) -> {
						try
						{
							final String goalString = input.getText().toString();
							final int goalInteger = Integer.parseInt(goalString);
							service.setCurrentGoal(user, clock, goalInteger).onResult(goalSnapshot -> callback.resolve(goalSnapshot.getGoalValue()));

							Log.i(TAG, "Successfully processed step goal");
						}
						catch (Exception e)
						{
							Log.w(TAG, "Failed to process step goal", e);

							if (!cancelable)
								GoalPrompt.show(context, service, user, clock, forceUpdate, false);
							else callback.reject();
						}
					})
					.setNegativeButton(context.getString(R.string.prompt_cancel), (dialog, which) -> {
						if (!cancelable)
							GoalPrompt.show(context, service, user, clock, forceUpdate, false);
						else if (forceUpdate)
							service.setCurrentGoal(user, clock, Integer.MAX_VALUE).onResult(goalSnapshot -> callback.resolve(goalSnapshot.getGoalValue()));
						else callback.reject();

						dialog.cancel();
					});
			builder.show();
		});
		return callback;
	}
}
