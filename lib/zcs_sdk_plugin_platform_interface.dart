import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'zcs_sdk_plugin_method_channel.dart';

abstract class ZcsSdkPluginPlatform extends PlatformInterface {
  ZcsSdkPluginPlatform() : super(token: _token);

  static final Object _token = Object();

  static ZcsSdkPluginPlatform _instance = MethodChannelZcsSdkPlugin();

  /// The default instance of [ZcsSdkPluginPlatform] to use.
  ///
  /// Defaults to [MethodChannelZcsSdkPlugin].
  static ZcsSdkPluginPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [ZcsSdkPluginPlatform] when
  /// they register themselves.
  static set instance(ZcsSdkPluginPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('getPlatformVersion() has not been implemented.');
  }

  Future<bool> initializeDevice() {
    throw UnimplementedError('initializeDevice() has not been implemented.');
  }

  Future<Map<String, dynamic>> openDevice() {
    throw UnimplementedError('openDevice() has not been implemented.');
  }

  Future<Map<String, dynamic>> closeDevice() {
    throw UnimplementedError('closeDevice() has not been implemented.');
  }

  Future<Map<String, dynamic>> printText(
    String text, {
    int fontSize = 50,
    bool isBold = false,
    bool isUnderline = false,
    String alignment = "LEFT",
  }) {
    throw UnimplementedError('printText() has not been implemented.');
  }

  /// Print a formatted receipt
  ///
  /// [receiptData] - Receipt data containing header, items, totals, etc.
  Future<Map<String, dynamic>> printReceipt(Map<String, dynamic> receiptData) {
    throw UnimplementedError('printReceipt() has not been implemented.');
  }

  Future<Map<String, dynamic>> printQrCode(
    String data, {
    int size = 200,
    String errorCorrectionLevel = "L", // "L", "M", "Q", "H"
  }) {
    throw UnimplementedError('printQrCode() has not been implemented.');
  }

  /// Print an image (logo, signature, etc.)
  ///
  /// [imageData] - Image data (base64 encoded or file path)
  Future<Map<String, dynamic>> printImage(String imageData) {
    throw UnimplementedError('printImage() has not been implemented.');
  }

  Future<String?> connectToDevice(String deviceId) {
    throw UnimplementedError('connectToDevice() has not been implemented.');
  }

  Future<List<String>> scanForDevices() {
    throw UnimplementedError('scanForDevices() has not been implemented.');
  }

  Future<bool> sendCommand(String command) {
    throw UnimplementedError('sendCommand() has not been implemented.');
  }

  Future<String?> getDeviceInfo() {
    throw UnimplementedError('getDeviceInfo() has not been implemented.');
  }

  Future<bool> disconnect() {
    throw UnimplementedError('disconnect() has not been implemented.');
  }

  Future<Map<String, dynamic>?> getDeviceStatus() {
    throw UnimplementedError('getDeviceStatus() has not been implemented.');
  }
}
