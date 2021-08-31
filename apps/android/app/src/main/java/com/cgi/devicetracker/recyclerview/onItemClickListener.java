package com.cgi.devicetracker.recyclerview;

import com.google.firebase.firestore.DocumentSnapshot;

public interface onItemClickListener {

  void onItemClick(DocumentSnapshot documentSnapshot, int position);
}
