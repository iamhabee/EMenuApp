package com.arke.sdk.workmanager;


import android.app.job.JobParameters;
import android.app.job.JobService;

import timber.log.Timber;

public class JobDispatcherService extends JobService {


    @Override
    public boolean onStartJob(JobParameters params) {
        queryServer();
        return false;
    }

    private void queryServer() {

    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
