package dev.encarnasion.droidreceiver.utils;

import android.support.design.widget.Snackbar;
import android.view.View;

import dev.encarnasion.droidreceiver.animators.Animators;
import dev.encarnasion.droidreceiver.animators.FloatingActionButtonAnimator;

public class Notifications {
    public static void ShowSnackbar(View view, int resId) {
        FloatingActionButtonAnimator fab = (FloatingActionButtonAnimator) Animators.get(Animators.K.FAB_CONNECT_TRANSMITTER);
        boolean fabIsShown = fab.get().isShown();
        if (fabIsShown) fab.hide();
        Snackbar.make(view, resId, Snackbar.LENGTH_SHORT)
                .setCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        if (fabIsShown) fab.show();
                        super.onDismissed(snackbar, event);
                    }
                }).show();
    }
}
