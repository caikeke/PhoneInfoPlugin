package com.phone.yhck;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class PhoneInfoPlugin extends CordovaPlugin {
  private CallbackContext mCallbackContext;
  private static final String marshmallowMacAddress = "02:00:00:00:00:00";
  private static final String fileAddressMac = "/sys/class/net/wlan0/address";
  // 定义WifiManager对象
  private WifiManager mWifiManager;
  public Handler mHandler = new Handler();

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
  }

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    this.mCallbackContext = callbackContext;
    if (!"".equals(action) || action != null) {
      initAppList();
      return true;
    }
    mCallbackContext.error("error");
    return false;
  }

  private void initAppList() {
    new Thread() {
      @Override
      public void run() {
        super.run();
        //扫描得到APP列表
        final List<MyAppInfo> appInfos = ApkTool.scanLocalInstallAppList(cordova.getActivity().getPackageManager());
        mHandler.post(new Runnable() {
          @Override
          public void run() {
            getPhoneInfo(appInfos);
          }
        });
      }
    }.start();
  }

  private void getPhoneInfo(List<MyAppInfo> appList) {
    TelephonyManager tm = (TelephonyManager) cordova.getActivity().getSystemService(Context.TELEPHONY_SERVICE);
    mWifiManager = (WifiManager) cordova.getActivity().getSystemService(Context.WIFI_SERVICE);
    JSONObject resultJson = new JSONObject();
    try {
      resultJson.put("phone", tm.getLine1Number());
      resultJson.put("IMEI", tm.getDeviceId());
      resultJson.put("operator", tm.getNetworkOperatorName());
      resultJson.put("simNumber", tm.getSimSerialNumber());
      resultJson.put("IMSI", tm.getSubscriberId());
      resultJson.put("phoneType", android.os.Build.MODEL);
      String m_szAndroidID = Settings.Secure.getString(cordova.getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
      resultJson.put("phoneID", m_szAndroidID);
      resultJson.put("WIFIMAC", getAdresseMAC(cordova.getActivity()));
      JSONArray mJSonArray = new JSONArray();
      for (MyAppInfo itemAPP : appList) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("appName", itemAPP.getAppName());
        jsonObject.put("appPackage", itemAPP.getPackageName());
        mJSonArray.put(jsonObject);
      }
      resultJson.put("installedList", mJSonArray);
      mCallbackContext.success(resultJson);
    } catch (JSONException e) {
      e.printStackTrace();
    }

  }

  public static String getAdresseMAC(Context context) {
    WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    WifiInfo wifiInf = wifiMan.getConnectionInfo();
    if (wifiInf != null && marshmallowMacAddress.equals(wifiInf.getMacAddress())) {
      String result = null;
      try {
        result = getAdressMacByInterface();
        if (result != null) {
          return result;
        } else {
          result = getAddressMacByFile(wifiMan);
          return result;
        }
      } catch (IOException e) {
        Log.e("MobileAccess", "Erreur lecture propriete Adresse MAC");
      } catch (Exception e) {
        Log.e("MobileAcces", "Erreur lecture propriete Adresse MAC ");
      }
    } else {
      if (wifiInf != null && wifiInf.getMacAddress() != null) {
        return wifiInf.getMacAddress();
      } else {
        return "";
      }
    }
    return marshmallowMacAddress;
  }

  private static String getAdressMacByInterface() {
    try {
      List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
      for (NetworkInterface nif : all) {
        if (nif.getName().equalsIgnoreCase("wlan0")) {
          byte[] macBytes = nif.getHardwareAddress();
          if (macBytes == null) {
            return "";
          }

          StringBuilder res1 = new StringBuilder();
          for (byte b : macBytes) {
            res1.append(String.format("%02X:", b));
          }

          if (res1.length() > 0) {
            res1.deleteCharAt(res1.length() - 1);
          }
          return res1.toString();
        }
      }

    } catch (Exception e) {
      Log.e("MobileAcces", "Erreur lecture propriete Adresse MAC ");
    }
    return null;
  }

  private static String getAddressMacByFile(WifiManager wifiMan) throws Exception {
    String ret;
    int wifiState = wifiMan.getWifiState();

    wifiMan.setWifiEnabled(true);
    File fl = new File(fileAddressMac);
    FileInputStream fin = new FileInputStream(fl);
    ret = crunchifyGetStringFromStream(fin);
    fin.close();

    boolean enabled = WifiManager.WIFI_STATE_ENABLED == wifiState;
    wifiMan.setWifiEnabled(enabled);
    return ret;
  }

  private static String crunchifyGetStringFromStream(InputStream crunchifyStream) throws IOException {
    if (crunchifyStream != null) {
      Writer crunchifyWriter = new StringWriter();

      char[] crunchifyBuffer = new char[2048];
      try {
        Reader crunchifyReader = new BufferedReader(new InputStreamReader(crunchifyStream, "UTF-8"));
        int counter;
        while ((counter = crunchifyReader.read(crunchifyBuffer)) != -1) {
          crunchifyWriter.write(crunchifyBuffer, 0, counter);
        }
      } finally {
        crunchifyStream.close();
      }
      return crunchifyWriter.toString();
    } else {
      return "No Contents";
    }
  }
}
