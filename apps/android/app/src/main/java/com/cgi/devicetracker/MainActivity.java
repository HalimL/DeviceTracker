package com.cgi.devicetracker;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.cgi.devicetracker.database.TestDevice;
import com.cgi.devicetracker.fragments.RegisteredFragment;
import com.cgi.devicetracker.repositories.TestDeviceRepository;
import com.cgi.devicetracker.utilities.BatteryLevelReceiver;
import com.cgi.devicetracker.viewmodel.MainActivityViewModel;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MainActivityViewModel mMainActivityViewModel;
    private TestDeviceRepository mRepo;
    private static Context mApplicationContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mApplicationContext = getApplicationContext();
        mMainActivityViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);
        mMainActivityViewModel.init();

        mRepo = mMainActivityViewModel.getmTestDeviceRepo();

        mMainActivityViewModel
                .getTestDevices()
                .observe(
                        this,
                        new Observer<List<TestDevice>>() {

                            @Override
                            public void onChanged(List<TestDevice> testDevices) {

                                changeToRegisteredFragment();
                            }
                        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter mIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mLocalBatteryLevelReceiver, mIntentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mLocalBatteryLevelReceiver);
    }

    public void changeToRegisteredFragment() {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        RegisteredFragment registeredFragment = new RegisteredFragment();

        fragmentTransaction.replace(R.id.update_fragment_id, registeredFragment);
        fragmentTransaction.commit();
    }

    public static Context getContext() {
        return mApplicationContext;
    }


    public void receivedBroadcast(Intent intent) {

        String action = intent.getAction();

        if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
            mLocalBatteryLevelReceiver.getBatteryStatus(intent);
            mLocalBatteryLevelReceiver.getBatteryPercentage(intent);
            mLocalBatteryLevelReceiver.getCurrentTime();
        }
    }


    private BatteryLevelReceiver mLocalBatteryLevelReceiver = new BatteryLevelReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            MainActivity.this.receivedBroadcast(intent);

            mMainActivityViewModel
                    .getDeviceInMyHand()
                    .observe(
                            MainActivity.this,
                            new Observer<TestDevice>() {

                                @Override
                                public void onChanged(TestDevice testDevice) {

                                    if (mMainActivityViewModel.getTestDevices().hasActiveObservers()) {

                                        if (timeToUpdate(testDevice) || testDevice.getLastUpdated() == 0L) {

                                            makeChanges(mRepo, testDevice);
                                        }
                                    }
                                }
                            });
        }
    };
}
