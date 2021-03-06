package com.budziaszek.tabmate.data;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.function.Consumer;


public class FirestoreRequests {

    private static final String USER_COLLECTION = "users";
    private static final String USER_COLLECTION_INVITATIONS_FIELD = "invitations";
    private static final String GROUP_COLLECTION = "groups";
    private static final String GROUP_COLLECTION_MEMBERS_FIELD = "members";
    private static final String TASK_COLLECTION = "tasks";
    private static final String TRANSACTION_COLLECTION = "transactions";

    private static final String TAG = "SyncProcedure";

    public static void getUser(String field, String value, Consumer<com.google.android.gms.tasks.Task> action) {
        FirebaseFirestore.getInstance().collection(USER_COLLECTION)
                .whereEqualTo(field, value)
                .get()
                .addOnCompleteListener(action::accept);
    }

    public static void getUser(String uid, Consumer<DocumentSnapshot> action) {
        FirebaseFirestore.getInstance().collection(USER_COLLECTION)
                .document(uid)
                .get()
                .addOnSuccessListener(action::accept);
    }

    public static void addUser(User user, String targetDocument, Consumer<Void> success, Consumer<Exception> failure) {
        FirebaseFirestore.getInstance().collection(FirestoreRequests.USER_COLLECTION)
                .document(targetDocument)
                .set(user)
                .addOnSuccessListener(success::accept)
                .addOnFailureListener(failure::accept);
    }

    public static void updateUser(User user, Consumer<Void> success, Consumer<Exception> failure) {
        addUser(user, user.getId(), success, failure);
    }

    public static void removeUser(String uid, Consumer<Void> success, Consumer<Exception> failure) {
        FirebaseFirestore.getInstance().collection(USER_COLLECTION)
                .document(uid)
                .delete()
                .addOnSuccessListener(success::accept)
                .addOnFailureListener(failure::accept);
    }

    public static void addInvitation(String uid, String gid, Consumer<Void> success, Consumer<Exception> failure) {
        FirebaseFirestore.getInstance().collection(USER_COLLECTION)
                .document(uid)
                .update(USER_COLLECTION_INVITATIONS_FIELD,
                        FieldValue.arrayUnion(gid))
                .addOnSuccessListener(success::accept)
                .addOnFailureListener(failure::accept);
    }

    public static void removeInvitation(String gid, String uid, Consumer<Void> success, Consumer<Exception> failure) {
        FirebaseFirestore.getInstance().collection(USER_COLLECTION)
                .document(uid)
                .update(USER_COLLECTION_INVITATIONS_FIELD,
                        FieldValue.arrayRemove(gid))
                .addOnSuccessListener(success::accept)
                .addOnFailureListener(failure::accept);
    }

    public static void getGroup(final String gid, final Consumer<DocumentSnapshot> action) {
        FirebaseFirestore.getInstance().collection(GROUP_COLLECTION)
                .document(gid)
                .get()
                .addOnSuccessListener(action::accept);
    }

    public static void getGroup(String field, String value, Consumer<com.google.android.gms.tasks.Task> action) {
        FirebaseFirestore.getInstance().collection(GROUP_COLLECTION)
                .whereArrayContains(field, value)
                .get()
                .addOnCompleteListener(action::accept);
        FirebaseFirestore.getInstance().collection(GROUP_COLLECTION)
                .whereArrayContains(field, value)
                .addSnapshotListener((v, e) -> {
                    DataManager.getInstance().setDataHasChanged(true);
                    Log.d(TAG, "Sync group");
                });
    }

    public static void addGroup(Group group, Consumer<DocumentReference> success, Consumer<Exception> failure) {
        FirebaseFirestore.getInstance().collection(GROUP_COLLECTION)
                .add(group)
                .addOnSuccessListener(success::accept)
                .addOnFailureListener(failure::accept);
    }

    public static void addGroup(Group group, String uid, Consumer<Void> success, Consumer<Exception> failure) {
        FirebaseFirestore.getInstance().collection(GROUP_COLLECTION)
                .document(uid)
                .set(group)
                .addOnSuccessListener(success::accept)
                .addOnFailureListener(failure::accept);
    }

