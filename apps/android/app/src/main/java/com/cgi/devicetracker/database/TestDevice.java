package com.cgi.devicetracker.database;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import java.io.Serializable;

public class TestDevice extends BaseObservable implements Serializable {

    private String name;
    private String version;
    private String deviceID;
    private String manufacturer;
    private String holder;
    private String batteryStatus;
    private int batteryPercentage;
    private Long lastUpdated;
    private String date;


    public TestDevice() {
    }

    public TestDevice(
            String name, String version, String deviceID, String manufacturer, String holder, Long lastUpdated, String batteryStatus, int batteryPercentage, String date) {
        this.name = name;
        this.version = version;
        this.deviceID = deviceID;
        this.manufacturer = manufacturer;
        this.holder = holder;
        this.lastUpdated = lastUpdated;
        this.batteryPercentage = batteryPercentage;
        this.batteryStatus = batteryStatus;
        this.date = date;
    }

    @Bindable
    public String getName() {
        return name;
    }

    @Bindable
    public String getVersion() {
        return version;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    @Bindable
    public String getHolder() {
        return holder;
    }

    @Bindable
    public Long getLastUpdated() {
        return lastUpdated;
    }

    @Bindable
    public String getDate() {
        return date;
    }

    @Bindable
    public String getBatteryStatus() {
        return batteryStatus;
    }

    @Bindable
    public int getBatteryPercentage() {
        return batteryPercentage;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setBatteryStatus(String batteryStatus) {
        this.batteryStatus = batteryStatus;
    }


    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public void setBatteryPercentage(int batteryPercentage) {
        this.batteryPercentage = batteryPercentage;
    }

    public void setNullHolder() {

        this.holder = null;
    }

    public void setNonNullHolder(String holder) {

        this.holder = holder;
    }
}
