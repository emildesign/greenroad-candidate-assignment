package assignment.candidate.greenroad.com.emiladjiev;

import android.location.Location;
import com.google.android.gms.maps.model.LatLng;

import io.realm.RealmObject;

/**
 * Created by Emil on 10/05/2016.
 */

public class RealmLocation extends RealmObject {

    private String latitude;
    private String longitude;
    private Long time;
    private float speed;

    public RealmLocation() {
    }

    public RealmLocation(Location location) {
        this(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), location.getTime(), location.getSpeed());
    }

    public RealmLocation(String latitude, String longitude, Long time, float speed) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
        this.speed = speed;
    }

    public Location getLocation() {
        Location location = new Location("RealmLocation");
        location.setLatitude(Double.valueOf(latitude));
        location.setLongitude(Double.valueOf(longitude));
        location.setSpeed(speed);
        location.setTime(time);

        return location;
    }

    public LatLng getLatLong() {
        return new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }
}
