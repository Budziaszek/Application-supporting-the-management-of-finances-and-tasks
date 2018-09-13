package com.budziaszek.tabmate;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class AddGroupFragment extends Fragment{

    private static final String TAG = "GroupProcedure";

    private View fView;
    private FirestoreRequests firestoreRequests = new FirestoreRequests();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fView = inflater.inflate(R.layout.add_group, container, false);

        Button submitGroupButton = fView.findViewById(R.id.submit_create_group);
        submitGroupButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                EditText groupNameEdit =  fView.findViewById(R.id.edit_group_name);
                EditText groupDescriptionEdit =  fView.findViewById(R.id.edit_group_description);

                String name =  groupNameEdit.getText().toString();
                String description =  groupDescriptionEdit.getText().toString();
                String id = ((MainActivity)getActivity()).getCurrentUserId();

                Manager.hideKeyboard(getActivity());

                addNewGroup(id, name, description);
            }
        });
        return fView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    /**
     * Attempts to create new group and save it to Firestore.
     */
    private void addNewGroup(String id, String name, String description) {
        Group newGroup = new Group(name + "_" + id, name, description);
        newGroup.addMember(id);

        firestoreRequests.addGroup(newGroup, name + " " + id,
                (Void) ->  {
                    InformUser.inform(getActivity(), TAG, "Group created");
                    ((MainActivity)getActivity()).startFragment(DisplayGroupFragment.class);
                },
                (e) ->
                    InformUser.informFailure(getActivity(), TAG, e)
                );
    }

}
