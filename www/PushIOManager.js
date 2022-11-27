/**
 * Copyright © 2022, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
 */

/**
 * @class PushIOManager
 */
var PushIOManager = function () {
}

/**
 * Engagement types to be used with [trackEngagement()]{@link PushIOManager#trackEngagement}
 * @readonly
 * @enum {number}
 * @memberof PushIOManager
 */
PushIOManager.prototype.engagementType = {
    /** Used by SDK to record app launch via push notification. Apps should avoid using this. */
    PUSHIO_ENGAGEMENT_METRIC_LAUNCH: 1,
    /** Used by SDK to record push receipt when app is in foreground. Apps should avoid using this. */
    PUSHIO_ENGAGEMENT_METRIC_ACTIVE_SESSION: 2,
    /** User did an In-App purchase. */
    PUSHIO_ENGAGEMENT_METRIC_INAPP_PURCHASE: 3,
    /** User accessed premium content in the app. */
    PUSHIO_ENGAGEMENT_METRIC_PREMIUM_CONTENT: 4,
    /** User did a social action, for example: share, like etc. */
    PUSHIO_ENGAGEMENT_METRIC_SOCIAL: 5,
    /** User did a commerce (or physical goods) purchase in the app */
    PUSHIO_ENGAGEMENT_METRIC_PURCHASE: 7,
    /** Any other user action that doesn't fit under other engagement-types */
    PUSHIO_ENGAGEMENT_METRIC_OTHER: 6
}

/**
 * Log level; to be used with [setLogLevel()]{@link PushIOManager#setLogLevel}
 * @readonly
 * @enum {number}
 * @memberof PushIOManager
 */
PushIOManager.prototype.logLevel = {
    /** No logs will be printed. */
    NONE: 0,
    /** Logs will include only Errors level logs. */
    ERROR: cordova.platformId === 'android' ? 6 : 1,
    /** Logs will include only Info level logs. */
    INFO: cordova.platformId === 'android' ? 4 : 2,
    /** Logs will include Warning level logs. */
    WARN: cordova.platformId === 'android' ? 5 : 3,
    /** Logs will include Debug level logs. */
    DEBUG: cordova.platformId === 'android' ? 3 : 4,
    /** Logs will include Verbose level logs. */
    VERBOSE: cordova.platformId === 'android' ? 2 : 5
}


// Helper method to call the native bridge
PushIOManager.prototype.call_native = function (success, failure, name, args) {
    console.log("Native called for: " + name + " with args: " + args);

    if (args === undefined) {
        args = []
    }

    if (success === undefined) {
        success = function () { };
    }

    if (failure === undefined) {
        failure = function () { };
    }

    return cordova.exec(
        success,
        failure,
        'PushIOManagerPlugin', // native class
        name, // action name
        args); // List of arguments to the plugin
}

/**
 * Gets the API Key used by the device to register with Responsys.
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 * @memberof PushIOManager
 */
PushIOManager.prototype.getAPIKey = function (success, failure) {
    this.call_native(success, failure, "getAPIKey");
}

/**
 * Gets the Account Token used by the device to register with Responsys.
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 * @memberof PushIOManager
 */
PushIOManager.prototype.getAccountToken = function (success, failure) {
    this.call_native(success, failure, "getAccountToken");
}

/**
 * Sets the External Device Tracking ID. Useful if you have another ID for this device.
 * @param {string} edti External Device Tracking ID.
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 * @memberof PushIOManager
 */
PushIOManager.prototype.setExternalDeviceTrackingID = function (edti, success, failure) {
    this.call_native(success, failure, "setExternalDeviceTrackingID", [edti]);
}

/**
 * Gets the External Device Tracking ID.
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 * @memberof PushIOManager
 */
PushIOManager.prototype.getExternalDeviceTrackingID = function (success, failure) {
    this.call_native(success, failure, "getExternalDeviceTrackingID");
}

/**
 * Sets the Advertising ID.
 * @param {string} adid Advertising ID.
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 * @memberof PushIOManager
 */
PushIOManager.prototype.setAdvertisingID = function (adid, success, failure) {
    this.call_native(success, failure, "setAdvertisingID", [adid]);
}

/**
 * Gets the Advertising ID.
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 * @memberof PushIOManager
 */
PushIOManager.prototype.getAdvertisingID = function (success, failure) {
    this.call_native(success, failure, "getAdvertisingID");
}

