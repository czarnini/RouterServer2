package com.bogucki.optimize;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

public interface onRequestDoneListener extends DatabaseReference.CompletionListener {
    @Override
    void onComplete(DatabaseError error, DatabaseReference ref);
}
