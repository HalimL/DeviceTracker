package com.cgi.devicetracker.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cgi.devicetracker.database.TestDevice;
import com.cgi.devicetracker.database.User;
import com.cgi.devicetracker.repositories.TestDeviceRepository;

import java.util.List;

public class MainActivityViewModel extends ViewModel {

  private MutableLiveData<List<TestDevice>> mTestDevices;
  private MutableLiveData<List<User>> mUsers;
  private TestDeviceRepository mTestDeviceRepo;
  private MutableLiveData<TestDevice> mDeviceInMyHand;

  public void init() {
    if (mTestDevices != null) {
      return;
    }
    mTestDeviceRepo = TestDeviceRepository.getInstance();
    mTestDevices = mTestDeviceRepo.getDevices();
    mUsers = mTestDeviceRepo.getAllMutableUsers();
    mDeviceInMyHand = mTestDeviceRepo.getCurrentDeviceInHand();
  }

  public LiveData<List<TestDevice>> getTestDevices() {
    return mTestDevices;
  }

  public LiveData<List<User>> getUsers() {
    return mUsers;
  }

  public TestDeviceRepository getmTestDeviceRepo() { return mTestDeviceRepo; }

  public LiveData<TestDevice> getDeviceInMyHand() { return mDeviceInMyHand; }
}
