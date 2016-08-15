package dev.encarnasion.droidreceiver;

import android.app.Application;
import android.content.Context;

import dev.encarnasion.droidreceiver.utils.Wifi;

public class Globals extends Application {
    private static Context _context;

    public static final int REQUEST_PERM_COARSE_LOCATION = 0;
    public static final int OPTIONS_MENU_SETTINGS = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        _context = getApplicationContext();
        Wifi.init();
    }

    public static Context getContext() { return _context; }
}
