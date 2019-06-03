import 'dart:async';

import 'package:flutter/services.dart';

class MobileNumber {
  static const MethodChannel _channel = const MethodChannel('mobile_number');

  static Future<String> get mobileNumber async {
    final String number = await _channel.invokeMethod('getMobileNumber');
    return number;
  }
}
