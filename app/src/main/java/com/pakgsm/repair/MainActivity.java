package com.pakgsm.repair;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.Toast;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends FragmentActivity {
    private WebView webView;
    private BluetoothAdapter adapter;
    private BluetoothGatt activeGatt;
    private static final int BT_PERMISSION_REQUEST = 410;
    private final Map<String, BluetoothDevice> discovered = new LinkedHashMap<>();
    private BroadcastReceiver discoveryReceiver;
    private WebViewAssetLoader assetLoader;
    private final Set<String> connectedAddresses = new HashSet<>();
    private final Map<String, Integer> deviceBatteryLevels = new HashMap<>();
    private final Map<Integer, BluetoothProfile> profileProxies = new HashMap<>();
    private final BluetoothProfile.ServiceListener profileListener = new BluetoothProfile.ServiceListener() {
        @Override public void onServiceConnected(int profile, BluetoothProfile proxy) {
            synchronized (connectedAddresses) { profileProxies.put(profile, proxy); refreshConnectedAddressesLocked(); }
        }
        @Override public void onServiceDisconnected(int profile) {
            synchronized (connectedAddresses) { profileProxies.remove(profile); refreshConnectedAddressesLocked(); }
        }
    };

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Keep Android system bars visible and prevent WebView content from drawing behind them.
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(android.graphics.Color.rgb(6, 63, 55));
        getWindow().setNavigationBarColor(android.graphics.Color.rgb(6, 63, 55));
        if (Build.VERSION.SDK_INT >= 30) getWindow().setDecorFitsSystemWindows(true);
        BluetoothManager manager=(BluetoothManager)getSystemService(BLUETOOTH_SERVICE);
        adapter=manager==null?null:manager.getAdapter();
        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(false);
        webView.getSettings().setAllowContentAccess(false);
        webView.setWebChromeClient(new WebChromeClient());
        assetLoader = new WebViewAssetLoader.Builder().addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this)).build();
        webView.setWebViewClient(new WebViewClientCompat() {
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                WebResourceResponse updated = updatedIndexResponse(request.getUrl());
                return updated != null ? updated : assetLoader.shouldInterceptRequest(request.getUrl());
            }
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                android.net.Uri uri = android.net.Uri.parse(url);
                WebResourceResponse updated = updatedIndexResponse(uri);
                return updated != null ? updated : assetLoader.shouldInterceptRequest(uri);
            }
        });
        webView.addJavascriptInterface(new NativeBluetoothBridge(), "PGRNativeBluetooth");
        setContentView(webView);
        webView.loadUrl("https://appassets.androidplatform.net/assets/index.html");
        registerDiscoveryReceiver();
        requestBluetoothPermissionsIfNeeded();
        refreshBluetoothProfiles();
    }

    private WebResourceResponse updatedIndexResponse(android.net.Uri uri) {
        if (uri == null || !"/assets/index.html".equals(uri.getPath())) return null;
        File saved = new File(new File(getFilesDir(), "pgr-web-update"), "index.html");
        if (!saved.isFile()) return null;
        try { return new WebResourceResponse("text/html", "UTF-8", new FileInputStream(saved)); }
        catch (Exception ignored) { return null; }
    }

    private void requestBluetoothPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= 31) {
            java.util.ArrayList<String> needed=new java.util.ArrayList<>();
            if(checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED) needed.add(Manifest.permission.BLUETOOTH_CONNECT);
            if(checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN)!=PackageManager.PERMISSION_GRANTED) needed.add(Manifest.permission.BLUETOOTH_SCAN);
            if(!needed.isEmpty()) requestPermissions(needed.toArray(new String[0]), BT_PERMISSION_REQUEST);
        } else if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, BT_PERMISSION_REQUEST);
        }
    }

    private void registerDiscoveryReceiver() {
        discoveryReceiver=new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                if (intent == null) return;
                String action = intent.getAction();
                BluetoothDevice d = null;
                try { d = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE); } catch (Exception ignored) {}
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    if (d != null) try { discovered.put(d.getAddress(), d); } catch (SecurityException ignored) {}
                    return;
                }
                if (d != null && "android.bluetooth.device.action.BATTERY_LEVEL_CHANGED".equals(action)) {
                    int level = intent.getIntExtra("android.bluetooth.device.extra.BATTERY_LEVEL", -1);
                    if (level >= 0 && level <= 100) {
                        String address = safeAddressStatic(d);
                        if (!address.isEmpty()) deviceBatteryLevels.put(address, level);
                        emitNativeBatteryUpdate(d, level);
                    }
                    return;
                }
                if ("android.bluetooth.device.action.ACL_CONNECTED".equals(action)
                        || "android.bluetooth.device.action.ACL_DISCONNECTED".equals(action)) {
                    synchronized (connectedAddresses) { refreshConnectedAddressesLocked(); }
                }
            }
        };
        IntentFilter f = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        f.addAction("android.bluetooth.device.action.BATTERY_LEVEL_CHANGED");
        f.addAction("android.bluetooth.device.action.ACL_CONNECTED");
        f.addAction("android.bluetooth.device.action.ACL_DISCONNECTED");
        // These broadcasts originate from Android's Bluetooth stack.
        if (Build.VERSION.SDK_INT >= 33) registerReceiver(discoveryReceiver, f, Context.RECEIVER_EXPORTED);
        else registerReceiver(discoveryReceiver, f);
    }

