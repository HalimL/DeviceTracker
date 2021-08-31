package com.cgi.devicetracker.repositories;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.MutableLiveData;

import com.cgi.devicetracker.MainActivity;
import com.cgi.devicetracker.database.TestDevice;
import com.cgi.devicetracker.database.User;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jaredrummler.android.device.DeviceName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestDeviceRepository {

    public static TestDeviceRepository mInstance;
    private static final String TAG = TestDeviceRepository.class.getSimpleName();
    private static final String MANUFACTURER = "manufacturer";
    private static final String DEVICE_ID = "deviceID";
    private static final String VERSION = "version";
    private static final String NAME = "name";
    private static final String HOLDER = "holder";
    private static final String BATTERY_STATUS = "batteryStatus";
    private static final String BATTERY_PERCENTAGE = "batteryPercentage";
    private static final String LAST_UPDATED = "lastUpdated";
    private static final String DATE = "date";
    private Context mApplicationContext = MainActivity.getContext();

    private FirebaseFirestore mDatabase = FirebaseFirestore.getInstance();
    private TestDevice mCurrentDeviceInHand;
    private Long lastUpdate = 0L;

    private final CollectionReference mTestDeviceRef = mDatabase.collection("testDevice");
    private final CollectionReference mUsersRef = mDatabase.collection("users");



    /**
     * Singleton Pattern
     */
    public static TestDeviceRepository getInstance() {
        if (mInstance == null) {
            mInstance = new TestDeviceRepository();
        }
        return mInstance;
    }

    /**
     * Gets devices from the firestore database and converts it to a mutable live object.
     */
    public MutableLiveData<List<TestDevice>> getDevices() {
        checkForExistence();

        ArrayList<TestDevice> devicesDataSet = new ArrayList<>();
        MutableLiveData<List<TestDevice>> devicesData = new MutableLiveData<>();

        mTestDeviceRef
                .get()
                .addOnCompleteListener(
                        task -> {
                            if (task.isSuccessful()) {

                                QuerySnapshot searchResult = task.getResult();
                                if (searchResult != null) {

                                    for (QueryDocumentSnapshot document : searchResult) {

                                        devicesDataSet.add(document.toObject(TestDevice.class));
                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                    }
                                    devicesData.setValue(devicesDataSet);

                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });

        return devicesData;
    }

    /**
     * Writes to the database
     */
    public void writeDocument() {

        Map<String, Object> testDevice = new HashMap<>();

        testDevice.put(MANUFACTURER, Build.MANUFACTURER);
        testDevice.put(
                DEVICE_ID,
                Settings.Secure.getString(
                        mApplicationContext.getContentResolver(), Settings.Secure.ANDROID_ID));
        testDevice.put(VERSION, Build.VERSION.RELEASE);
        testDevice.put(NAME, DeviceName.getDeviceName());
        testDevice.put(HOLDER, null);
        testDevice.put(BATTERY_STATUS, "");
        testDevice.put(BATTERY_PERCENTAGE, 0);
        testDevice.put(LAST_UPDATED,  0);
        testDevice.put(DATE, "");

        mTestDeviceRef
                .add(testDevice)
                .addOnSuccessListener(
                        documentReference -> {
                            Log.d(TAG, "Document Snapshot added with ID: " + documentReference.getId());
                        })
                .addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));
    }

    /**
     * Checks if a device already exists so as to prevent duplicate entries whenever the app is
     * launched.
     */
    public void checkForExistence() {

        mTestDeviceRef
                .whereEqualTo(
                        DEVICE_ID,
                        Settings.Secure.getString(
                                mApplicationContext.getContentResolver(), Settings.Secure.ANDROID_ID))
                .get()
                .addOnCompleteListener(
                        task -> {
                            if (task.isSuccessful()) {
                                QuerySnapshot document = task.getResult();
                                if (document != null && document.isEmpty()) {
                                    writeDocument();
                                } else {
                                    Log.d(TAG, "Documents already exists");
                                    // Do nothing
                                }
                            } else {
                                writeDocument();
                            }
                        });
    }


    public void rentDevice(String rentingUser, TestDevice testDevice) {

        mTestDeviceRef
                .whereEqualTo(DEVICE_ID, testDevice.getDeviceID())
                .get()
                .addOnSuccessListener(
                        dev -> {
                            if (dev.getDocumentChanges().size() == 1) {

                                mTestDeviceRef.document(dev.getDocuments().get(0).getId()).update(HOLDER, rentingUser);
                            }
                        });
    }

    public void returnDevice(TestDevice testDevice) {

        mTestDeviceRef
                .whereEqualTo(DEVICE_ID, testDevice.getDeviceID())
                .get()
                .addOnSuccessListener(
                        dev -> {
                            if (dev.getDocumentChanges().size() == 1) {

                                mTestDeviceRef.document(dev.getDocuments().get(0).getId()).update(HOLDER, null);
                            }
                        });

    }



    public void updateBatteryStatus(int batteryPercentage, String batteryStatus, Long dateLastUpdated, TestDevice device, String date) {
        mTestDeviceRef
                .whereEqualTo(DEVICE_ID, device.getDeviceID())
                .get()
                .addOnSuccessListener(
                        dev -> {
                            if (dev.getDocumentChanges().size() == 1) {

                                mTestDeviceRef.document(dev.getDocuments().get(0).getId()).update(BATTERY_STATUS, batteryStatus);
                                mTestDeviceRef.document(dev.getDocuments().get(0).getId()).update(LAST_UPDATED, dateLastUpdated);
                                mTestDeviceRef.document(dev.getDocuments().get(0).getId()).update(BATTERY_PERCENTAGE, batteryPercentage);
                                mTestDeviceRef.document(dev.getDocuments().get(0).getId()).update(DATE, date);
                            }
                        });
    }

    public Long getLastUpdate(TestDevice device) {
        mTestDeviceRef
                .whereEqualTo(DEVICE_ID, device.getDeviceID())
                .get()
                .addOnCompleteListener(
                        task -> {
                            if (task.isSuccessful()) {
                                QuerySnapshot searchResult = task.getResult();

                                if (searchResult != null) {

                                    for (QueryDocumentSnapshot document : searchResult) {

                                        lastUpdate = document.getLong(LAST_UPDATED);

                                    }
                                }
                            }
                        });
        return lastUpdate;
    }



    public MutableLiveData<TestDevice> getCurrentDeviceInHand() {

        MutableLiveData<TestDevice> testDevice = new MutableLiveData<>();

        mTestDeviceRef
                .whereEqualTo(
                        DEVICE_ID,
                        Settings.Secure.getString(
                                mApplicationContext.getContentResolver(), Settings.Secure.ANDROID_ID))
                .get()
                .addOnCompleteListener(
                        task -> {
                            if (task.isSuccessful()) {
                                QuerySnapshot searchResult = task.getResult();

                                if (searchResult != null) {
                                    for (QueryDocumentSnapshot document : searchResult) {

                                        mCurrentDeviceInHand = document.toObject(TestDevice.class);
                                        testDevice.setValue(mCurrentDeviceInHand);
                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                    }
                                }
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        });

        return testDevice;
    }

    public MutableLiveData<List<User>> getAllMutableUsers() {

        ArrayList<User> userDataSet = new ArrayList<>();
        MutableLiveData<List<User>> userData = new MutableLiveData<>();

        mUsersRef
                .get()
                .addOnCompleteListener(
                        task -> {
                            if (task.isSuccessful()) {
                                QuerySnapshot searchResult = task.getResult();

                                if (searchResult != null) {

                                    for (QueryDocumentSnapshot document : searchResult) {

                                        userDataSet.add(document.toObject(User.class));
                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                    }

                                    userData.setValue(userDataSet);

                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });

        return userData;
    }

    public ArrayList<String> getAllImmutableUsers() {

        ArrayList<String> users = new ArrayList<>();

        users.add("User");

        mUsersRef
                .get()
                .addOnCompleteListener(
                        task -> {
                            if (task.isSuccessful()) {
                                QuerySnapshot searchResult = task.getResult();

                                if (searchResult != null) {
                                    users.clear();

                                    for (QueryDocumentSnapshot document : searchResult) {

                                        String userName = document.getString("name");
                                        if (userName != null) {
                                            users.add(userName);
                                            Log.d(TAG, document.getId() + " => " + userName);

                                        } else {
                                            Log.d(TAG, "Username doesn't exist");
                                        }
                                    }
                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });

        return users;
    }


    public ArrayList<String> getDeviceNames() {


        ArrayList<String> names = new ArrayList<>();

        names.add("Filter by: Name");


        mTestDeviceRef
                .get()
                .addOnCompleteListener(
                        task -> {
                            if (task.isSuccessful()) {
                                QuerySnapshot searchResult = task.getResult();

                                if (searchResult != null) {
                                    names.clear();

                                    for (QueryDocumentSnapshot document : searchResult) {

                                        String deviceName = document.getString(NAME);
                                        if (deviceName != null) {
                                            names.add(deviceName);
                                            filterDuplicates(names);
                                            Log.d(TAG, document.getId() + " => " + deviceName);


                                        } else {
                                            Log.d(TAG, "Device name doesn't exist");

                                        }

                                    }
                                }

                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());

                            }


                        });

        return names;

    }
    
    public ArrayList<String> getAndroidVersions() {

        ArrayList<String> versions = new ArrayList<>();

        versions.add("Filter by: Version");

        mTestDeviceRef
                .get()
                .addOnCompleteListener(
                        task -> {
                            if (task.isSuccessful()) {
                                QuerySnapshot searchResult = task.getResult();

                                if (searchResult != null) {
                                    versions.clear();

                                    for (QueryDocumentSnapshot document : searchResult) {

                                        String androidVersion = document.getString(VERSION);
                                        if (androidVersion != null) {
                                            versions.add(androidVersion);
                                            filterDuplicates(versions);
                                            Log.d(TAG, document.getId() + " => " + androidVersion);

                                        } else {
                                            Log.d(TAG, "Android version doesn't exist");
                                        }
                                    }
                                }
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());

                            }

                        });
        return versions;

    }

    private void filterDuplicates(ArrayList<String> duplicatedArrayToBeFiltered) {

        ArrayList<String> arrayWithoutDuplicates = new ArrayList<>();
        arrayWithoutDuplicates.add("Filter by:");

        for (String element : duplicatedArrayToBeFiltered) {

            if (!arrayWithoutDuplicates.contains(element)) {

                arrayWithoutDuplicates.add(element);
            }
        }

        duplicatedArrayToBeFiltered.clear();
        duplicatedArrayToBeFiltered.addAll(arrayWithoutDuplicates);

    }


}





