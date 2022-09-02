import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:mobile_number/sim_card.dart';
import 'package:mobile_number/widget_lifecycle.dart';

export 'package:mobile_number/sim_card.dart';
export 'package:mobile_number/widget_lifecycle.dart';

class MobileNumber {
  static const MethodChannel _channel = const MethodChannel('mobile_number');

  static void listenPhonePermission(
    Function(bool isPermissionGranted) subscription,
  ) {
    WidgetsBinding.instance.addObserver(
      WidgetLifecycle(
        resumeCallBack: (() async {
          if (await MobileNumber.hasPhonePermission) {
            subscription(true);
          } else {
            subscription(false);
          }
        }),
      ),
    );
  }

  static Future<bool> get hasPhonePermission async {
    final bool hasPermission =
        await _channel.invokeMethod('hasPhonePermission');
    return hasPermission;
  }

  static Future<void> get requestPhonePermission async {
    await _channel.invokeMethod('requestPhonePermission');
  }

  static Future<String>? get mobileNumber async {
    final String simCardsJson = await _channel.invokeMethod('getMobileNumber');
    if (simCardsJson.isEmpty) {
      return '';
    }
    List<SimCard> simCards = SimCard.parseSimCards(simCardsJson);
    if (simCards.isNotEmpty && simCards[0].number != null) {
      return simCards[0].countryPhonePrefix! + simCards[0].number!;
    } else {
      return '';
    }
  }

  static Future<List<SimCard>>? get getSimCards async {
    final String simCardsJson = await _channel.invokeMethod('getMobileNumber');
    if (simCardsJson.isEmpty) {
      return <SimCard>[];
    }
    List<SimCard> simCards = SimCard.parseSimCards(simCardsJson);
    if (simCards.isNotEmpty) {
      return simCards;
    } else {
      return <SimCard>[];
    }
  }
}
