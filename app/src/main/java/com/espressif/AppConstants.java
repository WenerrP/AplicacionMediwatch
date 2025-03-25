// Copyright 2020 Espressif Systems (Shanghai) PTE LTD
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.espressif;

import android.content.Intent;

import com.espressif.wifi_provisioning.R;

public class AppConstants {

    // Keys used to pass data between activities and to store data in SharedPreference.
    public static final String KEY_WIFI_SECURITY_TYPE = "wifi_security";
    public static final String KEY_PROOF_OF_POSSESSION = "proof_of_possession";
    public static final String KEY_WIFI_DEVICE_NAME_PREFIX = "wifi_network_name_prefix";
    public static final String KEY_BLE_DEVICE_NAME_PREFIX = "ble_device_name_prefix";
    public static final String KEY_DEVICE_NAME = "device_name";
    public static final String KEY_STATUS_MSG = "status_msg";
    public static final String KEY_WIFI_SSID = "ssid";
    public static final String KEY_WIFI_PASSWORD = "password";
    public static final String KEY_DEVICE_TYPES = "device_types";
    public static final String KEY_SECURITY_TYPE = "security_type";
    public static final String KEY_USER_NAME_WIFI = "sec2_username_wifi";
    public static final String KEY_USER_NAME_THREAD = "sec2_username_thread";
    public static final String KEY_THREAD_DATASET = "thread_dataset";
    public static final String KEY_THREAD_SCAN_AVAILABLE = "thread_scan_available";

    public static final String ESP_PREFERENCES = "espressif_pref";

    public static final String DEVICE_TYPE_SOFTAP = "softap";
    public static final String DEVICE_TYPE_BLE = "ble";
    public static final String DEVICE_TYPE_BOTH = "both";
    public static final String DEVICE_TYPE_DEFAULT = DEVICE_TYPE_BOTH;

    public static final int SEC_TYPE_0 = 0;
    public static final int SEC_TYPE_1 = 1;
    public static final int SEC_TYPE_2 = 2;
    public static final int SEC_TYPE_DEFAULT = SEC_TYPE_2;
    public static final String DEFAULT_USER_NAME_WIFI = "wifiprov";
    public static final String DEFAULT_USER_NAME_THREAD = "threadprov";

    public static final String CAPABILITY_WIFI_SCAN = "wifi_scan";
    public static final String CAPABILITY_THREAD_SCAN = "thread_scan";
    public static final String CAPABILITY_THREAD_PROV = "thread_prov";

    // MQTT Related Constants
    public static final String MQTT_BROKER_URI = "tcp://broker.emqx.io:1883";
    public static final String MQTT_CLIENT_ID_PREFIX = "AndroidClient_";
    public static final String MQTT_FINDER_CLIENT_ID_PREFIX = "AndroidFinder_";
    public static final String MQTT_TOPIC_STATUS = "/device/status";
    public static final String MQTT_TOPIC_COMMANDS = "/device/commands";
    public static final String MQTT_TOPIC_HEARTBEAT = "/device/heartbeat";

    // Heartbeat Constants
    public static final int HEARTBEAT_TIMEOUT = 10000; // 10 seconds
    public static final int CONNECTION_CHECK_INTERVAL = 3000; // 3 seconds
    public static final int STABILITY_THRESHOLD = 2;

    // SharedPreferences Constants
    public static final String PREF_NAME = "EspProvisioningPrefs";
    public static final String KEY_IS_PROVISIONED = "isProvisioned";
    public static final String KEY_DEVICE_ID = "deviceId";
    public static final String KEY_FROM_RESET = "FROM_RESET";

    // Request Codes
    public static final int REQUEST_LOCATION = 1;
    public static final int REQUEST_ENABLE_BT = 2;
    public static final int REQUEST_FINE_LOCATION = 10;
    public static final int WIFI_SETTINGS_ACTIVITY_REQUEST = 11;

    // Device Search Constants
    public static final long DEVICE_SEARCH_TIMEOUT_MS = 10000; // 10 seconds
    public static final int MQTT_SEARCH_QOS = 1;
    public static final int MQTT_CONNECTION_TIMEOUT = 10; // seconds

    // Animation Constants
    public static final int LOGO_FADE_DURATION = 800;
    public static final int BUTTON_ANIMATION_DELAY = 300;
    public static final int BUTTON_ANIMATION_DURATION = 500;
    public static final float BUTTON_INITIAL_SCALE = 0.9f;

    // Firebase Database Paths
    public static final String FB_USERS_PATH = "users";
    public static final String FB_DEVICES_PATH = "devices";
    public static final String FB_ALARMS_PATH = "alarms";

    // Intent Extra Keys
    public static final String EXTRA_PATIENT_ID = "PATIENT_ID";
    public static final String EXTRA_DEVICE_ID = "DEVICE_ID";

    // Error Messages
    public static final String ERROR_EMPTY_PATIENT_ID = "Por favor ingrese un ID válido";
    public static final String ERROR_PATIENT_NOT_FOUND = "ID de paciente no encontrado";

