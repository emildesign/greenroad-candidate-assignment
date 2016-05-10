package assignment.candidate.greenroad.com.emiladjiev;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by Emil on 09/05/2016.
 */

public class LocationLoggerServiceManager extends BroadcastReceiver {

    protected final static String UPDATES_KEY = "updates_key";
    private SharedPreferences mPrefs;
    public static final String TAG = "LoggerServiceManager";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Make sure we are getting the right intent
        if( "android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            boolean mUpdatesRequested = false;

            //checking shared preferences for updating key.
            mPrefs = context.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
            if (mPrefs.contains(UPDATES_KEY)) {
                mUpdatesRequested = mPrefs.getBoolean(UPDATES_KEY, false);
            }
            if(mUpdatesRequested){
                ComponentName comp = new ComponentName(context.getPackageName(), BackgroundLocationService.class.getName());
                ComponentName service = context.startService(new Intent().setComponent(comp));
                if (null == service){
                    // something really wrong here
                    Log.e(TAG, "Could not start service " + comp.toString());
                }
            }

        } else {
            Log.e(TAG, "Received unexpected intent " + intent.toString());
        }
    }
}