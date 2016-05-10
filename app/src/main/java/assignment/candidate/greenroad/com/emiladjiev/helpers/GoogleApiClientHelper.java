package assignment.candidate.greenroad.com.emiladjiev.helpers;

import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
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
}
