package com.cgi.devicetracker.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.cgi.devicetracker.R;
import com.cgi.devicetracker.database.TestDevice;
import com.cgi.devicetracker.databinding.RentFragmentBinding;
import com.cgi.devicetracker.repositories.TestDeviceRepository;
import com.cgi.devicetracker.viewmodel.FragmentCommunicationViewModel;
import com.cgi.devicetracker.viewmodel.MainActivityViewModel;

import java.util.ArrayList;

public class RentDialog extends AppCompatDialogFragment {

    private MainActivityViewModel mMainActivityViewModel;
    private TestDevice mTestDeviceClicked;
    private TestDevice mTestDeviceInMyHand;
    private TestDeviceRepository mRepo;
    private String mSelectedUser;
    private RentFragmentBinding mRentFragmentBinding;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        mRentFragmentBinding = DataBindingUtil.inflate(inflater, R.layout.rent_fragment, null, false);

        FragmentCommunicationViewModel fragmentCommunicationViewModel =
                ViewModelProviders.of(getActivity()).get(FragmentCommunicationViewModel.class);
        mTestDeviceClicked = fragmentCommunicationViewModel.getTestDeviceClicked();

        mMainActivityViewModel = ViewModelProviders.of(getActivity()).get(MainActivityViewModel.class);
        mRepo = mMainActivityViewModel.getmTestDeviceRepo();
        mTestDeviceInMyHand = mMainActivityViewModel.getDeviceInMyHand().getValue();

        ArrayList<String> users = mRepo.getAllImmutableUsers();

        mRentFragmentBinding.setTestDevice(mTestDeviceClicked);
        mRentFragmentBinding.setMainActivityViewModel(mMainActivityViewModel);

        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(
                        getContext(), android.R.layout.simple_spinner_dropdown_item, users);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mRentFragmentBinding.spUsers.setAdapter(adapter);
        mRentFragmentBinding.spUsers.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        mSelectedUser = mRentFragmentBinding.spUsers.getItemAtPosition(position).toString();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

        mBuilder.setView(mRentFragmentBinding.getRoot());

        mRentFragmentBinding.btnCancel.setOnClickListener(
                click -> {
                    dismissDialog();
                });

        if (mTestDeviceClicked.getDeviceID().equals(mTestDeviceInMyHand.getDeviceID())
                && (mTestDeviceInMyHand.getHolder() != null)) {

            mRentFragmentBinding.btnRent.setOnClickListener(
                    click -> {
                        mRepo.returnDevice(mTestDeviceClicked);
                        mTestDeviceInMyHand.setNullHolder();
                        Toast.makeText(
                                this.getContext(),
                                mTestDeviceClicked.getName() + " was returned",
                                Toast.LENGTH_SHORT)
                                .show();
                        dismissDialog();
                    });

        } else if ((mTestDeviceInMyHand.getHolder() == null)) {

            mRentFragmentBinding.btnRent.setOnClickListener(
                    click -> {
                        if (mTestDeviceClicked.getDeviceID().equals(mTestDeviceInMyHand.getDeviceID())) {

                            mTestDeviceInMyHand.setNonNullHolder(mSelectedUser);
                        }

                        if (!mSelectedUser.equalsIgnoreCase("User")) {

                            mRepo.rentDevice(mSelectedUser, mTestDeviceClicked);
                            Toast.makeText(
                                    this.getContext(),
                                    mTestDeviceClicked.getName() + " rented by " + mSelectedUser,
                                    Toast.LENGTH_SHORT)
                                    .show();
                            dismissDialog();

                        } else {

                            Toast.makeText(this.getContext(), "Please select a valid user", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
        }

        Dialog rentDialog = mBuilder.create();
        rentDialog.getContext().setTheme(R.style.MyAlertDialog);
        return rentDialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        Window window = getDialog().getWindow();
        if (window == null) return;
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = 820;
        params.height = 780;
        window.setAttributes(params);
    }

    private void dismissDialog() {

        if (getDialog() != null) getDialog().dismiss();
    }
}
