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
  
### Or you could use it like this
```
  Future<void> initMobileNumberState() async {
    String mobileNumber = '';
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      mobileNumber = await MobileNumber.mobileNumber;
    } on PlatformException catch (e) {
      debugPrint("Failed to get mobile number because of '${e.message}'");
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _mobileNumber = mobileNumber;
    });
  }
  ```