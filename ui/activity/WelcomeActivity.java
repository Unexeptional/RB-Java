package pl.rozbijbank.ui.activity;
/*
 * CALL IT DONE 1.11.2018
 */
import android.animation.ArgbEvaluator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;
import pl.rozbijbank.R;
import pl.rozbijbank.databinding.ActivityWelcomeBinding;
import pl.rozbijbank.db.entity.BankEntity;
import pl.rozbijbank.db.entity.PromoEntity;
import pl.rozbijbank.db.entity.TaskEntity;
import pl.rozbijbank.networking.ApiClient;
import pl.rozbijbank.networking.ApiInterface;
import pl.rozbijbank.networking.pojo.BankPojo;
import pl.rozbijbank.networking.pojo.PromoTasksPojo;
import pl.rozbijbank.networking.pojo.TaskPojo;
import pl.rozbijbank.other.MyApplication;
import pl.rozbijbank.ui.dialog.LoadingDialog;
import pl.rozbijbank.utilities.Constants;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class WelcomeActivity extends AppCompatActivity {

    /**
     * The {@link androidx.viewpager.widget.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link androidx.fragment.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ActivityWelcomeBinding mBinding;

    /**
     * The {@link ViewPager} that will host the section contents.
     */

    private ImageView[] indicators;
    private SharedPreferences preferences;
    private LoadingDialog loadingDialog;
    private  Call<List<PromoTasksPojo>> promoTaskCall;
    private  Call<List<BankPojo>> bankCall;

    /**
     * mPage and mSize controls activity (indicators, colors etc) when fragment number controls what we see only
     *
     */
    int mPage = 0;   //  to track page position
    int mSize =4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        getStuffFromApi(true);

        //StatusColorControl
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black));
        }

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_welcome);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

       setViewPager();

       setClicks();
    }

    private void getStuffFromApi(boolean inTheShadow){
        ApiInterface apiInterface= ApiClient.getClient().create(ApiInterface.class);
        bankCall= apiInterface.getBanks();

        bankCall.enqueue(new Callback<List<BankPojo>>() {
            @Override
            public void onResponse(@NotNull Call<List<BankPojo>> call, @NotNull Response<List<BankPojo>> response) {
                if(response.code()== 200 && response.body()!=null){
                    List<BankPojo> banks= response.body();
                    List<BankEntity> bankEntities= new ArrayList<>();

                    for(BankPojo bankPojo: banks)
                        bankEntities.add(new BankEntity(bankPojo.getId(), bankPojo.getTitle()));

                    getPromosFromApi(apiInterface, bankEntities, inTheShadow);
                }
            }

            @Override
            public void onFailure(@NotNull Call<List<BankPojo>> call, @NotNull Throwable t) {
                if (!inTheShadow)
                    showGetDataFailDialog();
            }
        });
    }


    private void getPromosFromApi(ApiInterface apiInterface, List<BankEntity> bankEntities, boolean inTheShadow){
       promoTaskCall = apiInterface.getPromos();
       promoTaskCall.enqueue(new Callback<List<PromoTasksPojo>>() {
            @Override
            public void onResponse(@NotNull Call<List<PromoTasksPojo>> call, @NotNull Response<List<PromoTasksPojo>> response) {
                if(response.code()== 200 && response.body()!=null){
                    List<PromoEntity> promoEntities= new ArrayList<>();
                    List<TaskEntity> taskEntities= new ArrayList<>();

                    for (PromoTasksPojo promo: response.body()){
                        promoEntities.add(new PromoEntity(promo));
                        if(promo.getTasks()!=null)
                            for (TaskPojo taskPojo: promo.getTasks())
                                taskEntities.add(new TaskEntity(taskPojo));
                    }
                    ((MyApplication)getApplicationContext()).getUltraRepository().setDb(bankEntities, promoEntities, taskEntities);
                    setDatabaseDone();
                }
            }

            @Override
            public void onFailure(@NotNull Call<List<PromoTasksPojo>> call, @NotNull Throwable t) {
                if (!inTheShadow)
                    showGetDataFailDialog();
            }
        });
    }

    private void setDatabaseDone(){
        preferences.edit().putBoolean(getString(R.string.key_database_need_fetch), false).apply();

        ((MyApplication)getApplicationContext()).getUltraRepository().
                setTimestamp( Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis());

        if(loadingDialog!=null){
            loadingDialog.hideDialog();
            goToNextActivity();
        }

    }

    private void showGetDataFailDialog(){
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.warning_cant_get_data))
                .setCancelable(false)
                .setPositiveButton(R.string.retry, (dialog, which) -> {
                    getStuffFromApi(false);
                    loadingDialog= new LoadingDialog(this);
                    loadingDialog.showDialog(getString(R.string.db_loading));
                })

                .setNegativeButton(R.string.close_app, (dialog, which) ->
                        finish())
                .show();
    }

    private void setClicks(){
        mBinding.introBtnNext.setOnClickListener(v -> {
            mPage++;
            mBinding.viewPager.setCurrentItem(mPage, true);
        });

        mBinding.introBtnSkip.setOnClickListener(v ->
                goToNextActivity());

        mBinding.introBtnFinish.setOnClickListener(v ->
                goToNextActivity());
    }

    private void goToNextActivity(){
        if(promoTaskCall !=null)
            promoTaskCall.cancel();

        if (bankCall!=null)
            bankCall.cancel();

        boolean firstBoot= preferences.getBoolean(getString(R.string.key_database_need_fetch), true);

        if(firstBoot){
            showGetDataFailDialog();
        }else{
            String token= preferences.getString(getString(R.string.key_active_user_token), "");
            if(token != null && !token.isEmpty())
                startActivity(new Intent(this, MainActivity.class));
            else{
                Intent intent= new Intent(this, LoginActivity.class);
                intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_REGISTER_FRAGMENT);
                startActivity(intent);
            }
            finish();
        }


    }

    void setViewPager(){
        indicators = new ImageView[]{mBinding.introIndicator0, mBinding.introIndicator1, mBinding.introIndicator2,
                mBinding.introIndicator3, mBinding.introIndicator4, mBinding.introIndicator5, mBinding.introIndicator6};

        for (int i = 0; i< mSize ; i++)
            indicators[i].setVisibility(View.VISIBLE);

        // Set up the ViewPager with the sections adapter.
        mBinding.viewPager.setAdapter(mSectionsPagerAdapter);

        mBinding.viewPager.setCurrentItem(mPage);
        updateIndicators(mPage);
        final int color1 = ContextCompat.getColor(this, R.color.account_blue);
        final int color2 = ContextCompat.getColor(this, R.color.blue);
        final int color3 = ContextCompat.getColor(this, R.color.light_blue);
        final int color4 = ContextCompat.getColor(this, R.color.cyan);
        final int color5 = ContextCompat.getColor(this, R.color.teal);
        final int color6 = ContextCompat.getColor(this, R.color.indigo);

        final int[] colorList = new int[]{color1, color2, color3, color4, color5, color6};

        final ArgbEvaluator evaluator = new ArgbEvaluator();

        mBinding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                /*
                color update
                 */
                int colorUpdate = (Integer) evaluator.evaluate(positionOffset, colorList[position], colorList[position == mSize -1 ? position : position + 1]);
                //mBinding.viewPager.setBackgroundColor(colorUpdate);
                //mBinding.navigation.setBackgroundColor(colorUpdate);

            }

            @Override
            public void onPageSelected(int position) {

                mPage = position;

                updateIndicators(mPage);

/*
                switch (position) {
                    case 0:
                        mBinding.viewPager.setBackgroundColor(color1);
                        break;
                    case 1:
                        mBinding.viewPager.setBackgroundColor(color2);
                        break;
                    case 2:
                        mBinding.viewPager.setBackgroundColor(color3);
                        break;
                    case 3:
                        mBinding.viewPager.setBackgroundColor(color4);
                        break;
                    case 4:
                        mBinding.viewPager.setBackgroundColor(color5);
                        break;
                    case 5:
                        mBinding.viewPager.setBackgroundColor(color6);
                        break;
                }
*/

                mBinding.introBtnNext.setVisibility(position == mSize -1 ? View.GONE : View.VISIBLE);
                mBinding.introBtnFinish.setVisibility(position == mSize -1 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    void updateIndicators(int position) {
        for (int i = 0; i < indicators.length; i++) {
            indicators[i].setBackgroundResource(
                    i == position ? R.drawable.indicator_selected : R.drawable.indicator_unselected
            );
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        @BindView(R.id.section_img)
        ImageView mImg;
        @BindView(R.id.section_label)
        TextView mLabel;
        @BindView(R.id.section_description)
        TextView mDescription;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        private void setFragmentSpecifics(String title, int imageId, String description){
            mLabel.setText(title);
            mDescription.setText(description);
            Glide.with(this)
                    .load(imageId)
                    //.apply(RequestOptions.circleCropTransform())
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(50)))
                    .into(mImg);
        }

        private void getFragmentSpecifics(){
            if (getArguments() != null) {

                switch (getArguments().getInt(ARG_SECTION_NUMBER)){
                    case 1:
                        setFragmentSpecifics(getString(R.string.welcome_activity_title_1), R.drawable.welcome_1, getString(R.string.welcome_activity_description_1));
                        break;
                    case 2:
                        setFragmentSpecifics(getString(R.string.welcome_activity_title_2), R.drawable.welcome_2, getString(R.string.welcome_activity_description_2));
                        break;
                    case 3:
                        setFragmentSpecifics(getString(R.string.welcome_activity_title_3), R.drawable.welcome_3, getString(R.string.welcome_activity_description_3));
                        break;
                    case 4:
                        setFragmentSpecifics(getString(R.string.welcome_activity_title_4), R.drawable.welcome_4, getString(R.string.welcome_activity_description_4));
                        break;
                }
            }
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_pager, container, false);
            ButterKnife.bind(this, rootView);
            getFragmentSpecifics();
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {


        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @NotNull
        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given mPage.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);

        }

        @Override
        public int getCount() {
            return mSize;
        }
    }
}