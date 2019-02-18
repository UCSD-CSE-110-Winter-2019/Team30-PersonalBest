package team30.personalbest.goal;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Consumer;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.DataTypeCreateRequest;
import com.google.android.gms.fitness.request.DataUpdateRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.fitness.result.DataTypeResult;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import team30.personalbest.fitness.Callback;
import team30.personalbest.fitness.service.IFitnessService;

public class GoalService implements IGoalService {
    public static final String TAG = "GoalService";
    public static final String DATA_TYPE_GOAL_NAME = "team30.personalbest.goal";

    private final IFitnessService fitnessService;
    private final Activity activity;

    private final String SESSION_NAME = "GOAL";

    private final List<Callback<DataSource>> dataSourceCallbacks = new ArrayList<>();
    private volatile DataSource dataSource;

    private final GoogleApiClient googleApiClient;

    public GoalService(final Activity activity, final IFitnessService fitnessService) {
        this.activity = activity;
        this.fitnessService = fitnessService;

        this.googleApiClient = new GoogleApiClient.Builder(activity)
                .addApi(Fitness.CONFIG_API)
                .addApi(Fitness.HISTORY_API)
                .addApi(Fitness.SESSIONS_API)
                .addApi(Fitness.RECORDING_API)
                .addApi(Fitness.SENSORS_API)
                .build();
    }

    public void initialize()
    {
        this.googleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                Log.d(TAG, "Google API client connection has succeeded.");
                GoalService.this.createDataSource(GoalService.this.activity)
                        .onResult(new Consumer<DataSource>() {
                            @Override
                            public void accept(DataSource dataSource) {
                                synchronized (GoalService.this) {
                                    List<Callback<DataSource>> callbacks =
                                            GoalService.this.dataSourceCallbacks;
                                    for (Callback<DataSource> callback : callbacks) {
                                        callback.resolve(dataSource);
                                    }
                                    callbacks.clear();
                                    GoalService.this.dataSource = dataSource;
                                }
                            }
                        });
            }

