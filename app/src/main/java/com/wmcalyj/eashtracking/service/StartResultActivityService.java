package com.wmcalyj.eashtracking.service;

import android.content.Context;
import android.content.Intent;

import com.wmcalyj.eashtracking.Constants;
import com.wmcalyj.eashtracking.ResultViewActivity;

/**
 * Created by mengchaowang on 1/6/17.
 */
public class StartResultActivityService {
    private static StartResultActivityService ourInstance = new StartResultActivityService();

    private StartResultActivityService() {
    }

    public static StartResultActivityService getInstance() {
        return ourInstance;
    }

    public void startViewTrackingResultActivity(Context context, String url) {
        Intent intent = new Intent(context, ResultViewActivity.class);
        intent.putExtra(Constants.REQUEST_URL, url);
        context.startActivity(intent);
    }

}
