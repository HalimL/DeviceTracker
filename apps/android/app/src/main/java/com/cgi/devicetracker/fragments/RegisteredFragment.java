package com.cgi.devicetracker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.cgi.devicetracker.R;

public class RegisteredFragment extends Fragment {

    View mView;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle) {

        mView = inflater.inflate(R.layout.fragment_registered, container, false);

        Button okButton = mView.findViewById(R.id.ok_btn);

        okButton.setOnClickListener(
                click -> {
                    container.removeAllViews();
                    changeToRecyclerView();
                });

        return mView;
    }

    public void changeToRecyclerView() {

        DeviceListFragment deviceListFragment = new DeviceListFragment();

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.update_fragment_id, deviceListFragment);
        fragmentTransaction.commit();
    }
}