    public static void updateGroup(Group group, String targetDocument, Consumer<Void> success, Consumer<Exception> failure) {
        FirebaseFirestore.getInstance().collection(GROUP_COLLECTION)
                .document(targetDocument)
                .set(group)
                .addOnSuccessListener(success::accept)
                .addOnFailureListener(failure::accept);
    }

    public static void removeGroup(String gid, Consumer<Void> success, Consumer<Exception> failure) {
        FirebaseFirestore.getInstance().collection(GROUP_COLLECTION)
                .document(gid)
                .delete()
                .addOnSuccessListener(success::accept)
                .addOnFailureListener(failure::accept);
    }

    public static void addGroupMember(String gid, String uid, Consumer<Void> success, Consumer<Exception> failure) {
        FirebaseFirestore.getInstance().collection(GROUP_COLLECTION)
                .document(gid)
                .update(GROUP_COLLECTION_MEMBERS_FIELD,
                        FieldValue.arrayUnion(uid))
                .addOnSuccessListener(success::accept)
                .addOnFailureListener(failure::accept);
    }

    public static void removeGroupMember(String gid, String uid, Consumer<Void> success, Consumer<Exception> failure) {
        FirebaseFirestore.getInstance().collection(GROUP_COLLECTION)
                .document(gid)
                .update(GROUP_COLLECTION_MEMBERS_FIELD,
                        FieldValue.arrayRemove(uid))
                .addOnSuccessListener(success::accept)
                .addOnFailureListener(failure::accept);
    }

    public static void getTasks(String gid, Consumer<com.google.android.gms.tasks.Task> action) {
        FirebaseFirestore.getInstance().collection(GROUP_COLLECTION)
                .document(gid)
                .collection(TASK_COLLECTION)
                .get()
                .addOnCompleteListener(action::accept);
        FirebaseFirestore.getInstance().collection(GROUP_COLLECTION)
                .document(gid)
                .collection(TASK_COLLECTION)
                .addSnapshotListener((v, e) -> {
                    DataManager.getInstance().setDataHasChanged(true);
                    Log.d(TAG, "Sync tasks");
                });
    }

    public static void addTask(Task userTask, Consumer<DocumentReference> success, Consumer<Exception> failure) {
        FirebaseFirestore.getInstance().collection(GROUP_COLLECTION)
                .document(userTask.getGroup())
                .collection(TASK_COLLECTION)
                .add(userTask)
                .addOnSuccessListener(success::accept)
                .addOnFailureListener(failure::accept);
    }

    public static void updateTask(Task task, Consumer<Void> success, Consumer<Exception> failure) {
        FirebaseFirestore.getInstance().collection(GROUP_COLLECTION)
                .document(task.getGroup())
                .collection(TASK_COLLECTION)
                .document(task.getId())
                .set(task)
                .addOnSuccessListener(success::accept)
                .addOnFailureListener(failure::accept);
    }

    public static void removeTask(Task task, Consumer<Void> success, Consumer<Exception> failure) {
        FirebaseFirestore.getInstance().collection(GROUP_COLLECTION)
                .document(task.getGroup())
                .collection(TASK_COLLECTION)
                .document(task.getId())
                .delete()
                .addOnSuccessListener(success::accept)
                .addOnFailureListener(failure::accept);
    }

    public static void addTransaction(Transaction transaction, Consumer<DocumentReference> success, Consumer<Exception> failure) {
        FirebaseFirestore.getInstance().collection(GROUP_COLLECTION)
                .document(transaction.getGroup())
                .collection(TRANSACTION_COLLECTION)
                .add(transaction)
                .addOnSuccessListener(success::accept)
                .addOnFailureListener(failure::accept);
    }

    public static void getTransactions(String gid, Consumer<com.google.android.gms.tasks.Task> action) {
        FirebaseFirestore.getInstance().collection(GROUP_COLLECTION)
                .document(gid)
                .collection(TRANSACTION_COLLECTION)
                .get()
                .addOnCompleteListener(action::accept);
        FirebaseFirestore.getInstance().collection(GROUP_COLLECTION)
                .document(gid)
                .collection(TRANSACTION_COLLECTION)
                .addSnapshotListener((v, e) -> {
                    DataManager.getInstance().setDataHasChanged(true);
                    Log.d(TAG, "Sync transactions");
                });
    }
}
