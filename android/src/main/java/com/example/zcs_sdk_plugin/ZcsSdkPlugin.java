package com.example.zcs_sdk_plugin;

import androidx.annotation.NonNull;

// Flutter imports
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

// Android imports
import android.content.Context;
import android.util.Log;
import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;
import android.widget.TextView;

// Java utilities
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// ZCS SDK Core imports
import com.zcs.sdk.DriverManager;
import com.zcs.sdk.SdkResult;
import com.zcs.sdk.SdkData;
import com.zcs.sdk.Sys;
import com.zcs.sdk.ConnectTypeEnum;

// Card Reader imports
import com.zcs.sdk.card.CardInfoEntity;
import com.zcs.sdk.card.CardReaderManager;
import com.zcs.sdk.card.CardReaderTypeEnum;
import com.zcs.sdk.card.CardSlotNoEnum;
// import com.zcs.sdk.card.ICCard;
import com.zcs.sdk.card.MagCard;
import com.zcs.sdk.card.RfCard;
import com.zcs.sdk.card.SLE4428Card;
import com.zcs.sdk.card.SLE4442Card;
import com.zcs.sdk.card.NativeNfcCard;
import com.zcs.sdk.listener.OnSearchCardListener;
import com.zcs.sdk.listener.OnNativeNfcDetectedListener;

// EMV Transaction imports
import com.zcs.sdk.emv.EmvApp;
import com.zcs.sdk.emv.EmvCapk;
import com.zcs.sdk.emv.EmvData;
import com.zcs.sdk.emv.EmvHandler;
import com.zcs.sdk.emv.EmvResult;
import com.zcs.sdk.emv.EmvTermParam;
import com.zcs.sdk.emv.EmvTransParam;
import com.zcs.sdk.emv.OnEmvListener;

// PIN Pad imports
import com.zcs.sdk.pin.PinAlgorithmMode;
import com.zcs.sdk.pin.MagEncryptTypeEnum;
import com.zcs.sdk.pin.PinMacTypeEnum;
import com.zcs.sdk.pin.PinWorkKeyTypeEnum;
import com.zcs.sdk.pin.pinpad.PinPadManager;

// Printer imports
import com.zcs.sdk.Printer;
import com.zcs.sdk.print.PrnStrFormat;
import com.zcs.sdk.print.PrnTextFont;
import com.zcs.sdk.print.PrnTextStyle;

// Hardware Control imports
import com.zcs.sdk.Beeper;
import com.zcs.sdk.Led;
import com.zcs.sdk.LedLightModeEnum;
import com.zcs.sdk.HQrsanner;
import android.os.SystemClock;

import android.view.KeyEvent;
import android.widget.EditText;

// External Port imports
import com.zcs.sdk.exteranl.ExternalCardManager;
// import com.zcs.sdk.exteranl.ICCard;

// Bluetooth imports
import com.zcs.sdk.bluetooth.BluetoothListener;
import com.zcs.sdk.bluetooth.BluetoothManager;
import com.zcs.sdk.bluetooth.emv.CardDetectedEnum;
import com.zcs.sdk.bluetooth.emv.EmvStatusEnum;
import com.zcs.sdk.bluetooth.emv.OnBluetoothEmvListener;

// Utility imports
import com.zcs.sdk.util.StringUtils;
import com.zcs.sdk.util.LogUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import com.google.zxing.BarcodeFormat;
import java.io.InputStream;
import java.util.List;

/**
 * ZCSPlugin - Flutter plugin for ZCS ZCS SDK integration
 * 
 * This plugin provides a bridge between Flutter and the native ZCS SDK
 * allowing Flutter apps to interact with POS hardware features like:
 * - Card reading (magnetic, IC, contactless)
 * - EMV transaction processing
 * - Receipt printing
 * - PIN pad operations
 * - Device management
 * - QR Code scanning
 */
public class ZcsSdkPlugin implements FlutterPlugin, MethodCallHandler {
    
    // Channel name for communication between Flutter and Android
    private static final String CHANNEL_NAME = "zcs_sdk_plugin";
    private static final String TAG = "ZCSPLUGIN";
    
    // Flutter method channel for communication
    private MethodChannel channel;
    private Context context;
    private HQrsanner mHQrsanner;
    
    // Background thread executor for SDK operations
    private ExecutorService executor;
    private Handler mainHandler;
    
    // SDK instance variables
    private DriverManager mDriverManager;
    private Printer mPrinter;
    private boolean isSupportCutter = false;
    
