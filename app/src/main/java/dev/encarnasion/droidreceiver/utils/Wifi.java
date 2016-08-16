package dev.encarnasion.droidreceiver.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import com.google.common.collect.Iterables;

import java.util.List;

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
        String prefSSID = preferences.getString(view.getContext().getString(R.string.pref_key_trm_ssid), "");
        String formattedPrefSSID = String.format("\"%s\"", prefSSID);

        if (!formattedPrefSSID.equals(getCurrentSSID())) {
            List<ScanResult> scanResultList = _wifiManager.getScanResults();
            Log.d(TAG, String.format("\n\nscan results(%d)", scanResultList.size()));
            for (ScanResult sr : scanResultList) {
                Log.d(TAG, "\t" + sr.SSID);
            }
            ScanResult scanResult = Iterables.tryFind(scanResultList, sr -> sr.SSID.equals(prefSSID)).orNull();
            if (scanResult == null) {
                Notifications.ShowSnackbar(view, R.string.msg_warn_no_trm_found);
            } else {
                Log.d(TAG, "Access point found: " + scanResult.SSID);

                WifiConfiguration conf = Iterables.tryFind(_wifiManager.getConfiguredNetworks(), cn -> cn.SSID.equals(formattedPrefSSID)).orNull();
                String preSharedKey = String.format("\"%s\"", preferences.getString(view.getContext().getString(R.string.pref_key_trm_pass), ""));
                if (conf == null) {
                    conf = new WifiConfiguration();
                    conf.SSID = formattedPrefSSID;
                    conf.preSharedKey = preSharedKey;
                    _wifiManager.addNetwork(conf);
                } else if (!conf.preSharedKey.equals(preSharedKey)) {
                    conf.preSharedKey = preSharedKey;
                    _wifiManager.saveConfiguration();
                }

                _wifiManager.disconnect();
                _wifiManager.enableNetwork(conf.networkId, true);
                _wifiManager.reconnect();
            }
        }
    }

    protected static String getCurrentSSID() {
        return _wifiManager.getConnectionInfo().getSSID();
    }
}
