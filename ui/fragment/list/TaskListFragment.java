package pl.rozbijbank.ui.fragment.list;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import pl.rozbijbank.R;
import pl.rozbijbank.callback.TaskClickCallback;
import pl.rozbijbank.db.entity.TaskEntity;
import pl.rozbijbank.db.model.Task;
import pl.rozbijbank.utilities.Constants;
import pl.rozbijbank.ui.adapter.TaskViewAdapter;
import pl.rozbijbank.viewModel.TaskListViewModel;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link TaskClickCallback}
 * interface.
 */
public class TaskListFragment extends ComplexListFragment {

    private TaskViewAdapter activeListAdapter, inactiveListAdapter;
    private TaskClickCallback mListener;
    private TaskListViewModel viewModel;
    private Observer<List<TaskEntity>> observer;

    private long bankId=0;
    private int dateOption=0;
    private LiveData<List<TaskEntity>> liveData;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TaskListFragment() {
    }

    public static TaskListFragment newInstance() {
        TaskListFragment fragment = new TaskListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    //HELPERS
    private long getFromDate(){
        Calendar fromCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        fromCalendar.add(Calendar.HOUR, 2);
        switch (dateOption){
            case 0:
                break;
            case 1://this month
                fromCalendar.set(Calendar.DAY_OF_MONTH,  1);
                break;
            case 2://next 30
            case 3://next 60

        }

        fromCalendar.set(Calendar.HOUR_OF_DAY, 0);
        fromCalendar.set(Calendar.MINUTE, 0);
        fromCalendar.set(Calendar.SECOND, 0);
        fromCalendar.set(Calendar.MILLISECOND, 0);
        return fromCalendar.getTimeInMillis();
    }

    private long getToDate(){
        Calendar toCalendar= Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        toCalendar.add(Calendar.HOUR, 2);

        switch (dateOption){
            case 0:
                break;
            case 1://this_month
                toCalendar.set(Calendar.DATE, toCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                break;
            case 2://next 30
                toCalendar.add(Calendar.DAY_OF_YEAR, 30);
                break;
            case 3://next 60
                toCalendar.add(Calendar.DAY_OF_YEAR, 60);
                break;
        }

        toCalendar.set(Calendar.HOUR_OF_DAY, 23);
        toCalendar.set(Calendar.MINUTE, 59);
        toCalendar.set(Calendar.SECOND, 59);
        toCalendar.set(Calendar.MILLISECOND, 999);

        return toCalendar.getTimeInMillis();
    }

    //from basic
    @Override
    public void reloadItems() {
        if(liveData!=null)
            liveData.removeObservers(this);

        getBundle();

        if(bankId!=0){
            isFilterActive=true;

            if(dateOption!=0)
                liveData= viewModel.getTasks(bankId, getFromDate(), getToDate());
            else
                liveData=  viewModel.getTasks(bankId);

        }else {
            if(dateOption!=0){
                isFilterActive=true;
                liveData= viewModel.getTasks(getFromDate(), getToDate());
            } else{
                isFilterActive=false;
                liveData= viewModel.getTasks();
            }
        }

        liveData.observe(this, observer);
    }

    @Override
    protected void setAdapters() {
        activeListAdapter = new TaskViewAdapter(mListener);
        mBinding.activeList.setAdapter(activeListAdapter);

        inactiveListAdapter = new TaskViewAdapter(mListener);
        mBinding.inactiveList.setAdapter(inactiveListAdapter);
    }

    @Override
    protected void setViewModel() {
        viewModel = ViewModelProviders.of(TaskListFragment.this).get(TaskListViewModel.class);
    }

    @Override
    protected void setObserver() {
        observer = taskList -> {
            List<Task> active= new ArrayList<>();
            List<Task> inactive= new ArrayList<>();

            int count=0;
            if (taskList != null) {
                mBinding.setIsLoading(false);
                for (Task task: taskList)
                    if (task.getUserStartDate()!=null)
                        if (task.isCompleted())
                            inactive.add(task);
                        else
                            active.add(task);
                    else
                        count++;


                activeListAdapter.setTaskList(active);
                inactiveListAdapter.setTaskList(inactive);

                setEmptyText(taskList.isEmpty());

                if(inactive.isEmpty())
                    mBinding.inactiveButton.setVisibility(View.GONE);
                else
                    mBinding.inactiveButton.setVisibility(View.VISIBLE);

            } else {
                mBinding.setIsLoading(true);
            }

            mBinding.setIsFilterActive(isFilterActive);

            if(count>0){
                mBinding.temporaryNullDateInfo.setVisibility(View.VISIBLE);
                mBinding.temporaryNullDateInfo.setText(String.format("+ %s %s", String.valueOf(count), getString(R.string.require_sign_date)));
            }else
                mBinding.temporaryNullDateInfo.setVisibility(View.GONE);


            mBinding.executePendingBindings();
        };
    }

    @Override
    protected void getBundle() {
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(getActivity());
        bankId= preferences.getLong(getString(R.string.key_task_filter_bank_id), 0);
        dateOption= preferences.getInt(getString(R.string.key_task_filter_date_option), 0);
    }

    @Override
    protected void clearFilter() {
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(getActivity());
        preferences.edit().putInt(getString(R.string.key_task_filter_date_option), 0).apply();
        preferences.edit().putLong(getString(R.string.key_task_filter_bank_id), 0).apply();
    }

    @Override
    protected void setEmptyText(boolean value) {
        super.setEmptyText(value);
        if (isFilterActive)
            mBinding.emptyListText.setText(R.string.no_task_filter_active);
        else
            mBinding.emptyListText.setText(R.string.no_tasks);
    }

    @Override
    protected String setButtonText() {
        return getString(R.string.done);
    }

    @NotNull
    @Override
    public String toString() {
        return Constants.TAG_TASK_LIST_FRAGMENT;
    }

    //listener
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof TaskClickCallback) {
            mListener = (TaskClickCallback) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement TaskClickCallback");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
