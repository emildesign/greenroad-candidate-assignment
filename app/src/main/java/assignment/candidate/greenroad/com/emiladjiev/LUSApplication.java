package assignment.candidate.greenroad.com.emiladjiev;

import android.app.Application;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Configuration;
import com.activeandroid.query.Select;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import java.util.List;

import assignment.candidate.greenroad.com.emiladjiev.active_android.LUSLocation;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Emil on 10/05/2016.
 */
public class LUSApplication extends Application {

    private final Bus mBus = new Bus(ThreadEnforcer.ANY);

    private static LUSApplication mInstance = null;
    private Realm mRealm;

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

    public List<LUSLocation> getAllActiveAndroidLocations() {
        return new Select().from(LUSLocation.class).orderBy("time ASC").execute();
    }

    public RealmResults<RealmLocation> getAllRealmLocations() {
        return getRealm().where(RealmLocation.class).findAllSorted("time", Sort.ASCENDING);
    }

    private void initRealm() {
        if (mRealm != null) {
            return;
        }

        // Create the Realm configuration
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(this).name("locations.realm").build();
        // Open the Realm for the UI thread.
        mRealm = Realm.getInstance(realmConfig);
    }

    public Realm getRealm() {
        initRealm();
        return mRealm;
    }
}
