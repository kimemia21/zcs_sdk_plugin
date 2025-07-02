# zcs_sdk_plugin

A Flutter plugin that provides a wrapper around the [ZCS SDK](https://www.szzcs.com/), allowing developers to interact with ZCS-compatible hardware devices from Flutter apps.

> ⚠️ **Disclaimer:** This plugin is an unofficial integration of the ZCS SDK. It does not include the SDK itself and is not affiliated with or endorsed by ZCS. You must obtain and configure the original SDK separately as per ZCS's licensing and documentation.

---

## ✨ Features

- Initialize ZCS hardware devices
- Open/close the device connection
- Print plain text
- Print QR codes
- Query device status

This plugin helps simplify integration of ZCS hardware into your Flutter apps by exposing commonly used methods via a Dart-friendly API.

---

## 🛠 Installation

Add this to your `pubspec.yaml`:

```yaml
dependencies:
  zcs_sdk_plugin: ^<latest-version>
Then run:

bash
Copy
Edit
flutter pub get
📌 Replace <latest-version> with the latest version from pub.dev.

⚙️ Platform Setup
Android
Ensure you have added the official ZCS SDK .aar or .jar files to your Android project.

Add any required permissions to your AndroidManifest.xml (as specified by ZCS).

Initialize the SDK in your app as needed (see example below).

iOS
⚠️ Currently only Android is supported. iOS support may be added in the future if ZCS provides a native iOS SDK.

🚀 Usage Example
dart
Copy
Edit
import 'package:zcs_sdk_plugin/zcs_sdk_plugin_platform_interface.dart';

final ZcsSdkPluginPlatform _plugin = ZcsSdkPluginPlatform.instance;

// Initialize device
await _plugin.initializeDevice();

// Open connection
await _plugin.openDevice();

// Print text
await _plugin.printText(
  'Hello World',
  fontSize: 32,
  isBold: true,
  alignment: 'CENTER',
);

// Print QR code
await _plugin.printQrCode('https://example.com');

// Get status
final status = await _plugin.getDeviceStatus();

// Close device
await _plugin.closeDevice();
You can also explore the full working example in the example/ directory for more details.

📦 Available Methods
Method	Description
initializeDevice()	Initializes the printer device
openDevice()	Opens a connection to the device
printText(...)	Prints formatted text
printQrCode(...)	Prints a QR code
getDeviceStatus()	Returns device status as a string
closeDevice()	Closes the connection to the device

📄 License
This plugin is released under the MIT License. See LICENSE for details.

🔍 Disclaimer
This Flutter plugin is an independent wrapper created to simplify integration of the ZCS SDK into Flutter apps.

It does not include or redistribute the ZCS SDK

It does not modify or reverse-engineer the SDK

It is not affiliated with or endorsed by the creators of the ZCS SDK

Please consult the official ZCS documentation and license terms at https://www.szzcs.com before using the SDK in your application.
