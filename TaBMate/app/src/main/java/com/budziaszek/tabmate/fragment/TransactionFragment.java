package com.budziaszek.tabmate.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.budziaszek.tabmate.R;
import com.budziaszek.tabmate.activity.MainActivity;
import com.budziaszek.tabmate.firestoreData.DataManager;
import com.budziaszek.tabmate.firestoreData.FirestoreRequests;
import com.budziaszek.tabmate.firestoreData.Group;
import com.budziaszek.tabmate.firestoreData.Transaction;
import com.budziaszek.tabmate.view.InformUser;
import com.budziaszek.tabmate.view.KeyboardManager;

import java.util.Calendar;

public class TransactionFragment extends BasicFragment implements DatePickerDialog.OnDateSetListener,
        NumberPicker.OnValueChangeListener {

    private static final String TAG = "transactionFragmentProcedure";
    private Activity activity;

    private View fView;

    private Transaction transaction;

    private Boolean isEdited;
    private Boolean isCreated;

    private TextView transactionTitle;
    private TextView transactionDescription;
    private TextView transactionAmount;
    private TextView transactionTitleInput;
    private TextView transactionDescriptionInput;
    private TextView transactionAmountInput;
    private TextView transactionGroup;
    private TextView transactionDate;

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
            }
            return true;
        } else if (id == R.id.action_remove) {
            alertRemoveTransaction();
            return true;
        }
        return false;
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

        TextView transactionGroup = fView.findViewById(R.id.transaction_group);
        Group group = DataManager.getInstance().getGroup(transaction.getGroup());
        if (group != null)
            transactionGroup.setText(group.getName());
    }

    private void setEditing(Boolean edit) {
        isEdited = edit;

        transactionGroup.setText(transaction.getGroup());

        if (edit) {
            transactionTitleInput.setVisibility(View.VISIBLE);
            transactionDescriptionInput.setVisibility(View.VISIBLE);
            transactionAmountInput.setVisibility(View.VISIBLE);

            transactionTitle.setVisibility(View.INVISIBLE);
            transactionDescription.setVisibility(View.INVISIBLE);
            transactionAmount.setVisibility(View.INVISIBLE);

            if (isCreated) {
                transactionTitleInput.setText("");
                transactionDescriptionInput.setText("");
            } else {
                transactionTitleInput.setText(transactionTitle.getText());
                transactionDescriptionInput.setText(transactionDescription.getText());
            }

            Calendar calendar = Calendar.getInstance();
            transaction.setDate(calendar.getTime());
            transactionDate.setText(transaction.getDateString());
            /*transactionDate.setOnClickListener(view -> {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);
                DatePickerDialog picker = new DatePickerDialog(getContext(), TransactionFragment.this, year, month, day);
                picker.show();
            });*/
            //transactionDate.setBackgroundColor(getResources().getColor(R.color.colorAccentLight, activity.getTheme()));

        } else {
            transactionTitleInput.setVisibility(View.INVISIBLE);
            transactionDescriptionInput.setVisibility(View.INVISIBLE);
            transactionAmountInput.setVisibility(View.INVISIBLE);

            transactionTitle.setVisibility(View.VISIBLE);
            transactionTitle.setText(transactionTitleInput.getText().toString());

            transactionDescription.setVisibility(View.VISIBLE);
            transactionDescription.setText(transactionDescriptionInput.getText().toString());

            transactionAmount.setVisibility(View.VISIBLE);
            transactionAmount.setText(transactionAmountInput.getText().toString());

            transactionDate.setText(transaction.getDateString());
            //transactionDate.setOnClickListener(view -> {
            //});
            //transactionDate.setBackgroundColor(Color.TRANSPARENT);

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
        transaction.setAmount(amount);
        transaction.setTitle(title);
        transaction.setDescription(transactionDescriptionInput.getText().toString());

        //Save data
        if (isCreated) {
            firestoreRequests.addTransaction(transaction,
                    (x) -> {
                        InformUser.inform(activity, R.string.transaction_added);
                        Group group = ((MainActivity)activity).getCurrentGroup();
                        if(group.getBudgetBalance() == null)
                            group.setBudgetBalance(0.0);
                        group.setBudgetBalance(group.getBudgetBalance() + amount);
                        firestoreRequests.updateGroup(group, group.getId(),
                                (y) -> {
                                DataManager.getInstance().refresh(((MainActivity)activity).getCurrentUserId());
                                },
                                (e) -> {}
                                );
                    },
                    (e) -> InformUser.informFailure(activity, e));
            ((MainActivity) activity).startFragment(BudgetFragment.class);
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
