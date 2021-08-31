package com.cgi.devicetracker.recyclerview;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.cgi.devicetracker.MainActivity;
import com.cgi.devicetracker.R;
import com.cgi.devicetracker.database.TestDevice;
import com.cgi.devicetracker.databinding.SingleTestDeviceBinding;
import com.cgi.devicetracker.viewmodel.MainActivityViewModel;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class DeviceAdapter
        extends FirestoreRecyclerAdapter<TestDevice, DeviceAdapter.TestDeviceHolder> {

    private onItemClickListener mListener;
    private MainActivityViewModel mainActivityViewModel;
    private SingleTestDeviceBinding mSingleTestDeviceBinding;


    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query. See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public DeviceAdapter(@NonNull FirestoreRecyclerOptions<TestDevice> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull TestDeviceHolder holder, int position, @NonNull TestDevice model) {

        holder.bind(model);
    }

    @NonNull
    @Override
    public TestDeviceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        mSingleTestDeviceBinding =
                DataBindingUtil.inflate(layoutInflater, R.layout.single_test_device, parent, false);
        mainActivityViewModel =
                ViewModelProviders.of((MainActivity) parent.getContext()).get(MainActivityViewModel.class);

        return new TestDeviceHolder(mSingleTestDeviceBinding);
    }

    @Override
    public void onDataChanged() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getItemCount() == 0) {

                    Toast.makeText(
                            MainActivity.getContext(), "No device matches your preference",
                            Toast.LENGTH_SHORT)
                            .show();
                }
            }
        }, 100);
    }

    class TestDeviceHolder extends RecyclerView.ViewHolder {

        SingleTestDeviceBinding mSingleTestDeviceBinding;

        public TestDeviceHolder(SingleTestDeviceBinding mSingleTestDeviceBinding) {
            super(mSingleTestDeviceBinding.getRoot());
            this.mSingleTestDeviceBinding = mSingleTestDeviceBinding;
            itemView.setOnClickListener(
                    v -> {
                        int position = getAdapterPosition();

                        if (position != RecyclerView.NO_POSITION && mListener != null) {
                            mListener.onItemClick(getSnapshots().getSnapshot(position), position);
                        }
                    });
        }

        public void bind(TestDevice model) {

            mSingleTestDeviceBinding.setTestDevice(model);
            mSingleTestDeviceBinding.setMainActivityViewModel(mainActivityViewModel);
            mSingleTestDeviceBinding.executePendingBindings();
        }
    }

    public void setOnItemClickedListener(onItemClickListener listener) {
        this.mListener = listener;
    }

}
