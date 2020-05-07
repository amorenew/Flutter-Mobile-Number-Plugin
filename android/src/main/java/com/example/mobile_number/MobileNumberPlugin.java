package com.example.mobile_number;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;

import java.util.List;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.plugin.common.PluginRegistry.RequestPermissionsResultListener;

/**
 * MobileNumberPlugin
 */
public class MobileNumberPlugin implements FlutterPlugin, MethodCallHandler, RequestPermissionsResultListener {
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 0;
    private static Context context;
    private TelephonyManager telephonyManager;
    private Result result;

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "mobile_number");
        channel.setMethodCallHandler(new MobileNumberPlugin());
        MobileNumberPlugin.context = registrar.context();
    }
    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        final MethodChannel channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "mobile_number");
        channel.setMethodCallHandler(new MobileNumberPlugin());
        MobileNumberPlugin.context = flutterPluginBinding.getApplicationContext();

    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {

    }
    @Override
    public void onMethodCall(MethodCall call, Result result) {
        this.result = result;
        telephonyManager = (TelephonyManager) MobileNumberPlugin.context
                .getSystemService(Context.TELEPHONY_SERVICE);

        if (call.method.equals("getMobileNumber")) {
            getMobileNumber();
        } else {
            result.notImplemented();
        }
    }

    private void getMobileNumber() {
        if (ContextCompat.checkSelfPermission(MobileNumberPlugin.context,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) MobileNumberPlugin.context,
                    Manifest.permission.READ_PHONE_STATE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions((Activity) MobileNumberPlugin.context,
                        new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_PHONE_NUMBERS}, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
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
            subscriptionManager = SubscriptionManager.from(MobileNumberPlugin.context);

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
        // MobileNumberPlugin.registrar.super.onRequestPermissionsResult(requestCode,
        // permissions, grantResults);
        // If request is cancelled, the result arrays are empty.
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_PHONE_STATE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                generateMobileNumber();
                return true;
            }
        }
        result.error("PERMISSION", "onRequestPermissionsResult is not granted", null);
        return false;
    }


}
