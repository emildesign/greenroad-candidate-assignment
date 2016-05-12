package assignment.candidate.greenroad.com.emiladjiev.helpers;

import android.location.Location;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Created by Emil on 12/05/2016.
 */
public class MapHelper {
    public static void zoomInMapOnMyCurrentLocationWithZoom(float zoomLevel, GoogleMap map, Location location) {
        if (location != null && map != null) {
            map.animateCamera(CameraUpdateFactory.newCameraPosition(getCameraPositionFromLocationWithZoom(location, zoomLevel)));
        }
    }

    public static CameraPosition getCameraPositionFromLocationWithZoom(Location location, float zoomLevel) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(location.getLatitude(), location.getLongitude()))
                .zoom(zoomLevel).build();

        return cameraPosition;
    }

    public static void addPolylineOnMap(GoogleMap map, LatLng previousLocation, LatLng location) {
        if (previousLocation != null && location != null) {
            map.addPolyline((new PolylineOptions()).add(previousLocation, location));
        }
    }

    public static  LatLng getLatLngFromLocation(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }
}
