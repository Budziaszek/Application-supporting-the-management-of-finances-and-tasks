package com.budziaszek.tabmate.fragment;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.budziaszek.tabmate.R;
import com.budziaszek.tabmate.activity.MainActivity;
import com.budziaszek.tabmate.firestoreData.DataManager;
import com.budziaszek.tabmate.firestoreData.Group;
import com.budziaszek.tabmate.firestoreData.Transaction;
import com.budziaszek.tabmate.firestoreData.UserTask;
import com.budziaszek.tabmate.view.listener.DataChangeListener;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


//TODO refresh
public class DashboardFragment extends BasicFragment implements DataChangeListener {

    private static final String TAG = "DashboardFragmentProcedure";

    private Activity activity;

    private List<Group> groups = new ArrayList<>();
    private List<UserTask> tasks = new ArrayList<>();
    private List<Transaction> transactions = new ArrayList<>();

    private PieChart pieChartTasks;
    private PieDataSet dataSet;

    //private FirestoreRequests firestoreRequests = new FirestoreRequests();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "Created");
        fView = inflater.inflate(R.layout.dashboard, container, false);

        activity = getActivity();

        //mDisplayView = fView.findViewById(R.id.show_dashboard_layout);
        //mProgressView = fView.findViewById(R.id.progress_dashboard);

        //Refresh
        swipeLayout = fView.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(() -> {
            if(!DataManager.getInstance().isRefreshFinished())
                return;

            Log.d(TAG, "Ask for refresh groups and users");
            DataManager.getInstance().refresh(((MainActivity) activity).getCurrentUserId());

            informAboutNetworkConnection(); informAboutDataSynchronization();
        });
        swipeLayout.setColorSchemeColors(
                getResources().getColor(R.color.colorPrimary, getResources().newTheme()),
                getResources().getColor(R.color.colorAccent, getResources().newTheme()),
                getResources().getColor(R.color.colorAccentDark, getResources().newTheme()),
                getResources().getColor(R.color.colorAccent, getResources().newTheme()));

        DataManager instance = DataManager.getInstance();
        instance.addObserver(this);
        if (instance.getGroups() == null) {
            showProgress(true);
            //((MainActivity)activity).setDrawerVisible(false);
            Log.d(TAG, "Ask for refresh groups and users");
            instance.refresh(((MainActivity) activity).getCurrentUserId());
        } else {
            groupsChanged();
            tasksChanged();
            setPieChartTasks();
        }

        informAboutNetworkConnection(); informAboutDataSynchronization();
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
        getActivity().getMenuInflater().inflate(R.menu.menu_dashboard, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_find_tasks) {
            ((MainActivity) activity).enableBack(true);
            ((MainActivity) activity).startFragment(FindTasksFragment.class);
            return true;
        }
        return false;
    }

    //TODO refresh charts
    @Override
    public void groupsChanged() {
        List<Group> newGroups = DataManager.getInstance().getGroups();
        List<Group> oldGroups = groups;
        newGroups.sort(Comparator.comparing(Group::getName));
        groups = newGroups;
    }

    @Override
    public void tasksChanged(){
        List<UserTask> allTasks = DataManager.getInstance().getFiltratedTasks();
        List<UserTask> newTasks = new ArrayList<>();
        List<UserTask> oldTasks = tasks;
        tasks = allTasks;
        String uid = ((MainActivity)activity).getCurrentUserId();
    }

    @Override
    public void transactionsChanged() {
        List<Transaction> allTransactions = DataManager.getInstance().getTransactions();
        List<Transaction> newTransactions = new ArrayList<>();
        List<Transaction> oldTransactions = transactions;

        for (Transaction transaction : allTransactions) {
            if (transaction.getGroup().equals(((MainActivity)activity).getCurrentUserId())){
                newTransactions.add(transaction);
            }
        }
        newTransactions.sort(Comparator.comparing(Transaction::getDate).reversed());
        transactions = newTransactions;
    }

    private void setPieChartTasks(){
        pieChartTasks = fView.findViewById(R.id.chart);
        Description description = new Description();
        description.setText("");
        pieChartTasks.setDescription(description);
        pieChartTasks.setDrawHoleEnabled(false);
        pieChartTasks.setEntryLabelColor(getResources().getColor(R.color.colorPrimaryDark, activity.getTheme()));
        pieChartTasks.setEntryLabelTextSize(8);

        Legend legend = pieChartTasks.getLegend();
        legend.setTextColor(Color.WHITE);
        legend.setForm(Legend.LegendForm.CIRCLE);

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(countTasks(UserTask.Status.TODO), UserTask.Status.TODO.name));
        entries.add(new PieEntry(countTasks(UserTask.Status.DOING), UserTask.Status.DOING.name));
        entries.add(new PieEntry(countTasks(UserTask.Status.DONE), UserTask.Status.DONE.name));

        dataSet = new PieDataSet(entries, ""); // add entries to dataset
        dataSet.setColors(new int[] {R.color.colorItemTodo, R.color.colorItemDoing, R.color.colorItemDone}, getContext());
        dataSet.setValueTextColor(getResources().getColor(R.color.colorPrimaryDark, activity.getTheme()));
        dataSet.setValueTextSize(12);
        dataSet.setValueFormatter((value, entry, dataSetIndex, viewPortHandler) -> Integer.toString(((int) value)));

        PieData data = new PieData(dataSet);
        pieChartTasks.setData(data);
        pieChartTasks.invalidate(); // refresh

        //mLineData.notifyDataChanged();
        //mChart.notifyDataSetChanged();
    }

    private int countTasks(UserTask.Status status){
        int counter = 0;
        for(UserTask task : tasks){
            if(task.getStatus().equals(status)){
                counter++;
            }
        }
        Log.d(TAG, status.name + " size " + counter);
        return counter;
    }
}