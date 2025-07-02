
import 'zcs_sdk_plugin_platform_interface.dart';

class ZcsSdkPlugin {
  Future<String?> getPlatformVersion() {
    return ZcsSdkPluginPlatform.instance.getPlatformVersion();
  }
}
