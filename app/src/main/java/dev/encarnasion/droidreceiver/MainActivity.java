package dev.encarnasion.droidreceiver;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import dev.encarnasion.droidreceiver.animators.Animators;
import dev.encarnasion.droidreceiver.animators.FloatingActionButtonAnimator;
import dev.encarnasion.droidreceiver.utils.Wifi;
import dev.encarnasion.droidreceiver.utils.WifiListenerService;

public class MainActivity extends AppCompatActivity {
    protected class IncomingWifiHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                default:
                    super.handleMessage(msg);
            }
        }
    }

    protected final Messenger rMessenger = new Messenger(new IncomingWifiHandler());
    protected Messenger tMessenger = null;
    protected boolean tBound = false;

    private ServiceConnection _wifiServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            tMessenger = new Messenger(iBinder);
            tBound = true;
            try {
                Message answer = new Message();
                answer.what = WifiListenerService.MSG_IN_REG_MESSENGER;
                answer.arg1 = hashCode();
                answer.replyTo = rMessenger;
                tMessenger.send(answer);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            tMessenger = null;
            tBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        checkAppRequests();
        setActions();
        bindWifiService();
    }

    @Override
    protected void onResume() {
        Wifi.connectToTransmitter(null);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        unbindWifiService();
        super.onDestroy();
    }

    protected void setActions() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(Wifi::connectToTransmitter);
        Animators.put(Animators.K.FAB_CONNECT_TRANSMITTER, new FloatingActionButtonAnimator(fab));
        if (Wifi.isConnectedToTrm()) fab.hide();
        else fab.show();

        // test incoming temperature
        Random random = new Random();
        TextView tw = (TextView) findViewById(R.id.temperatureLabel);
        new Timer().scheduleAtFixedRate(new TimerTask() {
            double temperature = random.nextDouble() * 20 + 10;

            @Override
            public void run() {
                temperature += random.nextDouble() - 0.5;
                runOnUiThread(() -> tw.setText(String.format(Locale.ENGLISH, "%+.1f", temperature)));
            }
        }, 0, 10 * 1000);
    }

    private void bindWifiService() {
        if (!tBound) {
            bindService(new Intent(this, WifiListenerService.class), _wifiServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private void unbindWifiService() {
        if (tBound) {
            if (tMessenger != null) {
                try {
                    Message msg = new Message();
                    msg.what = WifiListenerService.MSG_IN_UNREG_MESSENGER;
                    msg.arg1 = hashCode();
                    tMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            unbindService(_wifiServiceConnection);
            tBound = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                Intent i = new Intent(MainActivity.this, UserSettingsActivity.class);
                startActivityForResult(i, Globals.OPTIONS_MENU_SETTINGS);
                return true;
            case R.id.action_exit:
                System.exit(0);
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkAppRequests() {
        // android M can't find access points without special permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, Globals.REQUEST_PERM_COARSE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Globals.REQUEST_PERM_COARSE_LOCATION:
                if (grantResults.length == 0 || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    AlertDialog.Builder b = new AlertDialog.Builder(this);
                    b.setTitle(R.string.alert_dlg_location_perm_denied_title);
                    b.setMessage(R.string.alert_dlg_location_perm_denied_msg);
                    b.setCancelable(false);
                    b.setPositiveButton(R.string.alert_dlg_repeat, (dialogInterface, i) -> checkAppRequests());
                    b.setNegativeButton(R.string.alert_dlg_exit, (dialogInterface, i) -> System.exit(0));
                    b.show();
                }
                break;
        }
    }
}
