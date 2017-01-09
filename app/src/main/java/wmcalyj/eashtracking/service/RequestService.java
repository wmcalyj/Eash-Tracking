package wmcalyj.eashtracking.service;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.jsoup.helper.StringUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import wmcalyj.eashtracking.Constants;
import wmcalyj.eashtracking.RecentlySearchedTrackingNumber;

/**
 * Created by mengchaowang on 1/5/17.
 */

public class RequestService {
    private static final String MyTag = "RequestServiceTag";
    private static RequestService instance = null;
    private static String requestResult;
    private static URLPinger currentTask;

    private RequestService() {
        // Nothing
    }

    public static RequestService getInstance() {
        if (instance == null) {
            instance = new RequestService();
        }
        return instance;
    }

    private static void broadCastRequestResult(boolean succeed, Context context) {
        Intent intent = new Intent(Constants.REQUEST_RESULT_BROADCAST_MESSAGE);
        intent.putExtra(Constants.REQUEST_RESULT_BOOLEAN, succeed);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public void makeRequest(final String trackingNumber, final String carrier, final
    Context context) {
        final String url = URLService.getInstance().getRequestUrl(trackingNumber, carrier);
        URLPingerHolder holder = new URLPingerHolder(url, trackingNumber, carrier, context);
        currentTask = new URLPinger();
        currentTask.execute(holder);
    }

    public boolean cancelRequest() {
        if (currentTask != null && (currentTask.getStatus() == AsyncTask.Status.PENDING ||
                currentTask.getStatus() == AsyncTask.Status.RUNNING)) {
            currentTask.cancel(true);
            return true;
        } else {
            return false;
        }
    }

    private class URLPingerHolder {
        String url, trackingNumber, carrier;
        Context context;
        int code;

        public URLPingerHolder(String url, String trackingNumber, String carrier, Context context) {
            this.url = url;
            this.trackingNumber = trackingNumber;
            this.carrier = carrier;
            this.context = context;
        }
    }

    private class URLPinger extends AsyncTask<URLPingerHolder, Void, URLPingerHolder> {
        /**
         * The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute()
         */
        protected URLPingerHolder doInBackground(URLPingerHolder... params) {
            HttpURLConnection connection = null;
            int code = -1;
            try {
                String url = params[0].url;
                Log.d(MyTag, "URL: " + url);
                URL u = new URL(url);
                connection = (HttpURLConnection) u.openConnection();
                connection.setRequestMethod("HEAD");
                code = connection.getResponseCode();
                // You can determine on HTTP return code received. 200 is success.
            } catch (IOException e) {
                Log.e(MyTag, e.getMessage(), e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            params[0].code = code;
            return params[0];
        }

        /**
         * The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground()
         */
        protected void onPostExecute(URLPingerHolder holder) {
            if (!validateURLPingerHolder(holder)) {
                Log.d(MyTag, "code: " + holder.code);
                broadCastRequestResult(false, holder.context);
            } else {
                // Successfully get response

                // 1. Add tracking number to LRU
                RecentlySearchedTrackingNumber.getInstance().addTrackingNumber
                        (holder.trackingNumber, holder.carrier);

                // 2. Open new activity to the url
                StartResultActivityService.getInstance().startViewTrackingResultActivity
                        (holder.context, holder.url);

                // 3. Broadcast the good news
                broadCastRequestResult(true, holder.context);
            }
        }

        private boolean validateURLPingerHolder(URLPingerHolder holder) {
            return holder.code != -1;

        }
    }
}
