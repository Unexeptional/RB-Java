package pl.rozbijbank.ui.activity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import pl.rozbijbank.R;
import pl.rozbijbank.db.entity.BankEntity;
import pl.rozbijbank.db.entity.PromoEntity;
import pl.rozbijbank.db.entity.TaskEntity;
import pl.rozbijbank.db.entity.TimestampEntity;
import pl.rozbijbank.networking.ApiClient;
import pl.rozbijbank.networking.ApiInterface;
import pl.rozbijbank.networking.ApiSessionInterface;
import pl.rozbijbank.networking.pojo.BankPojo;
import pl.rozbijbank.networking.pojo.CheckUpdatesPojo;
import pl.rozbijbank.networking.pojo.PromoTasksPojo;
import pl.rozbijbank.networking.pojo.TaskPojo;
import pl.rozbijbank.networking.pojo.session.FCMTokenPojo;
import pl.rozbijbank.other.AppExecutors;
import pl.rozbijbank.other.MyApplication;
import pl.rozbijbank.utilities.Constants;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity does:
 * show Splash screen with logo,
 * downloading data for the first time and
 * registering to push notification service
 * */
public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if(preferences.getBoolean(getString(R.string.key_create_notification_channels), true)) {
            createNotificationChannels();
            preferences.edit().putBoolean(getString(R.string.key_create_notification_channels), false).apply();
        }

        if(registerWithFirebase() || preferences.getBoolean(getString(R.string.key_google_services_dialog_shown), false)){
            goToNextActivity();
        }else{
            preferences.edit().putBoolean(getString(R.string.key_google_services_dialog_shown), true).apply();
        }
    }


    private void sendTimestamp(long timestamp){
        ApiInterface apiInterface= ApiClient.getClient().create(ApiInterface.class);

        Call<List<CheckUpdatesPojo>> call= apiInterface.getUpdates(timestamp);
        call.enqueue(new Callback<List<CheckUpdatesPojo>>() {
            @Override
            public void onResponse(@NotNull Call<List<CheckUpdatesPojo>> call, @NotNull Response<List<CheckUpdatesPojo>> response) {
                if(response.code()==200 && response.body()!=null){

                    for (CheckUpdatesPojo pojo: response.body()){
                        if(pojo.getItemType()!=null && pojo.getItemId()!=0)
                        switch (pojo.getItemType()) {
                            case "bank":
                                getBankFromApi(pojo.getItemId(), apiInterface, pojo.getAction());
                                break;
                            case "promo":
                                getPromoFromApi(pojo.getItemId(), apiInterface, pojo.getAction());
                                break;
                            case "task":
                                getTaskFromApi(pojo.getItemId(), apiInterface, pojo.getAction());
                                break;
                        }

                    }
                    ((MyApplication)getApplicationContext()).getUltraRepository().
                            setTimestamp( Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis());
                }
            }

            @Override
            public void onFailure(@NotNull Call<List<CheckUpdatesPojo>> call, @NotNull Throwable t) {

            }
        });
    }

    private void getTaskFromApi(long id, ApiInterface apiInterface, String action){

        Call<TaskPojo> call= apiInterface.getTask(id);
        call.enqueue(new Callback<TaskPojo>() {
            @Override
            public void onResponse(@NotNull Call<TaskPojo> call, @NotNull Response<TaskPojo> response) {
                if(response.code()== 200 && response.body()!=null){
                    TaskPojo taskPojo= response.body();

                    TaskEntity taskEntity= new TaskEntity(taskPojo);

                    switch (action){
                        case "update":
                            ((MyApplication) getApplicationContext()).getTaskRepository().updateTaskHead(taskEntity);
                            break;
                        case "add":
                            ((MyApplication) getApplicationContext()).getTaskRepository().insert(taskEntity);
                            break;
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<TaskPojo> call, @NotNull Throwable t) {
            }
        });
    }

    private void getPromoFromApi(long id, ApiInterface apiInterface, String action){

        Call<PromoTasksPojo> call= apiInterface.getPromo(id);
        call.enqueue(new Callback<PromoTasksPojo>() {
            @Override
            public void onResponse(@NotNull Call<PromoTasksPojo> call, @NotNull Response<PromoTasksPojo> response) {
                if(response.code()== 200 && response.body()!=null){

                    PromoEntity promoEntity= new PromoEntity(response.body());
                    switch (action){
                        case "update":
                            ((MyApplication) getApplicationContext()).getPromoRepository().updatePromoHead(promoEntity);
                            for (TaskPojo taskPojo :response.body().getTasks()){
                                ((MyApplication) getApplicationContext()).getTaskRepository().updateTaskHead(new TaskEntity(taskPojo));
                            }

                            break;
                        case "add":
                            List<TaskEntity> tasks=new ArrayList<>();
                            ((MyApplication) getApplicationContext()).getPromoRepository().insert(promoEntity);
                            for (TaskPojo taskPojo :response.body().getTasks()){
                                tasks.add(new TaskEntity(taskPojo));
                            }

                            ((MyApplication)getApplicationContext()).getUltraRepository().addPromoManual(promoEntity, tasks);

                            break;
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<PromoTasksPojo> call, @NotNull Throwable t) {
            }
        });
    }

    private void getBankFromApi(long id, ApiInterface apiInterface, String action){

        Call<BankPojo> call= apiInterface.getBank(id);
        call.enqueue(new Callback<BankPojo>() {
            @Override
            public void onResponse(@NotNull Call<BankPojo> call, @NotNull Response<BankPojo> response) {
                if(response.code()== 200 && response.body()!=null){
                    BankPojo bankPojo= response.body();

                    BankEntity bankEntity= new BankEntity(
                            bankPojo.getId(),
                            bankPojo.getTitle()
                    );
                    switch (action){
                        case "update":
                            ((MyApplication) getApplicationContext()).getBankRepository().update(bankEntity);

                            break;
                        case "add":
                            ((MyApplication) getApplicationContext()).getBankRepository().insert(bankEntity);

                            break;
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<BankPojo> call, @NotNull Throwable t) {
            }
        });
    }

    private void goToNextActivity(){
        boolean firstBoot= preferences.getBoolean(getString(R.string.key_database_need_fetch), true);
        boolean skipLogin=  preferences.getBoolean(getString(R.string.key_skip_login), false);

        new AppExecutors().diskIO().execute(() -> {
            TimestampEntity timestampEntity= ((MyApplication)getApplicationContext()).getUltraRepository().getTimestamp();
            long timestamp=0;

            if (timestampEntity!=null){
                timestamp= timestampEntity.getTimestamp();
            }

            if(timestamp==0){
                if (!firstBoot){
                    timestamp= 1553779284246L;
                    ((MyApplication)getApplicationContext()).getUltraRepository().
                            setTimestamp( Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis());
                }
            }


            if(timestamp==0){
                startActivity(new Intent(SplashActivity.this, WelcomeActivity.class));
            }else{

                sendTimestamp(timestamp);

                //handle Data prom notification and go to specific promo
                if(getIntent().getStringExtra("id")!=null && !getIntent().getStringExtra("id").equals("")){
                    Intent intent= new Intent(SplashActivity.this, PromoActivity.class);
                    intent.putExtra(Constants.KEY_PROMO_ID, Long.parseLong(getIntent().getStringExtra("id")));
                    startActivity(intent);
                }else {
                    String token= preferences.getString(getString(R.string.key_active_user_token), "");
                    if(token != null && !token.isEmpty() || skipLogin)
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    else
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                }
            }

            finish();
        });
    }

    // notification stuff
    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */

    private void createNotificationChannels(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = this.getSystemService(NotificationManager.class);

            List<NotificationChannel> channels= notificationManager.getNotificationChannels();

            for (NotificationChannel channel: channels){
                notificationManager.deleteNotificationChannel(channel.getId());
            }


            NotificationChannel taskChannel = new NotificationChannel(
                    getString(R.string.notification_channel_task_end),
                    getString(R.string.notification_channel_task_end_name),
                    NotificationManager.IMPORTANCE_HIGH);

            taskChannel.setDescription(getString(R.string.notification_channel_task_end_desc));
            taskChannel.setShowBadge(true);

            NotificationChannel promoChannel = new NotificationChannel(
                    getString(R.string.notification_channel_new_promo),
                    getString(R.string.notification_channel_new_promo_name),
                    NotificationManager.IMPORTANCE_HIGH);

            promoChannel.setDescription(getString(R.string.notification_channel_new_promo_desc));
            promoChannel.setShowBadge(true);

            NotificationChannel userChannel = new NotificationChannel(
                    getString(R.string.notification_channel_user_actions),
                    getString(R.string.notification_channel_user_actions_name),
                    NotificationManager.IMPORTANCE_HIGH);

            userChannel.setDescription(getString(R.string.notification_channel_user_actions_desc));
            userChannel.setShowBadge(true);

            notificationManager.createNotificationChannel(userChannel);
            notificationManager.createNotificationChannel(taskChannel);
            notificationManager.createNotificationChannel(promoChannel);

        }
        String token = preferences.getString(getString(R.string.key_active_user_token), "");
        if (token!=null && !token.equals(""))
            sendFCMToken();
    }

    private void sendFCMToken(){
        String tokenFCM= preferences.getString(getString(R.string.key_firebase_notification_token), "");
        ApiSessionInterface apiSessionInterface = ApiClient.getClient().create(ApiSessionInterface.class);

        Call<Void> call= apiSessionInterface.sendFCMToken(new FCMTokenPojo(tokenFCM));
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
            }

            @Override
            public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
            }
        });
    }


    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported by Google Play Services.");
                ToastNotify(getString(R.string.play_services_error));
                finish();
            }
            return false;
        }
        return true;
    }

    public boolean registerWithFirebase() {
        if (checkPlayServices()) {
            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }
                        // Get new Instance ID token
                        String token = Objects.requireNonNull(task.getResult()).getToken();
                        preferences.edit().putString(getString(R.string.key_firebase_notification_token), token).apply();
                    });

            //BETA ONLY
/*
            FirebaseMessaging.getInstance().subscribeToTopic("test")
                    .addOnCompleteListener(task -> {
                        String msg = "hula";
                        if (!task.isSuccessful()) {
                            msg = "nie hula";
                        }
                        Log.d(TAG, msg);
                        //Toast.makeText(SplashActivity.this, msg, Toast.LENGTH_SHORT).show();
                    });
*/

            return true;
        }else
            return false;
    }

    public void ToastNotify(final String notificationMessage) {
        runOnUiThread(() -> Toast.makeText(SplashActivity.this, notificationMessage, Toast.LENGTH_LONG).show());
    }


}