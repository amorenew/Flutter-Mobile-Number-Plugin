import 'dart:async';

import 'package:flutter/services.dart';

class MobileNumber {
  static const MethodChannel _channel =
      const MethodChannel('mobile_number');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}
