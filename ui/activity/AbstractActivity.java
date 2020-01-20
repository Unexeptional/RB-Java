package pl.rozbijbank.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.snackbar.Snackbar;

import pl.rozbijbank.R;
import pl.rozbijbank.callback.TaskClickCallback;
import pl.rozbijbank.db.converter.DateTypeConverter;
import pl.rozbijbank.db.entity.PromoEntity;
import pl.rozbijbank.db.entity.TaskEntity;
import pl.rozbijbank.networking.ApiClient;
import pl.rozbijbank.networking.ApiInterface;
import pl.rozbijbank.networking.RetrofitCallback;
import pl.rozbijbank.networking.pojo.RedirectPojo;
import pl.rozbijbank.networking.pojo.participation.ParticipationTask;
import pl.rozbijbank.other.AppExecutors;
import pl.rozbijbank.other.MyApplication;
import pl.rozbijbank.other.NotificationPublisher;
import pl.rozbijbank.viewModel.TaskViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//does not play with layout
public abstract class AbstractActivity  extends AppCompatActivity implements
        TaskClickCallback {

    protected SharedPreferences preferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences= PreferenceManager.getDefaultSharedPreferences(this);
    }

    //FOR ALL ACTIVITIES :>
    public void updateTaskOnApi(TaskEntity taskEntity, boolean showDialog, boolean showSnack){
        ApiInterface apiInterface= ApiClient.getClient().create(ApiInterface.class);
        String token = preferences.getString(getString(R.string.key_active_user_token), "");
        if(token!=null && token.equals(""))
            updateTaskRoom(taskEntity, showSnack);
        else {
            ParticipationTask participationTask= new ParticipationTask(
                    taskEntity.isCompleted(),
                    taskEntity.getActualAmount(),
                    taskEntity.getId(),
                    taskEntity.getParticipationId());

            if(taskEntity.getUserStartDate()!=null)
                participationTask.setStartDate(DateTypeConverter.toLong(taskEntity.getUserStartDate()));
            if(taskEntity.getUserEndDate()!=null)
                participationTask.setEndDate(DateTypeConverter.toLong(taskEntity.getUserEndDate()));

            Call<Void> call= apiInterface.updateParticipationTask(participationTask);
            if(showDialog)
                call.enqueue(new RetrofitCallback<>(this, new Callback<Void>() {
                    @Override
                    public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                        if (response.code()==200)
                            updateTaskRoom(taskEntity, showSnack);
                    }

                    @Override
                    public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {

                    }
                }));
            else
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                        if (response.code()==200)
                            updateTaskRoom(taskEntity, showSnack);
                    }

                    @Override
                    public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {

                    }
                });


        }

    }

    private void updateTaskRoom(TaskEntity taskEntity, boolean showSnack){
        ViewModelProviders.of(this).get(TaskViewModel.class).
                updateTaskUserData(taskEntity);

        if (showSnack)
            showSnackBar(taskEntity);

        if (preferences.getBoolean(getString(R.string.key_allow_task_notification), false)) {
            NotificationPublisher publisher= new NotificationPublisher();

            if(taskEntity.isCompleted())
                publisher.deleteNotification(this, taskEntity);
            else
                publisher.scheduleNotification(this, taskEntity);
        }

    }

    private void showSnackBar(TaskEntity oldTask){
        final Snackbar snackbar = Snackbar
                .make(findViewById(R.id.fragment_container) , getString(R.string.task_changed), Snackbar.LENGTH_LONG);

        snackbar.setAction(getString(R.string.undo), v -> {
            oldTask.setCompleted(!oldTask.isCompleted());
            updateTaskOnApi(oldTask, true, false);
        });

        snackbar.show();
    }


    //TASK CALLBACK
    @Override
    public void updateTask(TaskEntity taskEntity, boolean showSnack) {
        updateTaskOnApi(taskEntity, true, showSnack);
    }

    @Override
    public void openBrowser(long promoId) {
      new AppExecutors().diskIO().execute(() -> {
         PromoEntity promoEntity=  ((MyApplication)AbstractActivity.this.getApplicationContext()).getPromoRepository().getPromo(promoId);
          openBrowser(promoEntity.getUri(), promoEntity.getId());
      });
    }

    @Override
    public void openBrowser(String uri, long promoId) {
        String userId= preferences.getString(getString(R.string.key_active_user_id), "");

        try{
            assert userId != null;
            uri= uri.replace("horyhomik", userId + "--" + promoId);

            openBrowser(Uri.parse(uri), promoId);
        }catch (Exception e){
            Toast.makeText(this, getString(R.string.warning_cant_open_browser), Toast.LENGTH_SHORT).show();
        }

    }

    //OPEN BROWSER< SEND REDIRECT AND INTERNAL BRO

    protected void openBrowser(Uri uri, long promoId){
        try{
            if (preferences.getBoolean(getString(R.string.key_allow_internal_browser), false))
                openInternalBrowser(uri);
            else {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(browserIntent);
            }

            sendRedirection(uri, promoId);


        }catch (Exception e){
            Toast.makeText(this, getString(R.string.warning_cant_open_browser), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendRedirection(Uri uri, long promoId){
        ApiInterface apiInterface= ApiClient.getClient().create(ApiInterface.class);
        Call<Void> call= apiInterface.sendRedirection(new RedirectPojo(uri.toString(), promoId));
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {

            }

            @Override
            public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {

            }
        });
    }

    private void openInternalBrowser(Uri uri){

// create an intent builder
        CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();

// Begin customizing
// set toolbar colors
        intentBuilder.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        intentBuilder.setSecondaryToolbarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

// build custom tabs intent
        CustomTabsIntent customTabsIntent = intentBuilder.build();

// launch the url
        customTabsIntent.launchUrl(this, uri);
    }
}
