package com.example.mobile_number;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;

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
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.plugin.common.PluginRegistry.RequestPermissionsResultListener;

/**
 * MobileNumberPlugin
 */
public class MobileNumberPlugin implements FlutterPlugin, ActivityAware, MethodCallHandler, RequestPermissionsResultListener {
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 0;
    final String Event_phonePermissionResult = "requestPhonePermission=";
    EventChannel.EventSink permissionEvent;
    private Context applicationContext;
    private Activity activity;
    private TelephonyManager telephonyManager;
    private Result result;
    private MethodChannel methodChannel;
    private EventChannel permissionEventChannel;

    {

    }

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final MobileNumberPlugin instance = new MobileNumberPlugin();
        instance.onAttachedToEngine(registrar.context(), registrar.messenger(), registrar.activity());
    }

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        onAttachedToEngine(flutterPluginBinding.getApplicationContext(), flutterPluginBinding.getBinaryMessenger(), null);
    }

    private void onAttachedToEngine(Context applicationContext, BinaryMessenger messenger, Activity activity) {
        this.applicationContext = applicationContext;
        methodChannel = new MethodChannel(messenger, "mobile_number");
        methodChannel.setMethodCallHandler(this);
        permissionEventChannel = new EventChannel(messenger, "phone_permission_event");
        permissionEventChannel.setStreamHandler(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object o, EventChannel.EventSink eventSink) {
                permissionEvent = eventSink;
            }

            @Override
            public void onCancel(Object o) {

            }
        });
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {

    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding activityPluginBinding) {
        //MobileNumberPlugin.activity = activityPluginBinding.getActivity();
        //activityV2 = activityPluginBinding.getActivity();
        activity = activityPluginBinding.getActivity();
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding activityPluginBinding) {

    }

    @Override
    public void onDetachedFromActivity() {

    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        this.result = result;
        final String method_GetMobileNumber = "getMobileNumber";
        final String method_hasPhonePermission = "hasPhonePermission";
        final String method_requestPhonePermission = "requestPhonePermission";
        switch (call.method) {
            case method_GetMobileNumber:
                telephonyManager = (TelephonyManager) applicationContext
                        .getSystemService(Context.TELEPHONY_SERVICE);
                getMobileNumber();
                break;
            case method_hasPhonePermission:
                result.success(hasPhonePermission());
                break;
            case method_requestPhonePermission:
                requestPhonePermission();
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    private boolean hasPhonePermission() {
        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            return ContextCompat.checkSelfPermission(applicationContext,
                    Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(applicationContext,
                    Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPhonePermission() {
        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.READ_PHONE_NUMBERS)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.READ_PHONE_NUMBERS}, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
            }
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.READ_PHONE_STATE)) {
            } else {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.READ_PHONE_STATE}, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
            }
        }
    }

    private void getMobileNumber() {
        if (!hasPhonePermission()) {
            requestPhonePermission();
        } else {
            // Permission has already been granted
            generateMobileNumber();

        }
    }

    @SuppressLint("HardwareIds")
    private void generateMobileNumber() {
        String countryIso = telephonyManager.getSimCountryIso();
        String countryPhoneCode = CountryToPhonePrefix.prefixFor(countryIso);
        JSONArray simJsonArray = new JSONArray();
        final SubscriptionManager subscriptionManager;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            subscriptionManager = SubscriptionManager.from(activity);

            final List<SubscriptionInfo> activeSubscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
            for (SubscriptionInfo subscriptionInfo : activeSubscriptionInfoList) {
                SimCard simCard = new SimCard(subscriptionInfo);
                simJsonArray.put(simCard.toJSON());
            }
        }

//        String line1Number = telephonyManager.getLine1Number();
//        if (line1Number.startsWith("0"))
//            line1Number = line1Number.substring(1);
//        String mobileNumber = countryPhoneCode + line1Number;
        if (simJsonArray.toString().isEmpty()) {
            result.error("UNAVAILABLE", "No phone number on sim card", null);
        } else result.success(simJsonArray.toString());
    }

    @Override
    public boolean onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                              @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_PHONE_STATE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (permissionEvent != null)
                    permissionEvent.success(true);
                generateMobileNumber();
                return true;
            } else {
                if (permissionEvent != null)
                    permissionEvent.success(false);
            }
        }
        result.error("PERMISSION", "onRequestPermissionsResult is not granted", null);
        return false;
    }


}
