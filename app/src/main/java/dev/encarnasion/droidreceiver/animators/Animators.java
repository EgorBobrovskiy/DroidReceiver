package dev.encarnasion.droidreceiver.animators;

import java.util.HashMap;

public class Animators {
    private static HashMap<K, ControlAnimator> _animators = new HashMap<>();

    public enum K {
        FAB_CONNECT_TRANSMITTER,
    }

    public static ControlAnimator put(K key, ControlAnimator value) {
        return _animators.put(key, value);
    }

    public static ControlAnimator get(K key) {
        return _animators.get(key);
    }
}
