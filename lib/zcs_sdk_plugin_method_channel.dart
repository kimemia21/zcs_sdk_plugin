import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'zcs_sdk_plugin_platform_interface.dart';

class MethodChannelZcsSdkPlugin extends ZcsSdkPluginPlatform {
  @visibleForTesting
  final channel = const MethodChannel('zcs_sdk_plugin');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await channel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<bool> initializeDevice() async {
    try {
      final Map<String, dynamic> result = Map<String, dynamic>.from(
        await channel.invokeMethod('initializeDevice')
      );
      return result['success'] == true;
    } on PlatformException catch (e) {
      throw SmartPosException('Device initialization failed: ${e.message}');
    }
  }
  @override
  Future<Map<String, dynamic>> openDevice() async {
    try {
      final Map<String, dynamic> result = Map<String, dynamic>.from(
        await channel.invokeMethod('openDevice')
      );
      return result;
    } on PlatformException catch (e) {
      throw SmartPosException('Device opening failed: ${e.message}');
    }

  }

  @override
  Future<Map<String, dynamic>> closeDevice() async {
    try {
      final Map<String, dynamic> result = Map<String, dynamic>.from(
        await channel.invokeMethod('closeDevice')
      );
      return result;
    } on PlatformException catch (e) {
      throw SmartPosException('Device closing failed: ${e.message}');
    }
  }


   /// [text] - Text to print (required)
  /// [fontSize] - Font size (default: 24)
  /// [isBold] - Make text bold (default: false)
  /// [isUnderline] - Underline text (default: false)
  /// [alignment] - Text alignment: "LEFT", "CENTER", "RIGHT" (default: "LEFT")
  /// 
 @override
  Future<Map<String, dynamic>> printText(
    String text, {
    int fontSize = 50,
    bool isBold = false,
    bool isUnderline = false,
    String alignment = "LEFT",
  }) async {
    try {
      final Map<String, dynamic> result = Map<String, dynamic>.from(
        await channel.invokeMethod('printText', {
          'text': text,
          'fontSize': fontSize,
          'isBold': isBold,
          'isUnderline': isUnderline,
          'alignment': alignment,
        })
      );
      return result;
    } on PlatformException catch (e) {
      throw SmartPosException('Failed to print text: ${e.message}');
    }
  }
  
  /// Print a formatted receipt
  /// 
  /// [receiptData] - Receipt data containing header, items, totals, etc.
 @override
  Future<Map<String, dynamic>> printReceipt(
    Map<String, dynamic> receiptData
  ) async {
    try {
      final Map<String, dynamic> result = Map<String, dynamic>.from(
        await channel.invokeMethod('printReceipt', {
          'receiptData': receiptData,
        })
      );
      return result;
    } on PlatformException catch (e) {
      throw SmartPosException('Failed to print receipt: ${e.message}');
    }
  }
  
 @override
  Future<Map<String, dynamic>> printQrCode(
    String data, {
    int size = 200,
    String errorCorrectionLevel = "L", // "L", "M", "Q", "H"
  }) async {
    try {
      final Map<String, dynamic> result = Map<String, dynamic>.from(
        await channel.invokeMethod('printQRCode', {
          'data': data,
          'size': size,
          'errorCorrectionLevel': errorCorrectionLevel,
        })
      );
      return result;
    } on PlatformException catch (e) {
      throw SmartPosException('Failed to print QR code: ${e.message}');
    }
  }


  /// Print an image (logo, signature, etc.)
  /// 
  /// [imageData] - Image data (base64 encoded or file path)
 @override
  Future<Map<String, dynamic>> printImage(
    String imageData
  ) async {
    try {
      final Map<String, dynamic> result = Map<String, dynamic>.from(
        await channel.invokeMethod('printImage', {
          'imageData': imageData,
        })
      );
      return result;
    } on PlatformException catch (e) {
      throw SmartPosException('Failed to print image: ${e.message}');
    }
  }



 @override
  Future<String?> connectToDevice(String deviceId) async {
    try {
      final result = await channel.invokeMethod<String>('connectToDevice', {
        'deviceId': deviceId,
      });
      return result;
    } catch (e) {
      debugPrint('Error connecting to device: $e');
      return null;
    }
  }

  @override
  Future<List<String>> scanForDevices() async {
    try {
      final result = await channel.invokeMethod<List>('scanForDevices');
      return result?.cast<String>() ?? [];
    } catch (e) {
      debugPrint('Error scanning for devices: $e');
      return [];
    }
  }

  @override
  Future<bool> sendCommand(String command) async {
    try {
      final result = await channel.invokeMethod<bool>('sendCommand', {
        'command': command,
      });
      return result ?? false;
    } catch (e) {
      debugPrint('Error sending command: $e');
      return false;
    }
  }

  @override
  Future<String?> getDeviceInfo() async {
    try {
      final result = await channel.invokeMethod<String>('getDeviceInfo');
      return result;
    } catch (e) {
      debugPrint('Error getting device info: $e');
      return null;
    }
  }

  @override
  Future<bool> disconnect() async {
    try {
      final result = await channel.invokeMethod<bool>('disconnect');
      return result ?? false;
    } catch (e) {
      debugPrint('Error disconnecting: $e');
      return false;
    }
  }

  @override
  Future<Map<String, dynamic>?> getDeviceStatus() async {
    try {
      final result = await channel.invokeMethod<Map>('getDeviceStatus');
      return result?.cast<String, dynamic>();
    } catch (e) {
      debugPrint('Error getting device status: $e');
      return null;
    }
  }
}




class SmartPosException implements Exception {
  final String message;
  
  SmartPosException(this.message);
  
  @override
  String toString() {
    return 'SmartPosException: $message';
  }
}