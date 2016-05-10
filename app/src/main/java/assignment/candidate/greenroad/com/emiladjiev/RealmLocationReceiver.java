package assignment.candidate.greenroad.com.emiladjiev;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.LocationResult;

import assignment.candidate.greenroad.com.emiladjiev.active_android.LUSLocation;
import io.realm.Realm;

/**
 * Created by Emil on 09/05/2016.
 */
public class RealmLocationReceiver extends BroadcastReceiver {

    private String TAG = "RealmLocationReceiver";
    private LocationResult mLocationResult;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Need to check and grab the Intent's extras like so
        if(LocationResult.hasResult(intent)) {
            this.mLocationResult = LocationResult.extractResult(intent);
            Log.i(TAG, "Location Received: " + this.mLocationResult.toString());
            RealmHelper.saveLocationToRealmByCreatingObject(LUSApplication.getInstance().getRealm(), mLocationResult.getLastLocation());

            //notify bus of new location received
            LUSApplication.getInstance().getBus().post(mLocationResult);
        }
    }
}