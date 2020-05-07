import 'dart:async';

import 'package:flutter/services.dart';
import 'package:mobile_number/sim_card.dart';

class MobileNumber {
  static const MethodChannel _channel = const MethodChannel('mobile_number');

  static Future<String> get mobileNumber async {
    final String simCardsJson = await _channel.invokeMethod('getMobileNumber');
    if (simCardsJson.isEmpty) {
      return '';
    }
    List<SimCard> simCards = SimCard.parseSimCards(simCardsJson);
    if (simCards != null && simCards.isNotEmpty) {
      return simCards[0].countryPhonePrefix + simCards[0].number;
    } else {
      return '';
    }
  }

  static Future<List<SimCard>> get getSimCards async {
    final String simCardsJson = await _channel.invokeMethod('getMobileNumber');
    if (simCardsJson.isEmpty) {
      return <SimCard>[];
    }
    List<SimCard> simCards = SimCard.parseSimCards(simCardsJson);
    if (simCards != null && simCards.isNotEmpty) {
      return simCards;
    } else {
      return <SimCard>[];
    }
  }
}
