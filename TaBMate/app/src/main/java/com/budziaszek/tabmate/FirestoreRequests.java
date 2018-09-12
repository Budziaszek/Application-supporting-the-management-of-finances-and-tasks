package com.budziaszek.tabmate;

import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class FirestoreRequests {

    public static final String USER_COLLECTION = "users";
    public static final String USER_COLLECTION_INVITATIONS_FIELD = "invitations";
    public static final String GROUP_COLLECTION = "groups";
    public static final String GROUP_COLLECTION_MEMBERS_FIELD = "members";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void getUser(final String uid, final Consumer<DocumentSnapshot> action) {
        db.collection(MainActivity.USER_COLLECTION)
                .document(uid)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        action.accept(documentSnapshot);
                    }
                });
    }

    public void getUserByField(String field, String value, Consumer<Task<QuerySnapshot>> action){
        db.collection(MainActivity.USER_COLLECTION)
                .whereEqualTo(field, value)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        action.accept(task);
                    }
                });
    }

    public void addGroup(Group group, String targetDocument, Consumer succes, Consumer<Exception> failure){
        db.collection(MainActivity.GROUP_COLLECTION)
                .document(targetDocument)
                .set(group)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                       succes.accept(aVoid);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                       failure.accept(e);
                    }
                });
    }

    public void getGroup(final String gid, final Consumer<DocumentSnapshot> action) {
        db.collection(MainActivity.GROUP_COLLECTION)
                .document(gid)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        action.accept(documentSnapshot);
                    }
                });
    }

    public void getGroupByField(String field, String value, Consumer<Task<QuerySnapshot>> action){
        db.collection(GROUP_COLLECTION)
                .whereArrayContains(field, value)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        action.accept(task);
                    }
                });
    }

    public void addGroupMember(String gid, String uid){
        db.collection(MainActivity.GROUP_COLLECTION)
                .document(gid)
                .update(MainActivity.GROUP_COLLECTION_MEMBERS_FIELD,
                        FieldValue.arrayUnion(uid));
    }

    public void addInvitation(String uid, String gid){
        db.collection(MainActivity.USER_COLLECTION)
                .document(uid)
                .update(MainActivity.USER_COLLECTION_INVITATIONS_FIELD,
                        FieldValue.arrayUnion(gid));
    }

}
