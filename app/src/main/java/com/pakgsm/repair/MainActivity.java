package com.pakgsm.repair;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
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
import java.util.Map;
import java.util.Set;

public class MainActivity extends Activity {
    private WebView webView;
    private BluetoothAdapter adapter;
    private static final int BT_PERMISSION_REQUEST = 410;
    private final Map<String, BluetoothDevice> discovered = new LinkedHashMap<>();
    private BroadcastReceiver discoveryReceiver;
    private WebViewAssetLoader assetLoader;

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
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                return assetLoader.shouldInterceptRequest(android.net.Uri.parse(url));
            }
        });
        webView.addJavascriptInterface(new NativeBluetoothBridge(), "PGRNativeBluetooth");
        setContentView(webView);
        webView.loadUrl("https://appassets.androidplatform.net/assets/index.html");
        registerDiscoveryReceiver();
        requestBluetoothPermissionsIfNeeded();
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
                if(BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                    BluetoothDevice d=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if(d!=null) try { discovered.put(d.getAddress(),d); } catch(SecurityException ignored) {}
                }
            }
        };
        IntentFilter f=new IntentFilter(BluetoothDevice.ACTION_FOUND);
        if(Build.VERSION.SDK_INT>=33) registerReceiver(discoveryReceiver,f,Context.RECEIVER_NOT_EXPORTED); else registerReceiver(discoveryReceiver,f);
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==BT_PERMISSION_REQUEST) {
            boolean ok=grantResults.length>0;
            for(int result:grantResults) if(result!=PackageManager.PERMISSION_GRANTED) ok=false;
            Toast.makeText(this, ok?"Bluetooth permission granted":"Bluetooth permission is needed for device discovery", Toast.LENGTH_SHORT).show();
        }
    }
    @Override protected void onDestroy() {
        try { if(adapter!=null && adapter.isDiscovering()) adapter.cancelDiscovery(); } catch(Exception ignored) {}
        try { if(discoveryReceiver!=null) unregisterReceiver(discoveryReceiver); } catch(Exception ignored) {}
        if(webView!=null) webView.destroy();
        super.onDestroy();
    }
    @Override public void onBackPressed() { if(webView!=null && webView.canGoBack()) webView.goBack(); else super.onBackPressed(); }

    public class NativeBluetoothBridge {
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
                Map<String,BluetoothDevice> all=new LinkedHashMap<>();
                Set<BluetoothDevice> bonded=adapter.getBondedDevices();
                for(BluetoothDevice d:bonded) all.put(d.getAddress(),d);
                all.putAll(discovered);
                for(BluetoothDevice d:all.values()) {
                    JSONObject item=new JSONObject();
                    item.put("name",safeName(d)); item.put("address",safeAddress(d)); item.put("type",typeName(d.getType()));
                    item.put("bondState",d.getBondState()==BluetoothDevice.BOND_BONDED?"Paired":"Nearby — not paired");
                    item.put("connected",isConnected(d));
                    String battery="Not available through standard Android API";
                    // Some Android/OEM builds expose a battery getter; use reflection so the project
                    // still compiles against the public SDK and never assumes unsupported data.
                    try {
                        if(Build.VERSION.SDK_INT>=33) {
                            java.lang.reflect.Method m=d.getClass().getMethod("getBatteryLevel");
                            Object value=m.invoke(d);
                            if(value instanceof Number && ((Number)value).intValue()>=0) battery=((Number)value).intValue()+"%";
                        }
                    } catch(Exception ignored) {}
                    item.put("battery",battery);
                    devices.put(item);
                }
                result.put("devices",devices); result.put("scanning",adapter.isDiscovering());
            } catch(SecurityException e){try{result.put("error","Android blocked Bluetooth access. Grant Nearby devices permission and retry.");}catch(Exception ignored){}}
              catch(Exception e){try{result.put("error","Could not read Bluetooth devices: "+e.getMessage());}catch(Exception ignored){}}
            return result.toString();
        }
        private boolean isConnected(BluetoothDevice d) {
            try {
                if(Build.VERSION.SDK_INT>=31 && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED) return false;
                // Android does not expose a universal public per-device profile state without profile proxies.
                return false;
            } catch(Exception e){return false;}
        }
        private String safeName(BluetoothDevice d){try{String n=d.getName();return n==null||n.trim().isEmpty()?"Unknown Bluetooth device":n;}catch(Exception e){return "Bluetooth device";}}
        private String safeAddress(BluetoothDevice d){try{return d.getAddress()==null?"":d.getAddress();}catch(Exception e){return "";}}
        private String typeName(int t){if(t==BluetoothDevice.DEVICE_TYPE_LE)return "BLE";if(t==BluetoothDevice.DEVICE_TYPE_DUAL)return "Classic + BLE";if(t==BluetoothDevice.DEVICE_TYPE_CLASSIC)return "Classic";return "Unknown";}
    }
}
