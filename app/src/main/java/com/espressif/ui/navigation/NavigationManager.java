package com.espressif.ui.navigation;

import android.app.Activity;
import android.content.Intent;
import com.espressif.AppConstants;
import com.espressif.ui.activities.*;
import com.espressif.ui.animations.ActivityTransitionAnimator;

public class NavigationManager {
    
    public static void goToUserType(Activity activity) {
        Intent intent = new Intent(activity, UserTypeActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }

    public static void goToMqttDashboard(Activity activity, String deviceId) {
        Intent intent = new Intent(activity, MqttActivity.class);
        intent.putExtra(AppConstants.EXTRA_DEVICE_ID, deviceId);
        ActivityTransitionAnimator.startActivityWithSlideAnimation(activity, intent);
        activity.finish();
    }

    public static void goToProvisioning(Activity activity, String deviceType, int securityType) {
        Intent intent;
        if (deviceType.equals(AppConstants.DEVICE_TYPE_BLE)) {
            intent = new Intent(activity, BLEProvisionLanding.class);
        } else {
            intent = new Intent(activity, ProvisionLanding.class);
        }
        intent.putExtra(AppConstants.KEY_SECURITY_TYPE, securityType);
        activity.startActivity(intent);
    }

    public static void goToProvisioningAfterLogin(Activity activity) {
        Intent intent = new Intent(activity, EspMainActivity.class);
        intent.putExtra(AppConstants.EXTRA_FROM_LOGIN, true);
        ActivityTransitionAnimator.startActivityWithSlideAnimation(activity, intent);
        activity.finish();
    }

    public static void backToLogin(Activity activity) {
        Intent intent = new Intent(activity, UserTypeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
        activity.finish();
    }
}