package com.cgi.devicetracker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.cgi.devicetracker.R;

public class UpdateDatabaseFragment extends Fragment {

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle) {

        return inflater.inflate(R.layout.update_database_fragment, container, false);
    }
}
