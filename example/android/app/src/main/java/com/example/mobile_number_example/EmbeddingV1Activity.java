package com.example.mobile_number_example;

import android.os.Bundle;

import com.amorenew.mobile_number.MobileNumberPlugin;
import io.flutter.app.FlutterActivity;

public class EmbeddingV1Activity extends FlutterActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MobileNumberPlugin.registerWith(registrarFor("com.amorenew.mobile_number.MobileNumberPlugin()"));
    }
}
