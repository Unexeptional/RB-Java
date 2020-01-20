package pl.rozbijbank.ui.activity;

import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProviders;
import pl.rozbijbank.R;
import pl.rozbijbank.databinding.ActivityMyPromoBinding;
import pl.rozbijbank.db.converter.DateTypeConverter;
import pl.rozbijbank.db.entity.PromoEntity;
import pl.rozbijbank.db.entity.TaskEntity;
import pl.rozbijbank.db.model.Task;
import pl.rozbijbank.networking.ApiClient;
import pl.rozbijbank.networking.ApiInterface;
import pl.rozbijbank.networking.RetrofitCallback;
import pl.rozbijbank.networking.pojo.IdPojo;
import pl.rozbijbank.networking.pojo.addParticipationPojo;
import pl.rozbijbank.networking.pojo.participation.ParticipationUpdatePojo;
import pl.rozbijbank.other.NotificationPublisher;
import pl.rozbijbank.ui.adapter.TaskViewAdapter;
import pl.rozbijbank.utilities.Constants;
import pl.rozbijbank.viewModel.PromoViewModel;
import pl.rozbijbank.viewModel.TaskViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity does:
 * show promo and its tasks
 *
 * */
public class PromoActivity extends BasicActivity {

    private ActivityMyPromoBinding mBinding;
    private TaskViewAdapter taskViewAdapter, nullDateTaskViewAdapter;
    private PromoViewModel promoViewModel;
    private ApiInterface apiInterface;
    private PromoEntity activePromo;
    private String token;

    //BASIC
    @Override
    protected int provideActivityLayout() {
        return R.layout.activity_my_promo;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiInterface= ApiClient.getClient().create(ApiInterface.class);
        token = preferences.getString(getString(R.string.key_active_user_token), "");

        long promoId= getIntent().getLongExtra(Constants.KEY_PROMO_ID, 0);

        mBinding= (ActivityMyPromoBinding) dataBinding;

        //ViewModel for taking one promo
        PromoViewModel.Factory factory= new PromoViewModel.Factory(getApplication(), promoId);
        promoViewModel= ViewModelProviders.of(this, factory).get(PromoViewModel.class);

        //viewModel for binding
        mBinding.setPromoViewModel(promoViewModel);

        promoViewModel.getObservablePromo().observe(this, promoEntity -> {
            if (promoEntity != null){
                activePromo= promoEntity;
                setVisuals();
            }
        });

        setTaskViewAdapter();
        setClicks();
    }

    //CUSTOM
    private void setVisuals( ){
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("UTC"));

        TextView endDate= findViewById(R.id.card_promo_end_date);

        endDate.setText(String.format("%s  %s", getString(R.string.end_date),
                dateFormatGmt.format(activePromo.getEndDate())));

