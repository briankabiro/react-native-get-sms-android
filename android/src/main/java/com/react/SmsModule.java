
package com.react;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import android.os.Bundle;
import android.widget.Toast;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;

import android.telephony.SmsManager;
import android.telephony.SmsMessage;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SmsModule extends ReactContextBaseJavaModule /*implements LoaderManager.LoaderCallbacks<Cursor>*/ {
    //    private LoaderManager mManager;
    private Cursor smsCursor;
    private Map<Long, String> smsList;
    private Map<Long, Object> smsListBody;
    Activity mActivity = null;
    private static Context context;
    private ReactContext mReactContext;
    private Callback cb_autoSend_succ = null;
    private Callback cb_autoSend_err = null;

    public SmsModule(ReactApplicationContext reactContext) {
        super(reactContext);
        mReactContext = reactContext;
        smsList = new HashMap<Long, String>();
        context = reactContext.getApplicationContext();
    }

    @Override
    public String getName() {
        return "Sms";
    }

    @ReactMethod
    public void list(String filter, final Callback errorCallback, final Callback successCallback) {
        try {
            JSONObject filterJ = new JSONObject(filter);
            String uri_filter = filterJ.has("box") ? filterJ.optString("box") : "inbox";
            int fread = filterJ.has("read") ? filterJ.optInt("read") : -1;
            int fid = filterJ.has("_id") ? filterJ.optInt("_id") : -1;
            String faddress = filterJ.optString("address");
            String fcontent = filterJ.optString("body");
            int indexFrom = filterJ.has("indexFrom") ? filterJ.optInt("indexFrom") : 0;
            int maxCount = filterJ.has("maxCount") ? filterJ.optInt("maxCount") : -1;
            Cursor cursor = context.getContentResolver().query(Uri.parse("content://sms/" + uri_filter), null, "", null,
                    null);
            int c = 0;
            JSONArray jsons = new JSONArray();
            while (cursor.moveToNext()) {
                boolean matchFilter = false;
                if (fid > -1)
                    matchFilter = fid == cursor.getInt(cursor.getColumnIndex("_id"));
                else if (fread > -1)
                    matchFilter = fread == cursor.getInt(cursor.getColumnIndex("read"));
                else if (faddress.length() > 0)
                    matchFilter = faddress.equals(cursor.getString(cursor.getColumnIndex("address")).trim());
                else if (fcontent.length() > 0)
                    matchFilter = fcontent.equals(cursor.getString(cursor.getColumnIndex("body")).trim());
                else {
                    matchFilter = true;
                }
                if (matchFilter) {
                    if (c >= indexFrom) {
                        if (maxCount > 0 && c >= indexFrom + maxCount)
                            break;
                        c++;
                        // Long dateTime = Long.parseLong(cursor.getString(cursor.getColumnIndex("date")));
                        // String message = cursor.getString(cursor.getColumnIndex("body"));
                        JSONObject json;
                        json = getJsonFromCursor(cursor);
                        jsons.put(json);

                    }
                }

            }
            cursor.close();
            try {
                successCallback.invoke(c, jsons.toString());
            } catch (Exception e) {
                errorCallback.invoke(e.getMessage());
            }
        } catch (JSONException e) {
            errorCallback.invoke(e.getMessage());
            return;
        }
    }

    private JSONObject getJsonFromCursor(Cursor cur) {
        JSONObject json = new JSONObject();

        int nCol = cur.getColumnCount();
        String[] keys = cur.getColumnNames();
        try {
            for (int j = 0; j < nCol; j++)
                switch (cur.getType(j)) {
                    case 0:
                        json.put(keys[j], null);
                        break;
                    case 1:
                        json.put(keys[j], cur.getLong(j));
                        break;
                    case 2:
                        json.put(keys[j], cur.getFloat(j));
                        break;
                    case 3:
                        json.put(keys[j], cur.getString(j));
                        break;
                    case 4:
                        json.put(keys[j], cur.getBlob(j));
                }
        } catch (Exception e) {
            return null;
        }

        return json;
    }

    @ReactMethod
    public void send(String addresses, String text, final Callback errorCallback, final Callback successCallback) {
        mActivity = getCurrentActivity();
        try {
            JSONObject jsonObject = new JSONObject(addresses);
            JSONArray addressList = jsonObject.getJSONArray("addressList");
            int n;
            if ((n = addressList.length()) > 0) {
                PendingIntent sentIntent = PendingIntent.getBroadcast(mActivity, 0, new Intent("SENDING_SMS"), 0);
                SmsManager sms = SmsManager.getDefault();
                for (int i = 0; i < n; i++) {
                    String address;
                    if ((address = addressList.optString(i)).length() > 0)
                        sms.sendTextMessage(address, null, text, sentIntent, null);
                }
            } else {
                PendingIntent sentIntent = PendingIntent.getActivity(mActivity, 0, new Intent("android.intent.action.VIEW"), 0);
                Intent intent = new Intent("android.intent.action.VIEW");
                intent.putExtra("sms_body", text);
                intent.setData(Uri.parse("sms:"));
                try {
                    sentIntent.send(mActivity.getApplicationContext(), 0, intent);
                    successCallback.invoke("OK");
                } catch (PendingIntent.CanceledException e) {
                    errorCallback.invoke(e.getMessage());
                    return;
                }
            }
            return;
        } catch (JSONException e) {
            errorCallback.invoke(e.getMessage());
            return;
        }

    }

    @ReactMethod
    public void delete(Integer id, final Callback errorCallback, final Callback successCallback) {
        try {
            int res = context.getContentResolver().delete(Uri.parse("content://sms/" + id), null, null);
            if (res > 0) {
                successCallback.invoke("OK");
            } else {
                errorCallback.invoke("SMS not found");
            }
            return;
        } catch (Exception e) {
            errorCallback.invoke(e.getMessage());
            return;
        }
    }

    private void sendEvent(ReactContext reactContext, String eventName, String params) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
    }

    private void sendCallback(String message, boolean success) {
        if (success && cb_autoSend_succ != null) {
            cb_autoSend_succ.invoke(message);
            cb_autoSend_succ = null;
        } else if (!success && cb_autoSend_err != null) {
            cb_autoSend_err.invoke(message);
            cb_autoSend_err = null;
        }

    }


    @ReactMethod
    public void autoSend(String phoneNumber, String message, final Callback errorCallback,
                         final Callback successCallback) {

        cb_autoSend_succ = successCallback;
        cb_autoSend_err = errorCallback;

        try {
            String SENT = "SMS_SENT";
            String DELIVERED = "SMS_DELIVERED";

            PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, new Intent(SENT), 0);
            PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0, new Intent(DELIVERED), 0);

            //---when the SMS has been sent---
            context.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context arg0, Intent arg1) {
                    switch (getResultCode()) {
                        case Activity.RESULT_OK:
                            sendCallback("SMS sent", true);
                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            sendCallback("Generic failure", false);
                            break;
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                            sendCallback("No service", false);
                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            sendCallback("Null PDU", false);
                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            sendCallback("Radio off", false);
                            break;
                    }
                }
            }, new IntentFilter(SENT));

            //---when the SMS has been delivered---
            context.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context arg0, Intent arg1) {
                    switch (getResultCode()) {
                        case Activity.RESULT_OK:
                            sendEvent(mReactContext, "sms_onDelivery", "SMS delivered");
                            break;
                        case Activity.RESULT_CANCELED:
                            sendEvent(mReactContext, "sms_onDelivery", "SMS not delivered");
                            break;
                    }
                }
            }, new IntentFilter(DELIVERED));

            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);

        } catch (Exception e) {
            sendCallback(e.getMessage(), false);
        }
    }
}