package com.cgi.devicetracker.viewmodel;

import androidx.lifecycle.ViewModel;

import com.cgi.devicetracker.database.TestDevice;

public class FragmentCommunicationViewModel extends ViewModel {

    private TestDevice mTestDeviceClicked = new TestDevice();

    public void setTestDeviceClicked(TestDevice testDeviceClicked) {

        this.mTestDeviceClicked = testDeviceClicked;
    }

    public TestDevice getTestDeviceClicked() {

        return mTestDeviceClicked;
    }
}
