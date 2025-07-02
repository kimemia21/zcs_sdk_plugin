library zcs_sdk_plugin;

import 'zcs_sdk_plugin_platform_interface.dart';

class ZcsSdkPlugin {
  Future<String?> getPlatformVersion() {
    return ZcsSdkPluginPlatform.instance.getPlatformVersion();
  }

  Future<bool> initializeDevice() {
    return ZcsSdkPluginPlatform.instance.initializeDevice();
  }

  Future<String?> connectToDevice(String deviceId) {
    return ZcsSdkPluginPlatform.instance.connectToDevice(deviceId);
  }

  Future<List<String>> scanForDevices() {
    return ZcsSdkPluginPlatform.instance.scanForDevices();
  }

  Future<bool> sendCommand(String command) {
    return ZcsSdkPluginPlatform.instance.sendCommand(command);
  }

  Future<String?> getDeviceInfo() {
    return ZcsSdkPluginPlatform.instance.getDeviceInfo();
  }

  Future<bool> disconnect() {
    return ZcsSdkPluginPlatform.instance.disconnect();
  }

  Future<Map<String, dynamic>?> getDeviceStatus() {
    return ZcsSdkPluginPlatform.instance.getDeviceStatus();
  }
}