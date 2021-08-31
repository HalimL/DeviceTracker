// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'device.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Device _$DeviceFromJson(Map<String, dynamic> json) {
  return Device(
    name: json['name'] as String,
    deviceID: json['deviceID'] as String,
    manufacturer: json['manufacturer'] as String,
    version: json['version'] as String,
    holder: json['holder'] as String,
    lastUpdated: json['lastUpdated'] as int,
    batteryPercentage: json['batteryPercentage'] as int,
    batteryStatus: json['batteryStatus'] as String,
    date: json['date'] as String,
  );
}

Map<String, dynamic> _$DeviceToJson(Device instance) => <String, dynamic>{
      'name': instance.name,
      'deviceID': instance.deviceID,
      'manufacturer': instance.manufacturer,
      'version': instance.version,
      'holder': instance.holder,
      'lastUpdated': instance.lastUpdated,
      'batteryPercentage': instance.batteryPercentage,
      'batteryStatus': instance.batteryStatus,
      'date': instance.date,
    };
