package com.example.mobile_number;

import android.os.Build;
import android.telephony.SubscriptionInfo;

import androidx.annotation.RequiresApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class SimCard {

    private String carrierName;
    private  String displayName;
    private int slotIndex;
    private String number;
    private String countryIso;
    private  String countryPhonePrefix;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    public SimCard(SubscriptionInfo subscriptionInfo) {
        this.carrierName = subscriptionInfo.getCarrierName().toString();
        this.displayName =subscriptionInfo.getDisplayName().toString();
        this.slotIndex = subscriptionInfo.getSimSlotIndex();
        this.number = subscriptionInfo.getNumber();
        this.countryIso = subscriptionInfo.getCountryIso();
        this.countryPhonePrefix = CountryToPhonePrefix.prefixFor(subscriptionInfo.getCountryIso());
    }

    // final JSONArray jsonArray = new JSONArray();

    JSONObject toJSON(){
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




