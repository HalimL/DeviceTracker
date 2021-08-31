package com.cgi.devicetracker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cgi.devicetracker.R;
import com.cgi.devicetracker.database.TestDevice;
import com.cgi.devicetracker.databinding.FragmentRecyclerViewBinding;
import com.cgi.devicetracker.recyclerview.DeviceAdapter;
import com.cgi.devicetracker.viewmodel.FragmentCommunicationViewModel;
import com.cgi.devicetracker.viewmodel.MainActivityViewModel;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

public class DeviceListFragment extends Fragment {

    private FirebaseFirestore database = FirebaseFirestore.getInstance();
    private CollectionReference testDeviceRef = database.collection("testDevice");

    private Query mQuery;
    private DeviceAdapter mAdapter;
    private FragmentCommunicationViewModel mViewModelFragmentCommunication;
    private LiveData<TestDevice> mDeviceInMyHand;
    private String mDeviceNameToBeFilteredBy;
    private String mVersionToBeFilteredBy;
    public static MainActivityViewModel mViewModelMainActivity;

    private final String NAME = "name";
    private final String VERSION = "version";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle) {

        mViewModelMainActivity = ViewModelProviders.of(getActivity()).get(MainActivityViewModel.class);
        mDeviceInMyHand = mViewModelMainActivity.getDeviceInMyHand();
        FragmentRecyclerViewBinding mFragmentRecyclerViewBinding;
        mFragmentRecyclerViewBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_recycler_view, null, false);

        ArrayList<String> deviceNames = mViewModelMainActivity.getmTestDeviceRepo().getDeviceNames();
        ArrayList<String> androidVersions = mViewModelMainActivity.getmTestDeviceRepo().getAndroidVersions();

        //spinner adapter to filter according to device name
        ArrayAdapter<String> adapterDeviceNameSpinner =
                new ArrayAdapter<String>(
                        getContext() , android.R.layout.simple_spinner_dropdown_item
                        , deviceNames);

        adapterDeviceNameSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mFragmentRecyclerViewBinding.spinnerName.setAdapter(adapterDeviceNameSpinner);

        mFragmentRecyclerViewBinding.spinnerName.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        mDeviceNameToBeFilteredBy = deviceNames.get(position);

                        if (!mDeviceNameToBeFilteredBy.contains("Filter by:") && mVersionToBeFilteredBy.contains("Filter by:")) {

                            mQuery = testDeviceRef.whereEqualTo(NAME, mDeviceNameToBeFilteredBy);
                            updateRecyclerView(mQuery, mAdapter);

                        } else if (!mDeviceNameToBeFilteredBy.contains("Filter by:") && (!mVersionToBeFilteredBy.contains("Filter by:"))) {

                            mQuery = testDeviceRef.whereEqualTo(NAME, mDeviceNameToBeFilteredBy).whereEqualTo(VERSION, mVersionToBeFilteredBy);
                            updateRecyclerView(mQuery, mAdapter);

                        } else {

                            mQuery = testDeviceRef;
                            updateRecyclerView(mQuery, mAdapter);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }

                });


        //spinner adapter to filter according to device android version
        ArrayAdapter<String> adapterAndroidVersionSpinner =
                new ArrayAdapter<String>(
                        getContext(), android.R.layout.simple_spinner_dropdown_item, androidVersions);

        adapterAndroidVersionSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mFragmentRecyclerViewBinding.spinnerVersion.setAdapter(adapterAndroidVersionSpinner);

        mFragmentRecyclerViewBinding.spinnerVersion.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        mVersionToBeFilteredBy = androidVersions.get(position);

                        if (!mVersionToBeFilteredBy.contains("Filter by:") && mDeviceNameToBeFilteredBy.contains("Filter by:")) {

                            mQuery = testDeviceRef.whereEqualTo(VERSION, mVersionToBeFilteredBy);
                            updateRecyclerView(mQuery, mAdapter);

                        }  else if (!mDeviceNameToBeFilteredBy.contains("Filter by:") && (!mVersionToBeFilteredBy.contains("Filter by:"))) {

                            mQuery = testDeviceRef.whereEqualTo(NAME, mDeviceNameToBeFilteredBy).whereEqualTo(VERSION, mVersionToBeFilteredBy);
                            updateRecyclerView(mQuery, mAdapter);

                        } else {

                            mQuery = testDeviceRef;
                            updateRecyclerView(mQuery, mAdapter);

                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });


        View view = mFragmentRecyclerViewBinding.getRoot();
        setUpRecyclerView(view);

        //recycler view adapter
        mAdapter.setOnItemClickedListener(
                (documentSnapshot, position) -> {
                    mViewModelFragmentCommunication =
                            ViewModelProviders.of(getActivity()).get(FragmentCommunicationViewModel.class);

                    TestDevice testDevice = documentSnapshot.toObject(TestDevice.class);
                    mViewModelFragmentCommunication.setTestDeviceClicked(testDevice);
                        openRentDialog();
                });


        return view;
    }

    private void setUpRecyclerView(View view) {

        mQuery = testDeviceRef;

        FirestoreRecyclerOptions<TestDevice> options =
                new FirestoreRecyclerOptions.Builder<TestDevice>()
                        .setQuery(mQuery, TestDevice.class)
                        .build();

        mAdapter = new DeviceAdapter(options);

        RecyclerView recyclerView = view.findViewById(R.id.rv_device_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerView.setAdapter(mAdapter);
    }


    @Override
    public void onStart() {
        super.onStart();
        mAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }


    private void updateRecyclerView(Query query, DeviceAdapter adapter) {

        FirestoreRecyclerOptions<TestDevice> newOptions =
                new FirestoreRecyclerOptions.Builder<TestDevice>()
                        .setQuery(query, TestDevice.class)
                        .build();

        adapter.updateOptions(newOptions);
    }

    private void openRentDialog() {

        RentDialog rentDialog = new RentDialog();
        rentDialog.show(getChildFragmentManager(), "rent dialog");
    }
}