/**
 * Associates this app installation with the provided userId in Responsys.
 * <br/>Generally used when the user logs in.
 * 
 * @param {string} userId User ID
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 * @memberof PushIOManager
 */
PushIOManager.prototype.registerUserId = function (userId, success, failure) {
    this.call_native(success, failure, "registerUserId", [userId]);
}

/**
 * Gets the User ID set earlier using [registerUserId]{@link PushIOManager#registerUserId}.
 * @param {function} [success] Success callback.    
 * @param {function} [failure] Failure callback.
 * @memberof PushIOManager
 */
PushIOManager.prototype.getRegisteredUserId = function (success, failure) {
    this.call_native(success, failure, "getRegisteredUserId");
}

/**
 * Removes association between this app installation and the User ID that 
 * was set earlier using [registerUserId]{@link PushIOManager#registerUserId}.
 * <br/>Generally used when the user logs out.
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 * @memberof PushIOManager
 */
PushIOManager.prototype.unregisterUserId = function (success, failure) {
    this.call_native(success, failure, "unregisterUserId");
}

/**
 * Declares a preference that will be used later with [set...Preference()]{@link PushIOManager#setStringPreference}
 * 
 * @param {string} key Unique ID for this preference.
 * @param {string} label Human-Readable description of this preference.
 * @param {string} type Data type of this preference. Possible values: 'STRING', 'NUMBER', 'BOOLEAN'.
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 * @memberof PushIOManager
 */
PushIOManager.prototype.declarePreference = function (key, label, type, success, failure) {
    this.call_native(success, failure, "declarePreference", [key, label, type]);
}

/**
 * Gets all preferences set earlier using [set...Preference()]{@link PushIOManager#setStringPreference}.
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 * @returns {Preference[]} Array of [Preference]{@link Preference} in success callback.
 * @memberof PushIOManager
 */
PushIOManager.prototype.getPreferences = function (success, failure) {
    this.call_native(success, failure, "getPreferences");
}

/**
 * Gets a single preference for the provided key.
 * @param {string} key Unique ID for this preference.
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 * @returns {Preference} Single preference in success callback.
 * @memberof PushIOManager
 */
PushIOManager.prototype.getPreference = function (key, success, failure) {
    this.call_native(success, failure, "getPreference", [key]);
}

/**
 * Saves the key/value along with the label provided earlier in [declarePreference]{@link PushIOManager#declarePreference}
 * 
 * @param {string} key Unique ID for this preference.
 * @param {string} value Value of type String.
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 * @memberof PushIOManager
 */
PushIOManager.prototype.setStringPreference = function (key, value, success, failure) {
    this.call_native(success, failure, "setStringPreference", [key, value]);
}

/**
 * Saves the key/value along with the label provided earlier in [declarePreference]{@link PushIOManager#declarePreference}
 * 
 * @param {string} key Unique ID for this preference.
 * @param {number} value Value of type Number.
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 * @memberof PushIOManager
 */
PushIOManager.prototype.setNumberPreference = function (key, value, success, failure) {
    this.call_native(success, failure, "setNumberPreference", [key, value]);
}

/**
 * Saves the key/value along with the label provided earlier in [declarePreference]{@link PushIOManager#declarePreference}
 * 
 * @param {string} key Unique ID for this preference.
 * @param {boolean} value Value of type Boolean.
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 * @memberof PushIOManager
 */
PushIOManager.prototype.setBooleanPreference = function (key, value, success, failure) {
    this.call_native(success, failure, "setBooleanPreference", [key, value]);
}

/**
 * Removes preference data for the given key.
 * 
 * @param {string} key Unique ID for this preference.
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 * @memberof PushIOManager
 */
PushIOManager.prototype.removePreference = function (key, success, failure) {
    this.call_native(success, failure, "removePreference", [key]);
}

/**
 * Removes all preference data.
 * 
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 * @memberof PushIOManager
 */
PushIOManager.prototype.clearAllPreferences = function (success, failure) {
    this.call_native(success, failure, "clearAllPreferences");
}

PushIOManager.prototype.setNotificationsStacked = function (isNotificationStacked, success, failure) {
    if (cordova.platformId === 'android') {
        this.call_native(success, failure, "setNotificationsStacked", [isNotificationStacked]);
    } else {
        console.log("Not supported in iOS.");
    }
}

