package team30.personalbest.framework.google;

import team30.personalbest.framework.IFitnessAdapter;
import team30.personalbest.util.Callback;

public interface IGoogleService {
    Callback<? extends IGoogleService> initialize(IFitnessAdapter googleFitnessAdapter);
}
