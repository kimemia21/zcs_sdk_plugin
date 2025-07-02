import 'package:flutter_test/flutter_test.dart';
import 'package:zcs_sdk_plugin/zcs_sdk_plugin.dart';
import 'package:zcs_sdk_plugin/zcs_sdk_plugin_platform_interface.dart';
import 'package:zcs_sdk_plugin/zcs_sdk_plugin_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockZcsSdkPluginPlatform
    with MockPlatformInterfaceMixin
    implements ZcsSdkPluginPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
  
  @override
  Future<Map<String, dynamic>> closeDevice() {
    // TODO: implement closeDevice
    throw UnimplementedError();
  }
  
  @override
  Future<String?> connectToDevice(String deviceId) {
    // TODO: implement connectToDevice
    throw UnimplementedError();
  }
  
  @override
  Future<bool> disconnect() {
    // TODO: implement disconnect
    throw UnimplementedError();
  }
  
  @override
  Future<String?> getDeviceInfo() {
    // TODO: implement getDeviceInfo
    throw UnimplementedError();
  }
  
  @override
  Future<Map<String, dynamic>?> getDeviceStatus() {
    // TODO: implement getDeviceStatus
    throw UnimplementedError();
  }
  
  @override
  Future<bool> initializeDevice() {
    // TODO: implement initializeDevice
    throw UnimplementedError();
  }
  
  @override
  Future<Map<String, dynamic>> openDevice() {
    // TODO: implement openDevice
    throw UnimplementedError();
  }
  
  @override
  Future<Map<String, dynamic>> printImage(String imageData) {
    // TODO: implement printImage
    throw UnimplementedError();
  }
  
  @override
  Future<Map<String, dynamic>> printQrCode(String data, {int size = 200, String errorCorrectionLevel = "L"}) {
    // TODO: implement printQrCode
    throw UnimplementedError();
  }
  
  @override
  Future<Map<String, dynamic>> printReceipt(Map<String, dynamic> receiptData) {
    // TODO: implement printReceipt
    throw UnimplementedError();
  }
  
  @override
  Future<Map<String, dynamic>> printText(String text, {int fontSize = 50, bool isBold = false, bool isUnderline = false, String alignment = "LEFT"}) {
    // TODO: implement printText
    throw UnimplementedError();
  }
  
  @override
  Future<List<String>> scanForDevices() {
    // TODO: implement scanForDevices
    throw UnimplementedError();
  }
  
  @override
  Future<bool> sendCommand(String command) {
    // TODO: implement sendCommand
    throw UnimplementedError();
  }
}

void main() {
  final ZcsSdkPluginPlatform initialPlatform = ZcsSdkPluginPlatform.instance;

  test('$MethodChannelZcsSdkPlugin is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelZcsSdkPlugin>());
  });

  test('getPlatformVersion', () async {
    ZcsSdkPlugin zcsSdkPlugin = ZcsSdkPlugin();
    MockZcsSdkPluginPlatform fakePlatform = MockZcsSdkPluginPlatform();
    ZcsSdkPluginPlatform.instance = fakePlatform;

    expect(await zcsSdkPlugin.getPlatformVersion(), '42');
  });
}
