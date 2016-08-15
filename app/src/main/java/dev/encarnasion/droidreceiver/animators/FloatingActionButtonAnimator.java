package dev.encarnasion.droidreceiver.animators;

import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import dev.encarnasion.droidreceiver.Globals;

public class FloatingActionButtonAnimator implements ControlAnimator {
    private FloatingActionButton floatingActionButton;
    private Animation inAnimation;
    private Animation outAnimation;

    public FloatingActionButtonAnimator(FloatingActionButton floatingActionButton) {
        this.floatingActionButton = floatingActionButton;
        setAnimation();
    }

    protected void setAnimation() {
        inAnimation = AnimationUtils.makeInAnimation(Globals.getContext(), false);
        inAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                floatingActionButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) { }

            @Override
            public void onAnimationRepeat(Animation animation) { }
        });

        outAnimation = AnimationUtils.makeOutAnimation(Globals.getContext(), true);
        outAnimation.setDuration(100);
        outAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                floatingActionButton.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }
        });
    }

    public void show() {
        if (!floatingActionButton.isShown()) {
            floatingActionButton.startAnimation(inAnimation);
        }
    }

    public void hide() {
        if (floatingActionButton.isShown()) {
            floatingActionButton.startAnimation(outAnimation);
        }
    }

    public FloatingActionButton get() {
        return floatingActionButton;
    }
}
