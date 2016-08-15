package dev.encarnasion.droidreceiver.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import com.google.common.collect.Iterables;

import dev.encarnasion.droidreceiver.Globals;
import dev.encarnasion.droidreceiver.R;

public class Wifi {
    private static final String TAG = "utils.Wifi";
    protected static WifiManager _wifiManager = null;
    protected static WifiManager.WifiLock _wifiLock = null;

    public static void init() {
        if (_wifiManager == null)
            _wifiManager = (WifiManager) Globals.getContext().getSystemService(Context.WIFI_SERVICE);

        if (_wifiLock == null) {
            _wifiLock = _wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "wifilock__");
            _wifiLock.acquire();
        }
    }

    public static void connectToTransmitter(View view) {
        if (_wifiManager.isWifiEnabled()) {
            checkTrmConnection(view);
        } else {
            Notifications.ShowSnackbar(view, R.string.msg_warn_wifi_disabled);
        }
    }

    protected static void checkTrmConnection(View view) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        String prefSSID = preferences.getString("prefTrmSsid", "");
        String formattedPrefSSID = String.format("\"%s\"", prefSSID);

        if (!formattedPrefSSID.equals(getCurrentSSID())) {
            Log.d(TAG, String.format("scan results(%d)", _wifiManager.getScanResults().size()));
            for (ScanResult sr : _wifiManager.getScanResults()) {
                Log.d(TAG, "\t" + sr.SSID);
            }
            if (Iterables.any(_wifiManager.getScanResults(), sr -> sr.SSID.equals(prefSSID))) {
                ScanResult scanResult = Iterables.find(_wifiManager.getScanResults(), sr -> sr.SSID.equals(prefSSID));
                Log.d(TAG, scanResult.SSID);
            } else {
                Notifications.ShowSnackbar(view, R.string.msg_warn_no_trm_found);
            }
        }
    }

    protected static String getCurrentSSID() {
        return _wifiManager.getConnectionInfo().getSSID();
    }
}