        if(activePromo.getParticipationId()!=0){
            mBinding.addToMyPromo.setText(R.string.delete_from_my_promos);
            mBinding.addToMyPromo.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.twotone_remove_black_24,0,0,0);
        } else{
            mBinding.addToMyPromo.setText(R.string.add_to_my_promos);
            mBinding.addToMyPromo.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.twotone_add_black_24,0,0,0);
        }

        mBinding.completedCheckbox.setChecked(activePromo.isCompleted());


        try {
            Glide.with(this).load(activePromo.getBanner()).into( mBinding.myPromoBanner);

        }catch (Exception ex){
            mBinding.myPromoBanner.setVisibility(View.GONE);
        }


        if(activePromo.getContractSigningDate()!=null){
            mBinding.myPromoSigningDate.setText(String.format("%s\n%s", getString(R.string.signing_date),
                    dateFormatGmt.format(activePromo.getContractSigningDate())));
        } else{
            mBinding.myPromoSigningDate.setText(getString(R.string.add_signing_date));
        }

        promoViewModel.setPromo(activePromo);

        //reaload tasks to get onBindViewHolder and set CLicks
        mBinding.taskList.setAdapter(taskViewAdapter);
        mBinding.nullDateTaskList.setAdapter(nullDateTaskViewAdapter);
        mBinding.myPromoBanner.requestFocus();
    }

    private void setTaskViewAdapter(){
        taskViewAdapter = new TaskViewAdapter(this);
        mBinding.taskList.setAdapter(taskViewAdapter);
        mBinding.taskList.setNestedScrollingEnabled(false);
        nullDateTaskViewAdapter = new TaskViewAdapter(this);
        mBinding.nullDateTaskList.setAdapter(nullDateTaskViewAdapter);
        mBinding.nullDateTaskList.setNestedScrollingEnabled(false);

        promoViewModel.getTasks().observe(this, taskList -> {

            List<Task> tasks= new ArrayList<>();
            List<Task> nullDateTasks= new ArrayList<>();

            if (taskList != null) {
                mBinding.setIsLoading(false);
                for (Task task:taskList)
                    if((task.getMonthAfterSigning()>0 || task.getDaysAfterSigning()>0) && task.getUserEndDate()==null)
                        nullDateTasks.add(task);
                    else
                        tasks.add(task);

                taskViewAdapter.setTaskList(tasks);

                if(!nullDateTasks.isEmpty()){
                    mBinding.nullDateListWrapper.setVisibility(View.VISIBLE);
                    mBinding.showNullDates.setVisibility(View.VISIBLE);
                    nullDateTaskViewAdapter.setTaskList(nullDateTasks);
                } else {
                    mBinding.nullDateListWrapper.setVisibility(View.GONE);
                    mBinding.showNullDates.setVisibility(View.GONE);
                }
                mBinding.myPromoBanner.requestFocus();
            } else
                mBinding.setIsLoading(true);

            mBinding.executePendingBindings();
            mBinding.myPromoBanner.requestFocus();
        });
    }

    //SET CLICKS AND HELPERS
    private void setClicks(){
        mBinding.showNullDates.setOnClickListener(v ->
                setNullDateButton(mBinding.nullDateListWrapper.getVisibility()==View.VISIBLE));

        mBinding.addToMyPromo.setOnClickListener(v -> {
            if(activePromo.getParticipationId()!=0){
                showDeleteDialog();
            }else
                addParticipationToAPi(activePromo.getId());
        });

        mBinding.completedCheckbox.setOnClickListener(v -> {
            activePromo.setCompleted(mBinding.completedCheckbox.isChecked());
            updateParticipationOnApi(false);
            mBinding.completedCheckbox.setChecked(!mBinding.completedCheckbox.isChecked());
        });

        mBinding.myPromoSigningDate.setOnClickListener(view -> {
            if(activePromo.getParticipationId()!=0 &&activePromo.getContractSigningDate()==null)
                showDatePicker();
        });

        mBinding.myPromoShowRegulations.setOnClickListener(v ->
                openBrowser(Uri.parse(activePromo.getRegulationsUri()), activePromo.getId()   ));
    }

    private void showDatePicker( ){
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        DatePickerDialog.OnDateSetListener date = (view1, year, month, day) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            activePromo.setContractSigningDate(calendar.getTime());

            updateParticipationOnApi( true);

        };


        new DatePickerDialog(this, date,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();

    }

    private void setNullDateButton(boolean value){
        if(value){
            mBinding.nullDateListWrapper.setVisibility(View.GONE);
            mBinding.showNullDates.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.baseline_expand_less_white_24,0);
        }else{
            mBinding.nullDateListWrapper.setVisibility(View.VISIBLE);
            mBinding.showNullDates.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.baseline_expand_more_white_24,0);
        }
    }

    //UPDATE PARTICIPATION
    public void updateParticipationOnApi(boolean datesMode){
        if(token.equals(""))
            updateParticipationRoom(datesMode);
        else {
            Call<Void> call= apiInterface.updateParticipation(activePromo.getParticipationId() ,
                    new ParticipationUpdatePojo(
                            activePromo.getParticipationId(),
                            DateTypeConverter.toLong(activePromo.getContractSigningDate()),
                            activePromo.isCompleted()));

            call.enqueue(new RetrofitCallback<>(this ,new Callback<Void>() {
                @Override
                public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                    if(response.code()==200)
                        updateParticipationRoom( datesMode);


                }

                @Override
                public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
                    if(!datesMode)
                        activePromo.setCompleted(!activePromo.isCompleted());
                }
            }));
        }
    }

    private void updateParticipationRoom(boolean datesMode){
        Date date= activePromo.getContractSigningDate();
        promoViewModel.updatePromoUserData(activePromo.getParticipationId(), date, activePromo.getId(), activePromo.isCompleted());

        if(datesMode){
            List<TaskEntity> tasks=promoViewModel.getTasks().getValue();
            if(tasks!=null){
                for(TaskEntity taskEntity: tasks){
                    if(taskEntity.getMonthAfterSigning()>0 && taskEntity.getDaysAfterSigning()>0){
                        doBothAfterSigning(taskEntity, date);
                    }else{
                        if(taskEntity.getMonthAfterSigning()>0)
                            doMonthAfterSign(taskEntity, date);
                        if(taskEntity.getDaysAfterSigning()>0)
                            doDaysAfterSign(taskEntity, date);
                    }
                }
            }
        }else
            mBinding.completedCheckbox.setChecked(activePromo.isCompleted());
    }

    //special!! -,-
    private  void doBothAfterSigning(TaskEntity taskEntity, Date date){

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(date);

        calendar.add(Calendar.MONTH, taskEntity.getMonthAfterSigning());
        taskEntity.setUserStartDate(calendar.getTime());

        //ORDER MATTERS

        int days= taskEntity.getDaysAfterSigning();
        calendar.add(Calendar.DAY_OF_YEAR, days);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);

        taskEntity.setUserEndDate(calendar.getTime());

        updateTaskOnApi(taskEntity, false, false);
    }

    private  void doDaysAfterSign(TaskEntity taskEntity, Date date){

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(date);

        //ORDER MATTERS
        taskEntity.setUserStartDate(date);

        int days= taskEntity.getDaysAfterSigning();
        calendar.add(Calendar.DAY_OF_YEAR, days);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);

        taskEntity.setUserEndDate(calendar.getTime());

        updateTaskOnApi(taskEntity, false, false);
    }

    private  void doMonthAfterSign(TaskEntity taskEntity, Date date){
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(date);

        calendar.add(Calendar.MONTH, taskEntity.getMonthAfterSigning());
        calendar.set(Calendar.DAY_OF_MONTH,  1);
        taskEntity.setUserStartDate(calendar.getTime());

        calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);

        taskEntity.setUserEndDate(calendar.getTime());

        //ewentualnie pozwól na dodanie zarówno month jak i days wtedy month zwiększa miesiąc
        // a day ustala który to dzień miesiąca???
        updateTaskOnApi(taskEntity, false, false);
    }

    //DELETE PARTICIPATION
    private void showDeleteDialog(){
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.warning_title_delete_particip))
                .setMessage(getString(R.string.warning_desc_delete_paricip))
                .setPositiveButton(android.R.string.yes, (dialog, which) ->
                        deleteParticipationFromAPi(activePromo.getParticipationId(), activePromo.getId()))

                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    public void deleteParticipationFromAPi(long participationId, long promoId){

        if(token!=null &&token.equals(""))
            deleteParticipationRoom(promoId);
        else {
            Call<Void> call= apiInterface.deleteParticipation(participationId);
            call.enqueue(new RetrofitCallback<>(PromoActivity.this, new Callback<Void>() {
                @Override
                public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                    if(response.code()==200){
                        deleteParticipationRoom(promoId);
                    }
                }

                @Override
                public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
                }
            }));
        }

    }

    private void deleteParticipationRoom(long promoId){
        TaskViewModel taskViewModel= ViewModelProviders.of(this).get(TaskViewModel.class);
        promoViewModel.updatePromoUserData(0, null, promoId, false);
        NotificationPublisher notificationPublisher= new NotificationPublisher();

        List<TaskEntity> tasks=promoViewModel.getTasks().getValue();
        if(tasks!=null)
            for (TaskEntity taskEntity: tasks){
                taskEntity.setParticipationId(0);
                taskEntity.setUserStartDate(null);
                taskEntity.setUserEndDate(null);
                taskEntity.setCompleted(false);
                taskEntity.setActualAmount(0);

                taskViewModel.updateTaskUserData(taskEntity);
                notificationPublisher.deleteNotification(this, taskEntity);
            }
    }

    //ADD PARTICIPATION
    public void addParticipationToAPi(long promoId){

        if(token.equals(""))
            addParticipationRoom(promoId, 10000000);
        else {
            Call<IdPojo> call= apiInterface.addParticipation(new addParticipationPojo(promoId, 0, false));
            call.enqueue(new RetrofitCallback<>(this ,new Callback<IdPojo>() {
                @Override
                public void onResponse(@NotNull Call<IdPojo> call, @NotNull Response<IdPojo> response) {
                    if(response.code()== 201 && response.body()!=null){
                        IdPojo idPojo = response.body();
                        addParticipationRoom(promoId, idPojo.getId());
                    }
                }

                @Override
                public void onFailure(@NotNull Call<IdPojo> call, @NotNull Throwable t) {
                }
            }));
        }
    }

    private void addParticipationRoom(long promoId, long participationId){
        promoViewModel.updatePromoUserData(participationId, null, promoId, false);

        List<TaskEntity> tasks=promoViewModel.getTasks().getValue();
        if(tasks!=null)
            for (TaskEntity taskEntity: tasks){
                taskEntity.setParticipationId(participationId);
                if(taskEntity.getMonthAfterSigning()<=0 && taskEntity.getDaysAfterSigning()<=0){
                    taskEntity.setUserStartDate(taskEntity.getStartDate());
                    taskEntity.setUserEndDate(taskEntity.getEndDate());
                }

                updateTaskOnApi(taskEntity, false, false);
            }

            mBinding.myPromoBanner.requestFocus();
    }

}
