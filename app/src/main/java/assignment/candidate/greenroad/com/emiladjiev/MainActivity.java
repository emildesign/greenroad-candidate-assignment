package assignment.candidate.greenroad.com.emiladjiev;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.squareup.otto.Subscribe;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import assignment.candidate.greenroad.com.emiladjiev.helpers.AndroidHelper;
import assignment.candidate.greenroad.com.emiladjiev.helpers.GoogleApiClientHelper;
import assignment.candidate.greenroad.com.emiladjiev.realm.RealmLocation;
import assignment.candidate.greenroad.com.emiladjiev.rx_helpers.ListItemAtIndexFunc;
import assignment.candidate.greenroad.com.emiladjiev.helpers.MapHelper;
import assignment.candidate.greenroad.com.emiladjiev.helpers.PermissionUtils;
import assignment.candidate.greenroad.com.emiladjiev.rx_helpers.DisplayTextOnViewAction;
import assignment.candidate.greenroad.com.emiladjiev.rx_helpers.LocationToStringsArrayListFunc;
import io.realm.Realm;
import io.realm.RealmResults;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MainActivity";
    protected static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1001;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    // Keys for storing activity state in the Bundle.
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting_location_updates_key";
    protected final static String LOCATION_KEY = "location_key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last_updated_time_string_key";
    protected final static String LAST_UPDATED_SPEED_FLOAT_KEY = "last_updated_spped_float_key";

    private SupportMapFragment mMapFragment;
    private GoogleMap mMap;
    private Button bEnable;
    private Button bDisable;
    private TextView tvLatitudeValue;
    private TextView tvLongitudeValue;
    private TextView tvLastUpdateValue;
    private TextView tvSpeedValue;
    private boolean mBounded;
    private BackgroundLocationService mLocationService;
    private Observable<Location> mLocationUpdatesObservable;
    private CompositeSubscription mCompositeLocationSubscription;
    private Observable<ArrayList<String>> mSharedLocationUpdatesArrayObservable;
    private Subscription mLatitudeSubscription, mLongitudeSubscription, mLastUpdateSubscription, mSpeedSubscription;

    private Subscription mLastKnownLocationSubscription;
    private Subscription mUpdatableLocationSubscription;
    private Observable<Location> mLastKnownLocationObservable;

    /**
     * Represents a geographical location.
     */
    protected Location mCurrentLocation;

    /**
     * Time when the location was updated represented as a String.
     */
    protected String mLastUpdateTime;
    private Float mLastSpeed;

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    protected Boolean mRequestingLocationUpdates;
    private int mLocationCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        bEnable = (Button) findViewById(R.id.bEnable);
        bDisable = (Button) findViewById(R.id.bDisable);
        tvLatitudeValue = (TextView) findViewById(R.id.tvLatitudeValue);
        tvLongitudeValue = (TextView) findViewById(R.id.tvLongitudeValue);
        tvLastUpdateValue = (TextView) findViewById(R.id.tvLastUpdateValue);
        tvSpeedValue = (TextView) findViewById(R.id.tvSpeedValue);

        bEnable.setOnClickListener(v -> startUpdatesButtonHandler());
        bDisable.setOnClickListener(v -> stopUpdatesButtonHandler());

        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";
        mLastSpeed = 0f;
        mLocationCounter = 0;

        // Update values using data stored in the Bundle of save instance state.
        updateValuesFromBundle(savedInstanceState);
        setButtonsEnabledState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (PermissionUtils.checkForLocationPermission(this)) {
            setAllButtonsToDisableState();
            PermissionUtils.requestLocationPermission(this, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else startLocationService();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LUSApplication.getInstance().getBus().register(this);
    }

    @Override
    protected void onStop() {
        unsubscribeAllSubscriptions();
        LUSApplication.getInstance().getBus().unregister(this);
        unbindFromLocationService();
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //final LocationSettingsStates states = LocationSettingsStates.fromIntent(intent);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        startLocationService();
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setButtonsEnabledState();
                    startLocationService();
                }
                return;
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        addLocationsFromRealmDBToMap();
    }

    private void addLocationsFromRealmDBToMap() {
        if (mMap != null) {
            mMap.clear();
            RealmResults<RealmLocation> allRealmLocations = LUSApplication.getInstance().getAllRealmLocations();

            if (allRealmLocations.size() > 5000) {
                Realm realm = LUSApplication.getInstance().getRealm();
                realm.beginTransaction();
                realm.deleteAll();
                realm.commitTransaction();
                allRealmLocations = LUSApplication.getInstance().getAllRealmLocations();
            }

            for (int i = 0; i < allRealmLocations.size() - 1; i++) {
                mCurrentLocation = allRealmLocations.get(i + 1).getLocation();
                MapHelper.addPolylineOnMap(mMap, allRealmLocations.get(i).getLatLong(), allRealmLocations.get(i + 1).getLatLong());
            }
        }
    }

    private void setButtonsEnabledState() {
        if (mRequestingLocationUpdates) {
            bEnable.setEnabled(false);
            bDisable.setEnabled(true);
        } else {
            bEnable.setEnabled(true);
            bDisable.setEnabled(false);
        }
    }

    private void setAllButtonsToDisableState() {
            bEnable.setEnabled(false);
            bDisable.setEnabled(false);
    }

    /**
     * Handles the Start Updates button and requests start of location updates. Does nothing if
     * updates have already been requested.
     */
    public void startUpdatesButtonHandler() {
        if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;
            subscribeToLocationService();
        }
    }

    /**
     * Handles the Stop Updates button, and requests removal of location updates. Does nothing if
     * updates were not previously requested.
     */
    public void stopUpdatesButtonHandler() {
        if (mRequestingLocationUpdates) {
            mRequestingLocationUpdates = false;
            stopLocationService();
            setButtonsEnabledState();
        }
    }

    /**
     * Updates the latitude, the longitude, and the last location time in the UI.
     */
    private void updateUI() {
        tvLatitudeValue.setText(String.format("%s", mCurrentLocation != null ? "" + mCurrentLocation.getLatitude() : ""));
        tvLongitudeValue.setText(String.format("%s", mCurrentLocation != null ? "" + mCurrentLocation.getLongitude() : ""));
        tvLastUpdateValue.setText(String.format("%s", mLastUpdateTime));
        tvSpeedValue.setText(String.valueOf(mLastSpeed));
    }

    private void startLocationService() {
        Intent locationServiceIntent = new Intent(this, BackgroundLocationService.class);
        startService(locationServiceIntent);
        bindToLocationService();
    }

    private void stopLocationService() {
        unsubscribeAllSubscriptions();
        unbindFromLocationService();
        mLocationService.stopServiceSubscription();
        stopService(new Intent(this, BackgroundLocationService.class));
        mBounded = false;
    }

    public void handleNewLocation(Location location) {
        if (location != null && mCurrentLocation != null) {
            MapHelper.addPolylineOnMap(mMap, MapHelper.getLatLngFromLocation(mCurrentLocation), MapHelper.getLatLngFromLocation(location));
            mCurrentLocation = location;
        } else if (location != null) {
            mCurrentLocation = location;
        }

        if (mLocationCounter == 0) {
            MapHelper.zoomInMapOnMyCurrentLocationWithZoom(16, mMap, mCurrentLocation);
            mLocationCounter++;
        } else if (mLocationCounter > 9) {
            mLocationCounter = 0;
        } else {
            mLocationCounter++;
        }
    }

    private void unsubscribeAllSubscriptions() {
        if (mCompositeLocationSubscription != null && mCompositeLocationSubscription.hasSubscriptions()) {
            mCompositeLocationSubscription.unsubscribe();
        }
    }

    private void subscribeToLocationService() {
        setButtonsEnabledState();
        mLocationUpdatesObservable = mLocationService.getLocationUpdatesObservable();
        mSharedLocationUpdatesArrayObservable = mLocationUpdatesObservable
                .map(location -> {
                    handleNewLocation(location);
                    return location;
                })
                .map(new LocationToStringsArrayListFunc()).share();

        mLatitudeSubscription = mSharedLocationUpdatesArrayObservable
                .map(stringArrayList -> stringArrayList.get(0))
                .subscribe(s -> tvLatitudeValue.setText(s), new ErrorHandler());

        mLongitudeSubscription = mSharedLocationUpdatesArrayObservable
                .map(new ListItemAtIndexFunc(1))
                .subscribe(new DisplayTextOnViewAction(tvLongitudeValue), new ErrorHandler());

        mLastUpdateSubscription = mSharedLocationUpdatesArrayObservable
                .map(stringArrayList -> stringArrayList.get(2))
                .map(s -> {
                    SimpleDateFormat outFmt = new SimpleDateFormat("HH:mm:ss");
                    Date date = new Date(Long.valueOf(s));
                    mLastUpdateTime = outFmt.format(date);
                    return mLastUpdateTime;
                })
                .subscribe(new DisplayTextOnViewAction(tvLastUpdateValue), new ErrorHandler());

        mSpeedSubscription = mSharedLocationUpdatesArrayObservable
                .map(new ListItemAtIndexFunc(3))
                .subscribe(new DisplayTextOnViewAction(tvSpeedValue), new ErrorHandler());

        mCompositeLocationSubscription = new CompositeSubscription(mLatitudeSubscription, mLongitudeSubscription, mLastUpdateSubscription, mSpeedSubscription/*, mLastKnownLocationSubscription*/);
    }

    private class ErrorHandler implements Action1<Throwable> {
        @Override
        public void call(Throwable throwable) {
            Toast.makeText(MainActivity.this, "Error occurred.", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Error occurred", throwable);
        }
    }

    private void bindToLocationService() {
        Intent mIntent = new Intent(this, BackgroundLocationService.class);
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);
    }

    private void unbindFromLocationService() {
        if (mBounded) {
            unbindService(mConnection);
        }
    }

    ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceDisconnected(ComponentName name) {
            //Toast.makeText(MainActivity.this, "Service is disconnected", Toast.LENGTH_SHORT).show();
            mBounded = false;
            mLocationService = null;
            setButtonsEnabledState();
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            //Toast.makeText(MainActivity.this, "Service is connected", Toast.LENGTH_SHORT).show();
            mBounded = true;
            BackgroundLocationService.LocalBinder mLocalBinder = (BackgroundLocationService.LocalBinder)service;
            mLocationService = mLocalBinder.getServerInstance();
            mLocationService.getLocationSettingsResult()
                    .doOnNext(locationSettingsResult -> {
                        Status status = locationSettingsResult.getStatus();
                        if (status.getStatusCode() == LocationSettingsStatusCodes.SUCCESS) {
                            subscribeToLocationService();
                        } else if (status.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                            try {
                                status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException th) {
                                Log.e("MainActivity", "Error opening settings activity.", th);
                            }
                        } else if (status.getStatusCode() == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE) {
                            Log.e(TAG, "Settings change unavailable. We have no way to fix the settings so we won't show the dialog.");
                        }
                    });

          /*mLastKnownLocationObservable = mLocationService.getLocationProvider().getLastKnownLocation();
            mLastKnownLocationSubscription = mLastKnownLocationObservable
                    .map(new LocationToStringFunc())
                    .subscribe(new DisplayTextOnViewAction(lastKnownLocationView), new ErrorHandler());
            */

            if (mRequestingLocationUpdates) {
                subscribeToLocationService();
            }
            setButtonsEnabledState();
        }
    };

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        savedInstanceState.putFloat(LAST_UPDATED_SPEED_FLOAT_KEY, mLastSpeed);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i(TAG, "Updating values from bundle");
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY);
                setButtonsEnabledState();
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_SPEED_FLOAT_KEY)) {
                mLastSpeed = savedInstanceState.getFloat(LAST_UPDATED_SPEED_FLOAT_KEY);
            }
            updateUI();
        } else if (AndroidHelper.isServiceRunning(this, BackgroundLocationService.class)) {
            bindToLocationService();
            mRequestingLocationUpdates = true;
        }
    }
}
