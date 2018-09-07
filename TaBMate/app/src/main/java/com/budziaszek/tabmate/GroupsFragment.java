package com.budziaszek.tabmate;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class GroupsFragment extends Fragment {

    private View view;

    private Button joinGroupButton;
    private Button createGroupButton;
    private ViewFlipper flipper;
    private Button submitGroupButton;

    private final String GROUP_COLLECTION = "Groups";
    private final String GROUP_DOCUMENT = "Groups list";
    private final String NAME_KEY = "Name";
    private final String DESCRIPTION_KEY = "Description";
    private final String ID_KEY = "id";

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
        view = inflater.inflate(R.layout.groups_fragment, container, false);
        db = FirebaseFirestore.getInstance();
        initializeButtons();
        flipper = (ViewFlipper) view.findViewById(R.id.flipper);
        flipper.setDisplayedChild(FlipperPage.NO_GROUP.getValue());

        return view;
    }
    private void initializeButtons(){
        joinGroupButton = (Button) view.findViewById(R.id.join_button);
        joinGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "Join",
                        Toast.LENGTH_SHORT).show();
            }
        });

        createGroupButton = (Button) view.findViewById(R.id.create_button);
        createGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(view.getContext(), "Create",
                //Toast.LENGTH_SHORT).show();
                flipper.setDisplayedChild(FlipperPage.CREATE_GROUP.getValue());
            }
        });

        submitGroupButton = (Button) view.findViewById(R.id.submit_create_group);
        submitGroupButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                addNewGroup();
            }
        });
    }

    private void addNewGroup() {
        flipper.setDisplayedChild(FlipperPage.PROGRESS.getValue());

        Map<String, Object> newGroup = new HashMap<>();

        EditText groupNameEdit = (EditText) view.findViewById(R.id.edit_group_name);
        EditText groupDescriptionEdit = (EditText) view.findViewById(R.id.edit_group_description);

        newGroup.put(NAME_KEY, groupNameEdit.getText().toString());
        newGroup.put(DESCRIPTION_KEY, groupDescriptionEdit.getText().toString());
        newGroup.put(ID_KEY, "0");

        db.collection(GROUP_COLLECTION).document(GROUP_DOCUMENT).set(newGroup)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(view.getContext(), "Group created",
                                Toast.LENGTH_SHORT).show();
                        flipper.setDisplayedChild(FlipperPage.VIEW_GROUP.getValue());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(view.getContext(), "ERROR" + e.toString(),
                                Toast.LENGTH_SHORT).show();
                        Log.d("TAG", e.toString());
                        flipper.setDisplayedChild(FlipperPage.CREATE_GROUP.getValue());
                    }
                });
    }
}
