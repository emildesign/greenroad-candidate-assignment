package assignment.candidate.greenroad.com.emiladjiev;

import android.location.Location;
import android.support.annotation.Nullable;

import io.realm.Realm;
import io.realm.RealmObject;

/**
 * Created by Emil on 10/05/2016.
 */
public class RealmHelper {

    public static RealmObject saveLocationToRealmByCopyingObject(Realm realm, Location location) {
        //Creating object by copying a java object ot realm object;
        realm.beginTransaction();
        RealmLocation lastLocation = new RealmLocation(location);
        RealmLocation realmLocation = realm.copyToRealm(lastLocation); //realmLocation is a realm object that I can modify and will be saved to DB.
        realm.commitTransaction();

        return realmLocation;
    }

    public static RealmObject saveLocationToRealmByCreatingObject(Realm realm, Location location) {
        //Creating object by copying a java object ot realm object;
        realm.beginTransaction();
        RealmLocation realmLocation = realm.createObject(RealmLocation.class); //realmLocation is a realm object that I can modify and will be saved to DB.
        realmLocation.setLatitude(String.valueOf(location.getLatitude()));
        realmLocation.setLongitude(String.valueOf(location.getLongitude()));
        realmLocation.setSpeed(location.getSpeed());
        realmLocation.setTime(location.getTime());
        realm.commitTransaction();

        return realmLocation;
    }

    @Deprecated
    public static void saveLocationByExecutingTransactionWithCallback(Realm realm, final Location location, @Nullable Realm.Transaction.Callback callback) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmLocation realmLocation = realm.createObject(RealmLocation.class); //realmLocation is a realm object that I can modify and will be saved to DB.
                realmLocation.setLatitude(String.valueOf(location.getLatitude()));
                realmLocation.setLongitude(String.valueOf(location.getLongitude()));
                realmLocation.setSpeed(location.getSpeed());
                realmLocation.setTime(location.getTime());
            }
        }, callback);
    }

    public static void saveLocationByExecutingTransactionWithCallbacks(Realm realm, final Location location, @Nullable Realm.Transaction.OnSuccess onSuccess, @Nullable Realm.Transaction.OnError onError) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmLocation realmLocation = realm.createObject(RealmLocation.class); //realmLocation is a realm object that I can modify and will be saved to DB.
                realmLocation.setLatitude(String.valueOf(location.getLatitude()));
                realmLocation.setLongitude(String.valueOf(location.getLongitude()));
                realmLocation.setSpeed(location.getSpeed());
                realmLocation.setTime(location.getTime());
            }
        }, onSuccess, onError);
    }
}


