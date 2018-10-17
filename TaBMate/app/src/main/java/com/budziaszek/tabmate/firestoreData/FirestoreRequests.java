package com.budziaszek.tabmate.firestoreData;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.function.Consumer;

public class FirestoreRequests {

    private static final String USER_COLLECTION = "users";
    private static final String USER_COLLECTION_INVITATIONS_FIELD = "invitations";
    private static final String GROUP_COLLECTION = "groups";
    private static final String GROUP_COLLECTION_MEMBERS_FIELD = "members";
    private static final String TASK_COLLECTION = "tasks";
    private static final String TRANSACTION_COLLECTION = "transactions";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void addUser(User user, String targetDocument, Consumer<Void> success, Consumer<Exception> failure) {
        db.collection(FirestoreRequests.USER_COLLECTION)
                .document(targetDocument)
                .set(user)
                .addOnSuccessListener(success::accept)
                .addOnFailureListener(failure::accept);
    }

    public void updateUser(User user, Consumer<Void> success, Consumer<Exception> failure) {
        addUser(user, user.getId(), success, failure);
    }

    public void getUser(String uid, Consumer<DocumentSnapshot> action) {
        db.collection(USER_COLLECTION)
                .document(uid)
                .get()
                .addOnSuccessListener(action::accept);
    }

    public void getUserByField(String field, String value, Consumer<Task<QuerySnapshot>> action) {
        db.collection(USER_COLLECTION)
                .whereEqualTo(field, value)
                .get()
                .addOnCompleteListener(action::accept);
    }

    public void removeUser(String uid, Consumer<Void> success, Consumer<Exception> failure) {
        db.collection(USER_COLLECTION)
                .document(uid)
                .delete()
                .addOnSuccessListener(success::accept)
                .addOnFailureListener(failure::accept);
    }

    public void addGroup(Group group, Consumer<DocumentReference> success, Consumer<Exception> failure) {
        db.collection(GROUP_COLLECTION)
                .add(group)
                .addOnSuccessListener(success::accept)
                .addOnFailureListener(failure::accept);
    }

    public void addTask(UserTask userTask, Consumer<DocumentReference> success, Consumer<Exception> failure) {
        db.collection(GROUP_COLLECTION)
                .document(userTask.getGroup())
                .collection(TASK_COLLECTION)
                .add(userTask)
                .addOnSuccessListener(success::accept)
                .addOnFailureListener(failure::accept);
    }

    public void addTransaction(Transaction transaction, Consumer<DocumentReference> success, Consumer<Exception> failure) {
        db.collection(GROUP_COLLECTION)
                .document(transaction.getGroup())
                .collection(TRANSACTION_COLLECTION)
                .add(transaction)
                .addOnSuccessListener(success::accept)
                .addOnFailureListener(failure::accept);
    }

    public void updateTask(UserTask task, Consumer<Void> success, Consumer<Exception> failure) {
        db.collection(GROUP_COLLECTION)
                .document(task.getGroup())
                .collection(TASK_COLLECTION)
                .document(task.getId())
                .set(task)
                .addOnSuccessListener(success::accept)
                .addOnFailureListener(failure::accept);
    }

    public void getGroupTasks(String gid, Consumer<Task<QuerySnapshot>> action) {
        db.collection(GROUP_COLLECTION)
                .document(gid)
                .collection(TASK_COLLECTION)
                .get()
                .addOnCompleteListener(action::accept);
    }

    public void getGroupTransactions(String gid, Consumer<Task<QuerySnapshot>> action) {
        db.collection(GROUP_COLLECTION)
                .document(gid)
                .collection(TRANSACTION_COLLECTION)
                .get()
                .addOnCompleteListener(action::accept);
    }

    public void removeTask(UserTask task, Consumer<Void> success, Consumer<Exception> failure) {
        db.collection(GROUP_COLLECTION)
                .document(task.getGroup())
                .collection(TASK_COLLECTION)
                .document(task.getId())
                .delete()
                .addOnSuccessListener(success::accept)
                .addOnFailureListener(failure::accept);
    }

    public void updateGroup(Group group, String targetDocument, Consumer<Void> success, Consumer<Exception> failure) {
        db.collection(GROUP_COLLECTION)
                .document(targetDocument)
                .set(group)
                .addOnSuccessListener(success::accept)
                .addOnFailureListener(failure::accept);
    }

    public void getGroup(final String gid, final Consumer<DocumentSnapshot> action) {
        db.collection(GROUP_COLLECTION)
                .document(gid)
                .get()
                .addOnSuccessListener(action::accept);
    }

    public void removeGroup(String gid, Consumer<Void> success, Consumer<Exception> failure) {
        db.collection(GROUP_COLLECTION)
                .document(gid)
                .delete()
                .addOnSuccessListener(success::accept)
                .addOnFailureListener(failure::accept);
    }

    public void getGroupByField(String field, String value, Consumer<Task<QuerySnapshot>> action) {
        db.collection(GROUP_COLLECTION)
                .whereArrayContains(field, value)
                .get()
                .addOnCompleteListener(action::accept);
    }

    public void addGroupMember(String gid, String uid, Consumer<Void> success, Consumer<Exception> failure) {
        db.collection(GROUP_COLLECTION)
                .document(gid)
                .update(GROUP_COLLECTION_MEMBERS_FIELD,
                        FieldValue.arrayUnion(uid))
                .addOnSuccessListener(success::accept)
                .addOnFailureListener(failure::accept);
    }

    public void removeGroupMember(String gid, String uid, Consumer<Void> success, Consumer<Exception> failure) {
        db.collection(GROUP_COLLECTION)
                .document(gid)
                .update(GROUP_COLLECTION_MEMBERS_FIELD,
                        FieldValue.arrayRemove(uid))
                .addOnSuccessListener(success::accept)
                .addOnFailureListener(failure::accept);
    }

    public void addInvitation(String uid, String gid, Consumer<Void> success, Consumer<Exception> failure) {
        db.collection(USER_COLLECTION)
                .document(uid)
                .update(USER_COLLECTION_INVITATIONS_FIELD,
                        FieldValue.arrayUnion(gid))
                .addOnSuccessListener(success::accept)
                .addOnFailureListener(failure::accept);
    }

    public void removeInvitation(String gid, String uid, Consumer<Void> success, Consumer<Exception> failure) {
        db.collection(USER_COLLECTION)
                .document(uid)
                .update(USER_COLLECTION_INVITATIONS_FIELD,
                        FieldValue.arrayRemove(gid))
                .addOnSuccessListener(success::accept)
                .addOnFailureListener(failure::accept);
    }
}
