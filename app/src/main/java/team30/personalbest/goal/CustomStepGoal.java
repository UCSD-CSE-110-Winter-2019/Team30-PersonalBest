package team30.personalbest.goal;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Goal;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.DataTypeCreateRequest;
import com.google.android.gms.fitness.request.DataUpdateRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.fitness.result.DataTypeResult;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import team30.personalbest.fitness.service.GoogleFitAdapter;
import team30.personalbest.fitness.service.IFitnessService;

public class CustomStepGoal implements StepGoal
{
    private final String LOG_TAG = "PersonalBest";
    private final String goalDataTypeName = "team30.personalbest.goal";
    private DataType goalDataType;
    private static final int DEFAULT_GOAL = 5000;
    private Goal.MetricObjective stepGoal;
    private List<Goal> goals;
    private int currentGoalValue;

    private final IFitnessService fitnessService;
    private final GoogleFitAdapter googleFitAdapter;
    private final GoogleApiClient apiClient;
    private DataSource goalDataSource;
    private final String SESSION_NAME = "GOAL";

    public CustomStepGoal(IFitnessService fitnessService, GoogleFitAdapter googleFitAdapter)
    {
        this(fitnessService, googleFitAdapter, DEFAULT_GOAL);
    }

    public CustomStepGoal(IFitnessService fitnessService, GoogleFitAdapter googleFitAdapter, int initialGoal)
    {
        this.fitnessService = fitnessService;
        this.googleFitAdapter = googleFitAdapter;

        this.currentGoalValue = initialGoal;

        this.apiClient = new GoogleApiClient.Builder(this.googleFitAdapter.getActivity().getApplicationContext())
                .addApi(Fitness.CONFIG_API)
                .addApi(Fitness.HISTORY_API)
                .addApi(Fitness.SESSIONS_API)
                .addApi(Fitness.RECORDING_API)
                .addApi(Fitness.SENSORS_API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        setGoalDataType();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.i(LOG_TAG, "apiClient connection suspended");
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.i(LOG_TAG, "apiClient connection failed");
                    }
                })
                .build();
        this.apiClient.connect();


    }

    private void setGoalDataType()
    {
        PendingResult<DataTypeResult> readResult =
                Fitness.ConfigApi.readDataType(this.apiClient, goalDataTypeName);

        readResult.setResultCallback(
            new ResultCallback<DataTypeResult>() {
                @Override
                public void onResult(DataTypeResult dataTypeResult) {

                    if (dataTypeResult.getStatus().isSuccess())
                    {
                        goalDataType = dataTypeResult.getDataType();
                        Log.i(LOG_TAG, "found goalDataType");
                        Log.i(LOG_TAG, goalDataType.toString());
                        onGoalDataTypeIsReady();


                    }
                    else
                    {
                        handleGoalDataTypeNotFound();
                    }


                }
            }
        );
    }

    private void handleGoalDataTypeNotFound()
    {
        Log.i(LOG_TAG, "failed to find goalDataType, creating a new one");

        final DataTypeCreateRequest createRequest = new DataTypeCreateRequest.Builder()
                .setName(goalDataTypeName)
                .addField("value", Field.FORMAT_INT32)
                .addField( "day", Field.FORMAT_INT32)
                .build();

        PendingResult<DataTypeResult> createResult =
                Fitness.ConfigApi.createCustomDataType(apiClient, createRequest);

        if (createResult != null) {
            Log.i(LOG_TAG, "pending createResult is valid in CustomStepGoal()");

            createResult.setResultCallback(
                    new ResultCallback<DataTypeResult>() {
                        @Override
                        public void onResult(DataTypeResult dataTypeResult) {
                            goalDataType = dataTypeResult.getDataType();
                            Log.i(LOG_TAG, goalDataType.toString());
                            onGoalDataTypeIsReady();

                        }
                    }
            );
        } else {
            Log.i(LOG_TAG, "pending createResult is null in CustomStepGoal()");
        }
    }

    private void onGoalDataTypeIsReady()
    {
        //TODO(sintahks): do things that require goal data type

        goalDataSource = new DataSource.Builder()
                .setAppPackageName( "team30.personalbest")
                .setDataType( goalDataType )
                .setName( SESSION_NAME )
                .setType( DataSource.TYPE_RAW )
                .build();


    }


    public void setGoalValue(int value)
    {

        Log.i(LOG_TAG, "Updating Current Goal to: " + value );
        Calendar endTime = Calendar.getInstance();
        Calendar startTime = Calendar.getInstance();

        startTime.set(Calendar.HOUR_OF_DAY, 0);
        startTime.set(Calendar.MINUTE, 0 );
        startTime.set(Calendar.SECOND, 1);


        endTime.set(Calendar.HOUR_OF_DAY, 23);
        endTime.set(Calendar.MINUTE,59 );
        endTime.set(Calendar.SECOND, 59);


        while( goalDataSource == null ) { }



        DataSet goalDataSet = DataSet.create( goalDataSource );
        DataPoint newGoal = goalDataSet.createDataPoint().setTimeInterval(startTime.getTimeInMillis(), System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        newGoal.getValue( goalDataType.getFields().get(0)).setInt(value);
        goalDataSet.add( newGoal );

        DataUpdateRequest request = new DataUpdateRequest.Builder()
                .setDataSet( goalDataSet )
                .setTimeInterval( startTime.getTimeInMillis(), endTime.getTimeInMillis(), TimeUnit.MILLISECONDS )
                .build();

        Task<Void> response = Fitness.getHistoryClient( googleFitAdapter.getActivity(), GoogleSignIn.getLastSignedInAccount(googleFitAdapter.getActivity())).updateData(request);

        while( !response.isComplete() ) {}

    }


    public List<Integer> getGoalValues( long startTime, long endTime )
    {
        Calendar cal = Calendar.getInstance();

        ArrayList<Integer> result = new ArrayList();

        final DataReadRequest readRequest = new DataReadRequest.Builder()
                .read( goalDataType )
                .setTimeRange( startTime, endTime, TimeUnit.MILLISECONDS )
                .build();

        Task<DataReadResponse> response = Fitness.getHistoryClient( googleFitAdapter.getActivity(), GoogleSignIn.getLastSignedInAccount(googleFitAdapter.getActivity()) ).readData(readRequest);

        while( !response.isComplete() ) {

        }
        List<DataSet> dataSets = response.getResult().getDataSets();

        for(DataSet goalDataSet : dataSets ) {
            for( DataPoint dp : goalDataSet.getDataPoints() ) {
                for( Field field : dp.getDataType().getFields() ) {

                    result.add( dp.getValue(field).asInt() );
                    Log.i( LOG_TAG, "Heres what I got: " + dp.getValue(field));
                }
            }
        }







        return result;

    }

    @Override
    public int getGoalValue()
    {

        Calendar endTime = Calendar.getInstance();
        Calendar startTime = Calendar.getInstance();

        startTime.set(Calendar.HOUR_OF_DAY, 0);
        startTime.set(Calendar.MINUTE, 0 );
        startTime.set(Calendar.SECOND, 1);


        endTime.set(Calendar.HOUR_OF_DAY, 23);
        endTime.set(Calendar.MINUTE, 59 );
        endTime.set(Calendar.SECOND, 59);


        final DataReadRequest readRequest = new DataReadRequest.Builder()
                .read( goalDataType )
                .setTimeRange( startTime.getTimeInMillis(), endTime.getTimeInMillis(), TimeUnit.MILLISECONDS)
                .setLimit(1)
                .build();

        Task<DataReadResponse> response = Fitness.getHistoryClient( googleFitAdapter.getActivity(), GoogleSignIn.getLastSignedInAccount(googleFitAdapter.getActivity()) ).readData(readRequest);

        while( !response.isComplete() ) {

        }
        List<DataSet> dataSets = response.getResult().getDataSets();
        List<DataPoint> dataPoints = dataSets.get(0).getDataPoints();

        DataPoint dp = dataPoints.get(0);
        this.currentGoalValue = dp.getValue( dp.getDataType().getFields().get(0) ).asInt();
        Log.i( LOG_TAG, "Current Goal: " + this.currentGoalValue);

        return this.currentGoalValue;
    }
}
