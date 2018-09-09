package com.budziaszek.tabmate;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class GroupsFragment extends Fragment {

    private View fView;

    private Button joinGroupButton;
    private Button createGroupButton;
    private ViewFlipper flipper;
    private Button submitGroupButton;

    private MembersRecyclerAdapter mAdapter;
    private List<User> users;

    private String mNewMemberEmail = null;
    private String mNewMemberId = null;

    private List<Group> groups = new ArrayList<Group>();
    private Integer currentGroup = null;

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
        users = new ArrayList<User>();
        fView = inflater.inflate(R.layout.groups_fragment, container, false);
        db = FirebaseFirestore.getInstance();
        initializeButtons();
        flipper = (ViewFlipper) fView.findViewById(R.id.flipper);
        readGroup(FirebaseAuth.getInstance().getCurrentUser().getUid());

        return fView;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    //TODO set some options visible or invisible
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return false;
        }else if(id == R.id.action_new_member){
            alertNewMember();
            return true;
        }

        return false;
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

                SpecialFunction.hideKeyboard(getActivity());

                addNewGroup(id, name, description);
            }
        });
    }

    private void addNewGroup(String id, String name, String description) {
        flipper.setDisplayedChild(FlipperPage.PROGRESS.getValue());
        Group newGroup = new Group(name + "_" + id, name, description, Arrays.asList(id));

        db.collection(DatabaseInformation.GROUP_COLLECTION)
                .document(name + "_" + id)
                .set(newGroup)
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

    private void readUser(String id){
        db.collection(DatabaseInformation.USER_COLLECTION)
                .document(id)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    User user = documentSnapshot.toObject(User.class);
                    users.add(user);
                    mAdapter.notifyDataSetChanged();
                }
            });
    }

    private void getUsers(List<String> usersId){
        for(String uid : usersId){
            readUser(uid);
        }
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

                                getUsers(group.getMembers());

                                mAdapter = new MembersRecyclerAdapter(users);

                                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(fView.getContext());
                                members.setLayoutManager(mLayoutManager);
                                members.setItemAnimator(new DefaultItemAnimator());
                                members.setAdapter(mAdapter);

                                groupName.setText(group.getName());
                                groupDescription.setText(group.getDescription());

                                groups.add(group);

                                foundGroup = true;
                                currentGroup = 0;
                                break;
                                //TODO add multiple groups to view
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

    private void alertNewMember(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),android.R.style.Theme_Material_Dialog_Alert);
        builder.setTitle("Enter user email");

        // Set up the input
        final EditText input = new EditText(getActivity());
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        input.setPadding(20, 20, 20, 20);
        input.setBackgroundColor(getResources().getColor(R.color.colorAccentLightSemi, getActivity().getTheme()));

        FrameLayout container = new FrameLayout(getActivity());
        FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(30, 10, 30, 10);
        input.setLayoutParams(params);
        container.addView(input);

        builder.setView(container);

        // Set up the buttons
        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mNewMemberEmail = input.getText().toString();
                sendInvitation(groups.get(currentGroup).getName());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void sendInvitation(final String group){
        //TODO check if it is correct here
        findUserByEmailAndAddInvitation();
    }
    private void findUserByEmailAndAddInvitation(){
        //Find
        db.collection(DatabaseInformation.USER_COLLECTION)
                .whereEqualTo("email", mNewMemberEmail)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> documents = task.getResult().getDocuments();
                            if(documents.isEmpty()){
                                Toast.makeText(fView.getContext(), "User not found",
                                        Toast.LENGTH_SHORT).show();
                            }
                            User user = documents.get(0).toObject(User.class);
                            mNewMemberId = user.getId();

                            //Add
                            db.collection(DatabaseInformation.USER_COLLECTION)
                                    .document(mNewMemberId)
                                    .update(DatabaseInformation.USER_COLLECTION_INVITATION_FIELD,
                                            FieldValue.arrayUnion(groups.get(currentGroup).getId()));

                            Toast.makeText(fView.getContext(), "Invitation sent to " + mNewMemberEmail, Toast.LENGTH_SHORT).show();
                            Log.d("INVITATION", "user found " + mNewMemberId);
                        } else {
                            Toast.makeText(fView.getContext(), task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
