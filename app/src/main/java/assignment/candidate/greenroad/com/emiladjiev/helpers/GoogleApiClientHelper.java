package assignment.candidate.greenroad.com.emiladjiev.helpers;

import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by Emil on 10/05/2016.
 */
public class GoogleApiClientHelper {

    public static GoogleApiClient getApiClientForLocation(Context context, GoogleApiClient.OnConnectionFailedListener failureResponder, GoogleApiClient.ConnectionCallbacks connectionResponder) {
        return new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(connectionResponder)
                .addOnConnectionFailedListener(failureResponder)
                .addApi(LocationServices.API)
                .build();
    }

    public static LocationRequest getLocationRequest(boolean isHighAccuracy) {
        // Create the LocationRequest object
        LocationRequest locationRequest = LocationRequest.create();
        if (isHighAccuracy) {
            locationRequest.setInterval(Constants.FOREGROUND_UPDATE_INTERVAL);
            locationRequest.setFastestInterval(Constants.FOREGROUND_FASTEST_INTERVAL);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        } else {
            locationRequest.setInterval(Constants.BACKGROUND_UPDATE_INTERVAL);
            locationRequest.setFastestInterval(Constants.BACKGROUND_FASTEST_INTERVAL);
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        }
        return locationRequest;
    }
}
