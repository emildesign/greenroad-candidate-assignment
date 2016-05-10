package assignment.candidate.greenroad.com.emiladjiev;

import android.app.Application;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Configuration;
import com.activeandroid.query.Select;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import java.util.List;

/**
 * Created by Emil on 10/05/2016.
 */
public class LocationUpdatesApplication extends Application {

    private final Bus mBus = new Bus(ThreadEnforcer.ANY);

    private static LocationUpdatesApplication mInstance = null;

    // Getter to access Singleton instance
    public static LocationUpdatesApplication getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Configuration dbConfiguration = new Configuration.Builder(this).setDatabaseName("locations.db").addModelClass(LuLocation.class).create();
        ActiveAndroid.initialize(dbConfiguration);

        mInstance = this;
    }

    public Bus getBus() {
        return mBus;
    }

    public List<LuLocation> getAllSavedLocations() {
        return new Select()
                .from(LuLocation.class)
                .orderBy("time ASC")
                .execute();
    }
}
