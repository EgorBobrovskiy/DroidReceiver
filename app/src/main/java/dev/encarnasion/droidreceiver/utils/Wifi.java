package dev.encarnasion.droidreceiver.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import com.google.common.collect.Iterables;

import java.util.List;

import dev.encarnasion.droidreceiver.Globals;
import dev.encarnasion.droidreceiver.R;
import dev.encarnasion.droidreceiver.animators.Animators;
import dev.encarnasion.droidreceiver.animators.ControlAnimator;

public class Wifi {
    private static final String TAG = "utils.Wifi";
    protected static WifiManager _wifiManager = null;
    protected static WifiManager.WifiLock _wifiLock = null;
    protected static WifiReceiver _wifiReceiver = null;

    public static void init() {
        if (_wifiManager == null)
            _wifiManager = (WifiManager) Globals.C().getSystemService(Context.WIFI_SERVICE);

        if (_wifiLock == null) {
            _wifiLock = _wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "wifilock__");
            _wifiLock.acquire();
        }

        IntentFilter i = new IntentFilter();
        i.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        if (_wifiReceiver != null) {
            Globals.C().unregisterReceiver(_wifiReceiver);
        }
        _wifiReceiver = new WifiReceiver();
        Globals.C().registerReceiver(_wifiReceiver, i);
    }

    public static void connectToTransmitter(View view) {
        if (_wifiManager.isWifiEnabled()) {
            checkTrmConnection(view);
        } else {
            Notifications.ShowSnackbar(view, R.string.msg_warn_wifi_disabled);
        }
    }

    protected static void checkTrmConnection(View view) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Globals.C());
        String prefSSID = preferences.getString(Globals.C().getString(R.string.pref_key_trm_ssid), "");
        String formattedSSID = String.format("\"%s\"", prefSSID);

        if (prefSSID.equals("") || prefSSID.length() > 32) {
            Notifications.ShowSnackbar(view, R.string.msg_warn_invalid_ssid);
            return;
        }

        if (formattedSSID.equals(getCurrentSSID())) {
            Animators.get(Animators.K.FAB_CONNECT_TRANSMITTER).hide();
            return;
        }

        String prefPSK = preferences.getString(Globals.C().getString(R.string.pref_key_trm_pass), "");
        if (prefPSK.length() < 8 || prefPSK.length() > 63) {
            Notifications.ShowSnackbar(view, R.string.msg_warn_invalid_psk);
            return;
        }

        List<ScanResult> scanResultList = _wifiManager.getScanResults();

        Log.d(TAG, String.format("-----------------\nscan results(%d)", scanResultList.size()));
        for (ScanResult sr : scanResultList) {
            Log.d(TAG, "\t" + sr.SSID);
        }

        ScanResult scanResult = Iterables.tryFind(scanResultList, sr -> sr.SSID.equals(prefSSID)).orNull();
        if (scanResult == null) {
            Notifications.ShowSnackbar(view, R.string.msg_warn_no_trm_found);
            return;
        }

        Log.d(TAG, "Access point found: " + scanResult.SSID);

        WifiConfiguration conf = Iterables.tryFind(_wifiManager.getConfiguredNetworks(), cn -> cn.SSID.equals(formattedSSID)).orNull();
        String formattedPSK = String.format("\"%s\"", prefPSK);

        if (conf == null) {
            Log.d(TAG, "Saving wifi configuration for " + prefSSID);
            conf = new WifiConfiguration();
            conf.SSID = formattedSSID;
            conf.preSharedKey = formattedPSK;
            conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA); // For WPA
            conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN); // For WPA2
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            conf.networkId = _wifiManager.addNetwork(conf);
        } else if (!conf.preSharedKey.equals(formattedPSK)) {
            Log.d(TAG, "Changing preshared key for " + prefSSID);
            conf.preSharedKey = formattedPSK;
            _wifiManager.updateNetwork(conf);
        }

        _wifiManager.disconnect();
        _wifiManager.enableNetwork(conf.networkId, false);
        _wifiManager.reconnect();
    }

    protected static String getCurrentSSID() {
        return _wifiManager.getConnectionInfo().getSSID();
    }

    public static boolean isConnectedToTrm() {
        String ssid = PreferenceManager.getDefaultSharedPreferences(Globals.C())
                .getString(Globals.C().getString(R.string.pref_key_trm_ssid), "");
        return getCurrentSSID().equals(String.format("\"%s\"", ssid));
    }

    protected static class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
                ControlAnimator ca = Animators.get(Animators.K.FAB_CONNECT_TRANSMITTER);
                if (intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, 0) == WifiManager.ERROR_AUTHENTICATING) {
                    Log.d(TAG, "Error authenticating");
                    ca.show();
                    return;
                }

                SupplicantState supState = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
                if (supState == null) return;

                if (supState.equals(SupplicantState.COMPLETED)) {
                    ca.hide();
                } else if (supState.equals(SupplicantState.DISCONNECTED)) {
                    ca.show();
                }
            }
        }
    }
}
