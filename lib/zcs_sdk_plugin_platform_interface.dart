import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'zcs_sdk_plugin_method_channel.dart';

abstract class ZcsSdkPluginPlatform extends PlatformInterface {
  /// Constructs a ZcsSdkPluginPlatform.
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
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
