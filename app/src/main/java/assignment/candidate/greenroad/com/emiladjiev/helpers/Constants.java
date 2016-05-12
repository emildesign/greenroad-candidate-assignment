package assignment.candidate.greenroad.com.emiladjiev.helpers;

/**
 * Created by Emil on 09/05/2016.
 */
public final class Constants {

    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    private static final int FOREGROUND_UPDATE_INTERVAL_IN_SECONDS = 5;
    private static final int BACKGROUND_UPDATE_INTERVAL_IN_SECONDS = 20;

    // Foreground update frequency in milliseconds
    public static final long FOREGROUND_UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * FOREGROUND_UPDATE_INTERVAL_IN_SECONDS;

    // Background update frequency in milliseconds
    public static final long BACKGROUND_UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * BACKGROUND_UPDATE_INTERVAL_IN_SECONDS;

    // The fastest update frequency, in seconds
    private static final int FASTEST_FOREGROUND_INTERVAL_IN_SECONDS = FOREGROUND_UPDATE_INTERVAL_IN_SECONDS / 2;
    private static final int FASTEST_BACKGROUND_INTERVAL_IN_SECONDS = BACKGROUND_UPDATE_INTERVAL_IN_SECONDS / 2;

    // A fast frequency ceiling in milliseconds
    public static final long FOREGROUND_FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_FOREGROUND_INTERVAL_IN_SECONDS;
    public static final long BACKGROUND_FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_BACKGROUND_INTERVAL_IN_SECONDS;

    // Stores the lat / long pairs in a text file
    public static final String LOCATION_FILE = "sdcard/location.txt";

    // Stores the connect / disconnect data in a text file
    public static final String LOG_FILE = "sdcard/log.txt";

    /**
     * Suppress default constructor for noninstantiability
     */
    private Constants() {
        throw new AssertionError();
    }

    // DB Types:
    public static final int DB_TYPE_ACTIVE_ANDROID = 0;
    public static final int DB_TYPE_REALM = 1;
    public static final int DB_TYPE_GREEN_DAO = 2;
}