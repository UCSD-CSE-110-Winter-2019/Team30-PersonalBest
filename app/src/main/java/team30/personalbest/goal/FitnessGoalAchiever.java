package team30.personalbest.goal;

import android.support.v4.util.Consumer;

import java.util.ArrayList;
import java.util.List;

import team30.personalbest.fitness.OnFitnessUpdateListener;
import team30.personalbest.fitness.snapshot.IFitnessSnapshot;

public class FitnessGoalAchiever implements OnFitnessUpdateListener
{
    private final List<GoalListener> listeners = new ArrayList<>();
    private final IGoalService goalService;

    public FitnessGoalAchiever(IGoalService goalService)
    {
        this.goalService = goalService;
    }

    public FitnessGoalAchiever addGoalListener(GoalListener listener)
    {
        this.listeners.add(listener);
        return this;
    }

    public void removeGoalListener(GoalListener listener) {
        this.listeners.remove(listener);
    }

    private void achieveGoal()
    {
        for(GoalListener listener : this.listeners)
        {
            listener.onGoalAchievement(this.goalService);
        }
    }

    @Override
    public void onFitnessUpdate(final IFitnessSnapshot fitnessSnapshot)
    {
        this.goalService.getGoalValue().onResult(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) {
                if (fitnessSnapshot.getTotalSteps() >= integer)
                {
                    FitnessGoalAchiever.this.achieveGoal();
                }
            }
        });
    }
}
