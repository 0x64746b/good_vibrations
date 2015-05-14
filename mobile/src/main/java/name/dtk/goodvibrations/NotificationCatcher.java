package name.dtk.goodvibrations;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import java.util.Arrays;


public class NotificationCatcher extends NotificationListenerService {

    private final static String TAG = NotificationManager.class.getSimpleName();

    private SharedPreferences mPreferences;
    private GoogleApiClient mWear;

    @Override
    public void onCreate() {
        super.onCreate();

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        ConnectionHandler connectionHandler = new ConnectionHandler();
        mWear = new GoogleApiClient.Builder(this)
            .addApi(Wearable.API)
            .addConnectionCallbacks(connectionHandler)
            .addOnConnectionFailedListener(connectionHandler)
            .build();

        mWear.connect();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {

        Notification notification = statusBarNotification.getNotification();
        int defaultVibration = (notification.defaults & notification.DEFAULT_VIBRATE);

        Log.d(
            TAG,
            String.format(
                "Caught notification [%d] '%s' from %s with vibration pattern %s | (%s) %s",
                statusBarNotification.getId(),
                notification.tickerText,
                statusBarNotification.getPackageName(),
                Arrays.toString(notification.vibrate),
                notification.defaults,
                defaultVibration
            )
        );

        if (notification.vibrate != null || defaultVibration != 0) {

            if (mWear.isConnected()) {
                new SendVibrateTask(
                    mWear,
                    mPreferences.getString("default_pattern", null)
                ).execute();
            } else {
                Log.e(TAG, "Received notification while not connected to Wear");
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification statusBarNotification) {}


    private static class ConnectionHandler
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

        private final static String TAG = ConnectionHandler.class.getSimpleName();

        @Override
        public void onConnected(Bundle connectionHint) {
            Log.d(TAG, "Wear connected: " + connectionHint);
        }

        @Override
        public void onConnectionSuspended(int cause) {
            Log.d(TAG, "Wear connection suspended: " + cause);
        }

        @Override
        public void onConnectionFailed(ConnectionResult result) {
            Log.d(TAG, "Wear connection failed: " + result);

            if (result.getErrorCode() == ConnectionResult.API_UNAVAILABLE) {
                Log.e(TAG, "The Android Wear app is not installed");
            }
        }
    }
}