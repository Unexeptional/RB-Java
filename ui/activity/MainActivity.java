package pl.rozbijbank.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import pl.rozbijbank.R;
import pl.rozbijbank.callback.BankClickCallback;
import pl.rozbijbank.callback.CheckBankCallback;
import pl.rozbijbank.callback.PickBankCallback;
import pl.rozbijbank.callback.ProductClickCallback;
import pl.rozbijbank.callback.PromoClickCallback;
import pl.rozbijbank.databinding.ActivityMainBinding;
import pl.rozbijbank.db.TBRoomDatabase;
import pl.rozbijbank.db.entity.PromoEntity;
import pl.rozbijbank.db.entity.TaskEntity;
import pl.rozbijbank.db.model.Bank;
import pl.rozbijbank.db.model.Product;
import pl.rozbijbank.db.model.Promo;
import pl.rozbijbank.networking.ApiClient;
import pl.rozbijbank.networking.ApiInterface;
import pl.rozbijbank.networking.RetrofitCallback;
import pl.rozbijbank.networking.pojo.FeedbackPojo;
import pl.rozbijbank.networking.pojo.PromoTasksPojo;
import pl.rozbijbank.networking.pojo.TaskPojo;
import pl.rozbijbank.other.AppExecutors;
import pl.rozbijbank.other.MyApplication;
import pl.rozbijbank.ui.dialog.CheckBankDialog;
import pl.rozbijbank.ui.dialog.PickBankDialog;
import pl.rozbijbank.ui.fragment.AllProductsFragment;
import pl.rozbijbank.ui.fragment.MoreFragment;
import pl.rozbijbank.ui.fragment.list.MyPromoListFragment;
import pl.rozbijbank.ui.fragment.list.PromoListFragment;
import pl.rozbijbank.ui.fragment.list.TaskListFragment;
import pl.rozbijbank.utilities.Constants;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static pl.rozbijbank.db.TBRoomDatabase.DATABASE_NAME;

