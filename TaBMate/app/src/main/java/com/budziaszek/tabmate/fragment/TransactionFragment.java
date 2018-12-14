package com.budziaszek.tabmate.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.opengl.Visibility;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.budziaszek.tabmate.R;
import com.budziaszek.tabmate.activity.MainActivity;
import com.budziaszek.tabmate.firestoreData.DataManager;
import com.budziaszek.tabmate.firestoreData.FirestoreRequests;
import com.budziaszek.tabmate.firestoreData.Group;
import com.budziaszek.tabmate.firestoreData.Transaction;
import com.budziaszek.tabmate.view.InformUser;
import com.budziaszek.tabmate.view.KeyboardManager;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class TransactionFragment extends BasicFragment implements DatePickerDialog.OnDateSetListener,
        NumberPicker.OnValueChangeListener {

    private static final String TAG = "transactionFragmentProcedure";
    private Activity activity;

    private View fView;

    private Transaction transaction;

    private Boolean isEdited;
    private Boolean isCreated;

    private Switch switch_in_out;

    private TextView transactionTitle;
    private TextView transactionDescription;
    private TextView transactionAmount;
    private TextView transactionGroup;
    private TextView transactionDate;
    private TextView transactionCategory;
    private TextView transactionSubcategory;

    private TextView transactionTitleInput;
    private TextView transactionDescriptionInput;
    private TextView transactionAmountInput;
    private Spinner transactionCategoryInput;
    private Spinner transactionSubcategoryInput;
    private ArrayAdapter<String> dataAdapter;
    private ArrayAdapter<String> dataAdapterMain;

    private FirestoreRequests firestoreRequests = new FirestoreRequests();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "Created");
        fView = inflater.inflate(R.layout.transaction, container, false);

        activity = getActivity();
        transaction = ((MainActivity) activity).getCurrentTransaction();

        transactionTitle = fView.findViewById(R.id.transaction_title);
        transactionDescription = fView.findViewById(R.id.transaction_description);
        transactionTitleInput = fView.findViewById(R.id.transaction_title_input);
        transactionDescriptionInput = fView.findViewById(R.id.transaction_description_input);
        transactionGroup = fView.findViewById(R.id.transaction_group);
        transactionAmount = fView.findViewById(R.id.transaction_amount);
        transactionAmountInput = fView.findViewById(R.id.transaction_amount_input);
        transactionDate = fView.findViewById(R.id.transaction_date);
        ((TextView)fView.findViewById(R.id.transaction_currency)).setText(((MainActivity)activity).getCurrentGroup().getCurrency());
        transactionCategory = fView.findViewById(R.id.transaction_category);
        transactionSubcategory = fView.findViewById(R.id.transaction_subcategory);
        transactionCategoryInput = fView.findViewById(R.id.transaction_category_spinner);
        transactionSubcategoryInput = fView.findViewById(R.id.transaction_subcategory_spinner);

        transactionCategoryInput.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String category = adapterView.getSelectedItem().toString();
                transactionSubcategoryInput.setVisibility(View.VISIBLE);
                transactionSubcategory.setVisibility(View.VISIBLE);
                fView.findViewById(R.id.label_transaction_subcategory).setVisibility(View.VISIBLE);
                switch (category) {
                    case "Food":
                        updateAdapter(false, R.array.expenses_food, -1);
                        break;
                    case "Bills":
                        updateAdapter(false, R.array.expenses_bills, -1);
                        break;
                    case "Health care":
                        updateAdapter(false, R.array.expenses_health, -1);
                        break;
                    case "Sport and recreation":
                        updateAdapter(false, R.array.expenses_sport, -1);
                        break;
                    case "Home maintenance":
                        updateAdapter(false, R.array.expenses_home, -1);
                        break;
                    case "Transportation":
                        updateAdapter(false, R.array.expenses_transportation, -1);
                        break;
                    case "Personal":
                        updateAdapter(false, R.array.expenses_personal, -1);
                        break;
                    case "Gifts":
                        updateAdapter(false, R.array.expenses_gifts, -1);
                        break;
                    default:
                        transactionSubcategoryInput.setVisibility(View.GONE);
                        transactionSubcategory.setVisibility(View.GONE);
                        fView.findViewById(R.id.label_transaction_subcategory).setVisibility(View.GONE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        transactionCategoryInput.setSelection(8);

        switch_in_out = fView.findViewById(R.id.switch_in_out);
        switch_in_out.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            switchInOut(isChecked);
        });

        // Add transaction
        if (transaction == null) {
            TextView title = fView.findViewById(R.id.details_title);
            title.setText(R.string.add_new_transaction);
            transaction = new Transaction();
            transaction.setGroup(((MainActivity)activity).getCurrentGroup().getId());
            isCreated = true;
            setEditing(true);
        } else {
            isCreated = false;
            setEditing(false);
        }

        switchInOut(false);
        showTransaction();
        ((MainActivity) activity).enableBack(true);
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
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();    //remove all items
        getActivity().getMenuInflater().inflate(R.menu.menu_details, menu);

        MenuItem edit = menu.findItem(R.id.action_edit);
        MenuItem save = menu.findItem(R.id.action_save);
        MenuItem remove = menu.findItem(R.id.action_remove);

        if (isEdited) {
            edit.setVisible(false);
            save.setVisible(true);
            remove.setVisible(false);
        } else {
            edit.setVisible(false);
            save.setVisible(false);
            remove.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_edit) {
            setEditing(true);
            activity.invalidateOptionsMenu();
            return true;
        } else if (id == R.id.action_save) {
            if (update()) {
                setEditing(false);
                activity.invalidateOptionsMenu();
                KeyboardManager.hideKeyboard(activity);
                ((MainActivity)activity).enableBack(false);
            }
            return true;
        } else if (id == R.id.action_remove) {
            alertRemoveTransaction();
            return true;
        }
        return false;
    }

    private void updateAdapter(Boolean isMainCategory, int resourcesArray, int position){
        if(isMainCategory) {
            dataAdapterMain = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item,
                    Arrays.asList(getResources().getStringArray(resourcesArray)));
            dataAdapterMain.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            transactionCategoryInput.setAdapter(dataAdapterMain);
            transactionCategoryInput.setSelection(position >= 0 ? position : dataAdapterMain.getCount()-1);
        }else{
            dataAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item,
                    Arrays.asList(getResources().getStringArray(resourcesArray)));
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            transactionSubcategoryInput.setAdapter(dataAdapter);
            transactionSubcategoryInput.setSelection(position >= 0 ? position : dataAdapter.getCount()-1);
        }
    }

    private void switchInOut(boolean isChecked){
        if(isChecked){
            updateAdapter(true, R.array.income, 0);
            transactionSubcategoryInput.setVisibility(View.GONE);
            transactionSubcategory.setVisibility(View.GONE);
            fView.findViewById(R.id.label_transaction_subcategory).setVisibility(View.GONE);
        }else{
            updateAdapter(true, R.array.expenses, 8);
        }
    }

    /**
     * Displays current transaction data.
     */
    private void showTransaction() {
        if(transaction.getAmount()!= null) {
            String amount = transaction.getAmount().toString();
            transactionAmount.setText(amount);
        }
        transactionTitle.setText(transaction.getTitle());
        transactionDescription.setText(transaction.getDescription());
        transactionTitleInput.setText(transaction.getTitle());
        transactionDescriptionInput.setText(transaction.getDescription());
        transactionDate.setText(transaction.getDateString());
        transactionCategory.setText(transaction.getCategory());
        transactionSubcategory.setText(transaction.getSubcategory());

        TextView transactionGroup = fView.findViewById(R.id.transaction_group);
        Group group = DataManager.getInstance().getGroup(transaction.getGroup());
        if (group != null)
            transactionGroup.setText(group.getName());
    }

    private void setEditing(Boolean edit) {
        isEdited = edit;

        int inputVisibility;
        int displayVisibility;
        int switchVisibility;

        if(edit){
            inputVisibility = View.VISIBLE;
            displayVisibility = View.INVISIBLE;
            switchVisibility = View.VISIBLE;
        } else {
            inputVisibility = View.INVISIBLE;
            displayVisibility = View.VISIBLE;
            switchVisibility = View.GONE;
        }

        try {
            transactionGroup.setText(transaction.getGroup());
            transactionTitle.setText(transactionTitleInput.getText().toString());
            transactionDescription.setText(transactionDescriptionInput.getText().toString());
            transactionAmount.setText(transactionAmountInput.getText().toString());
            transactionDate.setText(transaction.getDateString());
            transactionCategory.setText(transactionCategoryInput.getSelectedItem().toString());
            transactionSubcategory.setText(transactionSubcategoryInput.getSelectedItem().toString());
        }catch (Exception e){
            Log.d(TAG, "Error setting text");
        }

        switch_in_out.setVisibility(switchVisibility);
        fView.findViewById(R.id.expenses).setVisibility(switchVisibility);
        fView.findViewById(R.id.income).setVisibility(switchVisibility);

        transactionTitleInput.setVisibility(inputVisibility);
        transactionDescriptionInput.setVisibility(inputVisibility);
        transactionAmountInput.setVisibility(inputVisibility);
        transactionSubcategoryInput.setVisibility(inputVisibility);
        transactionCategoryInput.setVisibility(inputVisibility);
        fView.findViewById(R.id.label_transaction_subcategory).setVisibility(inputVisibility);

        transactionTitle.setVisibility(displayVisibility);
        transactionDescription.setVisibility(displayVisibility);
        transactionAmount.setVisibility(displayVisibility);
        transactionSubcategory.setVisibility(displayVisibility);
        transactionCategory.setVisibility(displayVisibility);

        if (!isCreated) {
            transactionTitleInput.setText(transactionTitle.getText());
            transactionDescriptionInput.setText(transactionDescription.getText());
        }

        Calendar calendar = Calendar.getInstance();
        transaction.setDate(calendar.getTime());
        transactionDate.setText(transaction.getDateString());
        if(edit) {
            transactionDate.setOnClickListener(view -> {
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);
                DatePickerDialog picker = new DatePickerDialog(getContext(), TransactionFragment.this, year, month, day);
                picker.show();
            });
            transactionDate.setBackgroundColor(getResources().getColor(R.color.colorPrimaryTransparent, activity.getTheme()));
        }else{
            transactionDate.setOnClickListener(view -> {});
            transactionDate.setBackgroundColor(Color.TRANSPARENT);
        }

        if(!edit){
            if(transaction.getSubcategory() == null) {
                transactionSubcategoryInput.setVisibility(View.GONE);
                transactionSubcategory.setVisibility(View.GONE);
                fView.findViewById(R.id.label_transaction_subcategory).setVisibility(View.GONE);
            }
            if(transaction.getDescription() == null || transaction.getDescription().equals("")) {
                transactionDescription.setVisibility(View.GONE);
                transactionDescriptionInput.setVisibility(View.GONE);
                fView.findViewById(R.id.label_transaction_description).setVisibility(View.GONE);
            }
        }else{
            fView.findViewById(R.id.label_transaction_description).setVisibility(View.VISIBLE);
        }
    }

    private boolean update() {
        String title = transactionTitleInput.getText().toString();

        transactionTitleInput.setError(null);
        transactionAmountInput.setError(null);
        Boolean error = false;

        if(transactionAmountInput.getText().toString().equals("")
                || Double.parseDouble(transactionAmountInput.getText().toString()) == 0)
        {
            InformUser.inform(activity, R.string.error_amount);
            transactionAmountInput.setError(getActivity().getResources().getString(R.string.error_amount));
            error = true;
        }
        if(title.equals("")){
            InformUser.inform(activity, R.string.error_field_required);
            transactionTitleInput.setError(getActivity().getResources().getString(R.string.error_field_required));
            error = true;
        }
        if(error)
            return false;

        //Set data
        Double amount = Double.parseDouble(transactionAmountInput.getText().toString());
        transaction.setAmount(switch_in_out.isChecked() ? amount : -amount);
        transaction.setTitle(title);
        transaction.setDescription(transactionDescriptionInput.getText().toString());
        transaction.setCategory(transactionCategoryInput.getSelectedItem().toString());
        try {
            transaction.setSubcategory(transactionSubcategoryInput.getSelectedItem().toString());
        }catch (Exception e){
            Log.d(TAG, "No subcategory");
        }
        Group group = ((MainActivity)activity).getCurrentGroup();
        if(group.getBudgetBalance() == null)
            group.setBudgetBalance(0.0);
        group.setBudgetBalance(group.getBudgetBalance() + amount);
        transaction.setAmountBeforeTransaction(group.getBudgetBalance() - transaction.getAmount());

        //Save data
        if (isCreated) {
            firestoreRequests.addTransaction(transaction,
                    (x) -> {
                        InformUser.inform(activity, R.string.transaction_added);
                        firestoreRequests.updateGroup(group, group.getId(),
                                (y) -> {
                                DataManager.getInstance().refresh(((MainActivity)activity).getCurrentUserId());
                                },
                                (e) -> {}
                                );
                    },
                    (e) -> InformUser.informFailure(activity, e));
            //((MainActivity) activity).startFragment(BudgetFragment.class);
            activity.onBackPressed();
        } else {
            /*firestoreRequests.updateTransaction(transaction,
                    (x) -> {
                    },
                    (e) -> InformUser.informFailure(activity, e)
            );*/
        }
        return true;


    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, monthOfYear, dayOfMonth);
        transaction.setDate(calendar.getTime());
        transactionDate.setText(transaction.getDateString());
        DataManager.getInstance().refresh(((MainActivity)activity).getCurrentUserId());
    }

    /**
     * Displays alert and removes transaction if submitted.
     */
    public void alertRemoveTransaction() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, android.R.style.Theme_Material_Dialog_Alert);

        /*builder.setTitle(R.string.remove_transaction)
                .setMessage(R.string.confirm_remove_transaction)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    DataManager.getInstance().removetransaction(transaction, activity);
                    DataManager.getInstance().refreshAllGroupstransactions();
                    activity.onBackPressed();
                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();*/
    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int i, int i1) {

    }
}
