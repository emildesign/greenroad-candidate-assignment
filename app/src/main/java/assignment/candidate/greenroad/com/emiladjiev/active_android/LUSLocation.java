package assignment.candidate.greenroad.com.emiladjiev.active_android;

import android.location.Location;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Emil on 10/05/2016.
 */

@Table(name = "LUSLocation")
public class LUSLocation extends Model {

    @Column(name = "latitude")
    public String latitude;

    @Column(name = "longitude")
    public String longitude;

    @Column(name = "time")
    public Long time;

    @Column(name = "speed")
    public float speed;

    public LUSLocation() {
        super();
    }

    public LUSLocation(Location location) {
        this(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), location.getTime(), location.getSpeed());
    }

    public LUSLocation(String latitude, String longitude, Long time, float speed) {
        super();
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
        this.speed = speed;
    }

    public Location getLocation() {
        Location location = new Location("LUSLocation");
        location.setLatitude(Double.valueOf(latitude));
        location.setLongitude(Double.valueOf(longitude));
        location.setSpeed(speed);
        location.setTime(time);

        return location;
    }

    public LatLng getLatLong() {
        return new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
    }
}
