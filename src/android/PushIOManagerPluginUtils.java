/**
 * Copyright © 2022, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
 */

package com.pushio.manager.cordova;

import android.text.TextUtils;
import android.util.Log;

import com.pushio.manager.PIOBeaconRegion;
import com.pushio.manager.PIOGeoRegion;
import com.pushio.manager.PIOMCMessage;
import com.pushio.manager.PIOInteractiveNotificationButton;
import com.pushio.manager.PIOInteractiveNotificationCategory;
import com.pushio.manager.PIORegionEventType;
import com.pushio.manager.preferences.PushIOPreference;
import com.google.firebase.messaging.RemoteMessage;
 
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class PushIOManagerPluginUtils {
    private static final String TAG = "pushio-cordova";
    
    private static final String FBM_KEY_COLLAPSE_KEY = "collapseKey";
    private static final String FBM_KEY_DATA = "data";
    private static final String FBM_KEY_MESSAGE_ID = "google.message_id";
    private static final String FBM_KEY_MESSAGE_TYPE = "messageType";
    private static final String FBM_KEY_TTL = "ttl";
    private static final String DATE_FORMAT_ISO8601 = "yyyy-MM-dd'T'HH:mm:ssZZZZZ";

    public static Map<String, Object> toMap(JSONObject jsonobj) throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();
        Iterator<String> keys = jsonobj.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = jsonobj.get(key);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    public static Map<String, String> toMapStr(JSONObject jsonobj) throws JSONException {
        Map<String, String> map = new HashMap<>();
        Iterator<String> keys = jsonobj.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            String value = jsonobj.optString(key);
            map.put(key, value);
        }
        return map;
    }

    public static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<Object>();
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }

    static JSONArray toJSONArray(List<String> list){
        JSONArray jsonArray = new JSONArray();
        
        if(list != null){
            for(String item : list){
                jsonArray.put(item);
            }
        }

        return jsonArray;
    }

    static JSONArray preferencesAsJsonArray(List<PushIOPreference> preferences) {
        JSONArray preferencesJsonArray = new JSONArray();

        if (preferences != null) {
            try {
                for (PushIOPreference preference : preferences) {
                    JSONObject preferenceObj = new JSONObject();
                    preferenceObj.put("key", preference.getKey());
                    preferenceObj.put("label", preference.getLabel());

                    final PushIOPreference.Type type = preference.getType();
                    final Object value = preference.getValue();

                    if (type == PushIOPreference.Type.STRING) {
                        preferenceObj.put("value", String.valueOf(value));

                    } else if (type == PushIOPreference.Type.NUMBER) {

                        if (value instanceof Double) {
                            preferenceObj.put("value", (Double) value);
                        } else if (value instanceof Integer) {
                            preferenceObj.put("value", (Integer) value);
                        }

                    } else if (type == PushIOPreference.Type.BOOLEAN) {
                        preferenceObj.put("value", (Boolean) value);
                    }

                    preferenceObj.put("type", type.toString());
                    preferencesJsonArray.put(preferenceObj);
                }

            } catch (JSONException e) {
                Log.v(TAG, "Exception: " + e.getMessage());
            }
        }

        return preferencesJsonArray;
    }

    public static PIOGeoRegion geoRegionFromJsonArray(JSONArray jsonArray, PIORegionEventType type) {
        PIOGeoRegion geoRegion = new PIOGeoRegion();

        if (jsonArray != null) {
            JSONObject geoRegionJSONObj = jsonArray.optJSONObject(0);

            String geofenceId = geoRegionJSONObj.optString("geofenceId");
            String geofenceName = geoRegionJSONObj.optString("geofenceName");

            if (TextUtils.isEmpty(geofenceId) || TextUtils.isEmpty(geofenceName)) {
                return null;
            }

            geoRegion.setGeofenceId(geofenceId);
            geoRegion.setGeofenceName(geofenceName);
            geoRegion.setRegionEventType(type);
            geoRegion.setZoneName(geoRegionJSONObj.optString("zoneName"));
            geoRegion.setZoneId(geoRegionJSONObj.optString("zoneId"));
            geoRegion.setSource(geoRegionJSONObj.optString("source"));
            geoRegion.setDeviceBearing(geoRegionJSONObj.optDouble("deviceBearing"));
            geoRegion.setDeviceSpeed(geoRegionJSONObj.optDouble("deviceSpeed"));
            geoRegion.setDwellTime(geoRegionJSONObj.optInt("dwellTime"));

            if (geoRegionJSONObj.optJSONObject("extra") != null) {
                JSONObject extraData = geoRegionJSONObj.optJSONObject("extra");
                Iterator<String> keys = extraData.keys();
                Map<String, String> customParams = new HashMap<>();
                while (keys.hasNext()) {
                    String key = keys.next();
                    customParams.put(key, extraData.optString(key));
                }

                geoRegion.setExtra(customParams);
            }
        }
        return geoRegion;

    }

    public static PIOBeaconRegion beaconRegionFromJsonArray(JSONArray jsonArray, PIORegionEventType type) {
        PIOBeaconRegion region = new PIOBeaconRegion();

        if (jsonArray != null) {
            JSONObject beaconRegionJSONObj = jsonArray.optJSONObject(0);

            String beaconId = beaconRegionJSONObj.optString("beaconId");
            String beaconName = beaconRegionJSONObj.optString("beaconName");

            if (TextUtils.isEmpty(beaconId) || TextUtils.isEmpty(beaconName)) {
                return null;
            }

            region.setBeaconId(beaconId);
            region.setBeaconName(beaconName);

            region.setBeaconTag(beaconRegionJSONObj.optString("beaconTag"));
            region.setBeaconProximity(beaconRegionJSONObj.optString("beaconProximity"));
            region.setiBeaconUUID(beaconRegionJSONObj.optString("iBeaconUUID"));
            region.setiBeaconMajor(beaconRegionJSONObj.optInt("iBeaconMajor"));
            region.setiBeaconMinor(beaconRegionJSONObj.optInt("iBeaconMinor"));
            region.setEddyStoneID1(beaconRegionJSONObj.optString("eddyStoneId1"));
            region.setEddyStoneID2(beaconRegionJSONObj.optString("eddyStoneId2"));

            region.setRegionEventType(type);
            region.setZoneName(beaconRegionJSONObj.optString("zoneName"));
            region.setZoneId(beaconRegionJSONObj.optString("zoneId"));
            region.setSource(beaconRegionJSONObj.optString("source"));
            region.setDwellTime(beaconRegionJSONObj.optInt("dwellTime"));

            if (beaconRegionJSONObj.optJSONObject("extra") != null) {
                JSONObject extraData = beaconRegionJSONObj.optJSONObject("extra");
                if (extraData != null) {
                    Iterator<String> keys = extraData.keys();
                    Map<String, String> customParams = new HashMap<>();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        customParams.put(key, extraData.optString(key));
                    }
                    region.setExtra(customParams);
                }
            }

        }
        return region;

    }

    static JSONObject notificationCategoryAsJson(PIOInteractiveNotificationCategory notificationCategory) {
        final JSONObject parent = new JSONObject();

        try {
            parent.put("orcl_category", notificationCategory.getCategory());

            JSONArray notificationButtonJsonArray = new JSONArray();

            List<PIOInteractiveNotificationButton> notificationButtons = notificationCategory
                    .getInteractiveNotificationButtons();

            for (PIOInteractiveNotificationButton notificationButton : notificationButtons) {
                JSONObject notificationButtonJson = new JSONObject();
                notificationButtonJson.put("id", notificationButton.getId());
                notificationButtonJson.put("action", notificationButton.getAction());
                notificationButtonJson.put("label", notificationButton.getLabel());
                notificationButtonJsonArray.put(notificationButtonJson);
            }

            parent.put("orcl_btns", notificationButtonJsonArray);

            return parent;

        } catch (Exception e) {
            return null;
        }
    }

    static PIOInteractiveNotificationCategory notificationCategoryFromJsonArray(JSONArray jsonArray) {
        final JSONObject object = jsonArray.optJSONObject(0);
        String category = object.optString("orcl_category");
        JSONArray btnArray = object.optJSONArray("orcl_btns");

        if (TextUtils.isEmpty(category) || btnArray == null) {
            return null;
        }

        PIOInteractiveNotificationCategory notificationCategory = new PIOInteractiveNotificationCategory();
        notificationCategory.setCategory(category);

        for (int i = 0; i < btnArray.length(); ++i) {
            JSONObject btnObj = btnArray.optJSONObject(i);
            if (btnObj != null) {
                PIOInteractiveNotificationButton notificationButton = new PIOInteractiveNotificationButton();
                notificationButton.setId(btnObj.optString("id"));
                notificationButton.setAction(btnObj.optString("action"));
                notificationButton.setLabel(btnObj.optString("label"));

                notificationCategory.addInteractiveNotificationButton(notificationButton);
            }
        }
        return notificationCategory;
    }

    static JSONArray messageCenterMessagesAsJSONArray(List<PIOMCMessage> messages) {
        JSONArray messagesAsJson = new JSONArray();

        try {
            if (messages != null && !messages.isEmpty()) {
                for (PIOMCMessage message : messages) {
                    JSONObject messageAsJson = new JSONObject();
                    messageAsJson.put("messageID", message.getId());
                    messageAsJson.put("subject", message.getSubject());
                    messageAsJson.put("message", message.getMessage());
                    messageAsJson.put("iconURL", message.getIconUrl());
                    messageAsJson.put("messageCenterName", message.getMessageCenterName());
                    messageAsJson.put("deeplinkURL", message.getDeeplinkUrl());
                    messageAsJson.put("richMessageHTML", message.getRichMessageHtml());
                    messageAsJson.put("richMessageURL", message.getRichMessageUrl());
                    messageAsJson.put("sentTimestamp", getDateAsString(message.getSentTimestamp()));
                    messageAsJson.put("expiryTimestamp", getDateAsString(message.getExpiryTimestamp()));

                    messagesAsJson.put(messageAsJson);
                }
            }
        } catch (JSONException e) {
            Log.v(TAG, "Exception: " + e.getMessage());
        }

        return messagesAsJson;
    }

    private static String getDateAsString(Date date) {
        if (date != null) {
            DateFormat df = new SimpleDateFormat(DATE_FORMAT_ISO8601, Locale.getDefault());
            df.setTimeZone(TimeZone.getDefault());
            return df.format(date);
        }
        return null;
    }

    static RemoteMessage remoteMessageFromJson(JSONObject obj){
        
        RemoteMessage.Builder builder = new RemoteMessage.Builder("rsys_internal");

        if (obj.has(FBM_KEY_DATA)) {

            if (obj.has(FBM_KEY_TTL)) {
                builder.setTtl(obj.optInt(FBM_KEY_TTL));
            }

            if (obj.has(FBM_KEY_MESSAGE_ID)) {
                builder.setMessageId(obj.optString(FBM_KEY_MESSAGE_ID));
            }
    
            if (obj.has(FBM_KEY_MESSAGE_TYPE)) {
                builder.setMessageType(obj.optString(FBM_KEY_MESSAGE_TYPE));
            }
    
            if (obj.has(FBM_KEY_COLLAPSE_KEY)) {
                builder.setCollapseKey(obj.optString(FBM_KEY_COLLAPSE_KEY));
            }

            JSONObject dataObj = obj.optJSONObject(FBM_KEY_DATA);

            if(dataObj != null){
                for (Iterator<String> iterator = dataObj.keys(); iterator.hasNext(); ) {
                    final String key = iterator.next();
                    builder.addData(key, dataObj.optString(key));
                }
            }
        }else{
                for (Iterator<String> iterator = obj.keys(); iterator.hasNext(); ) {    
                    final String key = iterator.next();
                    builder.addData(key, obj.optString(key));
                }
        }

        return builder.build();
    }
}
