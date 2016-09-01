package dev.encarnasion.droidreceiver.utils;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class WifiListenerService extends IntentService {

    public static final String NAME = "WifiListenerService";

    public static final int MSG_IN_REG_MESSENGER = 0x0001;
    public static final int MSG_IN_UNREG_MESSENGER = 0x0002;

    /**
     * Handler for incoming messages
     */
    protected class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_IN_REG_MESSENGER:
                    // in arg1 get hash-code of owner class
                    if (msg.replyTo != null && msg.arg1 > 0) {
                        tMessengers.put(msg.arg1, msg.replyTo);
                    }
                    break;
                case MSG_IN_UNREG_MESSENGER:
                    tMessengers.remove(msg.arg1);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    protected final Messenger rMessenger = new Messenger(new IncomingHandler());
    protected final Map<Integer, Messenger> tMessengers = new HashMap<>(1);

    /**
     * A constructor is required, and must call the super IntentService(String)
     * constructor with a name for the worker thread.
     */
    public WifiListenerService() {
        super(NAME);
    }


    @Override
    protected void onHandleIntent(Intent intent) { }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(this, "binding", Toast.LENGTH_SHORT).show();
        return rMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Toast.makeText(this, "unbinding", Toast.LENGTH_SHORT).show();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service stopping", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    protected void broadcast(Message message) {
        for (Messenger messenger : tMessengers.values()) {
            try {
                messenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
