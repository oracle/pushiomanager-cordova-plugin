/**
 * Copyright © 2022, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
*/

package com.pushio.manager.cordova;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.net.Uri;
import android.util.Log;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.firebase.messaging.RemoteMessage;
import com.pushio.manager.PIOBadgeSyncListener;
import com.pushio.manager.PIOBeaconRegion;
import com.pushio.manager.PIOConfigurationListener;
import com.pushio.manager.PIOConversionEvent;
import com.pushio.manager.PIOConversionListener;
import com.pushio.manager.PIOGeoRegion;
import com.pushio.manager.PIOInteractiveNotificationCategory;
import com.pushio.manager.PIOMCMessage;
import com.pushio.manager.PIOMCMessageError;
import com.pushio.manager.PIOMCMessageListener;
import com.pushio.manager.PIOMCRichContentListener;
import com.pushio.manager.PIOMessageCenterUpdateListener;
import com.pushio.manager.PIORegionCompletionListener;
import com.pushio.manager.PIORegionEventType;
import com.pushio.manager.PIORegionException;
import com.pushio.manager.PIORsysIAMHyperlinkListener;
import com.pushio.manager.PushIOManager;
import com.pushio.manager.exception.PIOMCMessageException;
import com.pushio.manager.exception.PIOMCRichContentException;
import com.pushio.manager.exception.ValidationException;
import com.pushio.manager.preferences.PushIOPreference;
import com.pushio.manager.tasks.PushIOEngagementListener;
import com.pushio.manager.tasks.PushIOListener;
import com.pushio.manager.PIODeepLinkListener;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.pushio.manager.cordova.PushIOManagerPluginUtils;

public class PushIOManagerPlugin extends CordovaPlugin {

    private final String TAG = "pushio-cordova";
    private ExecutorService mExecutorService;

    private PushIOManager mPushIOManager;
    private PushIOManagerPlugin mPushIOManagerPlugin;
    private String mDeepLinkUrl = null;
    private String mWebLinkUrl = null;
    private Context mAppContext;