    // Device state tracking
    private boolean isDeviceInitialized = false;
    private boolean isDeviceOpened = false;
    private boolean isScannerActive = false;

private String lastScannedData = "";
private boolean isWaitingForScan = false;
private Result pendingScanResult = null;
private EditText scanResultEditText;


    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), CHANNEL_NAME);
        channel.setMethodCallHandler(this);
        context = flutterPluginBinding.getApplicationContext();
        
        // Initialize background executor and main handler
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        
        Log.d(TAG, "ZCS Plugin attached to engine");
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
        
        // Cleanup resources
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
        
        // Stop scanner if active
        if (isScannerActive && mHQrsanner != null) {
            try {
                mHQrsanner.QRScanerPowerCtrl((byte) 0);
                isScannerActive = false;
            } catch (Exception e) {
                Log.w(TAG, "Failed to stop scanner during cleanup", e);
            }
        }
        
        Log.d(TAG, "ZCS Plugin detached from engine");
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        switch (call.method) {
            case "initializeDevice":
                initializeDevice(result);
                break;
            case "openDevice":
                openDevice(result);
                break;
            case "closeDevice":
                closeDevice(result);
                break;
            case "getDeviceInfo":
                getDeviceInfo(result);
                break;
            case "getDeviceStatus":
                getDeviceStatus(result);
                break;
            case "printText":
                String text = call.argument("text");
                printText(text, result);
                break;
            case "printReceipt":
                Map<String, Object> receiptData = call.argument("receiptData");
                printReceipt(receiptData, result);
                break;
            case "printQRCode":
                String qrData = call.argument("data");
                Integer qrSize = call.argument("size");
                printQRCode(qrData, qrSize != null ? qrSize : 200, result);
                break;
            case "printBarcode":
                String barcodeData = call.argument("data");
                printBarcode(barcodeData, result);
                break;
            case "cutPaper":
                cutPaper(result);
                break;
            case "getPrinterStatus":
                getPrinterStatus(result);
                break;
             
        case "stopQRScan":
            stopQRScan(result);
            break;
        case "scanQRCode":
            scanQRCodeOnce(result);
            break;
        // case "getLastScannedData":
        //     getLastScannedData(result);
        //     break;

            default:
                result.notImplemented();
                break;
        }
    }

    private void initializeDevice(Result result) {
        executor.execute(() -> {
            try {
                Log.d(TAG, "Initializing ZCS ZCS SDK...");
                
                // Initialize the ZCS SDK
                mDriverManager = DriverManager.getInstance();
                if (mDriverManager == null) {
                    throw new Exception("Failed to get DriverManager instance");
                }
                
                // Get printer instance
                mPrinter = mDriverManager.getPrinter();
                if (mPrinter == null) {
                    throw new Exception("Failed to get Printer instance");
                }
                
                // Check if device supports paper cutter
                isSupportCutter = mPrinter.isSuppoerCutter();

                // Initialize QR scanner
                mHQrsanner = mDriverManager.getHQrsannerDriver();
                if (mHQrsanner == null) {

                    Log.w(TAG, "QR Scanner not available on this device");
                    // Don't throw exception, just log warning as some devices may not have scanner
                }
           
                isDeviceInitialized = true;
                
                // Return result on main thread
                mainHandler.post(() -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "ZCS SDK initialized successfully");
                    response.put("supportsCutter", isSupportCutter);
                    response.put("hasQRScanner", mHQrsanner != null);
                    result.success(response);
                });
                
                Log.d(TAG, "SDK initialization completed successfully");
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize SDK", e);
                mainHandler.post(() -> {
                    result.error("INIT_ERROR", "Failed to initialize SDK: " + e.getMessage(), null);
                });
            }
        });
    }

    private void openDevice(Result result) {
        if (!isDeviceInitialized) {
            result.error("DEVICE_NOT_INITIALIZED", "Device must be initialized first", null);
            return;
        }
        
        executor.execute(() -> {
            try {
                Log.d(TAG, "Opening printer device...");
                
                // Check printer status first
                int status = mPrinter.getPrinterStatus();

                if (status == SdkResult.SDK_OK) {
                    isDeviceOpened = true;
                    
                    mainHandler.post(() -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("success", true);
                        response.put("message", "Printer opened successfully");
                        response.put("status", "ready");
                        result.success(response);
                    });
                } else {
                    String statusMessage = getPrinterStatusMessage(status);
                    throw new Exception("Printer not ready: " + statusMessage);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to open device", e);
                mainHandler.post(() -> {
                    result.error("OPEN_ERROR", "Failed to open device: " + e.getMessage(), null);
                });
            }
        });
    }

    private void closeDevice(Result result) {
        executor.execute(() -> {
            try {
                Log.d(TAG, "Closing printer device...");
                
                // Stop scanner if active
                if (isScannerActive && mHQrsanner != null) {
                    try {
                        mHQrsanner.QRScanerPowerCtrl((byte) 0);
                        isScannerActive = false;
                        Log.d(TAG, "QR Scanner stopped during device close");
                    } catch (Exception e) {
                        Log.w(TAG, "Failed to stop scanner during close", e);
                    }
                }
                
                // ZCS SDK doesn't require explicit close for printer
                // Just update the state
                isDeviceOpened = false;
                
                mainHandler.post(() -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "Device closed successfully");
                    result.success(response);
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to close device", e);
                mainHandler.post(() -> {
                    result.error("CLOSE_ERROR", "Failed to close device: " + e.getMessage(), null);
                });
            }
        });
    }

    private void getDeviceInfo(Result result) {
        executor.execute(() -> {
            try {
                Log.d(TAG, "Getting device information...");
                
                if (!isDeviceInitialized) {
                    throw new Exception("Device not initialized");
                }
                
                Map<String, Object> deviceInfo = new HashMap<>();
                deviceInfo.put("model", "ZCS ZCS");
                deviceInfo.put("serialNumber", "ZCS_" + System.currentTimeMillis());
                deviceInfo.put("sdkVersion", "1.8.1+");
                deviceInfo.put("supportsCutter", isSupportCutter);
                deviceInfo.put("hasQRScanner", mHQrsanner != null);
                deviceInfo.put("printerStatus", getPrinterStatusMessage(mPrinter.getPrinterStatus()));
                deviceInfo.put("is80MMPrinter", mPrinter.is80MMPrinter());
                
                mainHandler.post(() -> result.success(deviceInfo));
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to get device info", e);
                mainHandler.post(() -> {
                    result.error("INFO_ERROR", "Failed to get device info: " + e.getMessage(), null);
                });
            }
        });
    }

    private void getDeviceStatus(Result result) {
        Map<String, Object> status = new HashMap<>();
        status.put("initialized", isDeviceInitialized);
        status.put("opened", isDeviceOpened);
        status.put("ready", isDeviceInitialized && isDeviceOpened);
        status.put("supportsCutter", isSupportCutter);
        status.put("hasQRScanner", mHQrsanner != null);
        status.put("scannerActive", isScannerActive);
        result.success(status);
    }

    private void printText(String text, Result result) {
        if (!checkDeviceReady(result)) return;
        
        if (text == null || text.trim().isEmpty()) {
            result.error("INVALID_INPUT", "Text cannot be null or empty", null);
            return;
        }
        
        executor.execute(() -> {
            try {
                int printStatus = mPrinter.getPrinterStatus();
                if (printStatus == SdkResult.SDK_PRN_STATUS_PAPEROUT) {
                    throw new Exception("Out of paper");
                }
                
                PrnStrFormat format = new PrnStrFormat();
                format.setTextSize(40);
                format.setStyle(PrnTextStyle.NORMAL);
                format.setFont(PrnTextFont.MONOSPACE);
                format.setAli(Layout.Alignment.ALIGN_NORMAL);
                
                mPrinter.setPrintAppendString(text, format);
                mPrinter.setPrintAppendString("\n", format);
                
                int result_code = mPrinter.setPrintStart();
                
                mainHandler.post(() -> {
                    if (result_code == SdkResult.SDK_OK) {
                        Map<String, Object> response = new HashMap<>();
                        response.put("success", true);
                        response.put("message", "Text printed successfully");
                        result.success(response);
                    } else {
                        result.error("PRINT_ERROR", "Print failed with code: " + result_code, null);
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to print text", e);
                mainHandler.post(() -> {
                    result.error("PRINT_ERROR", "Failed to print text: " + e.getMessage(), null);
                });
            }
        });
    }

    private void printReceipt(Map<String, Object> receiptData, Result result) {
        if (!checkDeviceReady(result)) return;
        
        if (receiptData == null) {
            result.error("INVALID_INPUT", "Receipt data cannot be null", null);
            return;
        }
        
        executor.execute(() -> {
            try {
                int printStatus = mPrinter.getPrinterStatus();
                if (printStatus == SdkResult.SDK_PRN_STATUS_PAPEROUT) {
                    throw new Exception("Out of paper");
                }
                
                // Create format objects
                PrnStrFormat headerFormat = new PrnStrFormat();
                headerFormat.setTextSize(50);
                headerFormat.setAli(Layout.Alignment.ALIGN_CENTER);
                headerFormat.setStyle(PrnTextStyle.BOLD);
                headerFormat.setFont(PrnTextFont.SANS_SERIF);
                
                PrnStrFormat subHeaderFormat = new PrnStrFormat();
                subHeaderFormat.setTextSize(30 );
                subHeaderFormat.setAli(Layout.Alignment.ALIGN_CENTER);
                subHeaderFormat.setStyle(PrnTextStyle.BOLD);
                subHeaderFormat.setFont(PrnTextFont.SANS_SERIF);
                
                PrnStrFormat normalFormat = new PrnStrFormat();
                normalFormat.setTextSize(22);
                normalFormat.setStyle(PrnTextStyle.NORMAL);
                normalFormat.setFont(PrnTextFont.MONOSPACE);
                normalFormat.setAli(Layout.Alignment.ALIGN_NORMAL);
                
                PrnStrFormat boldFormat = new PrnStrFormat();
                boldFormat.setTextSize(26);
                boldFormat.setStyle(PrnTextStyle.BOLD);
                boldFormat.setFont(PrnTextFont.MONOSPACE);
                boldFormat.setAli(Layout.Alignment.ALIGN_NORMAL);
                
                PrnStrFormat orderNumberFormat = new PrnStrFormat();
                orderNumberFormat.setTextSize(40);
                orderNumberFormat.setAli(Layout.Alignment.ALIGN_CENTER);
                orderNumberFormat.setStyle(PrnTextStyle.BOLD);
                orderNumberFormat.setFont(PrnTextFont.SANS_SERIF);
                
                PrnStrFormat smallFormat = new PrnStrFormat();
                smallFormat.setTextSize(20);
                smallFormat.setStyle(PrnTextStyle.NORMAL);
                smallFormat.setFont(PrnTextFont.MONOSPACE);
                smallFormat.setAli(Layout.Alignment.ALIGN_NORMAL);
                
                PrnStrFormat mediaFormat = new PrnStrFormat();
                mediaFormat.setTextSize(25);
                mediaFormat.setStyle(PrnTextStyle.NORMAL);
                mediaFormat.setFont(PrnTextFont.MONOSPACE);
                mediaFormat.setAli(Layout.Alignment.ALIGN_NORMAL);
                
                // Print store name
                String storeName = (String) receiptData.get("storeName");
                String receiptType = (String) receiptData.get("receiptType");
                if (storeName != null && !storeName.trim().isEmpty()) {
                    mPrinter.setPrintAppendString(storeName, headerFormat);
                } else {
                    mPrinter.setPrintAppendString("Blankets And Wine", headerFormat);
                }
                
                // Print receipt title
                mPrinter.setPrintAppendString(receiptType, subHeaderFormat);
                mPrinter.setPrintAppendString("", normalFormat); // Empty line
                                                                                                                
                // Print date and time
                String date = (String) receiptData.get("date");
                String time = (String) receiptData.get("time");
                if (date != null && !date.trim().isEmpty()) {
                    mPrinter.setPrintAppendString("Date: " + date, normalFormat);
                }
                if (time != null && !time.trim().isEmpty()) {
                    mPrinter.setPrintAppendString("Time: " + time, normalFormat);
                }
                
                // Print separator
                String separator = "--------------------------------";
                mPrinter.setPrintAppendString(separator, normalFormat);
                
                // Print column headers
                mPrinter.setPrintAppendString("ITEM            QTY    AMOUNT", boldFormat);
                mPrinter.setPrintAppendString(separator, normalFormat);
                
                // Print items
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> items = (List<Map<String, Object>>) receiptData.get("items");
                if (items != null) {
                    for (Map<String, Object> item : items) {
                        String itemName = (String) item.get("name");
                        String quantity = String.valueOf(item.get("quantity"));
                        String price = String.valueOf(item.get("price"));
                        
                        // Handle null values and truncate long names
                        if (itemName == null || itemName.trim().isEmpty()) itemName = "Unknown Item";
                        if (itemName.length() > 15) {
                            itemName = itemName.substring(0, 12) + "...";
                        }
                        
                        // Format line
                        String itemLine = String.format("%-15s %3sx %9s", 
                            itemName, quantity, "Kshs " + price);
                        mPrinter.setPrintAppendString(itemLine, mediaFormat);
                    }
                }
                
                mPrinter.setPrintAppendString(separator, normalFormat);
                
                // Print financial summary
                String subtotal = (String) receiptData.get("subtotal");
                String tax = (String) receiptData.get("tax");
                String total = (String) receiptData.get("total");
                
                if (subtotal != null && !subtotal.trim().isEmpty()) {
                    String subtotalLine = String.format("%-20s %10s", "Subtotal:", "Kshs " + subtotal);
                    mPrinter.setPrintAppendString(subtotalLine, normalFormat);
                }
                
                if (tax != null && !tax.trim().isEmpty()) {
                    String taxLine = String.format("%-20s %10s", "Tax:", "Kshs " + tax);
                    mPrinter.setPrintAppendString(taxLine, normalFormat);
                }
                
                if (total != null && !total.trim().isEmpty()) {
                    String doubleSeparator = "================================";
                    mPrinter.setPrintAppendString(doubleSeparator, normalFormat);
                    
                    String totalLine = String.format("%-20s %10s", "TOTAL:", "Kshs " + total);
                    mPrinter.setPrintAppendString(totalLine, boldFormat);
                    
                    mPrinter.setPrintAppendString(doubleSeparator, normalFormat);
                }
                
                // Add processing delay
                Thread.sleep(100);
                
                // Print payment method
                String paymentMethod = (String) receiptData.get("paymentMethod");
                if (paymentMethod != null && !paymentMethod.trim().isEmpty()) {
                    mPrinter.setPrintAppendString("", smallFormat);
                    String paymentLine = String.format("Payment Method: %s", paymentMethod);
                    mPrinter.setPrintAppendString(paymentLine, smallFormat);
                }
                
                // Footer messages
                mPrinter.setPrintAppendString("", smallFormat);
                mPrinter.setPrintAppendString("Thank you for your visit!", smallFormat);
                mPrinter.setPrintAppendString("Enjoy responsibly!", smallFormat);
                
                // Spacing before QR code
                mPrinter.setPrintAppendString("", smallFormat);
                mPrinter.setPrintAppendString("", smallFormat);
                
                // Generate order number
                String orderNumber = (String) receiptData.get("orderNumber");
                if (orderNumber == null || orderNumber.trim().isEmpty()) {
                    orderNumber = "ORD-" + String.format("%04d", (int)(Math.random() * 9999) + 1);
                }
                
                final String finalOrderNumber = orderNumber;
                
                // Print QR Code
                Object qrSizeObj = receiptData.get("qrSize");
                int qrSize = 200; // default size
                if (qrSizeObj != null) {
                    if (qrSizeObj instanceof Integer) {
                        qrSize = (Integer) qrSizeObj;
                    } else if (qrSizeObj instanceof String) {
                        try {
                            qrSize = Integer.parseInt((String) qrSizeObj);
                        } catch (NumberFormatException e) {
                            Log.w(TAG, "Invalid QR size, using default: " + qrSizeObj);
                        }
                    }
                }
                
                // Add QR code to receipt
                mPrinter.setPrintAppendString("Scan QR Code:", normalFormat);
                mPrinter.setPrintAppendString("", smallFormat);
                
                mPrinter.setPrintAppendQRCode(finalOrderNumber, qrSize, qrSize, Layout.Alignment.ALIGN_CENTER);
                
                // Spacing before order number
                mPrinter.setPrintAppendString("", smallFormat);
                mPrinter.setPrintAppendString("", smallFormat);
                
                // Print order number section
                String doubleSeparator = "================================";
                mPrinter.setPrintAppendString(doubleSeparator, smallFormat);
                mPrinter.setPrintAppendString("ORDER NUMBER", subHeaderFormat);
                mPrinter.setPrintAppendString(finalOrderNumber, orderNumberFormat);
                mPrinter.setPrintAppendString(doubleSeparator, smallFormat);
                
                // Extra spacing for easy tearing
                mPrinter.setPrintAppendString("", smallFormat);
                mPrinter.setPrintAppendString("", smallFormat);
                mPrinter.setPrintAppendString("", smallFormat);
                mPrinter.setPrintAppendString("", smallFormat);
                
                // Add line feeds for complete printing
                try {
                    mPrinter.setPrintAppendString("\n", smallFormat);
                    mPrinter.setPrintAppendString("\n", smallFormat);
                } catch (Exception e) {
                    Log.w(TAG, "Line feeds not supported", e);
                }
                
                // Start printing
                int result_code = mPrinter.setPrintStart();
                
                // Add delay to ensure printing completes
                Thread.sleep(2000);
                
                // Check final printer status
                int finalStatus = mPrinter.getPrinterStatus();
                Log.d(TAG, "Final printer status: " + finalStatus);
                
                mainHandler.post(() -> {
                    if (result_code == SdkResult.SDK_OK) {
                        Map<String, Object> response = new HashMap<>();
                        response.put("success", true);
                        response.put("message", "Receipt printed successfully");
                        response.put("orderNumber", finalOrderNumber);
                        result.success(response);
                    } else {
                        result.error("PRINT_ERROR", "Print failed with code: " + result_code, null);
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to print receipt", e);
                mainHandler.post(() -> {
                    result.error("PRINT_ERROR", "Failed to print receipt: " + e.getMessage(), null);
                });
            }
        });
    }

    private void printQRCode(String data, int size, Result result) {
        if (!checkDeviceReady(result)) return;
        
        if (data == null || data.trim().isEmpty()) {
            result.error("INVALID_INPUT", "QR code data cannot be null or empty", null);
            return;
        }
        
        executor.execute(() -> {
            try {
                int printStatus = mPrinter.getPrinterStatus();
                if (printStatus == SdkResult.SDK_PRN_STATUS_PAPEROUT) {
                    throw new Exception("Out of paper");
                }
                
                // Validate QR size
                int validSize = Math.max(100, Math.min(size, 600)); // Clamp between 100-600
                
                mPrinter.setPrintAppendQRCode(data, validSize, validSize, Layout.Alignment.ALIGN_CENTER);
                int result_code = mPrinter.setPrintStart();
                
                mainHandler.post(() -> {
                    if (result_code == SdkResult.SDK_OK) {
                        Map<String, Object> response = new HashMap<>();
                        response.put("success", true);
                        response.put("message", "QR Code printed successfully");
                        response.put("size", validSize);
                        result.success(response);
                    } else {
                        result.error("PRINT_ERROR", "Print failed with code: " + result_code, null);
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to print QR code", e);
                mainHandler.post(() -> {
                    result.error("PRINT_ERROR", "Failed to print QR code: " + e.getMessage(), null);
                });
            }
        });
    }
  private void printBarcode(String data, Result result) {
        if (!checkDeviceReady(result)) return;
        
        executor.execute(() -> {
            try {
                int printStatus = mPrinter.getPrinterStatus();
                if (printStatus == SdkResult.SDK_PRN_STATUS_PAPEROUT) {
                    throw new Exception("Out of paper");
                }
                
                mPrinter.setPrintAppendBarCode(context, data, 360, 100, true, 
                    Layout.Alignment.ALIGN_CENTER, BarcodeFormat.CODE_128);
                int result_code = mPrinter.setPrintStart();
                
                mainHandler.post(() -> {
                    if (result_code == SdkResult.SDK_OK) {
                        Map<String, Object> response = new HashMap<>();
                        response.put("success", true);
                        response.put("message", "Barcode printed successfully");
                        result.success(response);
                    } else {
                        result.error("PRINT_ERROR", "Print failed with code: " + result_code, null);
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to print barcode", e);
                mainHandler.post(() -> {
                    result.error("PRINT_ERROR", "Failed to print barcode: " + e.getMessage(), null);
                });
            }
        });
    }

    private void cutPaper(Result result) {
        if (!checkDeviceReady(result)) return;
        
        if (!isSupportCutter) {
            result.error("NOT_SUPPORTED", "Paper cutter not supported on this device", null);
            return;
        }
        
        executor.execute(() -> {
            try {
                int printStatus = mPrinter.getPrinterStatus();
                if (printStatus == SdkResult.SDK_OK) {
                    mPrinter.openPrnCutter((byte) 1);
                    
                    mainHandler.post(() -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("success", true);
                        response.put("message", "Paper cut successfully");
                        result.success(response);
                    });
                } else {
                    throw new Exception("Printer not ready for cutting");
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to cut paper", e);
                mainHandler.post(() -> {
                    result.error("CUT_ERROR", "Failed to cut paper: " + e.getMessage(), null);
                });
            }
        });
    }

    private void getPrinterStatus(Result result) {
        if (!isDeviceInitialized) {
            result.error("DEVICE_NOT_INITIALIZED", "Device must be initialized first", null);
            return;
        }
        
        executor.execute(() -> {
            try {
                int status = mPrinter.getPrinterStatus();
                String statusMessage = getPrinterStatusMessage(status);
                
                mainHandler.post(() -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("statusCode", status);
                    response.put("statusMessage", statusMessage);
                    response.put("isReady", status == SdkResult.SDK_OK);
                    response.put("isPaperOut", status == SdkResult.SDK_PRN_STATUS_PAPEROUT);
                    result.success(response);
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to get printer status", e);
                mainHandler.post(() -> {
                    result.error("STATUS_ERROR", "Failed to get printer status: " + e.getMessage(), null);
                });
            }
        });
    }


    private boolean checkDeviceReady(Result result) {
        if (!isDeviceInitialized) {
            result.error("DEVICE_NOT_INITIALIZED", "Device must be initialized first", null);
            return false;
        }
        if (!isDeviceOpened) {
            result.error("DEVICE_NOT_OPENED", "Device must be opened first", null);
            return false;
        }
        return true;
    }
    

    private String getPrinterStatusMessage(int status) {
        switch (status) {
            case SdkResult.SDK_OK:
                return "Ready";
            case SdkResult.SDK_PRN_STATUS_PAPEROUT:
                return "Out of paper";
            default:
                return "Status code: " + status;
        }
    }

 
private void stopQRScan(Result result) {
    if (!checkDeviceReady(result)) return;
    
    executor.execute(() -> {
        try {
            // Power off scanner
            mHQrsanner.QRScanerPowerCtrl((byte)0);
            mHQrsanner.QRScanerCtrl((byte)0);
            
            mainHandler.post(() -> {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "QR scanner powered off");
                response.put("data", "");
                result.success(response);
            });
        } catch (Exception e) {
            Log.e(TAG, "Failed to stop QR scanner", e);
            mainHandler.post(() -> {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Failed to stop QR scanner: " + e.getMessage());
                response.put("data", "");
                result.success(response);
            });
        }
    });
}

private void scanQRCodeOnce(Result result) {
    if (!checkDeviceReady(result)) return;
    
    if (mHQrsanner == null) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "QR Scanner not available on this device");
        response.put("data", "");
        result.success(response);
        return;
    }
    
    // Prevent multiple simultaneous scans
    if (isWaitingForScan) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Scanner is already in use");
        response.put("data", "");
        result.success(response);
        return;
    }
    
    executor.execute(() -> {
        try {
            Log.d(TAG, "Starting QR scan...");
            
            // Store the result callback
            pendingScanResult = result;
            isWaitingForScan = true;
            
            // Create EditText to capture scan results
            mainHandler.post(() -> {
                scanResultEditText = new EditText(context);
                scanResultEditText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
                    if (isWaitingForScan) {
                        String scannedData = textView.getText().toString().trim();
                        if (!scannedData.isEmpty()) {
                            handleScanResult(scannedData);
                        }
                    }
                    return false;
                });
                
                startScanningProcess();
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to start QR scan", e);
            cleanupScan();
            mainHandler.post(() -> {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Failed to start QR scan: " + e.getMessage());
                response.put("data", "");
                result.success(response);
            });
        }
    });
}

private void startScanningProcess() {
    try {
        // Power on and activate scanner
        mHQrsanner.QRScanerCtrl((byte)1);
        mHQrsanner.QRScanerPowerCtrl((byte)0);
        SystemClock.sleep(10);
        mHQrsanner.QRScanerPowerCtrl((byte)1);
        SystemClock.sleep(100);
        
        // Request focus to capture scan input
        if (scanResultEditText != null) {
            scanResultEditText.requestFocus();
        }
        
        // Set timeout for scan operation
        mainHandler.postDelayed(() -> {
            if (isWaitingForScan) {
                handleScanTimeout();
            }
        }, 10000);
        
        Log.d(TAG, "QR scanner activated, waiting for scan...");
        
    } catch (Exception e) {
        Log.e(TAG, "Failed to start scanning process", e);
        handleScanError("Failed to activate scanner: " + e.getMessage());
    }
}

private void handleScanResult(String scannedData) {
    if (!isWaitingForScan || pendingScanResult == null) {
        return;
    }
    
    try {
        Log.d(TAG, "QR scan result received: " + scannedData);
        
        // Immediately close scanner after successful scan
        closeScanner();
        
        // Return result to Flutter
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "QR code scanned successfully");
        response.put("data", scannedData);
        
        pendingScanResult.success(response);
        cleanupScan();
        
    } catch (Exception e) {
        Log.e(TAG, "Error handling scan result", e);
        handleScanError("Error processing scan result: " + e.getMessage());
    }
}

private void handleScanTimeout() {
    Log.d(TAG, "QR scan timeout");
    closeScanner();
    
    if (pendingScanResult != null) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Scan timeout - no QR code detected");
        response.put("data", "");
        
        pendingScanResult.success(response);
    }
    cleanupScan();
}

private void handleScanError(String errorMessage) {
    Log.e(TAG, "QR scan error: " + errorMessage);
    closeScanner();
    
    if (pendingScanResult != null) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", errorMessage);
        response.put("data", "");
        
        pendingScanResult.success(response);
    }
    cleanupScan();
}

private void closeScanner() {
    try {
        if (mHQrsanner != null) {
            mHQrsanner.QRScanerCtrl((byte)0);
            mHQrsanner.QRScanerPowerCtrl((byte)0);
        }
        Log.d(TAG, "Scanner closed successfully");
    } catch (Exception e) {
        Log.e(TAG, "Error closing scanner", e);
    }
}

private void cleanupScan() {
    isWaitingForScan = false;
    pendingScanResult = null;
    scanResultEditText = null;
}
// // Add this method to handle successful scan results
// private void handleScanResult(String scannedData) {
//     if (!isWaitingForScan || pendingScanResult == null) {
//         return;
//     }
    
//     try {
//         Log.d(TAG, "QR scan result received: " + scannedData);
        
//         // Stop the scanner
//         mHQrsanner.QRScanerCtrl((byte)0);
//         mHQrsanner.QRScanerPowerCtrl((byte)0);
        
//         // Clean up
//         isWaitingForScan = false;
//         lastScannedData = scannedData;
        
//         // Return result to Flutter
//         Map<String, Object> response = new HashMap<>();
//         response.put("success", true);
//         response.put("message", "QR code scanned successfully");
//         response.put("data", scannedData);
        
//         pendingScanResult.success(response);
//         pendingScanResult = null;
        
//     } catch (Exception e) {
//         Log.e(TAG, "Error handling scan result", e);
//         handleScanError("Error processing scan result: " + e.getMessage());
//     }
// }

// Add this method to handle scan timeouts
// private void handleScanTimeout() {
//     if (!isWaitingForScan || pendingScanResult == null) {
//         return;
//     }
    
//     try {
//         Log.w(TAG, "QR scan timeout");
        
//         // Stop the scanner
//         mHQrsanner.QRScanerCtrl((byte)0);
//         mHQrsanner.QRScanerPowerCtrl((byte)0);
        
//         // Clean up
//         isWaitingForScan = false;
        
//         // Return timeout result
//         Map<String, Object> response = new HashMap<>();
//         response.put("success", false);
//         response.put("message", "Scan timeout - no QR code detected");
//         response.put("data", "");
        
//         pendingScanResult.success(response);
//         pendingScanResult = null;
        
//     } catch (Exception e) {
//         Log.e(TAG, "Error handling scan timeout", e);
//         handleScanError("Scan timeout error: " + e.getMessage());
//     }
// }

// // Add this method to handle scan errors
// private void handleScanError(String errorMessage) {
//     if (!isWaitingForScan || pendingScanResult == null) {
//         return;
//     }
    
//     try {
//         // Stop the scanner
//         if (mHQrsanner != null) {
//             mHQrsanner.QRScanerCtrl((byte)0);
//             mHQrsanner.QRScanerPowerCtrl((byte)0);
//         }
//     } catch (Exception e) {
//         Log.w(TAG, "Failed to stop scanner during error handling", e);
//     }
    
//     // Clean up
//     isWaitingForScan = false;
    
//     // Return error result
//     pendingScanResult.error("SCANNER_ERROR", errorMessage, null);
//     pendingScanResult = null;
// }

// // Add this method to get the last scanned data
// private void getLastScannedData(Result result) {
//     Map<String, Object> response = new HashMap<>();
//     response.put("success", true);
//     response.put("data", lastScannedData);
//     response.put("message", lastScannedData.isEmpty() ? "No data scanned yet" : "Last scanned data retrieved");
//     result.success(response);
// }


}