PushIOManager.prototype.getNotificationStacked = function (success, failure) {
    if (cordova.platformId === 'android') {
        this.call_native(success, failure, "getNotificationStacked");
    } else {
        console.log("Not supported in iOS.");
    }
}

/**
 * Records pre-defined and custom events.<br/>You can set extra properties specific to this event via the properties parameter.
 * 
 * @param {string} eventName
 * @param {object} properties Custom data.
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 * @memberof PushIOManager
 */
PushIOManager.prototype.trackEvent = function (eventName, properties, success, failure) {
    this.call_native(success, failure, "trackEvent", [eventName, properties]);
}

/**
 * Fetches messages for the given message center.
 * 
 * @param {string} messageCenter 
 * @param {function(messageCenter, messages)} [success] Success callback.
 * @param {string} success.messageCenter 
 * @param {MessageCenterMessage[]} success.messages
 * @param {function(messageCenter, errorReason)} [failure] Failure callback.
 * @param {string} failure.messageCenter 
 * @param {string} failure.errorReason
 * @memberof PushIOManager
 */
PushIOManager.prototype.fetchMessagesForMessageCenter = function (messageCenter, success, failure) {
    this.call_native(success, failure, "fetchMessagesForMessageCenter", [messageCenter]);
}

/**
 * Sends push engagement information to Responsys.
 * 
 * @param {engagementType} metric One of [engagementType]{@link PushIOManager#engagementType}
 * @param {object=} properties Custom data to be sent along with this request.
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 * @memberof PushIOManager
 */
PushIOManager.prototype.trackEngagement = function (metric, properties, success, failure) {
    if (cordova.platformId === 'android') {
        this.call_native(success, failure, "trackEngagement", [metric, properties]);
    } else {
        var value = ((metric < 6) ? (metric - 1) : metric);
        this.call_native(success, failure, "trackEngagement", [value, properties]);
    }
}

/**
 * Sets the log level. 
 *
 * @param {number} logLevel
 */
PushIOManager.prototype.setLogLevel = function (logLevel, success, failure) {
    this.call_native(success, failure, "setLogLevel", [logLevel]);
}
/**
 * Sets delay in registration. 
 *
 * @param {boolean} delayRegistration
 */
 PushIOManager.prototype.setDelayRegistration = function (delayRegistration, success, failure) {
    if (cordova.platformId === 'ios') {
       this.call_native(success, failure, "setDelayRegistration", [delayRegistration]);
    } else {
        console.log("Not supported in android.");
    }
}

/**
 * This api provides the status, if `setDelayRegistration` is enabled of not. 
 * 
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.isDelayRegistration = function (success, failure) {
    if (cordova.platformId === 'ios') {
       this.call_native(success, failure, "isDelayRegistration");
    } else {
        console.log("Not supported in android.");
    }
    
}

/**
 * @param {boolean} isLoggingEnabled
 * @param {function} [success] Success callback with boolean value. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.setLoggingEnabled = function (isLoggingEnabled, success, failure) {
    this.call_native(success, failure, "setLoggingEnabled", [isLoggingEnabled]);
}

/**
 * @param {string} apiKey
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.overwriteApiKey = function (apiKey, success, failure) {
    if (cordova.platformId === 'android') {
        this.call_native(success, failure, "overwriteApiKey", [apiKey]);
    } else {
        console.log("Not supported in iOS.");
    }
}

/**
 * @param {string} accountToken
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.overwriteAccountToken = function (accountToken, success, failure) {
    if (cordova.platformId === 'android') {
        this.call_native(success, failure, "overwriteAccountToken", [accountToken]);
    } else {
        console.log("Not supported in iOS.");
    }
}

/**
 * Configures the SDK using the provided config file name.
 * 
 * <br/><br/>For Android, the file should be placed in the android <i>src/main/assets</i> directory
 * 
 * @param {string} fileName A valid filename.
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.configure = function (fileName, success, failure) {
    this.call_native(success, failure, "configure", [fileName]);
}

/**
 * Registers this app installation with Responsys.
 * 
 * @param {boolean} useLocation Whether to send location data along with the registration request. Passing `true` will show the default system location permission dialog prompt.
 * (User location is not available on iOS platform.)
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.registerApp = function (useLocation, success, failure) {
    this.call_native(success, failure, "registerApp", [useLocation]);
}

/**
 * Asks user permissions for all push notifications types. i.e.: Sound/Badge/Alert types. 
 * 
 * Only available on iOS platform.
 *
 * @param {function} [success] Success callback.
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.registerForAllRemoteNotificationTypes = function (success, failure) {
    this.call_native(success, failure, "registerForAllRemoteNotificationTypes");
}

/**
 * Asks user permissions for all push notifications types. i.e.: Sound/Badge/Alert types. You can pass the notification categories definitions to register. 
 * 
 * Only available on iOS platform.
 *
 * @param {InteractiveNotificationCategory[]} categories Contains the notification categories definitions.
 * @param {function} [success] Success callback.
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.registerForAllRemoteNotificationTypesWithCategories = function (categories, success, failure) {
    this.call_native(success, failure, "registerForAllRemoteNotificationTypesWithCategories", [categories]);
}



/**
* Asks user permissions for all push notifications types. i.e.: Sound/Badge/Alert types.
* 
* If readyForRegistrationCompHandler is not set, then provided completionHandler is assigned to it, to let application have access when SDK receives deviceToken.
*
* Only available on iOS platform.
*
* @param {int} authOptions Notification auth types i.e.: Sound/Badge/Alert.
* @param {InteractiveNotificationCategory[]} categories Contains the notification categories definitions.
* @param {function} [success] Success callback.
* @param {function} [failure] Failure callback.
*/
PushIOManager.prototype.registerForNotificationAuthorizations = function (authOptions, categories, success, failure) {
    this.call_native(success, failure, "registerForNotificationAuthorizations", [authOptions, categories]);
}


