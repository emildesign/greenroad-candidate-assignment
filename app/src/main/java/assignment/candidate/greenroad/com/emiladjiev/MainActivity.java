package assignment.candidate.greenroad.com.emiladjiev;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MainActivity";
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1001;

    // Keys for storing activity state in the Bundle.
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting_location_updates_key";
    protected final static String LOCATION_KEY = "location_key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last_updated_time_string_key";

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

    /**
     * Represents a geographical location.
     */
    protected Location mCurrentLocation;

    /**
     * Time when the location was updated represented as a String.
     */
    protected String mLastUpdateTime;

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    protected Boolean mRequestingLocationUpdates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        bEnable = (Button) findViewById(R.id.bEnable);
        bDisable = (Button) findViewById(R.id.bDisable);

        bEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startUpdatesButtonHandler();
            }
        });

        bDisable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopUpdatesButtonHandler();
            }
        });

        tvLatitudeValue = (TextView) findViewById(R.id.tvLatitudeValue);
        tvLongitudeValue = (TextView) findViewById(R.id.tvLongitudeValue);
        tvLastUpdateValue = (TextView) findViewById(R.id.tvLastUpdateValue);
        tvSpeedValue = (TextView) findViewById(R.id.tvSpeedValue);

        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);
        setButtonsEnabledState();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            setAllButtonsToDisableState();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else if (mRequestingLocationUpdates) {
            startLocationService();
        }
    }

    /**
     * Ensures that only one button is enabled at any time. The Start Updates button is enabled
     * if the user is not requesting location updates. The Stop Updates button is enabled if the
     * user is requesting location updates.
     */
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
            setButtonsEnabledState();
            startLocationService();
            //mLocationService.startLocationUpdates();
        }
    }

    /**
     * Handles the Stop Updates button, and requests removal of location updates. Does nothing if
     * updates were not previously requested.
     */
    public void stopUpdatesButtonHandler() {
        if (mRequestingLocationUpdates) {
            mRequestingLocationUpdates = false;
            setButtonsEnabledState();
            mLocationService.stopLocationUpdates();
        }
    }

    /**
     * Updates the latitude, the longitude, and the last location time in the UI.
     */
    private void updateUI() {
        tvLatitudeValue.setText(String.format("%f", mCurrentLocation.getLatitude()));
        tvLongitudeValue.setText(String.format("%f", mCurrentLocation.getLongitude()));
        tvLastUpdateValue.setText(String.format("%s", mLastUpdateTime));
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
            updateUI();
        }
    }


    private void startLocationService() {
        Intent locationServiceIntent = new Intent(this, BackgroundLocationService.class);
        startService(locationServiceIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setButtonsEnabledState();
                    if (mRequestingLocationUpdates) {
                        startLocationService();
                    }
                }
                return;
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent mIntent = new Intent(this, BackgroundLocationService.class);
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);
    };

    /**
     * Stores activity data in the Bundle.
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }

    ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(MainActivity.this, "Service is disconnected", Toast.LENGTH_SHORT).show();
            mBounded = false;
            mLocationService = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            Toast.makeText(MainActivity.this, "Service is connected", Toast.LENGTH_SHORT).show();
            mBounded = true;
            BackgroundLocationService.LocalBinder mLocalBinder = (BackgroundLocationService.LocalBinder)service;
            mLocationService = mLocalBinder.getServerInstance();
        }
    };
}
