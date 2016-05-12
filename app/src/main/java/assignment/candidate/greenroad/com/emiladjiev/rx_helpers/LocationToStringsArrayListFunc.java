package assignment.candidate.greenroad.com.emiladjiev.rx_helpers;

import android.location.Location;

import java.util.ArrayList;

import rx.functions.Func1;

public class LocationToStringsArrayListFunc implements Func1<Location, ArrayList<String>> {
    @Override
    public ArrayList<String> call(Location location) {
        ArrayList<String> locationDetails = null;
        if (location != null) {
            locationDetails = new ArrayList<>();
            locationDetails.add(String.valueOf(location.getLatitude()));
            locationDetails.add(String.valueOf(location.getLongitude()));
            locationDetails.add(String.valueOf(location.getTime()));
            locationDetails.add(String.valueOf(location.getSpeed()));
        }
        return locationDetails;
    }
}