/**
 * Unregisters this app installation with Responsys. This will prevent the app from receiving push notifications.
 * 
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.unregisterApp = function (success, failure) {
    this.call_native(success, failure, "unregisterApp");
}

/**
 * Gets the Responsys Device ID.
 * 
 * @param {function} [success] Success callback with device ID value. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.getDeviceID = function (success, failure) {
    this.call_native(success, failure, "getDeviceID");
}

/**
 * Gets the Responsys SDK version.
 * 
 * @param {function} [success] Success callback with the SDK version value. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.getLibVersion = function (success, failure) {
    this.call_native(success, failure, "getLibVersion");
}

/**
 * Sets the small icon used in notification display.
 * 
 * @param {int} icon Resource ID of the icon.
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.setDefaultSmallIcon = function (icon, success, failure) {
    if (cordova.platformId === 'android') {
        this.call_native(success, failure, "setDefaultSmallIcon", [icon]);
    } else {
        console.log("Not supported in iOS.");
    }
}

/**
 * Sets the large icon used in notification display.
 * 
 * @param {int} icon Resource ID of the icon.
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.setDefaultLargeIcon = function (icon, success, failure) {
    if (cordova.platformId === 'android') {
        this.call_native(success, failure, "setDefaultLargeIcon", [icon]);
    } else {
        console.log("Not supported in iOS.");
    }
}

/**
 * @param {function} [success] Success callback with boolean value. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.isMessageCenterEnabled = function (success, failure) {
    this.call_native(success, failure, "isMessageCenterEnabled");
}

/** 
 * @param {boolean} messageCenterEnabled
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.setMessageCenterEnabled = function (messageCenterEnabled, success, failure) {
    this.call_native(success, failure, "setMessageCenterEnabled", [messageCenterEnabled]);
}

/**
 * Fetches rich content for the given message ID.
 * 
 * @param {string} messageID
 * @param {function(messageId, richContent)} [success] Success callback. 
 * @param {string} success.messageId
 * @param {string} success.richContent
 * @param {function(messageId, errorReason)} [failure] Failure callback.
 * @param {string} failure.messageId
 * @param {string} failure.errorReason
 */
PushIOManager.prototype.fetchRichContentForMessage = function (messageID, success, failure) {
    this.call_native(success, failure, "fetchRichContentForMessage", [messageID]);
}

