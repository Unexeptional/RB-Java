package pl.rozbijbank.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;
import pl.rozbijbank.R;
import pl.rozbijbank.db.entity.UserEntity;
import pl.rozbijbank.networking.ApiClient;
import pl.rozbijbank.networking.ApiSessionInterface;
import pl.rozbijbank.networking.RetrofitCallback;
import pl.rozbijbank.networking.pojo.session.FCMTokenPojo;
import pl.rozbijbank.networking.pojo.session.LoginPojo;
import pl.rozbijbank.networking.pojo.session.RegisterPojo;
import pl.rozbijbank.networking.pojo.session.UserPojo;
import pl.rozbijbank.other.FlipViewPagerTransformer;
import pl.rozbijbank.other.MyApplication;
import pl.rozbijbank.ui.dialog.LoadingDialog;
import pl.rozbijbank.ui.fragment.LoginFragment;
import pl.rozbijbank.ui.fragment.RegisterFragment;
import pl.rozbijbank.utilities.Constants;
import pl.rozbijbank.viewModel.UserViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AbstractActivity implements
        LoginFragment.OnFragmentInteractionListener,
        RegisterFragment.OnFragmentInteractionListener{

    private ApiSessionInterface apiSessionInterface;
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private LoadingDialog loadingDialog;

    //BASIC
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        viewPager= findViewById(R.id.view_pager);
        viewPagerAdapter= new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setPageTransformer(true, new FlipViewPagerTransformer());


        if(getIntent()!=null){
            String fragmentTag= getIntent().getStringExtra(Constants.KEY_FRAGMENT_TAG);
            if(fragmentTag!=null){
                if(fragmentTag.equals(Constants.TAG_LOGIN_FRAGMENT))
                    orSignIn();
                else if(fragmentTag.equals(Constants.TAG_REGISTER_FRAGMENT))
                    orSignUp();
            }
        }


        //net
        apiSessionInterface = ApiClient.getClient().create(ApiSessionInterface.class);
    }

    @Override
    public void finish() {
        Intent intent= new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        super.finish();

    }

    //LOGIN AND REGISTER
    private void setUser(UserPojo userPojo){
        preferences.edit().putString(getString(R.string.key_active_user_id), userPojo.getUserId()).apply();
        preferences.edit().putString(getString(R.string.key_active_user_token), userPojo.getToken()).apply();

        ViewModelProviders.of(LoginActivity.this).get(UserViewModel.class)
                .insert(new UserEntity(userPojo));
    }

    private void sendFCMToken(){
        String token= preferences.getString(getString(R.string.key_firebase_notification_token), "");

        Call<Void> call= apiSessionInterface.sendFCMToken(new FCMTokenPojo(token));
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
            }

            @Override
            public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
            }
        });
    }

    //LOGIN
    Callback<UserPojo> loginCallback = new Callback<UserPojo>() {
        @Override
        public void onResponse(@NotNull Call<UserPojo> call, @NotNull Response<UserPojo> response) {
            if(response.code()==200){
                UserPojo userPojo= response.body();
                if(userPojo!=null){
                    setUser(userPojo);
                    ((MyApplication)getApplication()).getUltraRepository().login();
                    sendFCMToken();
                }
                loadingDialog.hideDialog();
                finish();

            }else{
                try {
                    assert response.errorBody() != null;
                    JSONObject jObjError = new JSONObject(response.errorBody().string());
                    viewPagerAdapter.loginFragment.setEmailError(jObjError.getString("description"));
                } catch (Exception ignored) {
                }
                loadingDialog.hideDialog();
            }
        }

        @Override
        public void onFailure(@NotNull Call<UserPojo> call, @NotNull Throwable t) {
            loadingDialog.hideDialogFailure();
        }
    };

    private void login(LoginPojo loginPojo) {
        loadingDialog= new LoadingDialog(this);
        loadingDialog.showDialog(getString(R.string.loading));
        Call<UserPojo> call= apiSessionInterface.login(loginPojo);
        call.enqueue(loginCallback);
    }
