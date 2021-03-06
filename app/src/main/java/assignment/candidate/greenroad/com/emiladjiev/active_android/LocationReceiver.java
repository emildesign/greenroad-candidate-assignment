package assignment.candidate.greenroad.com.emiladjiev.active_android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.LocationResult;

import assignment.candidate.greenroad.com.emiladjiev.LUSApplication;
import assignment.candidate.greenroad.com.emiladjiev.active_android.LUSLocation;

/**
 * Created by Emil on 09/05/2016.
 */
public class LocationReceiver extends BroadcastReceiver {

    private String TAG = this.getClass().getSimpleName();
    private LocationResult mLocationResult;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Need to check and grab the Intent's extras like so
        if(LocationResult.hasResult(intent)) {
            this.mLocationResult = LocationResult.extractResult(intent);
            Log.i(TAG, "Location Received: " + this.mLocationResult.toString());

            LUSLocation lastLocation = new LUSLocation(mLocationResult.getLastLocation());
            lastLocation.save();



            LUSApplication.getInstance().getBus().post(mLocationResult);
        }
    }
}