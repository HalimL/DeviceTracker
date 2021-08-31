package com.cgi.devicetracker.utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.text.format.DateFormat;

import com.cgi.devicetracker.database.TestDevice;
import com.cgi.devicetracker.repositories.TestDeviceRepository;

import java.util.Calendar;
import java.util.Locale;

public class BatteryLevelReceiver extends BroadcastReceiver {

  private int mPercentage;
  private Long mCurrentTime;
  private String mBatteryStatus;

  @Override
  public void onReceive(Context context, Intent intent) {}

  public boolean timeToUpdate(TestDevice testDevice) {

    boolean update;
    long diff;

    diff = getCurrentTime() - testDevice.getLastUpdated();

    long diffMinutes = (diff / 60);

    update = diffMinutes >= 15;
    return update;
  }

  public Long getCurrentTime() {

    mCurrentTime = System.currentTimeMillis() / 1000;
    return mCurrentTime;
  }

  public int getBatteryPercentage(Intent intent) {

    int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
    int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
    mPercentage = (level * 100 / scale);

    return mPercentage;
  }

  public String getBatteryStatus(Intent intent) {

    int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

    switch (status) {
      case BatteryManager.BATTERY_STATUS_FULL:
        mBatteryStatus = "Full";
        break;

      case BatteryManager.BATTERY_STATUS_CHARGING:
        mBatteryStatus = "Charging";
        break;

      case BatteryManager.BATTERY_STATUS_DISCHARGING:
        mBatteryStatus = "Discharging";
        break;
    }

    return mBatteryStatus;
  }

  public String getDate(Long timestamp) {
    Calendar cal = Calendar.getInstance(Locale.getDefault());
    cal.setTimeInMillis(timestamp * 1000);

    return DateFormat.format("dd-MM-yyyy hh:mm", cal).toString();
  }

  public void makeChanges(TestDeviceRepository mRepo, TestDevice testDevice) {

    mRepo.updateBatteryStatus(
        mPercentage, mBatteryStatus, mCurrentTime, testDevice, getDate(mCurrentTime));
    testDevice.setLastUpdated(mCurrentTime);
    testDevice.setBatteryPercentage(mPercentage);
    testDevice.setBatteryStatus(mBatteryStatus);
  }
}
