package com.budziaszek.tabmate.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.budziaszek.tabmate.R;
import com.budziaszek.tabmate.activity.MainActivity;
import com.budziaszek.tabmate.firestoreData.DataManager;
import com.budziaszek.tabmate.firestoreData.Group;
import com.budziaszek.tabmate.firestoreData.Transaction;
import com.budziaszek.tabmate.view.adapter.TransactionsItemsAdapter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BudgetFragment extends BasicFragment {

    private static final String TAG = "BudgetFragmentProcedure";

    private List<Group> groups = new ArrayList<>();
    private Integer currentGroup;

    private Activity activity;

    private TextView budgetBalance;
    private TextView group;

    private TransactionsItemsAdapter transactionsAdapter;
    private List<Transaction> transactions = new ArrayList<>();
    
    //private FirestoreRequests firestoreRequests = new FirestoreRequests();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "Created");
        fView = inflater.inflate(R.layout.budget, container, false);

        budgetBalance = fView.findViewById(R.id.balance);
        group = fView.findViewById(R.id.group);

        //Refresh
        swipeLayout = fView.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(() -> {
            if (!DataManager.getInstance().isRefreshFinished())
                return;

            Log.d(TAG, "Ask for refresh groups and users");
            DataManager.getInstance().refresh(((MainActivity) activity).getCurrentUserId());

            informAboutNetworkConnection();
        });

        activity = getActivity();
        informAboutNetworkConnection();

        // Transactions
        RecyclerView transactionsRecycler = fView.findViewById(R.id.transaction_history_list);
        transactionsAdapter = new TransactionsItemsAdapter(transactions, getContext(),
                position -> {
                    ((MainActivity) activity).setCurrentTransaction(transactions.get(position));
                    ((MainActivity) activity).startFragment(TransactionFragment.class);
                });

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(fView.getContext());
        transactionsRecycler.setLayoutManager(mLayoutManager);
        transactionsRecycler.setItemAnimator(new DefaultItemAnimator());
        transactionsRecycler.setAdapter(transactionsAdapter);

        DataManager instance = DataManager.getInstance();
        instance.addObserver(this);
        if (instance.getGroups() == null) {
            showProgress(true);
            Log.d(TAG, "Ask for refresh groups and users");
            instance.refresh(((MainActivity) activity).getCurrentUserId());
        } else {
            groupsChanged();
            transactionsChanged();
        }

        Button newTransactionButton = fView.findViewById(R.id.new_transation_button);
        newTransactionButton.setOnClickListener(view -> {
            ((MainActivity)activity).setCurrentGroup(groups.get(currentGroup));
            ((MainActivity)activity).startFragment(TransactionFragment.class);
        });

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
        if(currentGroup == null)
            return;
        menu.clear();
        getActivity().getMenuInflater().inflate(R.menu.menu_budget, menu);

        SubMenu sub = menu.findItem(R.id.group_item).getSubMenu();
        int count = 0;
        for (Group group : DataManager.getInstance().getGroups()) {
            MenuItem item = sub.add(1, count, count, group.getName());
            item.setCheckable(true);
            item.setChecked(currentGroup == count);
            count++;
        }
        sub.setGroupCheckable(1, true, true);

        Log.d(TAG, "items added");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Integer id = item.getItemId();
        if(id >= 0 && id < groups.size()) {
            currentGroup = id;
            item.setChecked(true);
            transactions.clear();
            transactionsAdapter.update(transactions);
            transactionsAdapter.notifyDataSetChanged();
            transactionsChanged();
            Log.d(TAG, currentGroup + " currentGroup");
            showGroupBudget(id);
        }
        return true;
    }

    @Override
    public void groupsChanged() {
        Log.d(TAG, "Transactions changed");
        groups = DataManager.getInstance().getGroups();
        if(currentGroup == null || groups.size() < currentGroup - 1){
            currentGroup = 0;
        }
        if(groups.size() != 0)
            showGroupBudget(currentGroup);
    }

    @Override
    public void transactionsChanged() {
        List<Transaction> allTransactions = DataManager.getInstance().getTransactions();
        List<Transaction> newTransactions = new ArrayList<>();
        List<Transaction> oldTransactions = transactions;

        for (Transaction transaction : allTransactions) {
            if (transaction.getGroup().equals(groups.get(currentGroup).getId())) {
                newTransactions.add(transaction);
            }
        }
        newTransactions.sort(Comparator.comparing(Transaction::getDate).reversed());
        transactions = newTransactions;
        transactionsAdapter.update(transactions);

        //TODO check what exactly changed
        for (int i = 0; i < newTransactions.size(); i++) {
            if (oldTransactions.size() <= i) {
                transactionsAdapter.notifyItemInserted(i);
            } else {
                Transaction newTransaction = newTransactions.get(i);
                Transaction oldTransaction = oldTransactions.get(i);
                if (!oldTransaction.equals(newTransaction)) {
                    transactionsAdapter.notifyItemChanged(i);
                }
            }
        }
        for (int i = newTransactions.size(); i < oldTransactions.size(); i++) {
            transactionsAdapter.notifyItemRemoved(i);
        }
    }

    private void showGroupBudget(Integer i){
        Double balance = groups.get(i).getBudgetBalance();
        if(balance == null)
            balance = 0.0;
        String balanceString = balance + " " + "zł";
        budgetBalance.setText(balanceString);
        group.setText(groups.get(i).getName());
    }
}
