package team30.personalbest.fitness.service;

import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;

public interface OnRecordDataUpdateListener
{
    void onRecordDataUpdate(DataSource dataSource, DataSet dataSet, DataPoint dataPoint, DataType dataType);
}
