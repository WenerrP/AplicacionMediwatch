package com.espressif.ui.animations;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import com.espressif.AppConstants;

public class ViewAnimator {

    public static void playLogoAnimation(ImageView imageView) {
        imageView.setAlpha(0f);
        imageView.animate()
                .alpha(1f)
                .setDuration(AppConstants.LOGO_FADE_DURATION)
                .start();
    }

    public static void playButtonScaleAnimation(Button button) {
        button.setScaleX(AppConstants.BUTTON_INITIAL_SCALE);
        button.setScaleY(AppConstants.BUTTON_INITIAL_SCALE);
        button.setAlpha(0f);
        button.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setStartDelay(AppConstants.BUTTON_ANIMATION_DELAY)
                .setDuration(AppConstants.BUTTON_ANIMATION_DURATION)
                .start();
    }
}