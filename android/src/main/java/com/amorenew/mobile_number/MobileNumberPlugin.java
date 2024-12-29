package com.amorenew.mobile_number;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

public class MobileNumberPlugin implements FlutterPlugin, ActivityAware, MethodCallHandler {
    private static final int REQUEST_PHONE_PERMISSION = 123;
    private static final String TAG = "MobileNumberPlugin";

    private Context applicationContext;
    private Activity activity;
    private TelephonyManager telephonyManager;
    private Result pendingResult;
    private MethodChannel methodChannel;
    private EventChannel permissionEventChannel;
    private EventChannel.EventSink permissionEvent;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        onAttachedToEngine(flutterPluginBinding.getApplicationContext(), flutterPluginBinding.getBinaryMessenger());
    }

    private void onAttachedToEngine(Context applicationContext, BinaryMessenger messenger) {
        this.applicationContext = applicationContext;

        methodChannel = new MethodChannel(messenger, "mobile_number");
        methodChannel.setMethodCallHandler(this);

        permissionEventChannel = new EventChannel(messenger, "phone_permission_event");
        permissionEventChannel.setStreamHandler(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object args, EventChannel.EventSink eventSink) {
                permissionEvent = eventSink;
            }

            @Override
            public void onCancel(Object args) {
                permissionEvent = null;
            }
        });
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        this.pendingResult = result;
        telephonyManager = (TelephonyManager) applicationContext.getSystemService(Context.TELEPHONY_SERVICE);

        switch (call.method) {
            case "getMobileNumber":
                handleGetMobileNumber();
                break;
            case "hasPhonePermission":
                result.success(hasPhonePermission());
                break;
            case "requestPhonePermission":
                requestPhonePermission();
                break;
            default:
                result.notImplemented();
        }
    }

    private String getRequiredPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Manifest.permission.READ_PHONE_NUMBERS;
        }
        return Manifest.permission.READ_PHONE_STATE;
    }

    private boolean hasPhonePermission() {
        return ContextCompat.checkSelfPermission(applicationContext, getRequiredPermission())
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPhonePermission() {
        if (activity == null) {
            if (permissionEvent != null) {
                permissionEvent.error("ACTIVITY_NULL", "Activity is null", null);
            }
            return;
        }

        ActivityCompat.requestPermissions(
                activity,
                new String[]{getRequiredPermission()},
                REQUEST_PHONE_PERMISSION
        );
    }

    private void handleGetMobileNumber() {
        if (!hasPhonePermission()) {
            requestPhonePermission();
            return;
        }

        try {
            List<SimCard> simCards = getSimCards();
            if (simCards != null && !simCards.isEmpty()) {
                JSONArray simJsonArray = new JSONArray();

                for (int i = 0; i < simCards.size(); i++) {
                    simJsonArray.put(simCards.get(i).toJSON());
                }
                pendingResult.success(simJsonArray.toString());
            } else {
                pendingResult.error(
                        "UNAVAILABLE",
                        "Phone number is not available",
                        null
                );
            }
        } catch (Exception e) {
            pendingResult.error(
                    "ERROR",
                    "Error retrieving phone number: " + e.getMessage(),
                    null
            );
        }
    }

    @SuppressLint({"HardwareIds", "MissingPermission"})
    private List<SimCard> getSimCards() {
        List<SimCard> simCards = new ArrayList<>();

        if (!hasPhonePermission()) {
            return simCards;
        }

        String phoneNumber = null;

        try {
            // Try to get phone number from default SIM
            phoneNumber = telephonyManager.getLine1Number();
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                simCards.add(new SimCard(telephonyManager));
            }
            // If default method failed, try to get from SubscriptionManager (Android 5.1 and above)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                SubscriptionManager subscriptionManager = (SubscriptionManager)
                        applicationContext.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

                if (subscriptionManager != null) {
                    List<SubscriptionInfo> subscriptionInfos = subscriptionManager.getActiveSubscriptionInfoList();
                    if (subscriptionInfos != null && !subscriptionInfos.isEmpty()) {
                        // Get the first active subscription
                        SubscriptionInfo subscriptionInfo = subscriptionInfos.get(0);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            // For Android N (7.0) and above
                            TelephonyManager subscriptionTelephonyManager =
                                    telephonyManager.createForSubscriptionId(subscriptionInfo.getSubscriptionId());
                            String subscriptionPhoneNumber = subscriptionTelephonyManager.getLine1Number();
                            if (subscriptionPhoneNumber != null && !subscriptionPhoneNumber.isEmpty() && !subscriptionPhoneNumber.equals(phoneNumber)) {
                                simCards.add(new SimCard(subscriptionTelephonyManager));
                            }

                        }
                    }
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error getting phone number: " + e.getMessage());
            return null;
        }

        return simCards;
    }

    // ActivityAware implementation
    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        activity = null;
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivity() {
        activity = null;
    }

    // FlutterPlugin implementation
    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        methodChannel.setMethodCallHandler(null);
        permissionEventChannel.setStreamHandler(null);
    }
}