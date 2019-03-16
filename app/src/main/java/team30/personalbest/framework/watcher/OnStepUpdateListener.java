package team30.personalbest.framework.watcher;

import team30.personalbest.framework.clock.IFitnessClock;
import team30.personalbest.framework.user.IFitnessUser;

public interface OnStepUpdateListener {
    void onStepUpdate(IFitnessUser user, IFitnessClock clock, Integer totalSteps);
}
