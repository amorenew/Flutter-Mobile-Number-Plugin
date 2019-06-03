# mobile_number

This is a FLutter Plugin to get the device mobile number.
#### Note: It works for Android only because getting mobile number of sim card is not supported in iOS.

## Installation 
#### Link on Flutter plugins
https://pub.dev/packages/mobile_number

#### Add this to your package's pubspec.yaml file:
```
dependencies:
  mobile_number: ^0.0.2
```

## Usage
#### Sample
```
Future<String> fillMobileNumber() async {
    final String mobileNumber = await MobileNumber.mobileNumber;
    return mobileNumber;
  }
  ```
  
