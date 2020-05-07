# mobile_number

This is a FLutter Plugin to get the device mobile number.
#### Note: It works for Android only because getting mobile number of sim card is not supported in iOS.
#### Note: If the mobile number is not pre-exist on sim card it will not return te phone number.

## Installation 
#### Link on Flutter plugins
https://pub.dev/packages/mobile_number

#### Add this to your package's pubspec.yaml file:
```
dependencies:
  mobile_number: ^0.0.5
```

## Usage
#### Get first sim card number

```
Future<String> getMobileNumber() async {
    final String mobileNumber = await MobileNumber.mobileNumber;
    return mobileNumber;
  }

#### Get List of sim cards for dual sim cards

```
Future<List<SimCard>> geSimCards() async {
    final List<SimCard> simCards = await MobileNumber.getSimCards;
    return simCards;
  }  ```
  

![alt text](https://raw.githubusercontent.com/amorenew/Flutter-Mobile-Number-Plugin/master/sample1.png)