    // Log Tags
    public static final String TAG_FAMILY_ACTIVITY = "FamilyActivity";

    // BLE Constants
    public static final long DEVICE_CONNECT_TIMEOUT = 20000;

    // Device Capabilities
    public static final String CAPABILITY_NO_POP = "no_pop";
    public static final String CAPABILITY_WIFI_CONFIG = "wifi_config";

    // Error Messages
    public static final String ERROR_BLE_NOT_SUPPORTED = "BLE is not supported on this device";
    public static final String ERROR_BLUETOOTH_NOT_SUPPORTED = "Bluetooth is not supported on this device";
    public static final String ERROR_DEVICE_NOT_FOUND = "No device found";
    public static final String ERROR_DEVICE_CONNECTION_FAILED = "Device connection failed";
    public static final String ERROR_DEVICE_NOT_SUPPORTED = "Device not supported";
    public static final String ERROR_LOCATION_PERMISSION = "Please give location permission to connect device";
    public static final String ERROR_LOCATION_PERMISSION_NOT_GRANTED = "Not able to connect device as Location permission is not granted.";
    public static final String ERROR_SECURITY_MISMATCH = "Security type mismatch";
    public static final String ERROR_DEVICE_CONNECT_FAILED = "Device connection failed";
    public static final String ERROR_MQTT_CONNECTION = "Error de conexión MQTT";
    public static final String ERROR_MQTT_PUBLISH = "Error al publicar mensaje";
    public static final String ERROR_MQTT_SUBSCRIBE = "Error al suscribirse al tópico";
    public static final String ERROR_MQTT_CONNECT = "Error de conexión al broker";
    public static final String ERROR_MQTT_DISCONNECT = "Error al desconectar";
    public static final String ERROR_MQTT_CONNECTION_LOST = "Conexión perdida con el broker";
    public static final String ERROR_MQTT_TIMEOUT = "Tiempo de espera agotado";

    // Authentication Constants
    public static final int RC_SIGN_IN = 9001;
    public static final String TAG_PATIENT_ACTIVITY = "PatientActivity";

    // Error Messages
    public static final String ERROR_GOOGLE_SIGN_IN = "Google sign in failed";
    public static final String ERROR_SIGN_IN_CREDENTIAL = "signInWithCredential:failure";
    public static final String ERROR_CREATE_USER = "createUserInDatabase:failure";

    // Activity Tags
    public static final String TAG_POP_ACTIVITY = "ProofOfPossessionActivity";
    public static final String TAG_PROVISION_ACTIVITY = "ProvisionActivity";
    public static final String TAG_PROVISION_LANDING = "ProvisionLanding";

    // Default Values
    public static final String DEFAULT_POP = "espressif123";
    
    // Device Events
    public static final String EVENT_DEVICE_DISCONNECTED = "device_disconnected";
    public static final String EVENT_DEVICE_CONNECTED = "device_connected";
    public static final String EVENT_PROVISIONING_FAILED = "provisioning_failed";
    public static final String EVENT_PROVISIONING_SUCCESS = "provisioning_success";
    public static final String EVENT_DEVICE_CONNECTION_FAILED = "device_connection_failed";

    // Error Messages
    public static final String ERROR_DEVICE_DISCONNECTED = "Device disconnected unexpectedly";
    public static final String ERROR_SESSION_CREATION = "Error creating session";
    public static final String ERROR_PROV_STEP_1 = "Error in step 1";
    public static final String ERROR_PROV_STEP_2 = "Error in step 2";
    public static final String ERROR_PROV_STEP_3 = "Error in step 3";
    public static final String ERROR_PROV_THREAD_STEP_1 = "Error in thread step 1";
    public static final String ERROR_PROV_THREAD_STEP_2 = "Error in thread step 2";
    public static final String ERROR_AUTH_FAILED = "Authentication failed";
    public static final String ERROR_NETWORK_NOT_FOUND = "Network not found";

    // Intent Flags
    public static final int FLAGS_NEW_TASK_CLEAR_TASK = 
            Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK;

    // Settings Constants
    public static final String SETTING_TYPE_SECURED = "secured";
    public static final String SETTING_TYPE_UNSECURED = "unsecured";
    
    // Version Format
    public static final String VERSION_PREFIX = "v";
    public static final String VERSION_SEPARATOR = " - ";

    // Preference Default Values
    public static final boolean DEFAULT_SECURITY_ENABLED = true;
    public static final String DEFAULT_USERNAME_WIFI = "admin_wifi";
    public static final String DEFAULT_USERNAME_THREAD = "admin_thread";

    // Activity Tags
    public static final String TAG_SETTINGS = "SettingsActivity";

    // Thread Network Constants
    public static final long THREAD_SCAN_TIMEOUT = 15000; // 15 seconds
    public static final float BUTTON_DISABLED_ALPHA = 0.5f;
    public static final float BUTTON_ENABLED_ALPHA = 1.0f;

    // Activity Tags
    public static final String TAG_THREAD_CONFIG = "ThreadConfigActivity";

