package com.amorenew.mobile_number;

import android.annotation.SuppressLint;
import android.os.Build;
import android.telephony.SubscriptionInfo;
import android.telephony.TelephonyManager;

import androidx.annotation.RequiresApi;

import org.json.JSONException;
import org.json.JSONObject;

public class SimCard {

    private String carrierName = "";
    private String displayName = "";
    private int slotIndex = 0;
    private String number = "";
    private String countryIso = "";
    private String countryPhonePrefix = "";

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    public SimCard(TelephonyManager telephonyManager, SubscriptionInfo subscriptionInfo) {
        this.carrierName = subscriptionInfo.getCarrierName().toString();
        this.displayName = subscriptionInfo.getDisplayName().toString();
        this.slotIndex = subscriptionInfo.getSimSlotIndex();
        this.number = subscriptionInfo.getNumber();
        if (subscriptionInfo.getCountryIso() != null && !subscriptionInfo.getCountryIso().isEmpty())
            this.countryIso = subscriptionInfo.getCountryIso();
        else if (telephonyManager.getSimCountryIso() != null)
            this.countryIso = telephonyManager.getSimCountryIso();
        this.countryPhonePrefix = CountryToPhonePrefix.prefixFor(this.countryIso);
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public SimCard(TelephonyManager telephonyManager) {
        if (telephonyManager.getSimOperator() != null)
            carrierName = telephonyManager.getSimOperatorName();
        if (telephonyManager.getSimOperator() != null)
            displayName = telephonyManager.getSimOperatorName();
        if (telephonyManager.getSimCountryIso() != null) {
            countryIso = telephonyManager.getSimCountryIso();
            countryPhonePrefix = CountryToPhonePrefix.prefixFor(countryIso);
        }
        if (telephonyManager.getLine1Number() != null && !telephonyManager.getLine1Number().isEmpty()) {
            if (telephonyManager.getLine1Number().startsWith("0"))
                number = countryPhonePrefix + telephonyManager.getLine1Number().substring(1);
            number = telephonyManager.getLine1Number();
        }
    }

// final JSONArray jsonArray = new JSONArray();

    JSONObject toJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put("carrierName", carrierName);
            json.put("displayName", displayName);
            json.put("slotIndex", slotIndex);
            json.put("number", number);
            json.put("countryIso", countryIso);
            json.put("countryPhonePrefix", countryPhonePrefix);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;

    }
}