/*

    private void setDefaultDB(){
        //((MyApplication)getApplicationContext()).getUltraRepository().setDefaultDb();

        getProducts();
       // getParticipationsFromApi();
    }

    private void getProducts(){
        Call<List<ProductPojo>> call= apiInterface.getProducts();
        call.enqueue(new Callback<List<ProductPojo>>() {
            @Override
            public void onResponse(@NotNull Call<List<ProductPojo>> call, @NotNull Response<List<ProductPojo>> response) {
                if(response.code()==200){
                  */
/*  ProductViewModel productViewModel= ViewModelProviders.of(LoginActivity.this).get(ProductViewModel.class);
                    if (response.body()!=null){
                        for (ProductPojo productPojo: response.body())
                            productViewModel.insert(new ProductEntity(productPojo));
                    }*//*

                    getParticipationsFromApi(response.body());

                }
            }

            @Override
            public void onFailure(@NotNull Call<List<ProductPojo>> call, @NotNull Throwable t) {
              loadingDialog.hideDialogFailure();
            }
        });
    }

    public void getParticipationsFromApi(List<ProductPojo> products) {
        Call<List<ParticipationPojo>> call= apiInterface.getParticipations();
        call.enqueue(new Callback<List<ParticipationPojo>>() {
            @Override
            public void onResponse(@NotNull Call<List<ParticipationPojo>> call, @NotNull Response<List<ParticipationPojo>> response) {
                if(response.code()==200){
                  */
/*  PromoSViewModel viewModel= ViewModelProviders.of(LoginActivity.this).get(PromoSViewModel.class);
                    TaskViewModel taskViewModel= ViewModelProviders.of(LoginActivity.this).get(TaskViewModel.class);

                    if(response.body()!=null)
                        for (ParticipationPojo pp: response.body()){

                            viewModel.updatePromoUserData(pp.getId(), DateTypeConverter.toDate(pp.getContractSigningDate()),
                                    pp.getPromoId(), pp.isCompleted());

                            for (ParticipationTask pt: pp.getParticipationTasks())
                                taskViewModel.updateTaskUserData(new TaskEntity(pt));

                        }*//*

                  List<ProductEntity> productEntities= new ArrayList<>();
                  for (ProductPojo productPojo: products)
                      productEntities.add(new ProductEntity(productPojo));


                    ((MyApplication)getApplicationContext()).getUltraRepository().testBEzpieczneLogowanie(productEntities, response.body());

                    loadingDialog.hideDialog();
                    finish();
                }
            }

            @Override
            public void onFailure(@NotNull Call<List<ParticipationPojo>> call, @NotNull Throwable t) {
                loadingDialog.hideDialogFailure();
            }
        });
    }
*/

    //REGISTER
    Callback<UserPojo> registerCallback = new Callback<UserPojo>() {
        @Override
        public void onResponse(@NotNull Call<UserPojo> call, @NotNull Response<UserPojo> response) {
            if(response.code()==200){
                UserPojo userPojo= response.body();
                if(userPojo!=null){
                    setUser(userPojo);

                    ((MyApplication)getApplication()).getUltraRepository().register();
                    sendFCMToken();

/*

                    LiveData<List<PromoEntity>> promoLiveData= ViewModelProviders.of(LoginActivity.this).get(PromoListViewModel.class).
                            getParticipatePromos();

                    promoLiveData.observe(LoginActivity.this, promoEntities -> {
                        if(promoEntities!=null){
                            promoLiveData.removeObservers(LoginActivity.this);
                            for(PromoEntity promoEntity: promoEntities){
                                addParticipationToApiRegister(promoEntity);
                            }
                        }
                    });

                    LiveData<List<ProductEntity>> productLiveData= ViewModelProviders.of(LoginActivity.this).get(ProductListViewModel.class).getAllProducts();
                    productLiveData.observe(LoginActivity.this, productEntities -> {
                        if(productEntities!=null){
                            productLiveData.removeObservers(LoginActivity.this);
                            for(ProductEntity productEntity: productEntities){
                                createProductApiRegister(productEntity);
                            }
                        }
                    });
*/


                }
                finish();
            }else{
                try {
                    assert response.errorBody() != null;
                    JSONObject jObjError = new JSONObject(response.errorBody().string());
                    String code =jObjError.getString("code");
                    if(code.equals("WrongRecommendationCode"))
                        viewPagerAdapter.registerFragment.setCodeError(jObjError.getString("description"));
                    else
                        viewPagerAdapter.registerFragment.setEmailError(jObjError.getString("description"));

                } catch (Exception ignored) {
                }

            }
        }

        @Override
        public void onFailure(@NotNull Call<UserPojo> call, @NotNull Throwable t) {
        }
    };

    private void register(RegisterPojo registerPojo) {
        Call<UserPojo> call= apiSessionInterface.register(registerPojo);
        call.enqueue(new RetrofitCallback<>(LoginActivity.this , registerCallback));
    }
