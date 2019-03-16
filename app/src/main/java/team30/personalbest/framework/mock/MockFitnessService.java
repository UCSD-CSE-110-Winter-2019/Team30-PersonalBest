package team30.personalbest.framework.mock;

import java.util.ArrayList;
import java.util.List;

import team30.personalbest.framework.IFitnessAdapter;
import team30.personalbest.framework.clock.IFitnessClock;
import team30.personalbest.framework.google.FitnessService;
import team30.personalbest.framework.snapshot.FitnessSnapshot;
import team30.personalbest.framework.snapshot.IFitnessSnapshot;
import team30.personalbest.framework.user.IFitnessUser;
import team30.personalbest.util.Callback;

public class MockFitnessService extends FitnessService {
    public static final long SECONDS_PER_DAY = 86400;
    public static final long MILLIS_PER_DAY = 1000 * SECONDS_PER_DAY;

    @Override
    public Callback<FitnessService> initialize(IFitnessAdapter googleFitnessAdapter) {
        return new Callback<>(this);
    }

    @Override
    public Callback<Integer> getDailySteps(IFitnessUser user, IFitnessClock clock, long dayTime) {
        return new Callback<>(0);
    }

    @Override
    public Callback<IFitnessSnapshot> getFitnessSnapshot(IFitnessUser user, IFitnessClock clock) {
        final FitnessSnapshot result = new FitnessSnapshot();
        result.setStartTime(clock.getCurrentTime());
        result.setStopTime(clock.getCurrentTime() + 1);
        return new Callback<>(result);
    }

    @Override
    public Callback<Iterable<IFitnessSnapshot>> getFitnessSnapshots(IFitnessUser user, IFitnessClock clock, long startTime, long stopTime) {
        List<IFitnessSnapshot> results = new ArrayList<>();
        long days = (stopTime - startTime) / MILLIS_PER_DAY;
        for (int i = 0; i < days; ++i) {
            final FitnessSnapshot result = new FitnessSnapshot();
            long time = startTime + i * MILLIS_PER_DAY;
            result.setStartTime(time);
            result.setStopTime(time + MILLIS_PER_DAY);
            results.add(result);
        }
        return new Callback<>(results);
    }
}
