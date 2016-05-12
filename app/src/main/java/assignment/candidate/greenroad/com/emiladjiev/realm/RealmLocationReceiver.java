package assignment.candidate.greenroad.com.emiladjiev.realm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.LocationResult;

import assignment.candidate.greenroad.com.emiladjiev.LUSApplication;

/**
 * Created by Emil on 09/05/2016.
 */
public class RealmLocationReceiver extends BroadcastReceiver {

    private String TAG = "RealmLocationReceiver";
    private LocationResult mLocationResult;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(LocationResult.hasResult(intent)) {
            this.mLocationResult = LocationResult.extractResult(intent);
            Log.i(TAG, "Location Received: " + this.mLocationResult.toString());
            RealmHelper.saveLocationToRealmByCreatingObject(LUSApplication.getInstance().getRealm(), mLocationResult.getLastLocation());
            LUSApplication.getInstance().getBus().post(mLocationResult);
        }
    }
}