/*
    private void createProductApiRegister(ProductEntity productEntity){
        Call<IdPojo> call= apiInterface.createProduct(new ProductPojo(productEntity));
        call.enqueue(new RetrofitCallback<>(this, new Callback<IdPojo>() {
            @Override
            public void onResponse(@NotNull Call<IdPojo> call, @NotNull Response<IdPojo> response) {
                if(response.code()==201){
                    if (response.body()!=null){
                        //set id
                        productEntity.setId(response.body().getId());
                        ViewModelProviders.of(LoginActivity.this).get(ProductViewModel.class).update(productEntity);
                    }

                }

            }

            @Override
            public void onFailure(@NotNull Call<IdPojo> call, @NotNull Throwable t) {
            }
        }));
    }

    public void addParticipationToApiRegister(PromoEntity promoEntity){
        Call<IdPojo> call= apiInterface.addParticipation(new addParticipationPojo(promoEntity.getId(),
                DateTypeConverter.toLong(promoEntity.getContractSigningDate()), promoEntity.isCompleted()));
        call.enqueue(new RetrofitCallback<>(this ,new Callback<IdPojo>() {
            @Override
            public void onResponse(@NotNull Call<IdPojo> call, @NotNull Response<IdPojo> response) {
                if(response.code()== 201 && response.body()!=null){
                    IdPojo idPojo = response.body();
                    addParticipationRoomRegister(promoEntity.getId(), idPojo.getId(), promoEntity.isCompleted());
                }
            }

            @Override
            public void onFailure(@NotNull Call<IdPojo> call, @NotNull Throwable t) {
            }
        }));
    }

    private void addParticipationRoomRegister(long promoId, long participationId, boolean completed){
        PromoViewModel.Factory factory= new PromoViewModel.Factory(getApplication(), promoId);
        PromoViewModel promoViewModel= ViewModelProviders.of(this, factory).get(PromoViewModel.class);

        promoViewModel.updatePromoUserData(participationId, null, promoId, completed);
        LiveData<List<TaskEntity>> liveData= promoViewModel.getTasks();

        liveData.observe(this, taskEntities -> {
            if(taskEntities!=null){
                liveData.removeObservers(this);
                for (TaskEntity taskEntity: taskEntities){
                    taskEntity.setParticipationId(participationId);
                    updateTaskOnApi(taskEntity, false);
                }
            }
        });

    }*/

    //FROM REGISTER AND LOGIN FRAGMENTS (CALLBACKS)
    @Override
    public void orSignUp() {
        viewPager.setCurrentItem(1, true);
    }

    @Override
    public void orSignIn() {
        viewPager.setCurrentItem(0, true);
    }

    @Override
    public void onRegister(RegisterPojo registerPojo) {
        register(registerPojo);
    }

    @Override
    public void onLogin(LoginPojo loginPojo) {
        login(loginPojo);
    }

    @Override
    public void onFinish(){
        finish();
    }

    //ADAPTER
    private class ViewPagerAdapter extends FragmentStatePagerAdapter {

        private LoginFragment loginFragment;
        private RegisterFragment registerFragment;

        ViewPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            if (position==0){
                if (loginFragment== null)
                    return loginFragment= LoginFragment.newInstance();
                else
                    return loginFragment;
            } else{
                if (registerFragment==null)
                    return registerFragment= RegisterFragment.newInstance();
                else
                    return registerFragment;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
