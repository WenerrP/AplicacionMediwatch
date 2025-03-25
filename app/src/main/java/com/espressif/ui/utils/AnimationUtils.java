package com.espressif.ui.utils;

import android.view.View;
import android.widget.ImageView;

import com.google.android.material.button.MaterialButton;

public class AnimationUtils {

    /**
     * Aplica animación de fade in al logo
     */
    public static void animateLogo(ImageView logo) {
        logo.setAlpha(0f);
        logo.animate()
            .alpha(1f)
            .setDuration(800)
            .start();
    }

    /**
     * Aplica animación de escala y fade in al botón
     */
    public static void animateButton(MaterialButton button) {
        button.setScaleX(0.9f);
        button.setScaleY(0.9f);
        button.setAlpha(0f);
        button.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setStartDelay(300)
            .setDuration(500)
            .start();
    }
}