/**
 * @param {boolean} inAppFetchEnabled
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.setInAppFetchEnabled = function (inAppFetchEnabled, success, failure) {
    this.call_native(success, failure, "setInAppFetchEnabled", [inAppFetchEnabled]);
}

/**
 * @param {boolean} crashLoggingEnabled
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.setCrashLoggingEnabled = function (crashLoggingEnabled, success, failure) {
    this.call_native(success, failure, "setCrashLoggingEnabled", [crashLoggingEnabled]);
}

/**
 * @param {function} [success] Success callback with boolean value. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.isCrashLoggingEnabled = function (success, failure) {
    this.call_native(success, failure, "isCrashLoggingEnabled");
}

/**
 * @param {string} deviceToken
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.setDeviceToken = function (deviceToken, success, failure) {
    if (cordova.platformId === 'android') {
        this.call_native(success, failure, "setDeviceToken", [deviceToken]);
    } else {
        console.log("Not supported in iOS.");
    }
}

/**
 * @param {boolean} messageCenterBadgingEnabled
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.setMessageCenterBadgingEnabled = function (messageCenterBadgingEnabled, success, failure) {
    if (cordova.platformId === 'android') {
        this.call_native(success, failure, "setMessageCenterBadgingEnabled", [messageCenterBadgingEnabled]);
    } else {
        console.log("Not supported in iOS.");
    }
}

/**
 * Sets the badge count on app icon for the no. of Message Center messages.
 * 
 * @param {number} badgeCount
 * @param {boolean} forceSetBadge Force a server-sync for the newly set badge count.
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.setBadgeCount = function (badgeCount, forceSetBadge, success, failure) {
    this.call_native(success, failure, "setBadgeCount", [badgeCount, forceSetBadge]);
}

/**
 * Gets the current badge count for Message Center messages.
 * 
 * @param {function} [success] Success callback as a number value. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.getBadgeCount = function (success, failure) {
    this.call_native(success, failure, "getBadgeCount");
}

/**
 * Resets the badge count for Message Center messages.<br/>This is equivalent to calling [setBadgeCount(0, true)]{@link PushIOManager#setsetBadgeCount}
 * 
 * @param {boolean} forceSetBadge Force a server-sync for the newly set badge count.
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.resetBadgeCount = function (forceSetBadge, success, failure) {
    this.call_native(success, failure, "resetBadgeCount", [forceSetBadge]);
}

/**
 * Removes all Message Center messages from the SDK's cache.<br/><br/>This does not affect your local cache of the messages.
 * 
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.resetMessageCenter = function (success, failure) {
    this.call_native(success, failure, "resetMessageCenter");
}

/**
 * Informs the SDK that the Message Center view is visible.
 * 
 * <br/><br/>This must be used along with [onMessageCenterViewFinish]{@link PushIOManager#onMessageCenterViewFinish} to track Message Center message displays.
 * 
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.onMessageCenterViewVisible = function (success, failure) {
    this.call_native(success, failure, "onMessageCenterViewVisible");
}

/**
 * Informs the SDK that the Message Center view is no longer visible.
 * 
 * <br/><br/>This must be used along with [onMessageCenterViewVisible]{@link PushIOManager#onMessageCenterViewVisible} to track Message Center message displays.
 * 
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.onMessageCenterViewFinish = function (success, failure) {
    this.call_native(success, failure, "onMessageCenterViewFinish");
}

/**
 * Sends Message Center message engagement to Responsys.
 * 
 * <br/><br/>This should be called when the message-detail view is visible to the user.
 * 
 * @param {string} messageID
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.trackMessageCenterOpenEngagement = function (messageID, success, failure) {
    this.call_native(success, failure, "trackMessageCenterOpenEngagement", [messageID]);
}

/**
 * Sends Message Center message engagement to Responsys.
 * 
 * <br/><br/>This should be called when the message-list view is visible to the user.
 * 
 * @param {string} messageID
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.trackMessageCenterDisplayEngagement = function (messageID, success, failure) {
    this.call_native(success, failure, "trackMessageCenterDisplayEngagement", [messageID]);
}

/**
 * Removes all In-App messages from the SDK's cache.
 * 
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.clearInAppMessages = function (success, failure) {
    this.call_native(success, failure, "clearInAppMessages");
}

/**
 * Removes all app-defined Interactive Notification categories from the SDK's cache.
 * 
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.clearInteractiveNotificationCategories = function (success, failure) {
    this.call_native(success, failure, "clearInteractiveNotificationCategories");
}

/**
 * Removes app-defined Interactive Notification category.
 * 
 * @param {string} categoryID
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.deleteInteractiveNotificationCategory = function (categoryID, success, failure) {
    this.call_native(success, failure, "deleteInteractiveNotificationCategory", [categoryID]);
}

/**
 * Gets a single Interactive Notification category for the given category ID.
 * 
 * @param {string} categoryID
 * @param {function(orcl_category, orcl_btns)} [success] Success callback.
 * @param {string} success.orcl_category
 * @param {InteractiveNotificationButton[]} success.orcl_btns
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.getInteractiveNotificationCategory = function (categoryID, success, failure) {
    if (cordova.platformId === 'android') {
        this.call_native(success, failure, "getInteractiveNotificationCategory", [categoryID]);
    } else {
        console.log("Not supported in iOS.");
    }
}

/**
 * Adds a new app-defined Interactive Notification category.
 * 
 * @param {InteractiveNotificationCategory} notificationCategory
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.addInteractiveNotificationCategory = function (notificationCategory, success, failure) {
    if (cordova.platformId === 'android') {
        this.call_native(success, failure, "addInteractiveNotificationCategory", [notificationCategory]);
    } else {
        console.log("Not supported in iOS.");
    }
}

/**
 * Returns `true` if the given push notification payload is from Responsys, `false` otherwise.
 * 
 * @param {RemoteMessage} remoteMessage
 * @param {function} [success] Success callback as a boolean value. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.isResponsysPush = function (remoteMessage, success, failure) {
    this.call_native(success, failure, "isResponsysPush", [remoteMessage]);
}

/**
 * Request the SDK to process the given push notification payload.
 * 
 * @param {RemoteMessage} remoteMessage
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.handleMessage = function (remoteMessage, success, failure) {
    if (cordova.platformId === 'android') {
        this.call_native(success, failure, "handleMessage", [remoteMessage]);
    } else {
        console.log("Not supported in iOS.");
    }
}

/**
 * Informs the SDK that the user has entered a geofence.
 * 
 * @param {GeoRegion} region
 * @param {function(regionID, regionType)} [success] Success callback. 
 * @param {string} success.regionID
 * @param {string} success.regionType
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.onGeoRegionEntered = function (region, success, failure) {
    this.call_native(success, failure, "onGeoRegionEntered", [region]);
}

/**
 * Informs the SDK that the user has exited a geofence.
 * 
 * @param {GeoRegion} region
 * @param {function(regionID, regionType)} [success] Success callback. 
 * @param {string} success.regionID
 * @param {string} success.regionType
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.onGeoRegionExited = function (region, success, failure) {
    this.call_native(success, failure, "onGeoRegionExited", [region]);
}

/**
 * Informs the SDK that the user has entered a beacon region.
 * 
 * @param {BeaconRegion} region
 * @param {function(regionID, regionType)} [success] Success callback. 
 * @param {string} success.regionID
 * @param {string} success.regionType
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.onBeaconRegionEntered = function (region, success, failure) {
    this.call_native(success, failure, "onBeaconRegionEntered", [region]);
}

/**
 * Informs the SDK that the user has exited a beacon region.
 * 
 * @param {BeaconRegion} region
 * @param {function(regionID, regionType)} [success] Success callback. 
 * @param {string} success.regionID
 * @param {string} success.regionType
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.onBeaconRegionExited = function (region, success, failure) {
    this.call_native(success, failure, "onBeaconRegionExited", [region]);
}

PushIOManager.prototype.setExecuteRsysWebUrl = function (flag, success, failure) {
    this.call_native(success, failure, "setExecuteRsysWebUrl", [flag]);
}

PushIOManager.prototype.getExecuteRsysWebUrl = function (success, failure) {
    this.call_native(success, failure, "getExecuteRsysWebUrl");
}

/**
 * @param {function} [success] Success callback as a string value. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.getConversionUrl = function (success, failure) {
    if (cordova.platformId === 'android') {
        this.call_native(success, failure, "getConversionUrl");
    } else {
        console.log("Not supported in iOS.");
    }
}

/**
 * @param {function} [success] Success callback as a number value. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.getRIAppId = function (success, failure) {
    if (cordova.platformId === 'android') {
        this.call_native(success, failure, "getRIAppId");
    } else {
        console.log("Not supported in iOS.");
    }

}

/**
 * @param {function} [success] Success callback as a string value. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.getEngagementTimestamp = function (success, failure) {
    this.call_native(success, failure, "getEngagementTimestamp");
}

/**
 * @param {function} [success] Success callback as a number value. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.getEngagementMaxAge = function (success, failure) {
    this.call_native(success, failure, "getEngagementMaxAge");
}

/**
 * Removes push engagement related data for a session.
 * 
 * <br/><br/>This will prevent further engagements from being reported until the app is opened again via a push notification.
 * 
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.resetEngagementContext = function (success, failure) {
    this.call_native(success, failure, "resetEngagementContext");
}

/**
 * Gets the deeplink/weblink URL, if the app was opened via a Responsys deeplink. 
 * 
 * Only for Android. For iOS use the document listener.
 * 
 * <br/><br/>This should be called everytime the app comes to the foreground.
 * 
 * @param {function(deepLinkURL, webLinkURL)} [success] Success callback. 
 * @param {string} success.deepLinkURL
 * @param {string} success.webLinkURL
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.onDeepLinkReceived = function (success, failure) {
    if (cordova.platformId === 'android') {
        this.call_native(success, failure, "onDeepLinkReceived");
    } else {
        console.log("Not supported in iOS. Please check docs for further information.");
    }
}

/**
 * Seting `true` this method will delay te rich push messages until `showRichPushMessage` API is called. 
 * 
 * Use this method when you are displaying intermediate screens like Login/Onboarding Screen.
 *  
 * @param {boolean} enabled Value of type Boolean.
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.setDelayRichPushDisplay = function (enabled, success, failure) {
    this.call_native(success, failure, "setDelayRichPushDisplay", [enabled]);
}
/**
 * Call this API to display rich push messages if they are being delayed with `setDelayRichPushDisplay`. 
 * 
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.showRichPushMessage = function (success, failure) {
    this.call_native(success, failure, "showRichPushMessage");
}

/**
 * This api provides the status, if `setDelayRichPushDisplay` is enabled of not. 
 * 
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.isRichPushDelaySet = function (success, failure) {
    this.call_native(success, failure, "isRichPushDelaySet");
}


/**
 * Call this API to intercept deep links/Open URLs sent by Responsys. 
 * You can intercept the URLs sent by Responsys Open URL and overide SDK default behaviour.
 * 
 * @param {boolean} enabled Value of type Boolean.
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback.
 */
