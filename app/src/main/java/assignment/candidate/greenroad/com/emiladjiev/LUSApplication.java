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
public class LUSApplication extends Application {

    private final Bus mBus = new Bus(ThreadEnforcer.ANY);

    private static LUSApplication mInstance = null;

    // Getter to access Singleton instance
    public static LUSApplication getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Configuration dbConfiguration = new Configuration.Builder(this).setDatabaseName("locations.db").addModelClass(LUSLocation.class).create();
        ActiveAndroid.initialize(dbConfiguration);

        mInstance = this;
    }

    public Bus getBus() {
        return mBus;
    }

    public List<LUSLocation> getAllSavedLocations() {
        return new Select()
                .from(LUSLocation.class)
                .orderBy("time ASC")
                .execute();
    }
}
