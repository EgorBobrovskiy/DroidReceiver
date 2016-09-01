package dev.encarnasion.droidreceiver.utils;

import android.widget.Toast;

import dev.encarnasion.droidreceiver.Globals;

public class Notifications {
    public static void toast(int resId) {
        Toast.makeText(Globals.C(), resId, Toast.LENGTH_SHORT).show();
    }

    public static void toast(String s) {
        Toast.makeText(Globals.C(), s, Toast.LENGTH_SHORT).show();
    }
}
