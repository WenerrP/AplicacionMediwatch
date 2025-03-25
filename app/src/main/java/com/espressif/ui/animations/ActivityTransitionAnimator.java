package com.espressif.ui.animations;

import android.app.Activity;
import android.content.Intent;

import com.espressif.AppConstants;
import com.espressif.wifi_provisioning.R;

public class ActivityTransitionAnimator {

    public static void startActivityWithSlideAnimation(Activity activity, Intent intent) {
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public static void finishWithSlideAnimation(Activity activity) {
        activity.finish();
        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}