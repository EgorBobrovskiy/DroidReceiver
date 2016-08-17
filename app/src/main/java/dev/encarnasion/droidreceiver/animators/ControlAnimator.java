package dev.encarnasion.droidreceiver.animators;

public abstract class ControlAnimator {
    protected boolean _isShown = false;

    public abstract void show();

    public abstract void hide();

    public boolean isShown() {
        return _isShown;
    }
}
