package assignment.candidate.greenroad.com.emiladjiev;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;

import assignment.candidate.greenroad.com.emiladjiev.helpers.AndroidHelper;
import assignment.candidate.greenroad.com.emiladjiev.helpers.GoogleApiClientHelper;
import assignment.candidate.greenroad.com.emiladjiev.realm.RealmHelper;
import assignment.candidate.greenroad.com.emiladjiev.realm.RealmLocationReceiver;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * BackgroundLocationService used for tracking user location in the background.
 * It uses the new GoogleApiClient LocationService for the retriving the location updates.
 *
 * @author Emil on 09/05/2016.
 */
public class BackgroundLocationService extends Service {

    private static final String TAG = "LocationService";
    private LocationRequest mLocationRequest;

    // Flag that indicates if a request is underway.
    private boolean mInProgress;
    private PowerManager.WakeLock mWakeLock;
    private Boolean servicesAvailable = false;
    private ReactiveLocationProvider mLocationProvider;
    private Observable<Location> mLocationUpdatesObservable;
    private Subscription mLocationUpdatesSubscription;

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
        mLocationProvider = new ReactiveLocationProvider(getApplicationContext());
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

        /* if (!servicesAvailable || mGoogleApiClient.isConnected() || mInProgress)
            return START_STICKY;
        }*/

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // Turn off the request flag
        this.mInProgress = false;

        if (this.mWakeLock != null) {
            this.mWakeLock.release();
            this.mWakeLock = null;
        }

        stopServiceSubscription();
        super.onDestroy();
    }

    public ReactiveLocationProvider getLocationProvider() {
        return mLocationProvider;
    }

    public Observable<LocationSettingsResult> getLocationSettingsResult() {
        return mLocationProvider.checkLocationSettings(
                new LocationSettingsRequest.Builder()
                        .addLocationRequest(mLocationRequest)
                        .setAlwaysShow(true)  //Refrence: http://stackoverflow.com/questions/29824408/google-play-services-locationservices-api-new-option-never
                        .build()
        );
    }

    public Observable<Location> getLocationUpdatesObservable() {
        mLocationUpdatesObservable = getLocationSettingsResult()
                .flatMap(locationSettingsResult -> mLocationProvider.getUpdatedLocation(mLocationRequest));

        mLocationUpdatesSubscription = mLocationUpdatesObservable.subscribe(location -> RealmHelper.saveLocationToRealmByCreatingObject(LUSApplication.getInstance().getRealm(), location));

        return mLocationUpdatesObservable;
    }

    public void stopServiceSubscription() {
        if (mLocationUpdatesSubscription != null && !mLocationUpdatesSubscription.isUnsubscribed()) {
            mLocationUpdatesSubscription.unsubscribe();
        }
    }
}