            @Override
            public void onConnectionSuspended(int i) {
                Log.d(TAG, "Google API client connection is suspended...");
            }
        });
        this.googleApiClient.registerConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                Log.d(TAG, "Google API client connection has failed.");
            }
        });
        this.googleApiClient.connect();
    }

    private Callback<DataSource> createDataSource(final Activity activity) {
        final Callback<DataSource> callback = new Callback<>();
        this.getDataType().onResult(new Consumer<DataType>() {
            @Override
            public void accept(DataType dataType) {
                final DataSource result = new DataSource.Builder()
                        .setAppPackageName(activity)
                        .setDataType(dataType)
                        .setName(SESSION_NAME)
                        .setType(DataSource.TYPE_RAW)
                        .build();
                callback.resolve(result);
            }
        });
        return callback;
    }

    private Callback<DataSource> getDataSource() {
        final Callback<DataSource> callback = new Callback<>();
        synchronized (this) {
            if (this.dataSource == null) {
                this.dataSourceCallbacks.add(callback);
            } else {
                callback.resolve(this.dataSource);
            }
        }
        return callback;
    }

    private Callback<DataType> getDataType() {
        final Callback<DataType> callback = new Callback<>();
        Fitness.ConfigApi.readDataType(this.googleApiClient, DATA_TYPE_GOAL_NAME)
                .setResultCallback(new ResultCallback<DataTypeResult>() {
                    @Override
                    public void onResult(@NonNull DataTypeResult dataTypeResult) {
                        if (dataTypeResult.getStatus().isSuccess()) {
                            Log.d(TAG, "Successfully found existing goal data type.");
                            final DataType result = dataTypeResult.getDataType();
                            callback.resolve(result);
                        } else {
                            Log.d(TAG, "Failed to find goal data type...creating a new one...");
                            final DataTypeCreateRequest createRequest = new DataTypeCreateRequest.Builder()
                                    .setName(DATA_TYPE_GOAL_NAME)
                                    .addField("value", Field.FORMAT_INT32)
                                    .addField("day", Field.FORMAT_INT32)
                                    .build();

                            Fitness.ConfigApi.createCustomDataType(GoalService.this.googleApiClient, createRequest)
                                    .setResultCallback(new ResultCallback<DataTypeResult>() {
                                        @Override
                                        public void onResult(@NonNull DataTypeResult dataTypeResult) {
                                            final DataType result = dataTypeResult.getDataType();
                                            callback.resolve(result);
                                        }
                                    });
                        }
                    }
                });
        return callback;
    }

    @Override
    public Callback<Integer> setGoalValue(final int value) {
        final Callback<Integer> callback = new Callback<>();
        this.getDataSource().onResult(new Consumer<DataSource>() {
            @Override
            public void accept(final DataSource dataSource) {
                GoalService.this.getDataType().onResult(new Consumer<DataType>() {
                    @Override
                    public void accept(final DataType dataType) {
                        final GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(GoalService.this.activity);
                        if (lastSignedInAccount != null) {
                            Calendar stopCal = Calendar.getInstance();
                            Calendar startCal = Calendar.getInstance();
                            startCal.set(Calendar.HOUR_OF_DAY, 0);
                            startCal.set(Calendar.MINUTE, 0);
                            startCal.set(Calendar.SECOND, 1);
                            startCal.add(Calendar.DAY_OF_MONTH, -1);
                            stopCal.set(Calendar.HOUR_OF_DAY, 23);
                            stopCal.set(Calendar.MINUTE, 59);
                            stopCal.set(Calendar.SECOND, 59);
                            stopCal.add(Calendar.DAY_OF_MONTH, -1);
                            final long startTime = startCal.getTimeInMillis();
                            final long stopTime = stopCal.getTimeInMillis();

                            final DataSet dataSet = DataSet.create(dataSource);
                            DataPoint newGoal = dataSet.createDataPoint()
                                    .setTimeInterval(startTime, stopTime, TimeUnit.MILLISECONDS);
                            newGoal.getValue(dataType.getFields().get(0)).setInt(value);
                            dataSet.add(newGoal);

                            final DataUpdateRequest updateRequest = new DataUpdateRequest.Builder()
                                    .setDataSet(dataSet)
                                    .setTimeInterval(startTime, stopTime, TimeUnit.MILLISECONDS)
                                    .build();

                            Fitness.getHistoryClient(GoalService.this.activity, lastSignedInAccount)
                                    .updateData(updateRequest)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "Successfully updated goal value.");
                                            callback.resolve(value);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Failed to update goal value.", e);
                                            callback.resolve(null);
                                        }
                                    });
                        } else {
                            throw new IllegalStateException("Unable to find google account to set goal");
                        }
                    }
                });
            }
        });

        return callback;
    }

    @Override
    public Callback<Iterable<Integer>> getGoalValues(final long startTime, final long stopTime) {
        final Callback<Iterable<Integer>> callback = new Callback<>();

        this.getDataType().onResult(new Consumer<DataType>() {
            @Override
            public void accept(DataType dataType) {
                final GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(GoalService.this.activity);
                if (lastSignedInAccount != null) {
                    final DataReadRequest readRequest = new DataReadRequest.Builder()
                            .read(dataType)
                            .setTimeRange(startTime, stopTime, TimeUnit.MILLISECONDS)
                            .build();

                    Fitness.getHistoryClient(GoalService.this.activity, lastSignedInAccount)
                            .readData(readRequest)
                            .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                                @Override
                                public void onSuccess(DataReadResponse dataReadResponse) {
                                    final List<Integer> goals = new ArrayList<>();
                                    final List<DataSet> dataSets = dataReadResponse.getDataSets();
                                    for (DataSet dataSet : dataSets) {
                                        for (DataPoint dataPoint : dataSet.getDataPoints()) {
                                            try {
                                                goals.add(dataPoint.getValue(dataPoint.getDataType().getFields().get(0)).asInt());
                                            } catch (Exception e) {
                                                Log.w(TAG, "Cannot find goal field value for data", e);
                                            }
                                        }
                                    }
                                    callback.resolve(goals);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Failed to find goal", e);
                                    callback.resolve(null);
                                }
                            });
                } else {
                    Log.w(TAG, "Unable to find google account for goal data");
                    callback.resolve(null);
                }
            }
        });

        return callback;
    }

    @Override
    public Callback<Integer> getGoalValue() {
        final Callback<Integer> callback = new Callback<>();
        this.getDataType().onResult(new Consumer<DataType>() {
            @Override
            public void accept(DataType dataType) {
                final GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(GoalService.this.activity);

                if (lastSignedInAccount != null) {
                    Calendar stopCal = Calendar.getInstance();
                    Calendar startCal = Calendar.getInstance();
                    startCal.set(Calendar.HOUR_OF_DAY, 0);
                    startCal.set(Calendar.MINUTE, 0);
                    startCal.set(Calendar.SECOND, 1);
                    startCal.add(Calendar.DAY_OF_MONTH, -1);
                    stopCal.set(Calendar.HOUR_OF_DAY, 23);
                    stopCal.set(Calendar.MINUTE, 59);
                    stopCal.set(Calendar.SECOND, 59);
                    stopCal.add(Calendar.DAY_OF_MONTH, -1);
                    final long startTime = startCal.getTimeInMillis();
                    final long stopTime = stopCal.getTimeInMillis();

                    final DataReadRequest readRequest = new DataReadRequest.Builder()
                            .read(dataType)
                            .setTimeRange(startTime, stopTime, TimeUnit.MILLISECONDS)
                            .setLimit(1)
                            .build();

                    Fitness.getHistoryClient(GoalService.this.activity, lastSignedInAccount)
                            .readData(readRequest)
                            .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                                @Override
                                public void onSuccess(DataReadResponse dataReadResponse) {
                                    List<DataSet> dataSets = dataReadResponse.getDataSets();
                                    if (!dataSets.isEmpty()) {
                                        List<DataPoint> dataPoints = dataSets.get(0).getDataPoints();
                                        if (!dataPoints.isEmpty()) {
                                            DataPoint dataPoint = dataPoints.get(0);
                                            final int goalValue = dataPoint.getValue(dataPoint.getDataType().getFields().get(0)).asInt();
                                            callback.resolve(goalValue);
                                            return;
                                        }
                                    }

                                    Log.w(TAG, "Unable to find goal");
                                    callback.resolve(0);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Failed to find goal", e);
                                    callback.resolve(null);
                                }
                            });
                } else {
                    Log.w(TAG, "Unable to find google account for goal data");
                    callback.resolve(null);
                }
            }
        });
        return callback;
    }
}