    private final List<String> mAvailableActions = Arrays.asList("getAPIKey", "getAccountToken",
            "setExternalDeviceTrackingID", "getExternalDeviceTrackingID", "setAdvertisingID", "getAdvertisingID",
            "registerUserId", "getRegisteredUserId", "unregisterUserId", "getVerifiedUserId", "setVerifiedUserId",
            "declarePreference", "getPreferences", "getPreference", "setStringPreference", "setBooleanPreference",
            "setNumberPreference", "removePreference", "clearAllPreferences", "setNotificationsStacked", "trackEvent",
            "fetchMessagesForMessageCenter", "trackEngagement", "setLogLevel", "setLoggingEnabled", "overwriteApiKey",
            "overwriteAccountToken", "configure", "registerApp", "unregisterApp", "getDeviceID", "getLibVersion",
            "setDefaultSmallIcon", "setDefaultLargeIcon", "addInteractiveNotificationCategory",
            "getInteractiveNotificationCategory", "deleteInteractiveNotificationCategory", "getRIAppId",
            "getConversionUrl", "setRIAppId", "setConversionUrl", "getExecuteRsysWebUrl", "setExecuteRsysWebUrl",
            "isMessageCenterEnabled", "setMessageCenterEnabled", "getNotificationStacked", "getEngagementTimestamp",
            "getEngagementMaxAge", "resetEngagementContext", "fetchRichContentForMessage", "isCrashLoggingEnabled",
            "setCrashLoggingEnabled", "setInAppFetchEnabled", "onGeoRegionEntered", "onGeoRegionExited",
            "onBeaconRegionEntered", "onBeaconRegionExited", "setDeviceToken", "setBadgeCount", "getBadgeCount",
            "setMessageCenterBadgingEnabled", "resetBadgeCount", "resetMessageCenter", "clearInAppMessages",
            "clearInteractiveNotificationCategories", "isResponsysPush", "handleMessage", "onMessageCenterViewVisible",
            "trackMessageCenterDisplayEngagement", "trackMessageCenterOpenEngagement", "onMessageCenterViewFinish",
            "onDeepLinkReceived", "setDelayRichPushDisplay", "isRichPushDelaySet", "showRichPushMessage", "trackConversionEvent",
            "setNotificationSmallIconColor", "setNotificationSmallIcon", "setNotificationLargeIcon",
            "setInAppMessageBannerHeight","getInAppMessageBannerHeight","setStatusBarHiddenForIAMBannerInterstitial",
            "isStatusBarHiddenForIAMBannerInterstitial","onMessageCenterUpdated");

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Log.v(TAG, "Initializing plugin");
        mAppContext = cordova.getActivity().getApplicationContext();
        mExecutorService = Executors.newSingleThreadExecutor();
        mPushIOManager = PushIOManager.getInstance(mAppContext);
    }

    @Override
    public boolean execute(final String action, final JSONArray args, final CallbackContext callbackContext)
            throws JSONException {
        if (!mAvailableActions.contains(action)) {
            Log.v(TAG, "Action not found: " + action);
            return false;
        }

        mPushIOManagerPlugin = this;

        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.v(TAG, "Plugin Execute: " + action);
                    Method method = mPushIOManagerPlugin.getClass().getDeclaredMethod(action, JSONArray.class,
                            CallbackContext.class);
                    method.setAccessible(true);
                    method.invoke(mPushIOManagerPlugin, args, callbackContext);
                    method.setAccessible(false);
                } catch (Exception e) {
                    Log.v(TAG, "Exception: " + e.getMessage());
                }
            }
        });

        return true;
    }

    @Override
    public void onStart() {
        Log.v(TAG, "onStart: " + cordova.getActivity().getIntent().getDataString());
        handleIntent(cordova.getActivity().getIntent());
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.v(TAG, "onNewIntent: " + intent.getDataString());
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        cordova.getActivity().setIntent(intent);
        mPushIOManager.trackEmailConversion(intent, new PIODeepLinkListener() {
            @Override
            public void onDeepLinkReceived(final String deepLinkUrl, final String webLinkUrl) {
                Log.v(TAG, "deepLinkUrl: " + deepLinkUrl + ", webLinkUrl: " + webLinkUrl);

                mDeepLinkUrl = deepLinkUrl;
                mWebLinkUrl = webLinkUrl;
            }
        });
    }

    private void onDeepLinkReceived(JSONArray data, final CallbackContext callbackContext) {
        if (TextUtils.isEmpty(mDeepLinkUrl) && TextUtils.isEmpty(mWebLinkUrl)) {
            return;
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("deepLinkUrl", mDeepLinkUrl);
            jsonObject.put("webLinkUrl", mWebLinkUrl);
            callbackContext.success(jsonObject);
        } catch (JSONException e) {
            Log.v(TAG, "Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
        }
    }

    private void getAPIKey(JSONArray data, CallbackContext callbackContext) {
        String value = mPushIOManager.getAPIKey();
        callbackContext.success(value);
    }

    private void getAccountToken(JSONArray data, CallbackContext callbackContext) {
        String value = mPushIOManager.getAccountToken();
        callbackContext.success(value);
    }

    private void setExternalDeviceTrackingID(JSONArray data, CallbackContext callbackContext) {

        try {
            String edti = data.getString(0);

            if (!TextUtils.isEmpty(edti)) {
                mPushIOManager.setExternalDeviceTrackingID(edti);
                callbackContext.success();
            } else {
                mPushIOManager.setExternalDeviceTrackingID(null);
                callbackContext.error("Error reading parameter");
            }
        } catch (JSONException e) {
            Log.v(TAG, "Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
        }
    }

    private void getExternalDeviceTrackingID(JSONArray data, CallbackContext callbackContext) {
        String value = mPushIOManager.getExternalDeviceTrackingID();
        callbackContext.success(value);
    }

    private void setAdvertisingID(JSONArray data, CallbackContext callbackContext) {

        try {
            String adid = data.getString(0);

            if (!TextUtils.isEmpty(adid)) {
                mPushIOManager.setAdvertisingID(adid);
                callbackContext.success();
            } else {
                mPushIOManager.setAdvertisingID(null);
                callbackContext.error("Error reading parameter");
            }
        } catch (JSONException e) {
            Log.v(TAG, "Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
        }
    }

    private void getAdvertisingID(JSONArray data, CallbackContext callbackContext) {
        String value = mPushIOManager.getAdvertisingID();
        callbackContext.success(value);
    }

    private void registerUserId(JSONArray data, CallbackContext callbackContext) {
        try {
            String userId = data.getString(0);

            if (!TextUtils.isEmpty(userId)) {
                mPushIOManager.registerUserId(userId);
                callbackContext.success();
            } else {
                callbackContext.error("Error reading parameter");
            }
        } catch (JSONException e) {
            Log.v(TAG, "Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
        }
    }

    private void getRegisteredUserId(JSONArray data, CallbackContext callbackContext) {
        String value = mPushIOManager.getRegisteredUserId();
        callbackContext.success(value);
    }

    private void unregisterUserId(JSONArray data, CallbackContext callbackContext) {
        mPushIOManager.unregisterUserId();
        callbackContext.success();
    }

    private void getVerifiedUserId(JSONArray data, CallbackContext callbackContext) {
        String value = mPushIOManager.getVerifiedUserId();
        callbackContext.success(value);
    }

    private void setVerifiedUserId(JSONArray data, CallbackContext callbackContext) {

        try {
            String vUserId = data.getString(0);

            if (!TextUtils.isEmpty(vUserId)) {
                mPushIOManager.setVerifiedUserId(vUserId);
                callbackContext.success();
            } else {
                callbackContext.error("Error reading parameter");
            }
        } catch (JSONException e) {
            Log.v(TAG, "Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
        }
    }

    private void declarePreference(JSONArray data, CallbackContext callbackContext) {

        try {
            final String key = data.optString(0);
            final String label = data.optString(1);
            final String type = data.optString(2);
            if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(type)) {
                PushIOPreference.Type preferenceType = PushIOPreference.Type.valueOf(type);

                mPushIOManager.declarePreference(key, label, preferenceType);
                callbackContext.success();
            } else {
                callbackContext.error("Error reading parameters");
            }
        } catch (ValidationException e) {
            Log.v(TAG, "Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
        }
    }

    private void setStringPreference(JSONArray data, CallbackContext callbackContext) {

        try {
            final String key = data.optString(0);
            final String value = data.optString(1);

            if (!TextUtils.isEmpty(key)) {
                mPushIOManager.setPreference(key, value);
                callbackContext.success();
            } else {
                callbackContext.error("Error reading parameter");
            }
        } catch (ValidationException e) {
            Log.v(TAG, "Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
        }
    }

    private void setNumberPreference(JSONArray data, CallbackContext callbackContext) {

        try {
            final String key = data.optString(0);
            final Double value = data.optDouble(1);

            if (!TextUtils.isEmpty(key)) {
                mPushIOManager.setPreference(key, value);
                callbackContext.success();
            } else {
                callbackContext.error("Error reading parameter");
            }
        } catch (ValidationException e) {
            Log.v(TAG, "Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
        }
    }

    private void setBooleanPreference(JSONArray data, CallbackContext callbackContext) {

        try {
            final String key = data.optString(0);
            final Boolean value = data.optBoolean(1);

            if (!TextUtils.isEmpty(key)) {
                mPushIOManager.setPreference(key, value);
                callbackContext.success();
            } else {
                callbackContext.error("Error reading parameter");
            }
        } catch (ValidationException e) {
            Log.v(TAG, "Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
        }
    }

    private void getPreferences(JSONArray data, CallbackContext callbackContext) {
        List<PushIOPreference> preferences = mPushIOManager.getPreferences();
        callbackContext.success(PushIOManagerPluginUtils.preferencesAsJsonArray(preferences));
    }

    private void getPreference(JSONArray data, CallbackContext callbackContext) {

        String key = data.optString(0);
        PushIOPreference value = mPushIOManager.getPreference(key);

        if (value != null) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("key", value.getKey());
                jsonObject.put("value", value.getValue());
                jsonObject.put("label", value.getLabel());
                jsonObject.put("type", value.getKey());
                callbackContext.success(jsonObject);
            } catch (JSONException e) {
                Log.v(TAG, "Exception: " + e.getMessage());
                callbackContext.error(e.getMessage());
            }
        } else {
            callbackContext.error("Preference Not found");
        }

    }

    private void removePreference(JSONArray data, CallbackContext callbackContext) {

        try {
            String preference = data.getString(0);

            if (!TextUtils.isEmpty(preference)) {
                mPushIOManager.removePreference(preference);
                callbackContext.success();
            } else {
                callbackContext.error("Error reading parameter.");
            }
        } catch (JSONException e) {
            Log.v(TAG, "Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
        }
    }

    private void clearAllPreferences(JSONArray data, CallbackContext callbackContext) {
        mPushIOManager.clearAllPreferences();
        callbackContext.success();
    }

    private void setNotificationsStacked(JSONArray data, CallbackContext callbackContext) {

        try {
            boolean bool = data.getBoolean(0);

            mPushIOManager.setNotificationsStacked(bool);
            callbackContext.success();

        } catch (JSONException e) {
            Log.v(TAG, "Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
        }
    }

    private void getNotificationStacked(JSONArray data, CallbackContext callbackContext) {
        boolean result = mPushIOManager.getNotificationStacked();
        callbackContext.success(String.valueOf(result));
    }

    private void trackEvent(JSONArray data, CallbackContext callbackContext) {
        try {
            final String eventType = data.optString(0);
            if (!TextUtils.isEmpty(eventType)) {
                Map<String, Object> properties = null;
                final JSONObject propertiesObject = data.optJSONObject(1);
                if (propertiesObject != null) {
                    properties = PushIOManagerPluginUtils.toMap(propertiesObject);
                }

                mPushIOManager.trackEvent(eventType, properties);
                callbackContext.success();
            } else {
                callbackContext.error("`event` value is required");
            }
        } catch (JSONException e) {
            Log.v(TAG, "Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
        }
    }

    private void unregisterDevice(JSONArray data, CallbackContext callbackContext) {
        mPushIOManager.unregisterDevice();
        callbackContext.success();
    }

    private void trackEngagement(JSONArray data, CallbackContext callbackContext) {

        try {
            int metric = data.optInt(0);
            JSONObject propertiesObject = data.optJSONObject(1);
            Map<String, String> properties = null;
            if (propertiesObject != null) {
                properties = PushIOManagerPluginUtils.toMapStr(propertiesObject);
            }

            mPushIOManager.trackEngagement(metric, null, properties, new PushIOEngagementListener() {
                @Override
                public void onEngagementSuccess() {
                    callbackContext.success();
                }

                @Override
                public void onEngagementError(String s) {
                    callbackContext.error(s);
                }
            });
        } catch (Exception e) {
            Log.v(TAG, "Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
        }

    }

    private void setLogLevel(JSONArray data, CallbackContext callbackContext) {
        try {

            int logLevel = data.getInt(0);

            mPushIOManager.setLogLevel(logLevel);

        } catch (JSONException e) {
            Log.v(TAG, "Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
        }
    }

    private void setLoggingEnabled(JSONArray data, CallbackContext callbackContext) {
        try {

            boolean isLoggingEnabled = data.getBoolean(0);

            mPushIOManager.setLoggingEnabled(isLoggingEnabled);

        } catch (JSONException e) {
            Log.v(TAG, "Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
        }
    }

    private void overwriteApiKey(JSONArray data, CallbackContext callbackContext) {

        String apiKey = data.optString(0);

        if (!TextUtils.isEmpty(apiKey)) {
            mPushIOManager.overwriteApiKey(apiKey);
        } else {
            callbackContext.error("Error reading parameters");
        }
    }

    private void overwriteAccountToken(JSONArray data, CallbackContext callbackContext) {
        String accountToken = data.optString(0);

        if (!TextUtils.isEmpty(accountToken)) {
            mPushIOManager.overwriteAccountToken(accountToken);
        } else {
            callbackContext.error("Error reading parameters");
        }

    }

    private void configure(JSONArray data, CallbackContext callbackContext) {
        String fileName = data.optString(0);
        if (!TextUtils.isEmpty(fileName)) {
            mPushIOManager.configure(fileName, new PIOConfigurationListener() {
                @Override
                public void onSDKConfigured(Exception e) {
                    if (e == null)
                        callbackContext.success();
                    else
                        callbackContext.error(e.getMessage());
                }
            });
        } else {
            callbackContext.error("Error reading parameters");
        }

    }

    private void registerApp(JSONArray data, CallbackContext callbackContext) {

        Boolean isUseLocation = data.optBoolean(0);
        mPushIOManager.registerPushIOListener(new PushIOListener() {
            @Override
            public void onPushIOSuccess() {
                callbackContext.success();
            }

            @Override
            public void onPushIOError(String s) {
                callbackContext.error(s);
            }
        });
        mPushIOManager.registerApp(isUseLocation);
    }

    private void unregisterApp(JSONArray data, CallbackContext callbackContext) {
        mPushIOManager.unregisterApp();
        callbackContext.success();
    }

    private void getDeviceID(JSONArray data, CallbackContext callbackContext) {
        String value = mPushIOManager.getDeviceId();
        callbackContext.success(value);
    }

    private void getLibVersion(JSONArray data, CallbackContext callbackContext) {
        String value = mPushIOManager.getLibVersion();
        callbackContext.success(value);
    }

    private void setDefaultLargeIcon(JSONArray data, CallbackContext callbackContext) {
        try {

            int icon = data.getInt(0);
            mPushIOManager.setDefaultLargeIcon(icon);
        } catch (JSONException e) {
            Log.v(TAG, "Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
        }
    }

    private void setDefaultSmallIcon(JSONArray data, CallbackContext callbackContext) {
        try {
            int icon = data.getInt(0);
            mPushIOManager.setDefaultSmallIcon(icon);

        } catch (JSONException e) {
            Log.v(TAG, "Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
        }
    }

    private void isMessageCenterEnabled(JSONArray data, CallbackContext callbackContext) {
        boolean value = mPushIOManager.isMessageCenterEnabled();
        callbackContext.success(String.valueOf(value));
    }

    private void setMessageCenterEnabled(JSONArray data, CallbackContext callbackContext) {
        try {
            boolean flag = data.getBoolean(0);
            mPushIOManager.setMessageCenterEnabled(flag);
            callbackContext.success();

        } catch (JSONException e) {
            Log.v(TAG, "Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
        }
    }

    private void fetchMessagesForMessageCenter(JSONArray data, CallbackContext callbackContext) {
        try {
            String msgCenterName = data.optString(0);
            mPushIOManager.fetchMessagesForMessageCenter(msgCenterName, new PIOMCMessageListener() {
                @Override
                public void onSuccess(String messageCenter, List<PIOMCMessage> messages) {
                    JSONArray messagesAsJson = PushIOManagerPluginUtils.messageCenterMessagesAsJSONArray(messages);
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("messageCenter", messageCenter);
                        jsonObject.put("messages", messagesAsJson);
                        callbackContext.success(jsonObject);
                    } catch (JSONException e) {
                        Log.v(TAG, "Exception: " + e.getMessage());
                        callbackContext.error(e.getMessage());
                    }
                }

                @Override
                public void onFailure(String messageCenter, PIOMCMessageError error) {
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("messageCenter", messageCenter);
                        jsonObject.put("errorReason", error.getErrorMessage());
                        callbackContext.error(jsonObject);
                    } catch (JSONException e) {
                        Log.v(TAG, "Exception: " + e.getMessage());
                        callbackContext.error(error.getErrorMessage());
                    }
                }
            });
        } catch (PIOMCMessageException e) {
            Log.v(TAG, "Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
        }
    }

    private void fetchRichContentForMessage(JSONArray data, CallbackContext callbackContext) {
        try {
            String messageId = data.optString(0);
            if (!TextUtils.isEmpty(messageId)) {
                mPushIOManager.fetchRichContentForMessage(messageId, new PIOMCRichContentListener() {

                    @Override
                    public void onSuccess(String messageId, String richContent) {
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("messageId", messageId);
                            jsonObject.put("richContent", richContent);
                            callbackContext.success(jsonObject);
                        } catch (JSONException e) {
                            Log.v(TAG, "Exception: " + e.getMessage());
                            callbackContext.error(e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(String messageId, PIOMCMessageError error) {
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("messageId", messageId);
                            jsonObject.put("errorReason", error.getErrorMessage());
                            callbackContext.error(jsonObject);
                        } catch (JSONException e) {
                            Log.v(TAG, "Exception: " + e.getMessage());
                            callbackContext.error(error.getErrorMessage());
                        }
                    }
                });
            } else {
                callbackContext.error("Error reading parameters.");
            }
        } catch (PIOMCRichContentException e) {
            Log.v(TAG, "Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
        }
    }

    private void setInAppFetchEnabled(JSONArray data, CallbackContext callbackContext) {
        try {
            boolean flag = data.getBoolean(0);
            mPushIOManager.setInAppFetchEnabled(flag);

        } catch (JSONException e) {
            Log.v(TAG, "Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
        }
    }

    private void setCrashLoggingEnabled(JSONArray data, CallbackContext callbackContext) {
        try {
            boolean flag = data.getBoolean(0);
            mPushIOManager.setCrashLoggingEnabled(flag);
            callbackContext.success();

        } catch (JSONException e) {
            Log.v(TAG, "Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
        }
    }

    private void isCrashLoggingEnabled(JSONArray data, CallbackContext callbackContext) {
        boolean value = mPushIOManager.isCrashLoggingEnabled();
        callbackContext.success(String.valueOf(value));
    }

    private void setDeviceToken(JSONArray data, CallbackContext callbackContext) {
        String deviceToken = data.optString(0);
        mPushIOManager.setDeviceToken(deviceToken);
    }

    private void setMessageCenterBadgingEnabled(JSONArray data, CallbackContext callbackContext) {
        boolean flag = data.optBoolean(0);
        mPushIOManager.setMessageCenterBadgingEnabled(flag);
    }

    private void setBadgeCount(JSONArray data, CallbackContext callbackContext) {
        int badgeCount = data.optInt(0);
        boolean forceSetBadge = data.optBoolean(1);
        mPushIOManager.setBadgeCount(badgeCount, forceSetBadge, new PIOBadgeSyncListener() {
            @Override
            public void onBadgeSyncedSuccess(String s) {
                callbackContext.success(s);
            }

            @Override
            public void onBadgeSyncedFailure(String s) {
                callbackContext.error(s);
            }
        });
    }

    private void getBadgeCount(JSONArray data, CallbackContext callbackContext) {
        int count = mPushIOManager.getBadgeCount();
        callbackContext.success(count);
    }

    private void resetBadgeCount(JSONArray data, CallbackContext callbackContext) {
        boolean forceSetBadge = data.optBoolean(0);
        mPushIOManager.resetBadgeCount(forceSetBadge, new PIOBadgeSyncListener() {
            @Override
            public void onBadgeSyncedSuccess(String s) {
                callbackContext.success(s);
            }

            @Override
            public void onBadgeSyncedFailure(String s) {
                callbackContext.error(s);
            }
        });
    }

    private void resetMessageCenter(JSONArray data, CallbackContext callbackContext) {
        mPushIOManager.resetMessageCenter();
        callbackContext.success();
    }

    private void onMessageCenterViewVisible(JSONArray data, CallbackContext callbackContext) {
        try {
            mPushIOManager.onMessageCenterViewVisible();
            callbackContext.success();

        } catch (PIOMCMessageException e) {
            Log.v(TAG, "Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
        }

    }

    private void onMessageCenterViewFinish(JSONArray data, CallbackContext callbackContext) {
        try {
            mPushIOManager.onMessageCenterViewFinish();
            callbackContext.success();

        } catch (PIOMCMessageException e) {
            Log.v(TAG, "Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
        }
    }

    private void trackMessageCenterOpenEngagement(JSONArray data, CallbackContext callbackContext) {
        String messageId = data.optString(0);
        mPushIOManager.trackMessageCenterOpenEngagement(messageId);
        callbackContext.success();

    }

    private void trackMessageCenterDisplayEngagement(JSONArray data, CallbackContext callbackContext) {
        String messageId = data.optString(0);
        mPushIOManager.trackMessageCenterDisplayEngagement(messageId);
        callbackContext.success();

    }

    private void clearInAppMessages(JSONArray data, CallbackContext callbackContext) {
        mPushIOManager.clearInAppMessages();
        callbackContext.success();
    }

    private void clearInteractiveNotificationCategories(JSONArray data, CallbackContext callbackContext) {
        mPushIOManager.clearInteractiveNotificationCategories();
        callbackContext.success();
    }

    private void deleteInteractiveNotificationCategory(JSONArray data, CallbackContext callbackContext) {
        String categoryId = data.optString(0);
        mPushIOManager.deleteInteractiveNotificationCategory(categoryId);
        callbackContext.success();
    }

    private void getInteractiveNotificationCategory(JSONArray data, CallbackContext callbackContext) {
        String categoryId = data.optString(0);

        if (!TextUtils.isEmpty(categoryId)) {
            PIOInteractiveNotificationCategory category = mPushIOManager.getInteractiveNotificationCategory(categoryId);

            if (category != null) {
                callbackContext.success(PushIOManagerPluginUtils.notificationCategoryAsJson(category));
            } else {
                callbackContext.error("Invalid categoryID passed");
            }
        } else {
            callbackContext.error("categoryID cannot be empty");
        }
    }

    private void addInteractiveNotificationCategory(JSONArray data, CallbackContext callbackContext) {
        PIOInteractiveNotificationCategory category = PushIOManagerPluginUtils.notificationCategoryFromJsonArray(data);
        if (category != null) {
            mPushIOManager.addInteractiveNotificationCategory(category);
            callbackContext.success();
        } else {
            callbackContext.error("Invalid category passed");
        }
    }

    private void onGeoRegionEntered(JSONArray data, CallbackContext callbackContext) {
        PIOGeoRegion geoRegion = PushIOManagerPluginUtils.geoRegionFromJsonArray(data,
                PIORegionEventType.GEOFENCE_ENTRY);
        if (geoRegion != null) {
            mPushIOManager.onGeoRegionEntered(geoRegion, new PIORegionCompletionListener() {
                @Override
                public void onRegionReported(String regionId, PIORegionEventType pioRegionEventType,
                        PIORegionException e) {
                    if (e == null) {
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("regionID", regionId);
                            jsonObject.put("regionType", pioRegionEventType);

                            callbackContext.success(jsonObject);
                        } catch (JSONException ex) {
                            Log.v(TAG, "Exception: " + ex.getMessage());
                            callbackContext.error(ex.getMessage());
                        }
                    } else {
                        callbackContext.error(e.getErrorMessage());
                    }

                }
            });
        } else {
            callbackContext.error("Error reading geoRegion jsonArray");
        }

    }

    private void onGeoRegionExited(JSONArray data, CallbackContext callbackContext) {
        PIOGeoRegion geoRegion = PushIOManagerPluginUtils.geoRegionFromJsonArray(data,
                PIORegionEventType.GEOFENCE_EXIT);
        if (geoRegion != null) {
            mPushIOManager.onGeoRegionExited(geoRegion, new PIORegionCompletionListener() {
                @Override
                public void onRegionReported(String regionId, PIORegionEventType pioRegionEventType,
                        PIORegionException e) {
                    if (e == null) {
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("regionID", regionId);
                            jsonObject.put("regionType", pioRegionEventType);
                            callbackContext.success(jsonObject);
                        } catch (JSONException ex) {
                            Log.v(TAG, "Exception: " + ex.getMessage());
                            callbackContext.error(ex.getMessage());
                        }
                    } else {
                        callbackContext.error(e.getErrorMessage());
                    }

                }
            });
        } else {
            callbackContext.error("Error reading geoRegion jsonArray");
        }

    }

    private void onBeaconRegionEntered(JSONArray data, CallbackContext callbackContext) {
        PIOBeaconRegion beaconRegion = PushIOManagerPluginUtils.beaconRegionFromJsonArray(data,
                PIORegionEventType.BEACON_ENTRY);
        if (beaconRegion != null) {
            mPushIOManager.onBeaconRegionEntered(beaconRegion, new PIORegionCompletionListener() {
                @Override
                public void onRegionReported(String regionId, PIORegionEventType pioRegionEventType,
                        PIORegionException e) {
                    if (e == null) {
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("regionID", regionId);
                            jsonObject.put("regionType", pioRegionEventType);
                            callbackContext.success(jsonObject);
                        } catch (JSONException ex) {
                            Log.v(TAG, "Exception: " + ex.getMessage());
                            callbackContext.error(ex.getMessage());
                        }
                    } else {
                        callbackContext.error(e.getErrorMessage());
                    }

                }
            });
        } else {
            callbackContext.error("Error reading geoRegion jsonArray");
        }

    }

    private void onBeaconRegionExited(JSONArray data, CallbackContext callbackContext) {
        PIOBeaconRegion beaconRegion = PushIOManagerPluginUtils.beaconRegionFromJsonArray(data,
                PIORegionEventType.BEACON_EXIT);
        if (beaconRegion != null) {
            mPushIOManager.onBeaconRegionExited(beaconRegion, new PIORegionCompletionListener() {
                @Override
                public void onRegionReported(String regionId, PIORegionEventType pioRegionEventType,
                        PIORegionException e) {
                    if (e == null) {
                        try {
                            JSONObject jsonObject = new JSONObject();

                            jsonObject.put("regionID", regionId);
                            jsonObject.put("regionType", pioRegionEventType);
                            callbackContext.success(jsonObject);
                        } catch (JSONException ex) {
                            Log.v(TAG, "Exception: " + ex.getMessage());
                            callbackContext.error(ex.getMessage());
                        }
                    } else {
                        callbackContext.error(e.getErrorMessage());
                    }

                }
            });
        } else {
            callbackContext.error("Error reading beaconRegion jsonArray");
        }

    }

    private void setExecuteRsysWebUrl(JSONArray data, CallbackContext callbackContext) {
        boolean executeRsysWebUrl = data.optBoolean(0);
        mPushIOManager.setExecuteRsysWebUrl(executeRsysWebUrl, new PIORsysIAMHyperlinkListener() {
            @Override
            public void onSuccess(String requestUrl, String deeplinkUrl, String weblinkUrl) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("requestUrl", requestUrl);
                    jsonObject.putOpt("deeplinkUrl", deeplinkUrl);
                    jsonObject.putOpt("weblinkUrl", weblinkUrl);
                    callbackContext.success(jsonObject);

                } catch (JSONException e) {
                    Log.v(TAG, "Exception: " + e.getMessage());
                    callbackContext.error(e.getMessage());
                }
            }

            @Override
            public void onFailure(String requestUrl, String errorReason) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("requestUrl", requestUrl);
                    jsonObject.put("errorReason", errorReason);
                    callbackContext.success(jsonObject);

                } catch (JSONException e) {
                    Log.v(TAG, "Exception: " + e.getMessage());
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }

    private void getExecuteRsysWebUrl(JSONArray data, CallbackContext callbackContext) {
        boolean result = mPushIOManager.getExecuteRsysWebUrl();
        callbackContext.success(String.valueOf(result));
    }

    private void getConversionUrl(JSONArray data, CallbackContext callbackContext) {
        String result = mPushIOManager.getConversionUrl();
        callbackContext.success(result);
    }

    private void getRIAppId(JSONArray data, CallbackContext callbackContext) {
        String result = mPushIOManager.getRIAppId();
        callbackContext.success(result);
    }

    private void getEngagementTimestamp(JSONArray data, CallbackContext callbackContext) {
        String result = mPushIOManager.getEngagementTimestamp();
        callbackContext.success(result);
    }

    private void getEngagementMaxAge(JSONArray data, CallbackContext callbackContext) {
        long result = mPushIOManager.getEngagementMaxAge();
        callbackContext.success(String.valueOf(result));
    }

    private void resetEngagementContext(JSONArray data, CallbackContext callbackContext) {
        mPushIOManager.resetEngagementContext();
        callbackContext.success();
    }

    private void setDelayRichPushDisplay(JSONArray data, CallbackContext callbackContext) {
        try {
            boolean flag = data.getBoolean(0);
            mPushIOManager.delayRichPushDisplay(flag);
            callbackContext.success();

        } catch (JSONException e) {
            Log.v(TAG, "Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
        }
    }

    private void isRichPushDelaySet(JSONArray data, CallbackContext callbackContext) {
        boolean result = mPushIOManager.isRichPushDelaySet();
        callbackContext.success(String.valueOf(result));
    }

    private void showRichPushMessage(JSONArray data, CallbackContext callbackContext) {
        mPushIOManager.showRichPushMessage();
        callbackContext.success();
    }

    private void trackConversionEvent(JSONArray data, CallbackContext callbackContext) {

        try {          
            JSONObject propertiesObject = data.getJSONObject(0);

            PIOConversionEvent conversionEvent = new PIOConversionEvent();
            conversionEvent.setConversionType(Integer.parseInt(propertiesObject.getString("conversionType")));
            conversionEvent.setOrderId(propertiesObject.getString("orderId"));
            conversionEvent.setOrderAmount(propertiesObject.getDouble("orderTotal"));
            conversionEvent.setOrderQuantity(propertiesObject.getInt("orderQuantity"));

            if (propertiesObject.has("customProperties")) {
                JSONObject customPropertiesObject = propertiesObject.optJSONObject("customProperties");

                if (customPropertiesObject != null) {
                    conversionEvent.setProperties(PushIOManagerPluginUtils.toMapStr(customPropertiesObject));
                }
            }

            mPushIOManager.trackConversionEvent(conversionEvent, new PIOConversionListener() {
                @Override
                public void onSuccess() {
                    callbackContext.success();
                }

                @Override
                public void onFailure(Exception e) {
                    callbackContext.error(e.getMessage());
                }
            });
        } catch (Exception e) {
            Log.v(TAG, "Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
        }

    }

    private void setNotificationSmallIconColor(JSONArray data, CallbackContext callbackContext) {
        try{
            String colorHex = data.getString(0);
            if (!TextUtils.isEmpty(colorHex)) {
                final int color = Color.parseColor(colorHex);
                mPushIOManager.setNotificationSmallIconColor(color);
            }
        } catch (JSONException e) {
            Log.v(TAG, "Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
        }
    }
    
    private void setInAppMessageBannerHeight(JSONArray data, CallbackContext callbackContext){
        try{
            String colorHex = data.getString(0);
            if (!TextUtils.isEmpty(colorHex)) {
                final int color = Color.parseColor(colorHex);
                mPushIOManager.setNotificationSmallIconColor(color);
            }
        } catch (JSONException e) {
            Log.v(TAG, "Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
        }
    }

    private void setInAppMessageBannerHeight(JSONArray data, CallbackContext callbackContext) {
        try {
            int height = data.getInt(0);
            mPushIOManager.setInAppMessageBannerHeight(height);
        } catch (Exception e) {
            Log.v(TAG, "Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
        }
    }

    private void setNotificationSmallIcon(JSONArray data, CallbackContext callbackContext) {
        try {
            String resourceName = data.getString(0);
            if (!TextUtils.isEmpty(resourceName)) {

                int resourceId = mAppContext.getResources().getIdentifier(
                        resourceName, "drawable", mAppContext.getPackageName());

                if (resourceId <= 0) {
                    resourceId = mAppContext.getResources().getIdentifier(
                            resourceName, "mipmap", mAppContext.getPackageName());
                }

                if (resourceId > 0) {
                    JSONArray resourceIdArray = new JSONArray();
                    resourceIdArray.put(resourceId);
                    setDefaultSmallIcon(resourceIdArray, callbackContext);
                }
            }

        } catch (JSONException e) {
            Log.v(TAG, "Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
        }
    }

    private void setNotificationLargeIcon(JSONArray data, CallbackContext callbackContext) {

        try {
            String resourceName = data.getString(0);
            if (!TextUtils.isEmpty(resourceName)) {

                int resourceId = mAppContext.getResources().getIdentifier(
                        resourceName, "drawable", mAppContext.getPackageName());

                if (resourceId <= 0) {
                    resourceId = mAppContext.getResources().getIdentifier(
                            resourceName, "mipmap", mAppContext.getPackageName());
                }

                if (resourceId > 0) {
                    JSONArray resourceIdArray = new JSONArray();
                    resourceIdArray.put(resourceId);
                    setDefaultLargeIcon(resourceIdArray, callbackContext);
                }
            }
        } catch (JSONException e) {
            Log.v(TAG, "Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
        }
    }

    private void setNotificationSmallIcon(JSONArray data, CallbackContext callbackContext) {
        try{
            String resourceName = data.getString(0);
            if (!TextUtils.isEmpty(resourceName)) {

                int resourceId = mAppContext.getResources().getIdentifier(
                        resourceName, "drawable", mAppContext.getPackageName());

                if (resourceId <= 0) {
                    resourceId = mAppContext.getResources().getIdentifier(
                            resourceName, "mipmap", mAppContext.getPackageName());
                }

                if (resourceId > 0) {
                    JSONArray resourceIdArray = new JSONArray();
                    resourceIdArray.put(resourceId);
                    setDefaultSmallIcon(resourceIdArray, callbackContext);
                }
            }

        } catch (JSONException e) {
            Log.v(TAG, "Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
        }
    }

    private void setNotificationLargeIcon(JSONArray data, CallbackContext callbackContext) {

        try{
            String resourceName = data.getString(0);
            if (!TextUtils.isEmpty(resourceName)) {
                
                int resourceId = mAppContext.getResources().getIdentifier(
                        resourceName, "drawable", mAppContext.getPackageName());

                if (resourceId <= 0) {
                    resourceId = mAppContext.getResources().getIdentifier(
                            resourceName, "mipmap", mAppContext.getPackageName());
                }

                if (resourceId > 0) {
                    JSONArray resourceIdArray = new JSONArray();
                    resourceIdArray.put(resourceId);
                    setDefaultLargeIcon(resourceIdArray,callbackContext);
                }
            }
        } catch (JSONException e) {
            Log.v(TAG, "Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
        }
    }

    private void getInAppMessageBannerHeight(JSONArray data, CallbackContext callbackContext) {
        int  height = mPushIOManager.getInAppMessageBannerHeight();
        callbackContext.success(String.valueOf(height));
    }

    private void setStatusBarHiddenForIAMBannerInterstitial(JSONArray data, CallbackContext callbackContext) {
        try {
            boolean statusBarHidden = data.getBoolean(0);
            mPushIOManager.setStatusBarHiddenForIAMBannerInterstitial(statusBarHidden);
        } catch (Exception e) {
            Log.v(TAG, "Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
        }
    }

    private void isResponsysPush(JSONArray data, CallbackContext callbackContext){
        JSONObject remoteMessageJson = data.optJSONObject(0);

        if(remoteMessageJson != null){
            RemoteMessage remoteMessage = PushIOManagerPluginUtils.remoteMessageFromJson(remoteMessageJson);
            boolean value = mPushIOManager.isResponsysPush(remoteMessage);
            callbackContext.success(String.valueOf(value));
        }else{
            callbackContext.error("Push Payload cannot be null");
        }
    }

    private void handleMessage(JSONArray data, CallbackContext callbackContext){
        JSONObject remoteMessageJson = data.optJSONObject(0);

        if(remoteMessageJson != null){
            RemoteMessage remoteMessage = PushIOManagerPluginUtils.remoteMessageFromJson(remoteMessageJson);
            mPushIOManager.handleMessage(remoteMessage);
            callbackContext.success();
        }else{
            callbackContext.error("Push Payload cannot be null");
        }
    }
    
    private void isStatusBarHiddenForIAMBannerInterstitial(JSONArray data, CallbackContext callbackContext) {
        boolean  result = mPushIOManager.isStatusBarHiddenForIAMBannerInterstitial();
        callbackContext.success(String.valueOf(result)); 
    }

    private void onMessageCenterUpdated(JSONArray data, CallbackContext callbackContext) {
        if (callbackContext != null) {
            mPushIOManager.addMessageCenterUpdateListener(new PIOMessageCenterUpdateListener() {
                @Override
                public void onUpdate(List<String> messageCenters) {
                    callbackContext.success(PushIOManagerPluginUtils.toJSONArray(messageCenters));
                }
            });
        }
    }
}
