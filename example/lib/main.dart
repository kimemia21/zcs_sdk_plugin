import 'package:flutter/material.dart';
import 'package:zcs_sdk_plugin/zcs_sdk_plugin_platform_interface.dart';

void main() {
  runApp(const MyApp());
}

/// Change this alias to whatever wrapper class you eventually expose.
/// For now we call the platform interface directly.
final ZcsSdkPluginPlatform _plugin = ZcsSdkPluginPlatform.instance;

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'ZCS SDK Plugin Demo',
      theme: ThemeData(useMaterial3: true, colorSchemeSeed: Colors.indigo),
      home: const DemoPage(),
    );
  }
}

class DemoPage extends StatefulWidget {
  const DemoPage({super.key});

  @override
  State<DemoPage> createState() => _DemoPageState();
}

class _DemoPageState extends State<DemoPage> {
  String _log = '';
  String _platformVersion = 'Unknown';

  @override
  void initState() {
    super.initState();
    _init();
  }

  Future<void> _init() async {
    try {
      final v = await _plugin.getPlatformVersion();
      setState(() => _platformVersion = v ?? 'null');
    } catch (e) {
      setState(() => _platformVersion = 'Error: $e');
    }
  }

  Future<void> _run(Future<dynamic> Function() call, String label) async {
    setState(() => _log = '⏳ $label…');
    final sw = Stopwatch()..start();
    try {
      final result = await call();
      setState(() =>
          _log = '✅ $label → ${result ?? "void"}  (${sw.elapsed.inMilliseconds} ms)');
    } catch (e) {
      setState(() =>
          _log = '❌ $label threw $e  (${sw.elapsed.inMilliseconds} ms)');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('ZCS SDK Plugin Demo'),
        // subtitle: Text('Platform: $_platformVersion'),
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          ElevatedButton(
            onPressed: () =>
                _run(() => _plugin.initializeDevice(), 'initializeDevice'),
            child: const Text('Initialize Device'),
          ),
          ElevatedButton(
            onPressed: () => _run(() => _plugin.openDevice(), 'openDevice'),
            child: const Text('Open Device'),
          ),
          ElevatedButton(
            onPressed: () => _run(() => _plugin.printText(
                  'Hello World ${DateTime.now()}',
                  fontSize: 32,
                  isBold: true,
                  alignment: 'CENTER',
                ), 'printText'),
            child: const Text('Print Text'),
          ),
          ElevatedButton(
            onPressed: () => _run(
                () => _plugin.printQrCode('https://example.com'),
                'printQrCode'),
            child: const Text('Print QR Code'),
          ),
          ElevatedButton(
            onPressed: () =>
                _run(() => _plugin.getDeviceStatus(), 'getDeviceStatus'),
            child: const Text('Get Device Status'),
          ),
          ElevatedButton(
            onPressed: () => _run(() => _plugin.closeDevice(), 'closeDevice'),
            child: const Text('Close Device'),
          ),
          const SizedBox(height: 24),
          SelectableText(
            _log,
            style: const TextStyle(fontFamily: 'monospace'),
          ),
        ],
      ),
    );
  }
}