    // Error Messages
    public static final String ERROR_USER_DENIED_REQUEST = "User denied request.";
    public static final String ERROR_NO_PREFERRED_CREDS = "No preferred credentials found!";
    public static final String ERROR_THREAD_SCAN_FAILED = "Failed to get thread scan list";
    
    // Progress Messages
    public static final String PROGRESS_THREAD_NETWORKS = "Searching for Thread networks...";
    public static final String PROGRESS_NETWORK_AVAILABLE = "Available Thread Network: %s\nDo you want to proceed?";

    // Log Messages
    public static final String LOG_THREAD_SCAN_START = "Start Thread Scan";
    public static final String LOG_THREAD_NETWORK_FOUND = "Thread Network available: %s";
    public static final String LOG_THREAD_SCAN_FAILED = "onWiFiScanFailed";
    public static final String LOG_PREFERRED_CREDS = "ThreadClient: getPreferredCredentials intent sent";
    public static final String LOG_PREFERRED_CREDS_NAME = "Preferred Credentials Network Name: %s";

    // User Types
    public static final String USER_TYPE_PATIENT = "patient";
    public static final String USER_TYPE_FAMILY = "family";
    
    // Activity Transitions
    public static final int TRANSITION_FADE = 0;
    public static final int TRANSITION_SLIDE = 1;
    public static final int DEFAULT_TRANSITION = TRANSITION_FADE;

    // Intent Extra Keys
    public static final String EXTRA_USER_TYPE = "user_type";

    // Activity Tags
    public static final String TAG_WIFI_CONFIG = "WiFiConfigActivity";

    // SharedPreferences
    public static final String PREFS_PROVISIONING = "EspProvisioningPrefs";

    // Dialog Constants
    public static final boolean DIALOG_NOT_CANCELABLE = false;

    // Intent Flags
    public static final int FLAG_CLEAR_ACTIVITIES = 
            Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK;

    // Error Messages
    public static final String ERROR_SSID_EMPTY = "Please enter SSID";
    public static final String ERROR_DEVICE_DISCONNECT = "Device disconnected unexpectedly";

    // Format Strings
    public static final String FORMAT_SETUP_INSTRUCTIONS = "Provide WiFi credentials for %s";

    // WiFi Scan Constants
    public static final long WIFI_SCAN_TIMEOUT = 15000; // 15 seconds
    public static final String JOIN_NETWORK_OPTION = "Join Other Network";
    
    // Activity Tags
    public static final String TAG_WIFI_SCAN = "WiFiScanActivity";

    // WiFi Security Types
    public static final int WIFI_SECURITY_OPEN = 0;
    public static final int WIFI_SECURITY_WEP = 1;
    public static final int WIFI_SECURITY_WPA = 2;
    public static final int WIFI_SECURITY_WPA2 = 3;

    // Error Messages
    public static final String ERROR_WIFI_SCAN_FAILED = "Failed to get Wi-Fi scan list";
    public static final String ERROR_PASSWORD_EMPTY = "Please enter password";

    // Device Search
    public static final String DEVICE_ID_PREFIX = "mediwatch_";
    public static final String ERROR_NO_DEVICE_FOUND = "No se encontró ningún dispositivo";

    // Error Messages
    public static final String ERROR_INVALID_DEVICE_ID = "Error: Intento de abrir MQTT Dashboard sin ID de dispositivo";
    public static final String ERROR_NO_DEVICE_ASSOCIATED = "Error: No hay dispositivo asociado";

    // Animation Resources
    public static final int ANIM_SLIDE_IN_RIGHT = R.anim.slide_in_right;
    public static final int ANIM_SLIDE_OUT_LEFT = R.anim.slide_out_left;
    public static final int ANIM_SLIDE_IN_LEFT = R.anim.slide_in_left;
    public static final int ANIM_SLIDE_OUT_RIGHT = R.anim.slide_out_right;

    // Navigation Extras
    public static final String EXTRA_FROM_LOGIN = "from_login";
    public static final String TAG_ESP_MAIN = "";

    public static final String TAG_DEVICE_MANAGER = "DeviceManager";
    public static final String TAG_LOCATION_MANAGER = "LocationManager";
    public static final String TAG_MQTT_HANDLER = "MqttHandler";
    public static final String TAG_THREAD_SCAN = "ThreadScanActivity";
    public static final String TAG_THREAD_PROVISION = "ThreadProvisionActivity";
    public static final String TAG_BLE_SCAN = "BleScanActivity";
    public static final String TAG_BLE_CONNECT = "BleConnectActivity";
    public static final String TAG_BLE_PROVISION = "BleProvisionActivity";
    public static final String TAG_MQTT_ACTIVITY = "MqttActivity";

    public static final String PREFS_NAME = "mediwatch";
    public static final String LOG_PROVISIONING_SUCCESS = "Provisionado correcto con ID: %s";
    public static final String ERROR_NO_DEVICE_ID = "Error: No hay ID de dispositivo";
    public static final String ERROR_EMPTY_DEVICE_ID = "Error: ID de dispositivo vacío";
}
