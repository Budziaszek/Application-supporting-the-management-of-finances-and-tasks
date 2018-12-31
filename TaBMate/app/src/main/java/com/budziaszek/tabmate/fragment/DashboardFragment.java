package com.budziaszek.tabmate.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.budziaszek.tabmate.R;
import com.budziaszek.tabmate.activity.MainActivity;
import com.budziaszek.tabmate.data.DataManager;
import com.budziaszek.tabmate.data.Group;
import com.budziaszek.tabmate.data.Task;
import com.budziaszek.tabmate.data.Transaction;
import com.budziaszek.tabmate.data.User;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


//TODO refresh
public class DashboardFragment extends BasicFragment implements IAxisValueFormatter {

    private static final String TAG = "DashboardFragmentProcedure";

    //private Activity activity;

    private List<Group> groups = new ArrayList<>();
    private List<User> users = new ArrayList<>();
    private List<Task> tasks = new ArrayList<>();
    private List<Transaction> transactions = new ArrayList<>();
    private List<Integer> colors = new ArrayList<>();
    private Integer currentGroup;
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat simpleDateFormatSmall = new SimpleDateFormat("dd-MM");

    //private FirestoreRequests firestoreRequests = new FirestoreRequests();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "Created");
        fView = inflater.inflate(R.layout.dashboard, container, false);

        activity = getActivity();
        colors.add(getResources().getColor(R.color.c1, getResources().newTheme()));
        colors.add(getResources().getColor(R.color.c2, getResources().newTheme()));
        colors.add(getResources().getColor(R.color.c3, getResources().newTheme()));
        colors.add(getResources().getColor(R.color.c4, getResources().newTheme()));
        colors.add(getResources().getColor(R.color.c5, getResources().newTheme()));
        colors.add(getResources().getColor(R.color.c6, getResources().newTheme()));
        colors.add(getResources().getColor(R.color.c7, getResources().newTheme()));
        colors.add(getResources().getColor(R.color.c8, getResources().newTheme()));
        colors.add(getResources().getColor(R.color.c9, getResources().newTheme()));

        //mDisplayView = fView.findViewById(R.id.show_dashboard_layout);
        //mProgressView = fView.findViewById(R.id.progress_dashboard);

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
            refreshFinished();
        }

        informAboutNetworkConnection();
        informAboutDataSynchronization();
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
            refreshFinished();
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
            update();
        }
    }

    @Override
    public void tasksChanged() {
        List<Task> allTasks = DataManager.getInstance().getFiltratedTasks();
        List<Task> newTasks = new ArrayList<>();

        for(Task task:allTasks){
            if(task.getGroup().equals(((MainActivity) activity).getCurrentGroup().getId()))
                newTasks.add(task);
        }

        tasks = newTasks;
    }

    @Override
    public void transactionsChanged() {
        List<Transaction> allTransactions = DataManager.getInstance().getFiltratedTransactions();
        List<Transaction> newTransactions = new ArrayList<>();

        for(Transaction transaction:allTransactions){
            if(transaction.getGroup().equals(((MainActivity) activity).getCurrentGroup().getId()))
                newTransactions.add(transaction);
        }

        transactions = newTransactions;
    }

    private void setPieChartTasks() {
        PieChart pieChartTasks = fView.findViewById(R.id.pie_chart);
        Description description = new Description();
        description.setTextSize(12);
        try {
            description.setTextColor(getResources().getColor(R.color.colorPrimaryDark, activity.getTheme()));
        }catch(Exception e){return;}
        description.setText(getResources().getString(R.string.tasks_done_pie));
        pieChartTasks.setDescription(description);
        pieChartTasks.setDrawHoleEnabled(false);
        pieChartTasks.setEntryLabelColor(getResources().getColor(R.color.colorPrimaryDark, activity.getTheme()));
        pieChartTasks.setEntryLabelTextSize(8);

        Legend legend = pieChartTasks.getLegend();
        legend.setTextColor(getResources().getColor(R.color.colorPrimaryDark, activity.getTheme()));
        legend.setForm(Legend.LegendForm.CIRCLE);

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(countTasksByStatus(Task.Status.TODO, false, false), Task.Status.TODO.name));
        entries.add(new PieEntry(countTasksByStatus(Task.Status.DOING, false, false), Task.Status.DOING.name));
        entries.add(new PieEntry(countTasksByStatus(Task.Status.DONE, false, false), Task.Status.DONE.name));

        PieDataSet dataSet = new PieDataSet(entries, "");
