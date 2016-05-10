package assignment.candidate.greenroad.com.emiladjiev.helpers;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import assignment.candidate.greenroad.com.emiladjiev.BackgroundLocationService;

/**
 * Created by Emil on 10/05/2016.
 */
public class AndroidHelper {

    /**
     * Gets the state of Airplane Mode.
     *
     * @param context
     * @return true if enabled.
     */
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isAirplaneModeOn(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.System.getInt(context.getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        } else {
            return Settings.Global.getInt(context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        }
    }

    public static ComponentName startLocationServiceUsingComponentName(Context context) {
        ComponentName comp = new ComponentName(context.getPackageName(), BackgroundLocationService.class.getName());
        ComponentName service = context.startService(new Intent().setComponent(comp));
        return service;
    }


}
