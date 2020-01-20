package pl.rozbijbank.ui.fragment;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import pl.rozbijbank.R;
import pl.rozbijbank.callback.PickIconCallback;
import pl.rozbijbank.callback.PickUserCallback;
import pl.rozbijbank.databinding.FragmentUserAccountBinding;
import pl.rozbijbank.databinding.PointsHistoryItemBinding;
import pl.rozbijbank.db.converter.DateTypeConverter;
import pl.rozbijbank.db.entity.UserEntity;
import pl.rozbijbank.networking.ApiClient;
import pl.rozbijbank.networking.ApiSessionInterface;
import pl.rozbijbank.networking.RetrofitCallback;
import pl.rozbijbank.networking.pojo.session.AvatarPojo;
import pl.rozbijbank.networking.pojo.session.ChangePasswordPojo;
import pl.rozbijbank.networking.pojo.session.CodePojo;
import pl.rozbijbank.networking.pojo.session.CorrectCodePojo;
import pl.rozbijbank.networking.pojo.session.PasswordPojo;
import pl.rozbijbank.networking.pojo.session.PointsHistoryPojo;
import pl.rozbijbank.networking.pojo.session.PointsPojo;
import pl.rozbijbank.other.MyApplication;
import pl.rozbijbank.ui.activity.SecondActivity;
import pl.rozbijbank.ui.dialog.PickIconDialog;
import pl.rozbijbank.ui.dialog.PickUserDialog;
import pl.rozbijbank.ui.dialog.popup.ChromeHelpPopup;
import pl.rozbijbank.utilities.Constants;
import pl.rozbijbank.viewModel.UserListViewModel;
import pl.rozbijbank.viewModel.UserViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//todo change email
//todo visual sucks

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link UserAccountFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserAccountFragment extends BasicFragment {

    private ApiSessionInterface apiSessionInterface;
    private FragmentUserAccountBinding mBinding;
    private UserViewModel viewModel;
    private LiveData<UserEntity> liveData;
    private UserEntity activeUser;
    private AlertDialog verifyEmailDialog;
    private AlertDialog changePasswordDialog;


    public UserAccountFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     **/
    public static UserAccountFragment newInstance() {
        UserAccountFragment fragment = new UserAccountFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences= PreferenceManager.getDefaultSharedPreferences(getActivity());
        viewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        apiSessionInterface = ApiClient.getClient().create(ApiSessionInterface.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding= DataBindingUtil.inflate(inflater, R.layout.fragment_user_account, container, false);

        getUser();
        setClicks();
        return mBinding.getRoot();
    }

    private void getUser(){
        String userId = preferences.getString(getString(R.string.key_active_user_id), "");

        //to make sure changing other accounts will not swich actual account
        if(liveData!=null)
            liveData.removeObservers(this);

        liveData= viewModel.getUser(userId);

        liveData.observe(this, userEntity -> {
            if(userEntity!=null){
                setVisuals(userEntity);
                activeUser= userEntity;
                preferences.edit().putString(getString(R.string.key_active_user_token), userEntity.getToken()).apply();
            }

        });
    }

    private void setVisuals(UserEntity userEntity){
        mBinding.setUser(userEntity);

        int id= MyApplication.getDrawableId(userEntity.getAvatar());
        if(id!=0)
            Picasso.get().load(id).into(mBinding.avatar);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setClicks(){
        mBinding.logout.setOnClickListener(v -> showLogoutDialog());

        //BETA ONLY
        mBinding.pointsTitle.setOnClickListener(v -> new ChromeHelpPopup(getActivity(),getString(R.string.popup_points)).
                show(mBinding.pointsTitle));

        //BETA ONLY
        mBinding.recommendationTitle.setOnClickListener(v -> {
            ChromeHelpPopup chromeHelpPopup = new ChromeHelpPopup(getActivity(),getString(R.string.popup_recomm));
            chromeHelpPopup.show(mBinding.recommendationTitle);
        });

        mBinding.switchAccount.setOnClickListener(v ->
                new PickUserDialog((SecondActivity) getActivity(), pickUserCallback).showDialog());

        mBinding.refreshPoints.setOnClickListener(v -> getPointsFromApi());

        mBinding.avatar.setOnClickListener(v ->
                new PickIconDialog((SecondActivity) getActivity(), pickIconCallback).showDialog());

        mBinding.changePassword.setOnClickListener(v -> showChangePasswordDialog());

        mBinding.share.setOnClickListener(v -> share());

        mBinding.deleteAccount.setOnClickListener(v -> showDeleteAccountDialog());

        mBinding.userRecommendationCode.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;

            if(event.getAction() == MotionEvent.ACTION_UP) {

                if(  mBinding.userRecommendationCode.getCompoundDrawables()[DRAWABLE_RIGHT]!=null)
                    if(event.getRawX() >= (  mBinding.userRecommendationCode.getRight() - 50 -  mBinding.userRecommendationCode.
                            getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        ClipboardManager cm = (ClipboardManager) MyApplication.getContext()
                                .getSystemService(Context.CLIPBOARD_SERVICE);
                        if (cm != null) {
                            ClipData myClip;
                            myClip = ClipData.newPlainText("text",      mBinding.userRecommendationCode.getText().toString());
                            cm.setPrimaryClip(myClip);
                            Toast.makeText(getActivity(), R.string.warning_copied_to_clipboard, Toast.LENGTH_SHORT).show();
                        }
                    }
            }
            return false;
        });


       /* mBinding.changeEmail.setOnClickListener(v -> {
            //make a call
            //onresponse start verifyEmailDialog with code
            new ChangeEmailDialog(getActivity(), "123456").showDialog();
        });*/

        mBinding.verifyEmail.setOnClickListener(v -> showVerifyEmailDialog());

        mBinding.pointsHistory.setOnClickListener(v -> getPointsHistory());

        mBinding.moreBtn.setOnClickListener(v -> showMorePopup());
    }

   private void showMorePopup(){
       PopupMenu popup = new PopupMenu(Objects.requireNonNull(getActivity()), mBinding.moreBtn);
       popup.getMenuInflater()
               .inflate(R.menu.user_account_more, popup.getMenu());

       popup.setOnMenuItemClickListener(item -> {
           switch (item.getItemId()) {
               case R.id.user_acc_logout:
                   showLogoutDialog();
                   return true;

               case R.id.user_acc_delete_acc:
                   showDeleteAccountDialog();
                   return true;

               case R.id.user_acc_change_pass:
                   showChangePasswordDialog();
                   return true;
               default:
                   return false;
           }
       });

       popup.show();
   }


    //LOGOUT
    private void showLogoutDialog(){
        new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                .setTitle(getString(R.string.logout_confirm))
                .setPositiveButton(android.R.string.yes, (dialog, which) -> logout())
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void logout(){
        viewModel.delete(activeUser);
        setDefaultDB();
        preferences.edit().putString(getString(R.string.key_active_user_token), "").apply();
        preferences.edit().putString(getString(R.string.key_active_user_id), "").apply();
        Toast.makeText(getActivity(), getString(R.string.logged_out), Toast.LENGTH_SHORT).show();
        Objects.requireNonNull(getActivity()).finish();

        //stuff related to switch accounts... later
   /*     LiveData<List<UserEntity>> liveData= ViewModelProviders.of(this).get(UserListViewModel.class).getUsers();
        liveData.observe(this, userEntities -> {
            if (userEntities != null) {
                liveData.removeObservers(this);
                if (!userEntities.isEmpty()) {
                    preferences.edit().putString(getString(R.string.key_active_user_token), userEntities.get(0).getToken()).apply();
                    preferences.edit().putString(getString(R.string.key_active_user_id), userEntities.get(0).getUserId()).apply();
                    getUser();
                    if(getActivity()!=null)
                        ((MyApplication)getActivity().getApplicationContext()).getUltraRepository().login();
                } else {
                    setDefaultDB();
                    preferences.edit().putString(getString(R.string.key_active_user_token), "").apply();
                    preferences.edit().putString(getString(R.string.key_active_user_id), "").apply();
                    Toast.makeText(getActivity(), getString(R.string.logged_out), Toast.LENGTH_SHORT).show();
                    Objects.requireNonNull(getActivity()).finish();
                }
            }else{
                setDefaultDB();
                preferences.edit().putString(getString(R.string.key_active_user_token), "").apply();
                preferences.edit().putString(getString(R.string.key_active_user_id), "").apply();
                Toast.makeText(getActivity(), getString(R.string.logged_out), Toast.LENGTH_SHORT).show();
                Objects.requireNonNull(getActivity()).finish();
            }
        });*/
    }

    private void setDefaultDB(){
        if (getActivity()!=null)
            ((MyApplication)getActivity().getApplication()).getUltraRepository().setDefaultDb();
    }

    //SHARE
    private void share(){
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = getString(R.string.share_desc) + activeUser.getRecCode() + "\n\n" +
                "https://play.google.com/store/apps/details?id=pl.rozbijbank";
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, R.string.share_subject);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    //GET POINTS
    private void getPointsFromApi( ) {
        Call<PointsPojo> call= apiSessionInterface.getPoints();

        call.enqueue(new RetrofitCallback<>(getActivity() , new Callback<PointsPojo>() {
            @Override
            public void onResponse(@NotNull Call<PointsPojo> call, @NotNull Response<PointsPojo> response) {
                if(response.code()==200 && response.body()!=null){
                    UserEntity userEntity= liveData.getValue();
                    if(userEntity!=null){
                        userEntity.setPoints(response.body().getPoints());
                        viewModel.update(userEntity);
                    }
                    Toast.makeText(getActivity(), getString(R.string.points_updated), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<PointsPojo> call, @NotNull Throwable t) {

            }
        }));
    }

    //AVATAR CHANGE
    private PickIconCallback pickIconCallback= this::updateAvatarOnApi;

    private void updateAvatarOnApi( String avatar) {
        Call<Void> call= apiSessionInterface.setAvatar(new AvatarPojo(avatar));

        call.enqueue(new RetrofitCallback<>(getActivity() , new Callback<Void>() {
            @Override
            public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                if(response.code()==200){
                    activeUser.setAvatar(avatar);
                    viewModel.update(activeUser);
                }
            }

            @Override
            public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
            }
        }));
    }

    //EMAIL VERIFICATION
    private void showVerifyEmailDialog(){
        AlertDialog.Builder builder  = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));

        View dialogView = View.inflate(getActivity(), R.layout.verify_email_dialog, null);
        builder.setView(dialogView);

        //VIEWS
        TextInputEditText codeEdit= dialogView.findViewById(R.id.code_input);
        TextView resendCode= dialogView.findViewById(R.id.resend_code);
        Button accept= dialogView.findViewById(R.id.btn_accept);
        Button decline= dialogView.findViewById(R.id.btn_decline);

        //CLICKS
        resendCode.setOnClickListener(v -> verificationResend());

        accept.setOnClickListener(v ->{
            String passedCode= Objects.requireNonNull(codeEdit.getText()).toString();
            isCodeCorrect(passedCode, codeEdit);
        });

        decline.setOnClickListener(v -> verifyEmailDialog.dismiss());

        verifyEmailDialog = builder.create();
        verifyEmailDialog.show();
    }

    private void verificationResend( ){
        Call<Void> call= apiSessionInterface.verificationResend();
        call.enqueue(new RetrofitCallback<>(getActivity() , new Callback<Void>() {
            @Override
            public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                if(response.code()==200){
                        Toast.makeText(getActivity(), getString(R.string.toast_code_sent), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
            }
        }));
    }

    private void isCodeCorrect(String code,TextInputEditText codeEdit ){
        Call<CorrectCodePojo> call= apiSessionInterface.verifyEmail(new CodePojo(code));
        call.enqueue(new RetrofitCallback<>(getActivity() , new Callback<CorrectCodePojo>() {
            @Override
            public void onResponse(@NotNull Call<CorrectCodePojo> call, @NotNull Response<CorrectCodePojo> response) {
                if(response.code()==200){
                    if(response.body()!=null)
                        if(response.body().isCorrectCode()){
                            activeUser.setEmailConfirmed(true);
                            activeUser.setPoints(activeUser.getPoints()+100);
                            viewModel.update(activeUser);
                            Toast.makeText(getActivity(), getString(R.string.email_verification_success), Toast.LENGTH_SHORT).show();
                            verifyEmailDialog.dismiss();
                        }else
                            codeEdit.setError(getString(R.string.error_wrong_code));
                }
            }

            @Override
            public void onFailure(@NotNull Call<CorrectCodePojo> call, @NotNull Throwable t) {
            }
        }));
    }

    //DELETE ACCOUNT
    private void showDeleteAccountDialog() {

        AlertDialog.Builder builder  = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));

        View dialogView = View.inflate(getActivity(), R.layout.delete_account_dialog, null);
        builder.setView(dialogView);

        //VIEWS
        TextInputEditText oldPassword= dialogView.findViewById(R.id.old_password_input_edit);
        Button accept= dialogView.findViewById(R.id.btn_accept);
        Button decline= dialogView.findViewById(R.id.btn_decline);

        //CLICKS
        accept.setOnClickListener(v ->{
            String oldPass= Objects.requireNonNull(oldPassword.getText()).toString();

            if(invalidPassword(oldPass)){
                oldPassword.setError(getString(R.string.error_password_short));
            }else
                deleteAccountOnApi(oldPass, oldPassword);

        });

        decline.setOnClickListener(v -> changePasswordDialog.dismiss());

        changePasswordDialog= builder.create();
        changePasswordDialog.show();
    }

    private void deleteAccountOnApi(String currentPass, TextInputEditText passwordEdit){
        ApiSessionInterface apiSessionInterface = ApiClient.getClient().create(ApiSessionInterface.class);

        Call<Void> call= apiSessionInterface.deleteAccount(new PasswordPojo(currentPass));
        call.enqueue(new RetrofitCallback<>(getActivity() , new Callback<Void>() {
            @Override
            public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                if(response.code()==200){
                    Toast.makeText(getActivity(), getString(R.string.account_deleted), Toast.LENGTH_SHORT).show();
                    logout();
                }else
                    passwordEdit.setError(getString(R.string.wrong_password));

            }

            @Override
            public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {

            }
        }));
    }


    //CHANGE PASSWORD
    private void showChangePasswordDialog() {

        AlertDialog.Builder builder  = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));

        View dialogView = View.inflate(getActivity(), R.layout.change_password_dialog, null);
        builder.setView(dialogView);

        //VIEWS
        TextInputEditText oldPassword= dialogView.findViewById(R.id.old_password_input_edit);
        TextInputEditText newPassword= dialogView.findViewById(R.id.password_input_edit);
        TextInputEditText confirmPassword= dialogView.findViewById(R.id.confirm_password_input_edit);
        Button accept= dialogView.findViewById(R.id.btn_accept);
        Button decline= dialogView.findViewById(R.id.btn_decline);

        //CLICKS
        accept.setOnClickListener(v ->{
            String oldPass= Objects.requireNonNull(oldPassword.getText()).toString();
            String newPass= Objects.requireNonNull(newPassword.getText()).toString();
            String confirmPass= Objects.requireNonNull(confirmPassword.getText()).toString();

            if(invalidPassword(oldPass)){
                oldPassword.setError(getString(R.string.error_password_short));
                return;
            }

            if(invalidPassword(newPass)){
                newPassword.setError(getString(R.string.error_password_short));
                return;
            }

            if(newPass.equals(confirmPass))
                changePasswordApi(oldPass, newPass, oldPassword);
            else
                confirmPassword.setError(getString(R.string.error_password_not_the_same));


        });

        decline.setOnClickListener(v -> changePasswordDialog.dismiss());

        changePasswordDialog= builder.create();
        changePasswordDialog.show();
    }

    private void changePasswordApi(String currentPass, String newPass , TextInputEditText oldPasswordEdit){
        ApiSessionInterface apiSessionInterface = ApiClient.getClient().create(ApiSessionInterface.class);
        Call<Void> call= apiSessionInterface.changePassword(new ChangePasswordPojo(currentPass, newPass));
        call.enqueue(new RetrofitCallback<>(getActivity() , new Callback<Void>() {
            @Override
            public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                if(response.code()==200){
                    Toast.makeText(getActivity(), getString(R.string.password_change_success), Toast.LENGTH_SHORT).show();
                    changePasswordDialog.dismiss();
                }else
                    oldPasswordEdit.setError(getString(R.string.wrong_password));
            }

            @Override
            public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {

            }
        }));
    }

    private boolean invalidPassword(String password){
        return password == null || password.equals("") || password.length() <= 5;
    }


        private PickUserCallback pickUserCallback= user -> {
        preferences.edit().putString(getString(R.string.key_active_user_token), user.getToken()).apply();
        preferences.edit().putString(getString(R.string.key_active_user_id), user.getUserId()).apply();

        getUser();
        if(getActivity()!=null)
            ((MyApplication)getActivity().getApplicationContext()).getUltraRepository().login();
    };

  /*  private void getParticipationsFromApi() {
       ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<List<ParticipationPojo>> call= apiInterface.getParticipations();
        call.enqueue(new RetrofitCallback<>(getActivity() ,new Callback<List<ParticipationPojo>>() {
            @Override
            public void onResponse(@NotNull Call<List<ParticipationPojo>> call, @NotNull Response<List<ParticipationPojo>> response) {
                if(response.code()==200){
                    PromoSViewModel viewModel= ViewModelProviders.of(UserAccountFragment.this).get(PromoSViewModel.class);
                    TaskViewModel taskViewModel= ViewModelProviders.of(UserAccountFragment.this).get(TaskViewModel.class);

                    List<ParticipationPojo> pojos= response.body();

                    if(pojos!=null)
                        for (ParticipationPojo pp: pojos){

                            if(pp.getContractSigningDate()!=0)
                                viewModel.updatePromoUserData(pp.getId(), DateTypeConverter.toDate(pp.getContractSigningDate()), pp.getPromoId(), pp.isCompleted());
                            else
                                viewModel.updatePromoUserData(pp.getId(), null, pp.getPromoId(), pp.isCompleted());


                            for (ParticipationTask participationTask: pp.getParticipationTasks())
                                taskViewModel.updateTaskUserData(new TaskEntity(participationTask));

                        }
                }

            }

            @Override
            public void onFailure(@NotNull Call<List<ParticipationPojo>> call, @NotNull Throwable t) {

            }
        }));
    }*/


    //POINTS HISTORY DIALOG
    private void getPointsHistory( ) {
        Call<List<PointsHistoryPojo>> call= apiSessionInterface.getPointsHistory();
        call.enqueue(new RetrofitCallback<>(getActivity() ,new Callback<List<PointsHistoryPojo>>() {
            @Override
            public void onResponse(@NotNull Call<List<PointsHistoryPojo>> call, @NotNull Response<List<PointsHistoryPojo>> response) {
                if(response.code()==200 && response.body()!=null){
                    showPointsHistory(response.body());

                }else if (response.code()==204){
                    Toast.makeText(getActivity(), getString(R.string.empty_list), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<List<PointsHistoryPojo>> call, @NotNull Throwable t) {
            }
    }));
    }

    private void showPointsHistory(List<PointsHistoryPojo> pojoList){
        AlertDialog.Builder builder  = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));

        View dialogView = View.inflate(getActivity(), R.layout.dialog_points_history, null);
        builder.setView(dialogView);

        //VIEWS
        RecyclerView list= dialogView.findViewById(R.id.list);

        PointsHistoryAdapter adapter= new PointsHistoryAdapter();
        list.setAdapter(adapter );
        list.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter.setProductList(pojoList);

        //CLICKS

        verifyEmailDialog = builder.create();
        verifyEmailDialog.show();
    }

    public class PointsHistoryAdapter extends RecyclerView.Adapter<PointsHistoryAdapter.ProductViewHolder> {

        private List<PointsHistoryPojo> historyPojos;

        PointsHistoryAdapter() {
            setHasStableIds(true);
        }

        void setProductList(final List<PointsHistoryPojo> pojoList) {
            historyPojos = pojoList;
            notifyItemRangeInserted(0, pojoList.size());
        }

        @NonNull
        @Override
        public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            PointsHistoryItemBinding binding = DataBindingUtil
                    .inflate(LayoutInflater.from(parent.getContext()), R.layout.points_history_item,
                            parent, false);
            return new ProductViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
            PointsHistoryPojo pointsHistoryPojo= historyPojos.get(position);
            holder.binding.setPointsHistory(pointsHistoryPojo);
            holder.binding.executePendingBindings();

            SimpleDateFormat dateFormatGmt = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            dateFormatGmt.setTimeZone(TimeZone.getTimeZone("UTC"));

            if(pointsHistoryPojo.getCreationDate()!=0)
                holder.binding.date.setText(dateFormatGmt.format(DateTypeConverter.toDate(pointsHistoryPojo.getCreationDate())));

            String promo= pointsHistoryPojo.getDescription();

            if(promo!= null && !promo.equals(""))
                holder.binding.reason.setOnClickListener(v -> new ChromeHelpPopup(getActivity(), promo).
                        show(holder.binding.reason));
        }


        @Override
        public int getItemCount() {
            return historyPojos == null ? 0 : historyPojos.size();
        }


        class ProductViewHolder extends RecyclerView.ViewHolder {

            final PointsHistoryItemBinding binding;

            ProductViewHolder(PointsHistoryItemBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }

    @NotNull
    @Override
    public String toString() {
        return Constants.TAG_USER_ACCOUNT_FRAGMENT;
    }

}
