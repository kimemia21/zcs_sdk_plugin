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
