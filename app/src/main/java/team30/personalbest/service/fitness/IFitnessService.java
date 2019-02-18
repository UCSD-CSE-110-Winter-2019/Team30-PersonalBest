package team30.personalbest.service.fitness;

import team30.personalbest.util.Callback;
import team30.personalbest.snapshot.IFitnessSnapshot;
import team30.personalbest.service.IService;

public interface IFitnessService extends IService
{
	Callback<Iterable<IFitnessSnapshot>> getFitnessSnapshots(long startTime, long stopTime);

	Callback<IFitnessSnapshot> getFitnessSnapshot();

	long getCurrentTime();
}