public class MainActivity extends BasicActivity implements
        MoreFragment.OnFragmentInteractionListener,
        BankClickCallback,
        PromoClickCallback,
        ProductClickCallback {

    private SharedPreferences preferences;
    private ActivityMainBinding mBinding;
    private AlertDialog taskFilter;
    private AlertDialog promoFilter;
    private AlertDialog feedbackDialog;

    boolean doubleBackToExitPressedOnce = false;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
                switch (item.getItemId()) {
                    case R.id.navigation_my_promos:
                        startFragment(MyPromoListFragment.newInstance());
                        mBinding.fab.setVisibility(View.GONE);
                        return true;
                    case R.id.navigation_actual_promos:
                        startFragment(PromoListFragment.newInstance());
                        //showHelloFragment();
                        mBinding.fab.setVisibility(View.VISIBLE);
                        mBinding.fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.baseline_filter_list_white_24));
                        return true;
                    case R.id.navigation_banks:
                        startFragment(AllProductsFragment.newInstance());
                        //showHelloFragment();
                        mBinding.fab.setVisibility(View.VISIBLE);
                        mBinding.fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.twotone_add_white_24));
                        return true;
                    case R.id.navigation_tasks:
                        startFragment(TaskListFragment.newInstance());
                        //showHelloFragment();
                        mBinding.fab.setVisibility(View.VISIBLE);
                        mBinding.fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.baseline_filter_list_white_24));
                        return true;
                    case R.id.navigation_more:
                        startFragment(MoreFragment.newInstance());
                        mBinding.fab.setVisibility(View.GONE);
                        return true;
                }
                return false;
            };

    //BASIC
    @Override
    protected int provideActivityLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences= PreferenceManager.getDefaultSharedPreferences(this);
        mBinding= (ActivityMainBinding) dataBinding;

        mBinding.navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        setClicks();


        if (savedInstanceState== null){

            switch (preferences.getInt(getString(R.string.key_show_at_start_option),1)){
                case 0:
                    mBinding.navigation.setSelectedItemId(R.id.navigation_my_promos);
                    break;
                case 1:
                    mBinding.navigation.setSelectedItemId(R.id.navigation_actual_promos);
                    break;
                case 2:
                    mBinding.navigation.setSelectedItemId(R.id.navigation_banks);
                    break;
                case 3:
                    mBinding.navigation.setSelectedItemId(R.id.navigation_tasks);
                    break;
            }
        }


        //BETA ONLY
        if (preferences.getBoolean(getString(R.string.key_show_tutorial_first), true)){
            showTutorialList();
            preferences.edit().putBoolean(getString(R.string.key_show_tutorial_first), false).apply();
        }
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount()>0){
            getSupportFragmentManager().popBackStack();
            return;
        }

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.toast_closing_app), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> doubleBackToExitPressedOnce=false, 2000);
    }

    //CUSTOM
    private void setClicks(){
        mBinding.fab.setOnClickListener(v -> {
            switch ( mBinding.navigation.getSelectedItemId()){
                case R.id.navigation_my_promos:
                    break;
                case R.id.navigation_actual_promos:
                    showPromoFilter();
                    //showPromoFilter();
                    break;
                case R.id.navigation_banks:
                    //showBankFilter();
                    AllProductsFragment allProductsFragment= (AllProductsFragment) getSupportFragmentManager().
                            findFragmentByTag(Constants.TAG_ALL_PRODUCTS_FRAGMENT);
                    if(allProductsFragment!=null)
                        allProductsFragment.newProduct();
                    break;
                case R.id.navigation_tasks:
                    showTaskFilter();
                    break;
            }
        });

    }


    private void startFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        fragmentTransaction.replace(R.id.fragment_container, fragment, fragment.toString()).commit();
    }

    //CALLBACKS
    @Override
    public void onBankItemClick(Bank bank) {
        Intent intent= new Intent(MainActivity.this, BankProductsActivity.class);
        intent.putExtra(Constants.KEY_BANK_ID, bank.getId());
        startActivity(intent);
    }

    @Override
    public void onPromoItemClick(Promo promo) {
        Intent intent= new Intent(MainActivity.this, PromoActivity.class);
        intent.putExtra(Constants.KEY_PROMO_ID, promo.getId());
        startActivity(intent);
    }

    @Override
    public void onProductItemClick(Product product) {
        Intent intent= new Intent(this, SecondActivity.class);
        intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_PRODUCT_FRAGMENT);
        intent.putExtra(Constants.KEY_BANK_ID, product.getBankId());
        intent.putExtra(Constants.KEY_PRODUCT_TYPE, (product.getProductType()/10));
        intent.putExtra(Constants.KEY_PRODUCT_ID, product.getId());
        startActivity(intent);
    }

    @Override
    public void startSettingsFragment() {
        Intent intent= new Intent(MainActivity.this, SecondActivity.class);
        intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_SETTINGS_FRAGMENT);
        startActivity(intent);
    }

    @Override
    public void startLoginFragment() {
        Intent intent= new Intent(MainActivity.this, LoginActivity.class);
        intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_LOGIN_FRAGMENT);
        startActivity(intent);
    }

    @Override
    public void startRegisterFragment() {
        Intent intent= new Intent(MainActivity.this, LoginActivity.class);
        intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_REGISTER_FRAGMENT);
        startActivity(intent);
    }

    @Override
    public void startAccountFragment() {
        Intent intent= new Intent(MainActivity.this, SecondActivity.class);
        intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_ACCOUNT_FRAGMENT);
        startActivity(intent);
    }

    @Override
    public void controlOfExpenditure() {
        String packageName="com.unexceptional.beast.banko";

        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent == null) {
            // Bring user to the market or let them choose an app?
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + packageName));
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void startTutorialsFragment() {
        showTutorialList();
    }

    @Override
    public void startRewardsFragment() {
        Intent intent= new Intent(MainActivity.this, SecondActivity.class);
        intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_REWARDS_FRAGMENT);
        startActivity(intent);
    }

    @Override
    public void startAboutFragment() {
        showAboutApp();
    }

    @Override
    public void rateThisApp() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=pl.rozbijbank")));
    }

    @Override
    public void sendFeedback() {
        showFeedbackDialog();
    }

    @Override
    public void exportDB() {
       exportDbStart();
    }

    @Override
    public void importDB() {
        importDbStart();
    }

    @Override
    public void privacyPolicy() {
        openBrowser(Uri.parse("http://rozbijbank.pl/politykaprywatnosci"), 0);
    }

    //Pobiera nowe promki i taski ALE NIE ROBI UPDATE NA STARE
    @Override
    public void getPromosData() {
        getPromosFromApi();
    }

    private void getPromosFromApi( ){
        ApiInterface apiInterface= ApiClient.getClient().create(ApiInterface.class);

        Call<List<PromoTasksPojo>> call= apiInterface.getPromos();
        call.enqueue(new RetrofitCallback<>(this, new Callback<List<PromoTasksPojo>>() {
            @Override
            public void onResponse(@NotNull Call<List<PromoTasksPojo>> call, @NotNull Response<List<PromoTasksPojo>> response) {
                if(response.code()== 200 && response.body()!=null){
                    List<PromoEntity> newPromos= new ArrayList<>();
                    List<TaskEntity> newTasks= new ArrayList<>();

                    for (PromoTasksPojo promo: response.body()){
                        newPromos.add(new PromoEntity(promo));
                        if(promo.getTasks()!=null)
                            for (TaskPojo taskPojo: promo.getTasks())
                                newTasks.add(new TaskEntity(taskPojo));
                    }

                    ((MyApplication)getApplicationContext()).getUltraRepository().addPromosManual(newPromos, newTasks);

                }
            }

            @Override
            public void onFailure(@NotNull Call<List<PromoTasksPojo>> call, @NotNull Throwable t) {
            }
        }));
    }


    //TASK FRAGMENT AND FILTER
    PickBankCallback taskFilterBankCallback= bank -> {
        preferences.edit().putLong(getString(R.string.key_task_filter_bank_id), bank.getId()).apply();

        reloadTaskFragment();
        if(taskFilter!=null)
            taskFilter.dismiss();

    };

    private void reloadTaskFragment(){
        TaskListFragment taskListFragment= (TaskListFragment) getSupportFragmentManager().
                findFragmentByTag(Constants.TAG_TASK_LIST_FRAGMENT);
        if(taskListFragment!=null)
            taskListFragment.reloadItems();

    }

    private void setTaskDateAdapter(Spinner spinner){
        ArrayAdapter<CharSequence> adapterDate= ArrayAdapter.
                createFromResource(this, R.array.FilterDateOptions,
                        android.R.layout.simple_spinner_item);
        adapterDate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapterDate);
        spinner.setSelection(preferences.getInt(getString(R.string.key_task_filter_date_option),0));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                preferences.edit().putInt(getString(R.string.key_task_filter_date_option), position).apply();
                reloadTaskFragment();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    protected void showTaskFilter(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //bind views
        View dialogView = View.inflate(this, R.layout.dialog_task_filter, null);
        builder.setView(dialogView);
        Spinner spinner= dialogView.findViewById(R.id.date_option_spinner);
        ImageView bankIcon= dialogView.findViewById(R.id.bank_icon);

        //setStuff
        setTaskDateAdapter(spinner);
        bankIcon.setOnClickListener(v -> new PickBankDialog(MainActivity.this, taskFilterBankCallback).showDialog());

        long bankId=preferences.getLong(getString(R.string.key_task_filter_bank_id), 0);
        if(bankId>0){
            int id= MyApplication.getBankIconId(bankId);
            if(id!=0)
                Picasso.get().load(id).into(bankIcon);
        }


        taskFilter = builder.create();
        taskFilter.show();
    }

    //PROMO FILTER
    CheckBankCallback promoFilterBankCallback = itemsIds -> {

        Set<String> stringSet= new HashSet<>(itemsIds.size());
        for (CheckBankDialog.CheckedItem checkedItem: itemsIds)
            if(checkedItem.isChecked())
                stringSet.add(String.valueOf(checkedItem.getId()));

        preferences.edit().putStringSet(getString(R.string.key_promo_filter_bank_ids), stringSet).apply();
        reloadPromoFragment();
        if(promoFilter!=null)
            promoFilter.dismiss();
    };

    private void reloadPromoFragment(){
        PromoListFragment promoListFragment= (PromoListFragment) getSupportFragmentManager().
                findFragmentByTag(Constants.TAG_PROMO_LIST_FRAGMENT);
        if(promoListFragment!=null)
            promoListFragment.reloadItems();
    }

    protected void showPromoFilter(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //bind views
        View dialogView = View.inflate(this, R.layout.dialog_promo_filter, null);
        builder.setView(dialogView);
        Button banks= dialogView.findViewById(R.id.promo_filter_banks);
        Spinner sortSpinner= dialogView.findViewById(R.id.sort_spinner);

        setPromoSortAdapter(sortSpinner);
        Set<String> stringSet= preferences.getStringSet(getString(R.string.key_promo_filter_bank_ids), new HashSet<>());
        if (stringSet==null || stringSet.isEmpty())
            banks.setText(getString(R.string.all));
        else
            banks.setText(getString(R.string.picked_banks));


        banks.setOnClickListener(v -> new CheckBankDialog(MainActivity.this, promoFilterBankCallback).showDialog());

        promoFilter = builder.create();
        promoFilter.show();
    }

    private void setPromoSortAdapter(Spinner spinner){
        ArrayAdapter<CharSequence> adapterDate= ArrayAdapter.
                createFromResource(this, R.array.PromoSortOptions,
                        android.R.layout.simple_spinner_item);
        adapterDate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapterDate);
        spinner.setSelection(preferences.getInt(getString(R.string.key_promo_filter_sort_option),0));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                preferences.edit().putInt(getString(R.string.key_promo_filter_sort_option), position).apply();
                String sort="";
                switch (position){
                    case 0:
                        sort= " ORDER BY end_date ASC";
                        break;
                    case 1:
                        sort= " ORDER BY id DESC";
                        break;
                }
                preferences.edit().putString(getString(R.string.key_promo_filter_sort_string), sort).apply();
                reloadPromoFragment();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    //FEEDBACK
    private void showFeedbackDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //bind views
        View dialogView = View.inflate(this, R.layout.dialog_send_feedback, null);
        builder.setView(dialogView);
        TextInputEditText emailInput= dialogView.findViewById(R.id.feedback_email_input);
        TextInputEditText textInput= dialogView.findViewById(R.id.feedback_text);
        Button accept= dialogView.findViewById(R.id.btn_accept);
        Button decline= dialogView.findViewById(R.id.btn_decline);

        decline.setOnClickListener(v -> feedbackDialog.dismiss());
        accept.setOnClickListener(v -> {
            String email= Objects.requireNonNull(emailInput.getText()).toString();
            String text= Objects.requireNonNull(textInput.getText()).toString();
            sendFeedbackToApi(email, text);
        });

        feedbackDialog = builder.create();
        feedbackDialog.show();
    }

    private void sendFeedbackToApi(String email, String text){
        ApiInterface apiInterface= ApiClient.getClient().create(ApiInterface.class);
        Call<Void> call= apiInterface.sendFeedback(new FeedbackPojo(email, text));
        call.enqueue(new RetrofitCallback<>(MainActivity.this, new Callback<Void>() {
            @Override
            public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                if(response.code()==200){
                    Toast.makeText(MainActivity.this, getString(R.string.thank_for_feedback), Toast.LENGTH_SHORT).show();
                    feedbackDialog.dismiss();
                }
            }

            @Override
            public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {

            }
        }));
    }

    //TUTORIALS
    protected void showTutorialList(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View dialogView = View.inflate(this, R.layout.dialog_tutorials, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

        Button btnFirstTransaction= dialogView.findViewById(R.id.btn_first_promo);
        Button btnPoints= dialogView.findViewById(R.id.btn_points);


        btnFirstTransaction.setOnClickListener(view -> {
            Intent intent= new Intent(MainActivity.this, TutorialActivity.class);
            intent.putExtra(Constants.KEY_TUTORIAL_TYPE, Constants.TUTORIAL_TYPE_FIRST_PROMO);
            startActivity(intent);
        });

        btnPoints.setOnClickListener(view -> {
            Intent intent= new Intent(MainActivity.this, TutorialActivity.class);
            intent.putExtra(Constants.KEY_TUTORIAL_TYPE, Constants.TUTORIAL_TYPE_POINTS);
            startActivity(intent);
        });


        dialog.show();
    }

    //ABOUT APP
    protected void showAboutApp(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View dialogView = View.inflate(this, R.layout.dialog_about_app, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

        dialog.show();
    }

    //BACKUP
    static final int EXPORT_DB = 24;
    static final int IMPORT_DB = 25;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    public static boolean verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
            return false;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
            }

            if(requestCode == EXPORT_DB){
                exportDb(uri);
            }else if(requestCode == IMPORT_DB){
              importDb(uri);
            }
        }
    }

    public void exportDbStart() {
        if(verifyStoragePermissions(this)){
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/octet-stream");
            SimpleDateFormat dateFormatGmt = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

            intent.putExtra(Intent.EXTRA_TITLE, "RozbijBank_" + dateFormatGmt.format(Calendar.getInstance().getTime()));
            startActivityForResult(intent, EXPORT_DB);
        }
    }

    private void exportDb(Uri uri) {

        try {
            ParcelFileDescriptor fileDescriptorDatabase = this.getContentResolver().openFileDescriptor
                    (Uri.fromFile(getDatabasePath(DATABASE_NAME)), "r");
            assert fileDescriptorDatabase != null;
            FileInputStream currentDb = new FileInputStream(fileDescriptorDatabase.getFileDescriptor());

            try {
                ParcelFileDescriptor fileDescriptorBackup = this.getContentResolver().openFileDescriptor(uri, "w");
                assert fileDescriptorBackup != null;
                FileOutputStream backup =
                        new FileOutputStream(fileDescriptorBackup.getFileDescriptor());

                byte[] buffer = new byte[1024];
                int len;
                while ((len = currentDb.read(buffer)) != -1) {
                    backup.write(buffer, 0, len);

                }

                Toast.makeText(this, getResources().getString(R.string.warning_db_export_success), Toast.LENGTH_LONG).show();

                fileDescriptorBackup.close();
                backup.close();
            } catch (FileNotFoundException e) {
                Toast.makeText(this, getResources().getString(R.string.warning_db_export_fail), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, getResources().getString(R.string.warning_db_export_fail), Toast.LENGTH_LONG).show();
        }
    }

    public void importDbStart( ) {
        if(verifyStoragePermissions(this)){
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/octet-stream");
            startActivityForResult(intent, IMPORT_DB);
        }

    }

    private void importDb(Uri uri) {
        try {
            ParcelFileDescriptor fileDescriptorDatabase = this.getContentResolver().openFileDescriptor
                    (Uri.fromFile(getDatabasePath(DATABASE_NAME)), "w");
            assert fileDescriptorDatabase != null;
            FileOutputStream currentDb = new FileOutputStream(fileDescriptorDatabase.getFileDescriptor());

            try {
                ParcelFileDescriptor fileDescriptorBackup = this.getContentResolver().openFileDescriptor(uri, "r");
                assert fileDescriptorBackup != null;
                FileInputStream backup =
                        new FileInputStream(fileDescriptorBackup.getFileDescriptor());



                byte[] buffer = new byte[1024];
                int len;
                while ((len = backup.read(buffer)) != -1) {
                    currentDb.write(buffer, 0, len);
                }

                Toast.makeText(this, getResources().getString(R.string.warning_db_import_success), Toast.LENGTH_LONG).show();

                fileDescriptorBackup.close();
                backup.close();

                //getFileLastModified(uri);

                //refresh database
                TBRoomDatabase.getInstance(this, new AppExecutors());
                ((MyApplication)getApplicationContext()).getUltraRepository().triggerRefreshAfterBackup();

            } catch (FileNotFoundException e) {
                Toast.makeText(this, getResources().getString(R.string.warning_db_import_fail), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, getResources().getString(R.string.warning_db_import_fail), Toast.LENGTH_LONG).show();
        }
    }
}
