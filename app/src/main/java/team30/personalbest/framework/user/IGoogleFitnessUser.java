package team30.personalbest.framework.user;

import team30.personalbest.framework.IFitnessAdapter;
import team30.personalbest.framework.google.FitnessService;
import team30.personalbest.framework.google.GoalService;
import team30.personalbest.framework.google.HeightService;
import team30.personalbest.framework.google.RecordingService;

public interface IGoogleFitnessUser extends IFitnessUser
{
	FitnessService getFitnessService();
	GoalService getGoalService();
	HeightService getHeightService();
	RecordingService getRecordingService();
	IFitnessAdapter getGoogleFitnessAdapter();
}