// Newer Android builds may expose separate earbud/case battery metadata.
        // Reflection keeps this compatible with Android versions lacking these APIs.
        private int readMetadataBattery(BluetoothDevice device, String metadataField) {
            if (device == null || Build.VERSION.SDK_INT < 33) return -1;
            try {
                java.lang.reflect.Field field = BluetoothDevice.class.getField(metadataField);
                int key = field.getInt(null);
                java.lang.reflect.Method method = BluetoothDevice.class.getMethod("getMetadata", int.class);
                Object raw = method.invoke(device, key);
                if (!(raw instanceof byte[])) return -1;
                byte[] bytes = (byte[]) raw;
                if (bytes.length == 0) return -1;
                String text = new String(bytes, StandardCharsets.UTF_8).trim();
                if (text.matches("\\d{1,3}")) {
                    int value = Integer.parseInt(text);
                    if (value >= 0 && value <= 100) return value;
                }
                if (bytes.length == 1) {
                    int value = bytes[0] & 0xff;
                    if (value >= 0 && value <= 100) return value;
                }
            } catch (Exception ignored) {}
            return -1;
        }

    private void emitNativeBatteryUpdate(BluetoothDevice device, int level) {
        if (device == null || level < 0 || level > 100) return;
        JSONObject result = new JSONObject();
        try {
            result.put("address", safeAddressStatic(device));
            result.put("deviceName", device.getName() == null ? "Bluetooth device" : device.getName());
            result.put("batteryLevel", level);
            int leftBattery = readMetadataBattery(device, "METADATA_UNTETHERED_LEFT_BATTERY");
            int rightBattery = readMetadataBattery(device, "METADATA_UNTETHERED_RIGHT_BATTERY");
            if (leftBattery >= 0) result.put("leftBattery", leftBattery);
            if (rightBattery >= 0) result.put("rightBattery", rightBattery);
        } catch (Exception ignored) { return; }
        runOnUiThread(() -> {
            if (webView != null) webView.evaluateJavascript(
                "window.onNativeBluetoothBatteryUpdate && window.onNativeBluetoothBatteryUpdate(" + result.toString() + ")", null);
        });
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==BT_PERMISSION_REQUEST) {
            boolean ok=grantResults.length>0;
            for(int result:grantResults) if(result!=PackageManager.PERMISSION_GRANTED) ok=false;
            Toast.makeText(this, ok?"Bluetooth permission granted":"Bluetooth permission is needed for device discovery", Toast.LENGTH_SHORT).show();
            if(ok) refreshBluetoothProfiles();
        }
    }
    @Override protected void onDestroy() {
        try { if(adapter!=null && adapter.isDiscovering()) adapter.cancelDiscovery(); } catch(Exception ignored) {}
        try { for(BluetoothProfile proxy : profileProxies.values()) adapter.closeProfileProxy(profileProxies.entrySet().stream().filter(e -> e.getValue()==proxy).map(Map.Entry::getKey).findFirst().orElse(-1), proxy); } catch(Exception ignored) {}
        try { if(discoveryReceiver!=null) unregisterReceiver(discoveryReceiver); } catch(Exception ignored) {}
        try { if(activeGatt!=null) { activeGatt.disconnect(); activeGatt.close(); activeGatt=null; } } catch(Exception ignored) {}
        if(webView!=null) webView.destroy();
        super.onDestroy();
    }
    @Override public void onBackPressed() { if(webView!=null && webView.canGoBack()) webView.goBack(); else super.onBackPressed(); }

    private void refreshBluetoothProfiles() {
        if(adapter==null) return;
        try {
            if(Build.VERSION.SDK_INT>=31 && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED) return;
            adapter.getProfileProxy(this, profileListener, BluetoothProfile.HEADSET);
            adapter.getProfileProxy(this, profileListener, BluetoothProfile.A2DP);
            if(Build.VERSION.SDK_INT>=33) adapter.getProfileProxy(this, profileListener, BluetoothProfile.LE_AUDIO);
        } catch(Exception ignored) {}
    }
    private void refreshConnectedAddressesLocked() {
        connectedAddresses.clear();
        for(BluetoothProfile proxy : profileProxies.values()) {
            try { for(BluetoothDevice device : proxy.getConnectedDevices()) { String address=safeAddressStatic(device); if(!address.isEmpty()) connectedAddresses.add(address); } }
            catch(Exception ignored) {}
        }
    }
    private static String safeAddressStatic(BluetoothDevice device) {
        try { String address=device.getAddress(); return address==null?"":address; } catch(Exception e) { return ""; }
    }

    public class NativeBluetoothBridge {
        @JavascriptInterface public String applyWebUpdate(String html, String version) {
            try {
                if (html == null || html.length() < 50000 || html.length() > 1500000) return "Downloaded HTML size is invalid.";
                if (version == null || !version.matches("\\d+(\\.\\d+)*")) return "Update version is invalid.";
                String one = "const APP_VERSION='" + version + "';";
                String two = "const APP_VERSION=\"" + version + "\";";
                if (!html.contains(one) && !html.contains(two)) return "Downloaded HTML version does not match " + version + ".";
                File dir = new File(getFilesDir(), "pgr-web-update");
                if (!dir.exists() && !dir.mkdirs()) return "Could not create app update folder.";
                File tmp = new File(dir, "index.html.tmp"), saved = new File(dir, "index.html");
                try (FileOutputStream out = new FileOutputStream(tmp)) { out.write(html.getBytes(StandardCharsets.UTF_8)); out.flush(); }
                if (saved.exists() && !saved.delete()) { tmp.delete(); return "Could not replace previous update."; }
                if (!tmp.renameTo(saved)) { tmp.delete(); return "Could not activate downloaded update."; }
                getSharedPreferences("pgr_web_update", MODE_PRIVATE).edit().putString("version", version).apply();
                return "OK";
            } catch (Exception e) { return "Update could not be saved: " + e.getMessage(); }
        }
        @JavascriptInterface public void reloadWebApp() {
            runOnUiThread(() -> { if (webView != null) webView.loadUrl("https://appassets.androidplatform.net/assets/index.html?updated=" + System.currentTimeMillis()); });
        }

        @JavascriptInterface public void requestBluetoothPermissions() {
            runOnUiThread(() -> requestBluetoothPermissionsIfNeeded());
        }
        @JavascriptInterface public boolean hasBluetoothPermissions() {
            if (Build.VERSION.SDK_INT >= 31) {
                return checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
            }
            return Build.VERSION.SDK_INT >= 31 || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
        @JavascriptInterface public boolean isBluetoothEnabled() {
            try { return adapter != null && adapter.isEnabled(); } catch (SecurityException e) { return false; }
        }
        @JavascriptInterface public void requestBluetoothEnable() {
            runOnUiThread(() -> {
                try {
                    if (adapter == null) { Toast.makeText(MainActivity.this, "Bluetooth is not available on this phone", Toast.LENGTH_LONG).show(); return; }
                    if (!adapter.isEnabled()) startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 411);
                } catch (SecurityException e) {
                    requestBluetoothPermissionsIfNeeded();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Please turn Bluetooth on in Android settings", Toast.LENGTH_LONG).show();
                }
            });
        }
        @JavascriptInterface public String getPairedDevices() { return listDevices(); }
        @JavascriptInterface public String getConnectedBluetoothDevices() {
            try {
                JSONObject all = new JSONObject(listDevices());
                if (all.has("error")) return all.toString();
                JSONArray source = all.optJSONArray("devices");
                JSONArray connected = new JSONArray();
                if (source != null) {
                    for (int i = 0; i < source.length(); i++) {
                        JSONObject device = source.optJSONObject(i);
                        if (device != null && device.optBoolean("connected", false)) connected.put(device);
                    }
                }
                JSONObject result = new JSONObject();
                result.put("devices", connected);
                result.put("connectedOnly", true);
                result.put("message", connected.length() > 0
                    ? "Connected Bluetooth devices found."
                    : "Android did not report any currently connected Bluetooth devices.");
                return result.toString();
            } catch (Exception e) {
                JSONObject error = new JSONObject();
                try { error.put("error", "Could not read connected Bluetooth devices: " + e.getMessage()); }
                catch (Exception ignored) {}
                return error.toString();
            }
        }
        @JavascriptInterface public String startDiscovery() {
            JSONObject out=new JSONObject();
            try {
                if(adapter==null){out.put("ok",false);out.put("error","This phone does not provide Bluetooth.");return out.toString();}
                if(Build.VERSION.SDK_INT>=31 && (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN)!=PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED)) {
                    out.put("ok",false);out.put("error","Allow Nearby devices permission, then scan again.");return out.toString();
                }
                if(!adapter.isEnabled()){out.put("ok",false);out.put("error","Bluetooth is off. Turn it on, then scan again.");return out.toString();}
                if(adapter.isDiscovering()) adapter.cancelDiscovery();
                discovered.clear();
                boolean started=adapter.startDiscovery();
                out.put("ok",started);out.put("message",started?"Scanning for nearby discoverable Bluetooth devices for about 12 seconds.":"Android could not start scanning. Try Android Bluetooth settings.");
            } catch(SecurityException e){try{out.put("ok",false);out.put("error","Android blocked scanning. Allow Nearby devices permission.");}catch(Exception ignored){}}
              catch(Exception e){try{out.put("ok",false);out.put("error","Scan failed: "+e.getMessage());}catch(Exception ignored){}}
            return out.toString();
        }
        @JavascriptInterface public String connectGattDevice(String address) {
            try {
                if (address == null || address.trim().isEmpty()) return "No Bluetooth device address was supplied.";
                if (adapter == null) return "Bluetooth is not available on this phone.";
                if (Build.VERSION.SDK_INT >= 31 && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    requestBluetoothPermissions();
                    return "Allow Nearby devices permission, then select the device again.";
                }
                if (!adapter.isEnabled()) return "Bluetooth is off. Turn it on, then select the device again.";
                BluetoothDevice target = null;
                for (BluetoothDevice d : adapter.getBondedDevices()) {
                    if (address.equalsIgnoreCase(d.getAddress())) { target = d; break; }
                }
                if (target == null) target = discovered.get(address);
                if (target == null) return "This device is no longer in the Bluetooth list. Scan again.";
                final BluetoothDevice selectedTarget = target;
                if (target.getType() == BluetoothDevice.DEVICE_TYPE_CLASSIC) {
                    JSONObject result = new JSONObject();
                    result.put("deviceName", safeName(target));
                    result.put("address", safeAddress(target));
                    result.put("connected", isConnected(target));
                    int batteryLevel = readBatteryLevel(target);
                    if (batteryLevel >= 0) result.put("batteryLevel", batteryLevel);
                    result.put("message", isConnected(target)
                        ? "Android confirms this classic Bluetooth device is connected, but it does not expose a standard BLE battery service. Battery and headset controls may require the manufacturer's app/protocol."
                        : "This is a classic Bluetooth device. Android's public API cannot force-connect every headset from an app. Use Android Bluetooth settings to pair/connect it; battery data is shown only if the headset exposes it.");
                    emitNativeGattResult(result);
                    return result.getString("message");
                }
                try { if (activeGatt != null) { activeGatt.disconnect(); activeGatt.close(); } } catch (Exception ignored) {}
                activeGatt = selectedTarget.connectGatt(MainActivity.this, false, new BluetoothGattCallback() {
                    @Override public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                        if (status != BluetoothGatt.GATT_SUCCESS) {
                            JSONObject result = new JSONObject();
                            try { result.put("deviceName", safeName(selectedTarget)); result.put("connected", false); result.put("message", "BLE connection failed (status " + status + "). The headset may not support app-accessible GATT."); } catch (Exception ignored) {}
                            emitNativeGattResult(result);
                            try { gatt.close(); } catch (Exception ignored) {}
                            return;
                        }
                        if (newState == android.bluetooth.BluetoothProfile.STATE_CONNECTED) {
                            boolean started = gatt.discoverServices();
                            if (!started) {
                                JSONObject result = new JSONObject();
                                try { result.put("deviceName", safeName(selectedTarget)); result.put("connected", true); result.put("message", "Connected over BLE, but Android could not start service discovery."); } catch (Exception ignored) {}
                                emitNativeGattResult(result);
                            }
                        } else if (newState == android.bluetooth.BluetoothProfile.STATE_DISCONNECTED) {
                            JSONObject result = new JSONObject();
                            try { result.put("deviceName", safeName(selectedTarget)); result.put("connected", false); result.put("message", "BLE connection disconnected."); } catch (Exception ignored) {}
                            emitNativeGattResult(result);
                            try { gatt.close(); } catch (Exception ignored) {}
                        }
                    }
                    @Override public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                        JSONObject result = new JSONObject();
                        try { result.put("deviceName", safeName(selectedTarget)); result.put("address", safeAddress(selectedTarget)); result.put("connected", status == BluetoothGatt.GATT_SUCCESS); } catch (Exception ignored) {}
                        if (status != BluetoothGatt.GATT_SUCCESS) {
                            try { result.put("message", "BLE connected, but service discovery failed (status " + status + ")."); } catch (Exception ignored) {}
                            emitNativeGattResult(result); return;
                        }
                        JSONArray serviceRows = new JSONArray();
                        try {
                            for (BluetoothGattService service : gatt.getServices()) {
                                JSONObject serviceRow = new JSONObject();
                                JSONArray characteristicRows = new JSONArray();
                                serviceRow.put("uuid", service.getUuid().toString());
                                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                                    int properties = characteristic.getProperties();
                                    JSONObject characteristicRow = new JSONObject();
                                    characteristicRow.put("uuid", characteristic.getUuid().toString());
                                    characteristicRow.put("readable", (properties & BluetoothGattCharacteristic.PROPERTY_READ) != 0);
                                    characteristicRow.put("writable", (properties & (BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) != 0);
                                    characteristicRow.put("notify", (properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0);
                                    characteristicRow.put("indicate", (properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0);
                                    characteristicRows.put(characteristicRow);
                                }
                                serviceRow.put("characteristics", characteristicRows);
                                serviceRows.put(serviceRow);
                            }
                            result.put("services", serviceRows);
                            result.put("message", "BLE connected. Found " + serviceRows.length() + " GATT service(s); use Read value for characteristics marked Read.");
                        } catch (Exception ignored) {}
                        BluetoothGattService batteryService = gatt.getService(java.util.UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb"));
                        BluetoothGattCharacteristic batteryChar = batteryService == null ? null : batteryService.getCharacteristic(java.util.UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb"));
                        emitNativeGattResult(result);
                        if (batteryChar != null) {
                            boolean started = gatt.readCharacteristic(batteryChar);
                            if (!started) {
                                try { result.put("message", "Services were found, but Android could not read the battery characteristic."); } catch (Exception ignored) {}
                                emitNativeGattResult(result);
                            }
                        }
                    }
                    @Override public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                        JSONObject result = new JSONObject();
                        try {
                            result.put("deviceName", safeName(selectedTarget));
                            result.put("address", safeAddress(selectedTarget));
                            result.put("connected", true);
                            result.put("serviceUuid", characteristic.getService() == null ? "" : characteristic.getService().getUuid().toString());
                            result.put("characteristicUuid", characteristic.getUuid().toString());
                            if (status == BluetoothGatt.GATT_SUCCESS && characteristic.getValue() != null) {
                                byte[] bytes = characteristic.getValue();
                                StringBuilder hex = new StringBuilder();
                                for (byte value : bytes) { if (hex.length() > 0) hex.append(' '); hex.append(String.format(java.util.Locale.US, "%02X", value & 0xff)); }
                                result.put("valueHex", hex.toString());
                                String decoded = new String(bytes, StandardCharsets.UTF_8).trim();
                                StringBuilder printable = new StringBuilder();
                                for (int k = 0; k < decoded.length(); k++) {
                                    char ch = decoded.charAt(k);
                                    if (ch >= 32 && ch <= 126) printable.append(ch);
                                }
                                if (printable.length() > 0) result.put("valueText", printable.toString());
                                if ("00002a19-0000-1000-8000-00805f9b34fb".equalsIgnoreCase(characteristic.getUuid().toString()) && bytes.length > 0) {
                                    int level = bytes[0] & 0xff;
                                    result.put("batteryLevel", level);
                                    deviceBatteryLevels.put(safeAddress(selectedTarget), level);
                                    result.put("message", "Read battery percentage from the standard BLE Battery Service.");
                                } else result.put("message", "Characteristic value read successfully.");
                            } else result.put("message", "Characteristic read failed (status " + status + ").");
                        } catch (Exception ignored) {}
                        emitNativeGattResult(result);
                    }
                }, android.bluetooth.BluetoothDevice.TRANSPORT_LE);
                return "Attempting a real BLE GATT connection. The result will appear here when the device responds.";
            } catch (SecurityException e) {
                return "Android denied Bluetooth access. Allow Nearby devices permission and retry.";
            } catch (Exception e) {
                return "Could not start BLE connection: " + e.getMessage();
            }
        }

        @JavascriptInterface public String readGattCharacteristic(String serviceUuid, String characteristicUuid) {
            try {
                if (activeGatt == null) return "No active BLE GATT connection. Select a BLE device first.";
                java.util.UUID serviceId = java.util.UUID.fromString(serviceUuid);
                java.util.UUID characteristicId = java.util.UUID.fromString(characteristicUuid);
                BluetoothGattService service = activeGatt.getService(serviceId);
                BluetoothGattCharacteristic characteristic = service == null ? null : service.getCharacteristic(characteristicId);
                if (characteristic == null) return "Characteristic is no longer available. Reconnect and try again.";
                if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) == 0) return "This characteristic does not support Read.";
                boolean started = activeGatt.readCharacteristic(characteristic);
                return started ? "Read requested. Waiting for the device…" : "Android could not start the read; try again in a moment.";
            } catch (IllegalArgumentException e) {
                return "Invalid service or characteristic UUID.";
            } catch (SecurityException e) {
                return "Android denied Bluetooth access. Allow Nearby devices permission and retry.";
            } catch (Exception e) {
                return "Could not read characteristic: " + e.getMessage();
            }
        }

        @JavascriptInterface public String disconnectGattDevice() {
            try {
                BluetoothGatt gatt = activeGatt;
                activeGatt = null;
                if (gatt != null) {
                    try { gatt.disconnect(); } catch (Exception ignored) {}
                    try { gatt.close(); } catch (Exception ignored) {}
                }
                JSONObject result = new JSONObject();
                result.put("connected", false);
                result.put("message", "BLE diagnostic connection closed. Bluetooth audio pairing, if any, is still managed by Android.");
                emitNativeGattResult(result);
                return "BLE diagnostic connection closed. Audio pairing remains managed by Android.";
            } catch (Exception e) {
                return "Could not close BLE connection: " + e.getMessage();
            }
        }

        private void emitNativeGattResult(JSONObject result) {
            if (result == null) return;
            runOnUiThread(() -> {
                if (webView != null) webView.evaluateJavascript("window.onNativeGattResult && window.onNativeGattResult(" + result.toString() + ")", null);
            });
        }

        @JavascriptInterface public void openBluetoothSettings() {
            runOnUiThread(() -> { try { startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS)); }
                catch(Exception e){Toast.makeText(MainActivity.this,"Unable to open Bluetooth settings",Toast.LENGTH_SHORT).show();} });
        }
        private String listDevices() {
            JSONObject result=new JSONObject(); JSONArray devices=new JSONArray();
            try {
                if(Build.VERSION.SDK_INT>=31 && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED){result.put("error","Bluetooth permission is not granted. Allow Nearby devices permission, then try again.");return result.toString();}
                if(adapter==null){result.put("error","This phone does not provide Bluetooth.");return result.toString();}
                if(!adapter.isEnabled()){result.put("error","Bluetooth is off. Turn Bluetooth on in Android settings, then try again.");return result.toString();}
                synchronized (connectedAddresses) { refreshConnectedAddressesLocked(); }
                Map<String,BluetoothDevice> all=new LinkedHashMap<>();
                Set<BluetoothDevice> bonded=adapter.getBondedDevices();
                for(BluetoothDevice d:bonded) all.put(d.getAddress(),d);
                all.putAll(discovered);
                for(BluetoothDevice d:all.values()) {
                    JSONObject item=new JSONObject();
                    item.put("name",safeName(d)); item.put("address",safeAddress(d)); item.put("type",typeName(d.getType()));
                    item.put("bondState",d.getBondState()==BluetoothDevice.BOND_BONDED?"Paired":"Nearby — not paired");
                    item.put("connected",isConnected(d));
                    int batteryLevel = readBatteryLevel(d);
                    int leftBattery = readMetadataBattery(d, "METADATA_UNTETHERED_LEFT_BATTERY");
                    int rightBattery = readMetadataBattery(d, "METADATA_UNTETHERED_RIGHT_BATTERY");
                    int caseBattery = readMetadataBattery(d, "METADATA_UNTETHERED_CASE_BATTERY");
                    item.put("battery", batteryLevel >= 0 ? batteryLevel + "%" : "Not exposed by this device");
                    if (leftBattery >= 0) item.put("leftBattery", leftBattery);
                    if (rightBattery >= 0) item.put("rightBattery", rightBattery);
                    if (caseBattery >= 0) item.put("caseBattery", caseBattery);
                    devices.put(item);
                }
                result.put("devices",devices); result.put("scanning",adapter.isDiscovering());
            } catch(SecurityException e){try{result.put("error","Android blocked Bluetooth access. Grant Nearby devices permission and retry.");}catch(Exception ignored){}}
              catch(Exception e){try{result.put("error","Could not read Bluetooth devices: "+e.getMessage());}catch(Exception ignored){}}
            return result.toString();
        }
        private int readBatteryLevel(BluetoothDevice device) {
            if (device == null) return -1;
            try {
                Integer cached = deviceBatteryLevels.get(safeAddress(device));
                if (cached != null && cached >= 0 && cached <= 100) return cached;
            } catch (Exception ignored) {}
            // Android 13+ may expose the classic Bluetooth headset battery getter.
            try {
                if (Build.VERSION.SDK_INT >= 33) {
                    java.lang.reflect.Method method = device.getClass().getMethod("getBatteryLevel");
                    Object value = method.invoke(device);
                    if (value instanceof Number) {
                        int level = ((Number) value).intValue();
                        if (level >= 0 && level <= 100) {
                            deviceBatteryLevels.put(safeAddress(device), level);
                            return level;
                        }
                    }
                }
            } catch (Exception ignored) {}
            return -1;
        }
        private boolean isConnected(BluetoothDevice d) {
            try {
                if(Build.VERSION.SDK_INT>=31 && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED) return false;
                synchronized (connectedAddresses) { return connectedAddresses.contains(d.getAddress()); }
            } catch(Exception e){return false;}
        }
        private String safeName(BluetoothDevice d){try{String n=d.getName();return n==null||n.trim().isEmpty()?"Unknown Bluetooth device":n;}catch(Exception e){return "Bluetooth device";}}
        private String safeAddress(BluetoothDevice d){try{return d.getAddress()==null?"":d.getAddress();}catch(Exception e){return "";}}
        private String typeName(int t){if(t==BluetoothDevice.DEVICE_TYPE_LE)return "BLE";if(t==BluetoothDevice.DEVICE_TYPE_DUAL)return "Classic + BLE";if(t==BluetoothDevice.DEVICE_TYPE_CLASSIC)return "Classic";return "Unknown";}
    }
}
