package com.budziaszek.tabmate.fragment;

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
import com.budziaszek.tabmate.data.DataManager;
import com.budziaszek.tabmate.data.Group;
import com.budziaszek.tabmate.data.Transaction;
import com.budziaszek.tabmate.view.adapter.TransactionsItemsAdapter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BudgetFragment extends BasicFragment {

    private static final String TAG = "BudgetFragmentProcedure";

    private List<Group> groups = new ArrayList<>();
    private Integer currentGroup;

    private TextView budgetBalance;
    private TextView group;
    private List<Transaction> transactions = new ArrayList<>();
    private TransactionsItemsAdapter transactionsAdapter;

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
            informAboutDataSynchronization();
        });

        activity = getActivity();
        informAboutNetworkConnection();
        informAboutDataSynchronization();

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
            setLineChartBudget();
        }

        Button newTransactionButton = fView.findViewById(R.id.new_transation_button);
        newTransactionButton.setOnClickListener(view -> {
            ((MainActivity) activity).setCurrentGroup(groups.get(currentGroup));
            ((MainActivity) activity).setCurrentTransaction(null);
            ((MainActivity) activity).startFragment(TransactionFragment.class);
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
        if (currentGroup == null)
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Integer id = item.getItemId();
        if (id == R.id.action_find) {
            ((MainActivity) activity).setBackEnabled(true);
            ((MainActivity) activity).setFiltrateGroups(false);
            ((MainActivity) activity).startFragment(FiltrateFragment.class);
            return true;
        }else if (id >= 0 && id < groups.size()) {
            currentGroup = id;
            ((MainActivity) activity).setCurrentGroup(groups.get(id));
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
        groups = DataManager.getInstance().getGroups();
        if (currentGroup == null) {
            currentGroup = groups.indexOf(((MainActivity) activity).getCurrentGroup());
        }
        if (currentGroup == -1 || groups.size() < currentGroup - 1) {
            currentGroup = 0;
        }
        if (groups.size() != 0) {
            showGroupBudget(currentGroup);
        }
    }

    @Override
    public void transactionsChanged() {
        Boolean removeOption = false;
        if(!DataManager.getInstance().getSelectedGroupsIds().contains(groups.get(currentGroup).getId())) {
            DataManager.getInstance().addFiltrationOptionGroup(groups.get(currentGroup).getId());
            removeOption = true;
        }
        List<Transaction> allTransactions = DataManager.getInstance().getFiltratedTransactions();
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
        if(removeOption)
            DataManager.getInstance().removeFiltrationOptionGroup(groups.get(currentGroup).getId());
        setLineChartBudget();
    }

    private void setLineChartBudget() {
        Log.d(TAG, "Update chart");

        LineChart lineChartBudget = fView.findViewById(R.id.budget_linear);
        Description description = new Description();
        description.setText("");
        lineChartBudget.setDescription(description);
        lineChartBudget.setDragEnabled(false);
        lineChartBudget.setScaleEnabled(false);

        lineChartBudget.getXAxis().setEnabled(false);
        lineChartBudget.getAxisRight().setEnabled(false);

        Legend legend = lineChartBudget.getLegend();
        legend.setEnabled(false);

        List<Entry> entries = new ArrayList<>();
        if (transactions.size() == 0) {
            lineChartBudget.clear();
            return;
        }
        Transaction transaction = null;
        for (int i = transactions.size() - 1; transactions.size() - i < 100 && i >= 0; i--) {
            transaction = transactions.get(i);
            entries.add(new Entry((float) transactions.size() - i, transaction.getAmountBeforeTransaction().floatValue()));
        }
        if (transaction != null)
            entries.add(new Entry((float) transactions.size() + 1,
                    (float) (transaction.getAmountBeforeTransaction() + transaction.getAmount())));

        LineDataSet dataSet = new LineDataSet(entries, "");
        LineData data = new LineData(dataSet);
        lineChartBudget.setData(data);
        lineChartBudget.invalidate();
    }


    private void showGroupBudget(Integer i) {
        Double balance = groups.get(i).getBudgetBalance();
        budgetBalance.setText(groups.get(i).getStringBudgetBalance());
        group.setText(groups.get(i).getName());
        setLineChartBudget();
    }
}
