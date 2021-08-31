import 'package:json_annotation/json_annotation.dart';

part 'device.g.dart';

@JsonSerializable()
class Device {
  String name;
  String deviceID;
  String manufacturer;
  String version;
  String holder;
  int lastUpdated;
  int batteryPercentage;
  String batteryStatus;
  String date;

  Device(
      {this.name,
      this.deviceID,
      this.manufacturer,
      this.version,
      this.holder,
      this.lastUpdated,
      this.batteryPercentage,
      this.batteryStatus,
      this.date});

  factory Device.fromJson(Map<String, dynamic> json) => _$DeviceFromJson(json);

  Map<String, dynamic> toJson() => _$DeviceToJson(this);
}
