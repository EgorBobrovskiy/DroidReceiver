package dev.encarnasion.droidreceiver;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import dev.encarnasion.droidreceiver.utils.Wifi;

public class Globals extends Application {
    private static Context _context;
    private static ActivityManager _activityManager;

    public static final int REQUEST_PERM_COARSE_LOCATION = 0;
    public static final int OPTIONS_MENU_SETTINGS = 1;

    // force portrait orientation
    private static final ActivityLifecycleCallbacks ACTIVITY_LIFECYCLE_CALLBACKS = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }

        public void onActivityStarted(Activity activity) {}

        public void onActivityResumed(Activity activity) {}

        public void onActivityPaused(Activity activity) {}

        public void onActivityStopped(Activity activity) {}

        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {}

        public void onActivityDestroyed(Activity activity) {}
    };

    @Override
    public void onCreate() {
        super.onCreate();
        _context = getApplicationContext();
        _activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        Wifi.init();
        registerActivityLifecycleCallbacks(ACTIVITY_LIFECYCLE_CALLBACKS);
    }

    public static Context C() { return _context; }

    public static boolean isServiceRunning(Class<?> serviceClass) {
        String serviceName = serviceClass.getName();
        for (ActivityManager.RunningServiceInfo service : _activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceName.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