//        dataSet.setValueFormatter((value, entry, dataSetIndex, viewPortHandler) -> {
//            if(value!=0)
//                return String.valueOf(value);
//            return "";
//        });
        dataSet.setColors(new int[]{R.color.c3, R.color.c2, R.color.c8}, getContext());
        dataSet.setValueTextColor(getResources().getColor(R.color.colorPrimaryDark, activity.getTheme()));
        dataSet.setValueTextSize(12);
        dataSet.setValueFormatter((value, entry, dataSetIndex, viewPortHandler) -> Integer.toString(((int) value)));

        PieData data = new PieData(dataSet);
        pieChartTasks.setData(data);
        pieChartTasks.invalidate(); // refresh

        //mLineData.notifyDataChanged();
        //mChart.notifyDataSetChanged();
    }

    private void setBarChartTasks(boolean time) {
        BarChart barChartTasks;
        if (time)
            barChartTasks = fView.findViewById(R.id.bar_chart2);
        else
            barChartTasks = fView.findViewById(R.id.bar_chart1);
        Description description = new Description();
        description.setTextSize(12);
        description.setTextColor(getResources().getColor(R.color.colorPrimaryDark, activity.getTheme()));
        description.setYOffset(-10);
        try {
        if (time)
            description.setText(getResources().getString(R.string.time_spent_bar));
        else
            description.setText(getResources().getString(R.string.tasks_done_bar));
        }catch(Exception e){return;}
        barChartTasks.setDescription(description);
        barChartTasks.setDragEnabled(false);
        barChartTasks.setScaleEnabled(false);
        barChartTasks.setPinchZoom(false);
        barChartTasks.setDrawGridBackground(false);

        XAxis xAxis = barChartTasks.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setCenterAxisLabels(true);
        xAxis.setValueFormatter(this);

        Legend legend = barChartTasks.getLegend();
        legend.setTextColor(getResources().getColor(R.color.colorPrimaryDark, activity.getTheme()));

        barChartTasks.getAxisLeft().setEnabled(false);
        barChartTasks.getAxisRight().setEnabled(false);

        int groupCount = users.size() > colors.size() ? colors.size() : users.size();
        float groupSpace = 0.01f;
        float barSpace = 0.01f;
        float barWidth = 0.99f / groupCount - 0.01f;

        float startValue = 0;
        if (groupCount == 1)
            startValue = 0.5f;
        List<List<BarEntry>> values = new ArrayList<>();
        for (int u = 0; u < groupCount; u++) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, -6);
            values.add(new ArrayList<>());
            for (float i = startValue; i < 7 + startValue; i++) {
                if (time)
                    values.get(u).add(new BarEntry(i, countTimeSpentByUserByDate(users.get(u).getId(), calendar.getTime())));
                else
                    values.get(u).add(new BarEntry(i, countCompletedTasksByUserByDate(users.get(u).getId(), calendar.getTime())));
                calendar.add(Calendar.DATE, 1);
            }
        }

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        for (int u = 0; u < groupCount; u++) {
            BarDataSet s = new BarDataSet(values.get(u), users.get(u).getName());
            s.setColor(colors.get(u));
            //set.add(s);
            dataSets.add(s);
        }
        BarData data = new BarData(dataSets);
        data.setValueFormatter((value, entry, dataSetIndex, viewPortHandler) -> {
            if (value != 0)
                return String.valueOf(value);
            return "";
        });
        barChartTasks.setData(data);

        barChartTasks.getXAxis().setAxisMinimum(0);
        barChartTasks.getXAxis().setAxisMaximum(7);

        if (groupCount > 1) {
            barChartTasks.getBarData().setBarWidth(barWidth);
            barChartTasks.groupBars(0, groupSpace, barSpace);
        }
        barChartTasks.invalidate();
        if (users.size() > groupCount) {
            fView.findViewById(R.id.info_bar1).setVisibility(View.VISIBLE);
            fView.findViewById(R.id.info_bar2).setVisibility(View.VISIBLE);
        } else {
            fView.findViewById(R.id.info_bar1).setVisibility(View.GONE);
            fView.findViewById(R.id.info_bar2).setVisibility(View.GONE);
        }
    }

    private void setPieChartBudget(boolean expense) {
        Description description = new Description();
        description.setTextSize(9);
        description.setTextColor(getResources().getColor(R.color.colorPrimaryDark, activity.getTheme()));
        PieChart pieChartTasks;

        try {
            if (expense) {
                pieChartTasks = fView.findViewById(R.id.pie_chart_budget);
                description.setText(getResources().getString(R.string.budget_pie_expenses));
            } else {
                pieChartTasks = fView.findViewById(R.id.pie_chart_budget2);
                description.setText(getResources().getString(R.string.budget_pie_income));
            }
        }catch (Exception e){return;}


        pieChartTasks.setDescription(description);
        pieChartTasks.setDrawHoleEnabled(false);
        pieChartTasks.setEntryLabelTextSize(8);
        pieChartTasks.setDrawEntryLabels(false);

        Legend legend = pieChartTasks.getLegend();
        legend.setTextColor(getResources().getColor(R.color.colorPrimaryDark, activity.getTheme()));
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setWordWrapEnabled(true);

        List<PieEntry> entries = new ArrayList<>();
        List<String> categories;
        float total = 0;
        if(expense)
            categories = Arrays.asList(getResources().getStringArray(R.array.expenses));
        else
            categories = Arrays.asList(getResources().getStringArray(R.array.income));
        for (String category : categories) {
            float n = countTransactionsByCategoryByMonth(category, -1);
            entries.add(new PieEntry(n, category));
            total+=n;
        }

        if(expense)
            ((TextView)fView.findViewById(R.id.expenses_total_t)).setText(new DecimalFormat("#.##").format(total));
        else
            ((TextView)fView.findViewById(R.id.income_total_t)).setText(new DecimalFormat("#.##").format(total));


        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{R.color.c1, R.color.c2, R.color.c3, R.color.c4, R.color.c5, R.color.c6, R.color.c7, R.color.c8, R.color.c9}, getContext());
        dataSet.setValueTextColor(getResources().getColor(R.color.colorPrimaryDark, activity.getTheme()));
        dataSet.setValueTextSize(12);
        dataSet.setValueFormatter((value, entry, dataSetIndex, viewPortHandler) -> Integer.toString(((int) value)));

        PieData data = new PieData(dataSet);
        data.setValueTextColor(getResources().getColor(R.color.colorAccentLight, activity.getTheme()));
        data.setValueFormatter((value, entry, dataSetIndex, viewPortHandler) -> {
            if (value != 0)
                return String.valueOf(value);
            return "";
        });
        pieChartTasks.setData(data);
        pieChartTasks.invalidate(); // refresh
    }

    private int countTasksByStatus(Task.Status status, Boolean useDate, Boolean onTime) {
        int counter = 0;
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH);
        for (Task task : tasks) {
            if (task.getStatus().equals(status)) {
                if(!useDate)
                    counter++;
                else{
                    cal.setTime(task.getCompletionDate());
                    if (cal.get(Calendar.MONTH) == month)
                        if(onTime && task.getCompletionDate()!= null && task.getDeadline() != null){
                            if(task.getDeadline().getTime() >= task.getCompletionDate().getTime())
                                counter++;
                        }
                        else
                            counter++;
                }
            }
        }
        Log.d(TAG, status.name + " size " + counter);
        return counter;
    }

    private int countTasksAfterDeadline() {
        int counter = 0;
        Calendar cal = Calendar.getInstance();
        for (Task task : tasks) {
                if(task.getDeadline() != null){
                    if(task.getDeadline().getTime() < cal.getTimeInMillis())
                        counter++;
                }
        }
        Log.d(TAG, "after deadline size " + counter);
        return counter;
    }

    private float countTransactionsByCategoryByMonth(String category, int month) {
        float counter = 0;
        Calendar cal = Calendar.getInstance();
        if (month == -1) //current month
            month = cal.get(Calendar.MONTH);

        for (Transaction transaction : transactions) {
            if (transaction.getCategory().equals(category)) {
                cal.setTime(transaction.getDate());
                if (cal.get(Calendar.MONTH) == month) {
                    counter += Math.abs(transaction.getAmount());
                }
            }
        }
        Log.d(TAG, category + " size " + counter);
        return counter;
    }

    private int countCompletedTasksByUserByDate(String user, Date date) {
        int counter = 0;
        for (Task task : tasks) {
            if (task.getStatus() == Task.Status.DONE || task.getStatusBeforeArchive() == Task.Status.DONE) {
                if (task.getDoers().contains(user) && task.getCompletionDate() != null)
                    if (simpleDateFormat.format(task.getCompletionDate()).equals(simpleDateFormat.format(date)))
                        counter++;
            }
        }
        Log.d(TAG, user + " " + simpleDateFormat.format(date) + " size " + counter);
        return counter;
    }

    private int countTimeSpentByUserByDate(String user, Date date) {
        int counter = 0;
        for (Task task : tasks) {
            if (task.getTimeSpentByDate().containsKey(simpleDateFormat.format(date))) {
                if (task.getDoers().contains(user))
                    counter += task.getTimeSpentByDate().get(simpleDateFormat.format(date));
            }
        }
        Log.d(TAG, user + " " + simpleDateFormat.format(date) + " time spent " + counter);
        return counter / 60;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -((Float) (6f - value)).intValue());
        return simpleDateFormatSmall.format(calendar.getTime());
    }

    @Override
    public void refreshFinished() {
        super.refreshFinished();
        if (activity != null) {
            groupsChanged();
            tasksChanged();
            transactionsChanged();
            update();
        }
    }

    private void update(){
        ((TextView)fView.findViewById(R.id.group_name)).setText(((MainActivity)activity).getCurrentGroup().getName());
        users = DataManager.getInstance().getSelectedUsers();
        setPieChartTasks();
        setBarChartTasks(true);
        //setBarChartTasks(false);
        fView.findViewById(R.id.bar_chart1).setVisibility(View.GONE);
        setPieChartBudget(true);
        setPieChartBudget(false);
        ((TextView)fView.findViewById(R.id.tasks_finished_total_t))
                .setText(String.valueOf(
                        countTasksByStatus(Task.Status.DONE, true, false)
                                + countTasksByStatus(Task.Status.ARCHIVED, true, false)
                ));
        ((TextView)fView.findViewById(R.id.tasks_finished_on_time_t))
                .setText(String.valueOf(
                        countTasksByStatus(Task.Status.DONE, true, true)
                                + countTasksByStatus(Task.Status.ARCHIVED, true, true)
                ));
        ((TextView)fView.findViewById(R.id.tasks_after_deadline_t))
                .setText(String.valueOf(countTasksAfterDeadline()));
    }


}