package pl.rozbijbank.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import pl.rozbijbank.R;
import pl.rozbijbank.databinding.FragmentSettingsBinding;
import pl.rozbijbank.db.entity.TaskEntity;
import pl.rozbijbank.other.NotificationPublisher;
import pl.rozbijbank.utilities.Constants;
import pl.rozbijbank.viewModel.TaskListViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends BasicFragment {

    private FragmentSettingsBinding mBinding;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     **/
    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mBinding= DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false);

        setClicks();
        setVisuals();
        return mBinding.getRoot();
    }

    private void setClicks(){
        mBinding.allowInternalBrowser.setOnCheckedChangeListener((buttonView, isChecked) ->
                preferences.edit().putBoolean(getString(R.string.key_allow_internal_browser), isChecked).apply());

        mBinding.allowTaskNotification.setOnClickListener(v ->{
            if(mBinding.allowTaskNotification.isChecked())
                showDaysDialog();
            else {
                NotificationPublisher notificationPublisher= new NotificationPublisher();
                ViewModelProviders.of(this).get(TaskListViewModel.class).getTasks().observe(this, taskEntities -> {
                    if (taskEntities!=null){
                        for (TaskEntity taskEntity: taskEntities)
                            notificationPublisher.deleteNotification(getActivity(), taskEntity);
                    }
                });


                preferences.edit().remove(getString(R.string.key_allow_task_notification)).apply();
            }
        });

        mBinding.skipLoging.setOnCheckedChangeListener((buttonView, isChecked) ->
                preferences.edit().putBoolean(getString(R.string.key_skip_login), isChecked).apply());

        mBinding.showAtStart.setOnClickListener(v -> showOnStartOptions());

    }

    private void setVisuals(){
        mBinding.allowInternalBrowser.setChecked(preferences.getBoolean(getString(R.string.key_allow_internal_browser), false));
        mBinding.skipLoging.setChecked(preferences.getBoolean(getString(R.string.key_skip_login), false));
        mBinding.allowTaskNotification.setChecked(preferences.getBoolean(getString(R.string.key_allow_task_notification), false));
    }

    private void showOnStartOptions() {
        final String[] contentOptions = getResources().getStringArray(R.array.ShowAtStartOptions);
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        mBuilder.setTitle(R.string.show_at_start);
        mBuilder.setSingleChoiceItems(contentOptions, preferences.getInt(getString(R.string.key_show_at_start_option), 1),
                (dialogInterface, i) -> {
                    preferences.edit().putInt(getString(R.string.key_show_at_start_option), i).apply();
                    dialogInterface.dismiss();
                });

        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }

    private void showDaysDialog() {

        AlertDialog.Builder builder  = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));

        //...that's the layout i told you will inflate later
        View dialogView = View.inflate(getActivity(), R.layout.dialog_pick_days, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        ImageButton minus= dialogView.findViewById(R.id.minus);
        TextView amount= dialogView.findViewById(R.id.amount);
        ImageButton plus= dialogView.findViewById(R.id.plus);

        Button accept= dialogView.findViewById(R.id.btn_accept);
        Button decline= dialogView.findViewById(R.id.btn_decline);

        int days= preferences.getInt(getString(R.string.key_task_notification_days_amount), 5);
        amount.setText(String.valueOf(days));

        //CLICKS
        minus.setOnClickListener(v ->{
            int days2= Integer.parseInt( amount.getText().toString());
            if(days2>1)
            amount.setText(String.valueOf(days2-1));
        });

        plus.setOnClickListener(v ->{
            int days2= Integer.parseInt( amount.getText().toString());
            if(days2<30)
            amount.setText(String.valueOf(days2+1));
        });

        accept.setOnClickListener(v ->{
            preferences.edit().putBoolean(getString(R.string.key_allow_task_notification), true).apply();
            preferences.edit().putInt(getString(R.string.key_task_notification_days_amount), Integer.parseInt(amount.getText().toString())).apply();

            NotificationPublisher notificationPublisher= new NotificationPublisher();
            ViewModelProviders.of(this).get(TaskListViewModel.class).getTasks().observe(this, taskEntities -> {
                if (taskEntities!=null){
                    for (TaskEntity taskEntity: taskEntities)
                        notificationPublisher.scheduleNotification(getActivity(), taskEntity);
                }
            });

            dialog.dismiss();
        });

        decline.setOnClickListener(v -> {
            mBinding.allowTaskNotification.setChecked(false);
            dialog.dismiss();
        });

        dialog.show();
    }

    @NotNull
    @Override
    public String toString() {
        return Constants.TAG_SETTINGS_FRAGMENT;
    }

}
