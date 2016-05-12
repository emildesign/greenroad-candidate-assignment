package assignment.candidate.greenroad.com.emiladjiev;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import assignment.candidate.greenroad.com.emiladjiev.helpers.AndroidHelper;
import assignment.candidate.greenroad.com.emiladjiev.helpers.GoogleApiClientHelper;

/**
 * BackgroundLocationService used for tracking user location in the background.
 * It uses the new GoogleApiClient LocationService for the retriving the location updates.
 *
 * @author Emil on 09/05/2016.
 */
public class BackgroundLocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "LocationService";
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    // Flag that indicates if a request is underway.
    private boolean mInProgress;
    private PowerManager.WakeLock mWakeLock;
    private Boolean servicesAvailable = false;

    // Binder
    IBinder mBinder = new LocalBinder();
    public class LocalBinder extends Binder {
        public BackgroundLocationService getServerInstance() {
            return BackgroundLocationService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInProgress = false;
        servicesAvailable = AndroidHelper.isPlayServicesAvailable(this);
        mLocationRequest = GoogleApiClientHelper.getLocationRequest(true);
        setUpGoogleApiClientIfNeeded();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        PowerManager mgr = (PowerManager) getSystemService(Context.POWER_SERVICE);

        /*
        WakeLock is reference counted so we don't want to create multiple WakeLocks. So do a check before initializing and acquiring.
        This will fix the "java.lang.Exception: WakeLock finalized while still held: MyWakeLock" error that you may find.
        */
        if (this.mWakeLock == null) {
            this.mWakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
        }

        if (!this.mWakeLock.isHeld()) {
            this.mWakeLock.acquire();
        }

        if (!servicesAvailable || mGoogleApiClient.isConnected() || mInProgress)
            return START_STICKY;

        setUpGoogleApiClientIfNeeded();
        if (!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting() && !mInProgress) {
            //appendLog(DateFormat.getDateTimeInstance().format(new Date()) + ": Started", Constants.LOG_FILE);
            mInProgress = true;
            mGoogleApiClient.connect();
        } else {
            startLocationUpdates();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // Turn off the request flag
        this.mInProgress = false;

        stopLocationUpdates();
        if (this.servicesAvailable && this.mGoogleApiClient != null) {
            this.mGoogleApiClient.unregisterConnectionCallbacks(this);
            this.mGoogleApiClient.unregisterConnectionFailedListener(this);
            this.mGoogleApiClient.disconnect();
            this.mGoogleApiClient = null;
        }

        if (this.mWakeLock != null) {
            this.mWakeLock.release();
            this.mWakeLock = null;
        }

        //Toast.makeText(this, DateFormat.getDateTimeInstance().format(new Date()) + " : Disconnected", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    /*
     * Create a new location client, using the enclosing class to
     * handle callbacks.
     */
    private void setUpGoogleApiClientIfNeeded() {
        if (mGoogleApiClient == null)
            mGoogleApiClient = GoogleApiClientHelper.getApiClientForLocation(this, this, this);
    }


    @Override
    public void onConnected(Bundle bundle) {
        //Toast.makeText(this, DateFormat.getDateTimeInstance().format(new Date()) + " : Connected", Toast.LENGTH_SHORT).show();
        startLocationUpdates();
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        Intent intent = new Intent(this, RealmLocationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 54321, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(this.mGoogleApiClient, mLocationRequest, pendingIntent); // This is the changed line.
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        Intent intent = new Intent(this, RealmLocationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 54321, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, pendingIntent);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mInProgress = false;
        mGoogleApiClient = null;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        mInProgress = false;

        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            // If no resolution is available, display an error dialog
        } else {}
    }
}