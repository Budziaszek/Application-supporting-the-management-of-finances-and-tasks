package com.budziaszek.tabmate;

import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class DatabaseInformation {



    // Document: name_uid (uid of owner - user who created)
    // Fields: like in Group class
    public static final String GROUP_COLLECTION = "groups";

    // Document: uid
    // Fields: like in User class
    public static final String USER_COLLECTION = "users";
    public static final String USER_COLLECTION_INVITATION_FIELD = "invitations";

}
