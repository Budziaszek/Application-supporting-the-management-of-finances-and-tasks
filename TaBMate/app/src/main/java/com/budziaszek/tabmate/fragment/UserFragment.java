package com.budziaszek.tabmate.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.budziaszek.tabmate.R;
import com.budziaszek.tabmate.activity.LoginActivity;
import com.budziaszek.tabmate.activity.MainActivity;
import com.budziaszek.tabmate.firestoreData.DataManager;
import com.budziaszek.tabmate.firestoreData.FirestoreRequests;
import com.budziaszek.tabmate.firestoreData.User;
import com.budziaszek.tabmate.view.InformUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class UserFragment extends BasicFragment {

    private static final String TAG = "UserFragmentProcedure";
    private Activity activity;

    private View fView;
    private TextView email;
    private TextView nick;

    private FirestoreRequests firestoreRequests = new FirestoreRequests();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "Created");
        fView = inflater.inflate(R.layout.user, container, false);

        activity = getActivity();

        email = fView.findViewById(R.id.email);
        email.setText(((MainActivity) getActivity()).getCurrentUserEmail());

        nick = fView.findViewById(R.id.task_description);
        nick.setText(DataManager.getInstance().getUsersInMap().get(((MainActivity) getActivity()).getCurrentUserId()).getName());

        Button changePasswordButton = fView.findViewById(R.id.change_password_button);
        changePasswordButton.setOnClickListener(view -> alertChangePassword());

        Button changeEmailButton = fView.findViewById(R.id.change_email_button);
        changeEmailButton.setOnClickListener(view -> alertChangeEmail());

        Button changeNickButton = fView.findViewById(R.id.change_nick_button);
        changeNickButton.setOnClickListener(view -> alertChangeNick());

        Button removeAccountButton = fView.findViewById(R.id.remove_account_button);
        removeAccountButton.setOnClickListener(view -> alertRemoveAccount());

        return fView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    /**
     * Displays alert and changes password if submitted.
     **/
    public void alertChangePassword() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, android.R.style.Theme_Material_Dialog_Alert);

        builder.setTitle(R.string.change_password)
                .setMessage(R.string.submit_change_password)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        FirebaseAuth.getInstance().sendPasswordResetEmail(Objects.requireNonNull(user.getEmail()))
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        InformUser.inform(activity, R.string.email_reset_sent);
                                        Log.d(TAG, "Email sent.");
                                    }
                                });
                    }
                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    /**
     * Displays alert and changes email if submitted.
     **/
    public void alertChangeEmail() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Material_Dialog_Alert);
        builder.setTitle(getResources().getString(R.string.enter_new_email));

        // Set up the input
        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        input.setPadding(20, 20, 20, 20);
        input.setBackgroundColor(getResources().getColor(R.color.colorAccentLightSemi, getActivity().getTheme()));

        FrameLayout container = new FrameLayout(getActivity());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(30, 10, 30, 10);
        input.setLayoutParams(params);
        container.addView(input);

        builder.setView(container);

        // Set up the buttons
        builder.setPositiveButton(getResources().getString(R.string.submit), (dialog, which) -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                user.updateEmail(input.getText().toString())
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser userChanged = FirebaseAuth.getInstance().getCurrentUser();
                                email.setText(Objects.requireNonNull(Objects.requireNonNull(userChanged).getEmail()));
                                ((MainActivity) activity).updateUser();

                                User userData = DataManager.getInstance().getUsersInMap().get(userChanged.getUid());
                                userData.setEmail(userChanged.getEmail());
                                firestoreRequests.updateUser(userData, (x) -> {
                                }, (e) -> {
                                });
                                DataManager.getInstance().refresh(userChanged.getUid());
                            } else if (task.getException() != null) {
                                InformUser.informFailure(activity, task.getException());
                            }
                        });
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.cancel), (dialog, which) -> dialog.cancel());
        builder.show();
    }

    /**
     * Displays alert and changes nick if submitted.
     **/
    public void alertChangeNick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Material_Dialog_Alert);
        builder.setTitle(getResources().getString(R.string.enter_new_nick));

        // Set up the input
        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        input.setPadding(20, 20, 20, 20);
        input.setBackgroundColor(getResources().getColor(R.color.colorAccentLightSemi, getActivity().getTheme()));

        FrameLayout container = new FrameLayout(getActivity());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(30, 10, 30, 10);
        input.setLayoutParams(params);
        container.addView(input);

        builder.setView(container);

        // Set up the buttons
        builder.setPositiveButton(getResources().getString(R.string.submit), (dialog, which) -> {
            User user = DataManager.getInstance().getUsersInMap().get(((MainActivity) getActivity()).getCurrentUserId());
            user.setName(input.getText().toString());
            firestoreRequests.updateUser(user, (x) -> {
            }, (x) -> {
            });
            DataManager.getInstance().refresh(user.getId());
            nick.setText(Objects.requireNonNull(user.getName()));
        });
        builder.setNegativeButton(getResources().getString(R.string.cancel), (dialog, which) -> dialog.cancel());
        builder.show();
    }

    /**
     * Shows alert when user choose log out action. If action is confirmed Firebase signOut is called
     * and activity finishes.
     */
    private void alertRemoveAccount() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, android.R.style.Theme_Material_Dialog_Alert);

        builder.setTitle(R.string.remove_account)
                .setMessage(R.string.are_you_sure)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        String uid = user.getUid();
                        user.delete()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "User account deleted.");
                                        firestoreRequests.removeUser(uid, (x) -> {}, (e) -> Log.e(TAG, e.getMessage()));
                                        //TODO remove from groups (members, tasks doers) but not doing it probably will not affect execution
                                        FirebaseAuth.getInstance().signOut();
                                        Intent myIntent = new Intent(activity, LoginActivity.class);
                                        activity.startActivity(myIntent);
                                    }
                                });

                    }
                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
