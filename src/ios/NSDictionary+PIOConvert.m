/**
* Copyright © 2022, Oracle and/or its affiliates. All rights reserved.
* Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
*/


#import "NSDictionary+PIOConvert.h"

@implementation NSDictionary(PIOConvert)

- (PIOGeoRegion *)geoRegion {
    NSString *geofenceId = self[@"geofenceId"];
    NSString *geofenceName = self[@"geofenceName"];
    double speed = [self[@"speed"] doubleValue];
    double bearing = [self[@"bearing"] doubleValue];
    NSString *zoneId = self[@"zoneId"];
    NSString *zoneName = self[@"zoneName"];
    NSString *source = self[@"source"];
    NSInteger dwellTime = [self[@"dwellTime"] integerValue];
    NSDictionary *extra = self[@"extra"];

    PIOGeoRegion *geoRegion = [[PIOGeoRegion alloc] initWithGeofenceId:geofenceId geofenceName:geofenceName speed:speed bearing:bearing source:source zoneId:zoneId zoneName:zoneName dwellTime:dwellTime extra:extra];
    
    return geoRegion;
}

- (PIOBeaconRegion *)beaconRegion {
    NSString *iBeaconUUID = self[@"iBeaconUUID"];
    NSInteger iBeaconMajor = [self[@"iBeaconMajor"] integerValue];
    NSInteger iBeaconMinor = [self[@"iBeaconMinor"] integerValue];
    NSString *beaconId = self[@"beaconId"];
    NSString *beaconName = self[@"beaconName"];
    NSString *beaconTag = self[@"beaconTag"];
    NSString *proximity = self[@"proximity"];
    NSString *zoneId = self[@"zoneId"];
    NSString *zoneName = self[@"zoneName"];
    NSString *source = self[@"source"];
    NSInteger dwellTime = [self[@"dwellTime"] integerValue];
    NSDictionary *extra = self[@"extra"];
    NSString *eddyStoneId1 = self[@"eddyStoneId1"];
    NSString *eddyStoneId2 = self[@"eddyStoneId2"];
    PIOBeaconRegion *beaconRegion = [[PIOBeaconRegion alloc] initWithiBeaconUUID:iBeaconUUID iBeaconMajor:iBeaconMajor iBeaconMinor:iBeaconMinor beaconId:beaconId beaconName:beaconName beaconTag:beaconTag proximity:proximity source:source zoneId:zoneId zoneName:zoneName dwellTime:dwellTime extra:extra];
    beaconRegion.eddyStoneId1 = eddyStoneId1;
    beaconRegion.eddyStoneId2 = eddyStoneId2;
    
    return beaconRegion;
}

- (PIONotificationCategory *)notificationCategory {
    NSArray *oracleButtons = self[@"orcl_btns"];
    NSMutableArray *actions = [NSMutableArray new];
    for (NSDictionary *action in oracleButtons) {
        PIONotificationAction *newAction = [[PIONotificationAction alloc] initWithIdentifier:action[@"id"] title:action[@"label"] isDestructive:[action[@"action"] isEqualToString:@"DE"] isForeground:[action[@"action"] isEqualToString:@"FG"] isAuthenticationRequired:[action[@"action"] isEqualToString:@"AR"]];
        [actions addObject:newAction];
    }
    return [[PIONotificationCategory alloc] initWithIdentifier:self[@"orcl_category"] actions:actions];
}

- (PIOConversionEvent *)conversionEvent {
    NSString *orderId = self[@"orderId"];
    double orderTotal = [self[@"orderTotal"] doubleValue];
    NSInteger orderQuantity = [self[@"orderQuantity"] integerValue];
    int conversionType = [self[@"conversionType"] intValue];
    NSDictionary *customProperties = self[@"customProperties"];
    PIOConversionEvent *conversion = [[PIOConversionEvent alloc] initWithOrderId:orderId orderTotal:orderTotal orderQuantity:orderQuantity conversionType:conversionType customProperties:customProperties];
    return conversion;
}

+ (NSDictionary *)dictionaryFromPreference:(PIOPreference *)preference {
    NSMutableDictionary *dictionary = [NSMutableDictionary dictionary];
    dictionary[@"key"] = preference.key;
    dictionary[@"value"] = preference.value;
    dictionary[@"label"] = preference.label;
      switch (preference.type) {
          case PIOPreferenceTypeString:
              dictionary[@"type"] = @"STRING";
              break;
          case PIOPreferenceTypeBoolean:
              dictionary[@"type"] = @"BOOLEAN";
              break;
          case PIOPreferenceTypeNumeric:
              dictionary[@"type"] = @"NUMBER";
              break;
      }
    return dictionary;
}

- (NSString *)JSON {
    NSError *err;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:self options:0 error:&err];
    
    if(err != nil) {
        return nil;
    }
    
    return [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
}

-(NSDictionary *)parseNotificationPayload {
    
    NSDictionary *payload =  nil;
    
    if (self[@"notification"][@"data"] != nil) {
        payload = self[@"notification"][@"data"];
    }else if (self[@"data"] != nil) {
        payload = self[@"data"];
    } else {
        payload = self;
    }
    return  payload;
}

@end