PushIOManager.prototype.setInterceptOpenURL = function (enabled, success, failure) {
    this.call_native(success, failure, "setInterceptOpenURL", [enabled]);
}

/**
 * Tracks the conversions for PUSHIO_ENGAGEMENT_METRIC_INAPP_PURCHASE and PUSHIO_ENGAGEMENT_METRIC_PURCHASE events.
 * 
 * @param {ConversionEvent} event
 * @param {function} [success] Success callback. 
 * @param {function} [failure] Failure callback. 
 */
PushIOManager.prototype.trackConversionEvent = function (event, success, failure) {
    if (cordova.platformId === 'ios') {
        event["conversionType"] = ((event["conversionType"] < 6) ? (event["conversionType"] - 1) : event["conversionType"]);
    }

    this.call_native(success, failure, "trackConversionEvent", [event]);
}

/**
 * Sets the given color to the small icon in push notifications.
 * 
 * @param {string} color as Hex String
 */
PushIOManager.prototype.setNotificationSmallIconColor = function (color, success, failure) {
    if (cordova.platformId === 'android') {
        this.call_native(success, failure, "setNotificationSmallIconColor", [color]);
    } else {
        console.log("API not supported");
    }
}

/**
 * Sets the given icon as the small icon in push notifications.
 * 
 * @param {string} resourceName Name of the resource in drawable/mipmap folder, without the file extension.
 */
