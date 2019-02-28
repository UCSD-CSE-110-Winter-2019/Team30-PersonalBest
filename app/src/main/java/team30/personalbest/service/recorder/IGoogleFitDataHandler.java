package team30.personalbest.service.recorder;

import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;

public interface IGoogleFitDataHandler
{
	DataPoint onProcessDataPoint(DataSource dataSource, DataSet dataSet, DataPoint dataPoint, DataType dataType);
}
