package com.cgi.devicetracker.utilities;

import android.widget.ImageView;

public class BindingAdapter {

    @androidx.databinding.BindingAdapter("android:src")
    public static void setImageResource(ImageView imageView, int imageResource) {

        imageView.setImageResource(imageResource);

    }


}
