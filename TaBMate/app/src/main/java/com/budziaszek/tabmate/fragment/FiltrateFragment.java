package com.budziaszek.tabmate.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.budziaszek.tabmate.R;
import com.budziaszek.tabmate.activity.MainActivity;
import com.budziaszek.tabmate.data.DataManager;
import com.budziaszek.tabmate.data.Group;
import com.budziaszek.tabmate.data.User;

import java.util.List;

public class FiltrateFragment extends BasicFragment {

    private static final String TAG = "FindTasksFragmentProcedure";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "Created");
        View fView = inflater.inflate(R.layout.filtrate, container, false);

        activity = getActivity();
        if(((MainActivity) activity).getFiltrateGroups()){
            fView.findViewById(R.id.find_group_layout).setVisibility(View.VISIBLE);
        }else{
            fView.findViewById(R.id.find_group_layout).setVisibility(View.GONE);
        }

        // Allow groups selection
        LinearLayout layout = fView.findViewById(R.id.groups_checkboxes);
        List<Group> groups = DataManager.getInstance().getGroups();
        for(Group group: groups){
            CheckBox cb = new CheckBox(activity);
            cb.setText(group.getName());
            cb.setChecked(DataManager.getInstance().getSelectedGroupsIds().contains(group.getId()));
            cb.setPadding(5, 5, 5, 5);
            cb.setTextSize(18);
            layout.addView(cb);
            cb.setOnClickListener(v -> {
                CheckBox checkBox = (CheckBox) v;
                if(checkBox.getText().equals(group.getName())) {
                    boolean checked = checkBox.isChecked();
                    if (checked) {
                        Log.d(TAG, ((CheckBox) v).getText().toString() + " clicked add");
                        DataManager.getInstance().addFiltrationOptionGroup(group.getId());
                    } else {
                        Log.d(TAG, ((CheckBox) v).getText().toString() + " clicked remove");
                        DataManager.getInstance().removeFiltrationOptionGroup(group.getId());
                    }
                }
            });
        }

        // Allow users selection
        LinearLayout layoutUsers = fView.findViewById(R.id.users_checkboxes);
        List<User> users = DataManager.getInstance().getUsers();

        for(User user: users){
            CheckBox cb = new CheckBox(activity);
            cb.setText(user.getName());
            cb.setChecked(DataManager.getInstance().getSelectedUsersIds().contains(user.getId()));
            cb.setPadding(5, 5, 5, 5);
            cb.setTextSize(18);
            layoutUsers.addView(cb);
            cb.setOnClickListener(v -> {
                CheckBox checkBox = (CheckBox) v;
                if(checkBox.getText().equals(user.getName())) {
                    boolean checked = checkBox.isChecked();
                    if (checked) {
                        Log.d(TAG, ((CheckBox) v).getText().toString() + " clicked add");
                        DataManager.getInstance().addFiltrationOptionUser(user.getId());
                    } else {
                        Log.d(TAG, ((CheckBox) v).getText().toString() + " clicked remove");
                        DataManager.getInstance().removeFiltrationOptionUser(user.getId());
                    }
                }
            });
        }
        CheckBox cb = new CheckBox(activity);
        cb.setText(R.string.no_users);
        cb.setChecked(DataManager.getInstance().getUserUnspecifiedSelected());
        cb.setPadding(5, 5, 5, 5);
        cb.setTextSize(18);
        layoutUsers.addView(cb);
        cb.setOnClickListener(v -> {
            CheckBox checkBox = (CheckBox) v;
            if(checkBox.getText().equals(getResources().getString(R.string.no_users))) {
                boolean checked = checkBox.isChecked();
                if (checked) {
                    Log.d(TAG, ((CheckBox) v).getText().toString() + " clicked true");
                    DataManager.getInstance().setUserUnspecifiedSelected(true);
                } else {
                    Log.d(TAG, ((CheckBox) v).getText().toString() + " false");
                    DataManager.getInstance().setUserUnspecifiedSelected(false);
                }
            }
        });
        fView.findViewById(R.id.accept_button).setOnClickListener(view -> activity.onBackPressed());

//        //Allow category selection
//        LinearLayout layoutCategories = fView.findViewById(R.id.category_checkboxes);
//        List<String> categories = Arrays.asList(getResources().getStringArray(R.array.expenses));
//
//        for(String category: categories){
//            cb = new CheckBox(activity);
//            cb.setText(category);
//            cb.setChecked(DataManager.getInstance().getSelectedCategories().contains(category));
//            cb.setPadding(5, 5, 5, 5);
//            cb.setTextSize(18);
//            layoutCategories.addView(cb);
//            cb.setOnClickListener(v -> {
//                CheckBox checkBox = (CheckBox) v;
//                if(checkBox.getText().equals(category)) {
//                    boolean checked = checkBox.isChecked();
//                    if (checked) {
//                        Log.d(TAG, ((CheckBox) v).getText().toString() + " clicked add");
//                        DataManager.getInstance().addSelectedCategory(category);
//                    } else {
//                        Log.d(TAG, ((CheckBox) v).getText().toString() + " clicked remove");
//                        DataManager.getInstance().removeSelectedCategory(category);
//                    }
//                }
//            });
//        }
//        Calendar calendar = Calendar.getInstance();
//        transaction.setDate(calendar.getTime());
//        fView.findViewById(R.id.date).setText(transaction.getDateString());
//        fView.findViewById(R.id.date).setOnClickListener(view -> {
//            int day = calendar.get(Calendar.DAY_OF_MONTH);
//            int month = calendar.get(Calendar.MONTH);
//            int year = calendar.get(Calendar.YEAR);
//            DatePickerDialog picker = new DatePickerDialog(getContext(), TransactionFragment.this, year, month, day);
//            picker.show();
//        });

        return fView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

}
