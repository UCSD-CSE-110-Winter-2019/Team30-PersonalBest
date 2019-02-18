package team30.personalbest.fitness.service;

import team30.personalbest.fitness.Callback;

public interface IHeightService
{
    Callback<Float> setHeight(float value);
    Callback<Float> getHeight();
}