PushIOManager.prototype.setNotificationSmallIcon = function (resourceName, success, failure) {
    if (cordova.platformId === 'android') {
        this.call_native(success, failure, "setNotificationSmallIcon", [resourceName]);
    } else {
        console.log("API not supported");
    }
}

/**
 * Sets the given icon as the large icon in push notifications.
 * 
 * @param {string} resourceName Name of the resource in drawable/mipmap folder, without the file extension.
 */
PushIOManager.prototype.setNotificationLargeIcon = function (resourceName, success, failure) {
    if (cordova.platformId === 'android') {
        this.call_native(success, failure, "setNotificationLargeIcon", [resourceName]);
    } else {
        console.log("API not supported");
    }
}

 /** Sets the height of In-App banner message height.
 * <br> Banner height should be between 100 and 200 (inclusive) density-independent unit.
 *
 * @param {number} height
 * @param {function} [success] Success callback.
 * @param {function} [failure] Failure callback.
 */

PushIOManager.prototype.setInAppMessageBannerHeight = function (height, success,failure) {
    this.call_native(success, failure, "setInAppMessageBannerHeight", [height]);
}

/**
 * Returns the height of In-App Banner message.
 *
 * @param {function} [success] Success callback.
 * @param {function} [failure] Failure callback.
 */

