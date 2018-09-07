package com.budziaszek.tabmate;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.Arrays;



public class GroupsFragment extends Fragment {

    private View fView;

    private Button joinGroupButton;
    private Button createGroupButton;
    private ViewFlipper flipper;
    private Button submitGroupButton;

    private final String GROUP_COLLECTION = "groups";

    public enum FlipperPage{

        PROGRESS(0),
        NO_GROUP(1),
        CREATE_GROUP(2),
        VIEW_GROUP(3);

        private final int id;

        FlipperPage(int id) { this.id = id; }
        public int getValue() { return id; }
    }

    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fView = inflater.inflate(R.layout.groups_fragment, container, false);
        db = FirebaseFirestore.getInstance();
        initializeButtons();
        flipper = (ViewFlipper) fView.findViewById(R.id.flipper);
        readGroup(FirebaseAuth.getInstance().getCurrentUser().getUid());

        return fView;
    }
    private void initializeButtons(){
        joinGroupButton = (Button) fView.findViewById(R.id.join_button);
        joinGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "Join",
                        Toast.LENGTH_SHORT).show();
            }
        });

        createGroupButton = (Button) fView.findViewById(R.id.create_button);
        createGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flipper.setDisplayedChild(FlipperPage.CREATE_GROUP.getValue());
            }
        });

        submitGroupButton = (Button) fView.findViewById(R.id.submit_create_group);
        submitGroupButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                EditText groupNameEdit = (EditText) fView.findViewById(R.id.edit_group_name);
                EditText groupDescriptionEdit = (EditText) fView.findViewById(R.id.edit_group_description);

                String name =  groupNameEdit.getText().toString();
                String description =  groupDescriptionEdit.getText().toString();
                String id = FirebaseAuth.getInstance().getCurrentUser().getUid();

                addNewGroup(id, name, description);
            }
        });
    }

    private void addNewGroup(String id, String name, String description) {
        flipper.setDisplayedChild(FlipperPage.PROGRESS.getValue());
       Group newGroup = new Group(name + "_" + id, name, description, Arrays.asList(id));

       db.collection(GROUP_COLLECTION).document(name + "_" + id).set(newGroup)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(fView.getContext(), "Group created",
                                Toast.LENGTH_SHORT).show();
                        readGroup(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(fView.getContext(), "ERROR" + e.toString(),
                                Toast.LENGTH_SHORT).show();
                        Log.d("TAG", e.toString());
                        flipper.setDisplayedChild(FlipperPage.CREATE_GROUP.getValue());
                    }
                });
    }

    private void readGroup(String id) {
        flipper.setDisplayedChild(FlipperPage.PROGRESS.getValue());
        db.collection("groups")
                .whereArrayContains("members", id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            boolean foundGroup = false;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //Toast.makeText(fView.getContext(), document.getId() + " => " + document.getData(),
                                        //Toast.LENGTH_SHORT).show();
                                Group group = document.toObject(Group.class);

                                TextView groupName = (TextView) fView.findViewById(R.id.group_name);
                                TextView groupDescription = (TextView) fView.findViewById(R.id.group_description);
                                RecyclerView members = (RecyclerView) fView.findViewById(R.id.members_list);
                                MembersRecyclerAdapter mAdapter = new MembersRecyclerAdapter(group.getMembers());

                                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(fView.getContext());
                                members.setLayoutManager(mLayoutManager);
                                members.setItemAnimator(new DefaultItemAnimator());
                                members.setAdapter(mAdapter);

                                groupName.setText(group.getName());
                                groupDescription.setText(group.getDescription());

                                foundGroup = true;
                                break;//TODO add multiple groups
                            }
                            if(!foundGroup){
                                flipper.setDisplayedChild(FlipperPage.NO_GROUP.getValue());
                                return;
                            }
                            else{
                                flipper.setDisplayedChild(FlipperPage.VIEW_GROUP.getValue());
                            }
                        } else {
                            Toast.makeText(fView.getContext(), task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
