import 'package:battery/battery.dart';
import 'package:intl/intl.dart';
import 'package:flutter_device_tracker/database/repo.dart';
import 'package:flutter_device_tracker/model/device.dart';

class DeviceBatteryInfo {
  final Battery _battery = new Battery();
  var _timeToUpdate = false;
  var _batteryStatus;

  String getBatteryStatus(BatteryState state) {
    if (state == BatteryState.charging) {
      _batteryStatus = 'Charging';
    } else if (state == BatteryState.discharging) {
      _batteryStatus = 'Discharging';
    } else if (state == BatteryState.full) {
      _batteryStatus = 'Full';
    } else {
      _batteryStatus = 'Unknown';
    }

    return _batteryStatus;
  }

  Future<int> getBatteryLevel() async {
    int _batteryLevel = await _battery.batteryLevel;

    return _batteryLevel;
  }

  int getTime() {
    return DateTime.now().millisecondsSinceEpoch;
  }

  String getDate() {
    var date = new DateFormat("yyyy-MM-dd hh:mm:ss").format(DateTime.now());
    return date;
  }

  bool timeToUpdate(Device deviceToUpdate) {
    var lastUpdated = deviceToUpdate.lastUpdated;
    var diff = getTime() - lastUpdated;
    var diffInMinutes = diff / (1000 * 60);

    if (diffInMinutes >= 15) {
      _timeToUpdate = true;
    }
    return _timeToUpdate;
  }

  void updateBatteryInfo(Device deviceToUpdate, String batteryStatus) async {
    int batteryPercentage = await getBatteryLevel();
    deviceToUpdate.batteryPercentage = batteryPercentage;
    deviceToUpdate.batteryStatus = batteryStatus;
    deviceToUpdate.date = getDate();
    deviceToUpdate.lastUpdated = getTime();

    Repo().updateBatterStatus(
        deviceToUpdate, batteryStatus, batteryPercentage, getDate(), getTime());
  }
}