PushIOManager.prototype.getInAppMessageBannerHeight = function (success,failure) {
    this.call_native(success, failure, "getInAppMessageBannerHeight");
}

/**
 * Sets the boolean to hide status bar of In-App Banner and Interstitial message
 * <br> true to hide status bar otherwise false
 *
 * @param {boolean} hideStatusBar
 * @param {function} [success] Success callback.
 * @param {function} [failure] Failure callback.
 */

PushIOManager.prototype.setStatusBarHiddenForIAMBannerInterstitial = function (hideStatusBar, success,failure) {
    this.call_native(success, failure, "setStatusBarHiddenForIAMBannerInterstitial", [hideStatusBar]);
}

/**
 * Returns the boolean value of status bar hidden for In-App Banner and Interstitial message.
 * <br> true if status bar hidden otherwise false
 *
 * @param {function} [success] Success callback.
 * @param {function} [failure] Failure callback.
 */

PushIOManager.prototype.isStatusBarHiddenForIAMBannerInterstitial = function (success,failure) {
    this.call_native(success, failure, "isStatusBarHiddenForIAMBannerInterstitial");
}

/**
 * Returns the list of message centers that have been fetched.
 * 
 * @param {function} success 
 * @param {function} failure Failure callback.
 */
PushIOManager.prototype.onMessageCenterUpdated = function (success, failure) {
    this.call_native(success, failure, "onMessageCenterUpdated");
}


/**
 * @typedef {object} Preference
 * @property {string} key - Unique Identifier for this preference.
 * @property {string} label - Human-Readable description of this preference.
 * @property {string} type - Data type of this preference. Possible values: 'STRING', 'NUMBER', 'BOOLEAN'.
 * @property {string} value - Preference value.
 */

/**
 * @typedef {object} MessageCenterMessage
 * @property {string} messageID
 * @property {string} subject
 * @property {string} message
 * @property {string} iconURL
 * @property {string} messageCenterName
 * @property {string} deeplinkURL
 * @property {string} richMessageHTML
 * @property {string} richMessageURL
 * @property {string} sentTimestamp
 * @property {string} expiryTimestamp
 * @property {object} customKeyValuePairs
 */

/**
 * @typedef {object} InteractiveNotificationCategory
 * @property {string} orcl_category
 * @property {InteractiveNotificationButton[]} orcl_btns
 */

/**
 * @typedef {object} InteractiveNotificationButton
 * @property {string} id
 * @property {string} action 
 * @property {string} label
 */

/**
 * @typedef {object} RemoteMessage
 * @property {string} to
 * @property {string=} collapseKey 
 * @property {string=} messageId
 * @property {string=} messageType
 * @property {string=} ttl
 * @property {object} data
 */

/**
 * @typedef {object} GeoRegion
 * @property {string} geofenceId
 * @property {string} geofenceName 
 * @property {string} zoneName
 * @property {string} zoneId
 * @property {string} source
 * @property {number} deviceBearing
 * @property {number} deviceSpeed
 * @property {number} dwellTime
 * @property {object} extra
 */

/**
 * @typedef {object} BeaconRegion
 * @property {string} beaconId
 * @property {string} beaconName 
 * @property {string} beaconTag
 * @property {string} beaconProximity
 * @property {string} iBeaconUUID
 * @property {number} iBeaconMajor
 * @property {number} iBeaconMinor
 * @property {string} eddyStoneId1
 * @property {string} eddyStoneId2
 * @property {string} zoneName
 * @property {string} zoneId
 * @property {string} source
 * @property {number} dwellTime
 * @property {object} extra
 */

/**
 * @typedef {object} ConversionEvent
 * @property {string} orderId
 * @property {number} orderTotal
 * @property {number} orderQuantity
 * @property {number} conversionType
 * @property {object} customProperties
 */


if (!cordova.plugins) {
    cordova.plugins = {};
}

if (!cordova.plugins.PushIOManager) {
    cordova.plugins.PushIOManager = new PushIOManager();
}

if (typeof module != undefined && module.exports) {
    module.exports = PushIOManager;
}
