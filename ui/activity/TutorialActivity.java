package pl.rozbijbank.ui.activity;
/*
 * CALL IT DONE 1.11.2018
 */
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;
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
import pl.rozbijbank.utilities.Constants;


public class TutorialActivity extends AppCompatActivity {

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
    private ImageView[] indicators;

    /**
     * mPage and mSize controls activity (indicators, colors etc) when fragment number controls what we see only
     *
     */
    int mPage = 0;   //  to track page position
    int mSize =5;
    int tutType=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tutType= getIntent().getIntExtra(Constants.KEY_TUTORIAL_TYPE, 0);
        switch (tutType){
            case 1:
                mSize=8;
                break;
            case 2:
                mSize=5;
                break;
          /*  case 3:
                mSize=4;
                break;
            case 4:
                mSize=12;
                break;
            case 5:
                mSize=6;
                break;
            case 6:
                mSize=4;
                break;*/
        }


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


    private void setClicks(){
        mBinding.introBtnNext.setOnClickListener(v -> {
            mPage++;
            mBinding.viewPager.setCurrentItem(mPage, true);
        });

        mBinding.introBtnSkip.setOnClickListener(v ->
               finish());

        mBinding.introBtnFinish.setOnClickListener(v ->
                finish());
    }


    void setViewPager(){
        indicators = new ImageView[]{mBinding.introIndicator0, mBinding.introIndicator1, mBinding.introIndicator2,
                mBinding.introIndicator3, mBinding.introIndicator4, mBinding.introIndicator5, mBinding.introIndicator6,
                mBinding.introIndicator7,  mBinding.introIndicator8,  mBinding.introIndicator9};

        for (int i = 0; i< mSize ; i++)
            indicators[i].setVisibility(View.VISIBLE);

        // Set up the ViewPager with the sections adapter.
        mBinding.viewPager.setAdapter(mSectionsPagerAdapter);

        mBinding.viewPager.setCurrentItem(mPage);
        updateIndicators(mPage);
        mBinding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                mPage = position;

                updateIndicators(mPage);


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

        @BindView(R.id.section_img) ImageView mImg;
        @BindView(R.id.section_label) TextView mLabel;
        @BindView(R.id.section_description) TextView mDescription;


        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int tutType, int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            args.putInt(Constants.KEY_TUTORIAL_TYPE, tutType);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_tutorial, container, false);
            ButterKnife.bind(this, rootView);
            getTutorialType();
            return rootView;
        }

        private void setFragmentSpecifics(String title, final int imageId, String description){
            mLabel.setText(title);
            Glide
                    .with(this)
                    .load(imageId)
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(50)))
                    .into(mImg);
            mDescription.setText(description);
        }


        private void getTutorialType(){
            if (getArguments() != null) {

                switch (getArguments().getInt(Constants.KEY_TUTORIAL_TYPE)){
                    case 1:// trans
                        firstPromoTut();
                        break;

                    case 2:// trans
                        pointsTutorial();
                        break;
                }
            }
        }

        private void pointsTutorial(){
            if (getArguments() != null) {

                switch (getArguments().getInt(ARG_SECTION_NUMBER)){
                    case 1:
                        setFragmentSpecifics(getString(R.string.tutorial_points_title_1), R.drawable.tut_points_1, getString(R.string.tutorial_points_desc_1));
                        break;
                    case 2:
                        setFragmentSpecifics(getString(R.string.tutorial_points_title_2), R.drawable.tut_points_2, getString(R.string.tutorial_points_desc_2));
                        break;
                    case 3:
                        setFragmentSpecifics(getString(R.string.tutorial_points_title_3), R.drawable.tut_points_3, getString(R.string.tutorial_points_desc_3));
                        break;
                    case 4:
                        setFragmentSpecifics(getString(R.string.tutorial_points_title_4), R.drawable.tut_points_4, getString(R.string.tutorial_points_desc_4));
                        break;
                    case 5:
                        setFragmentSpecifics(getString(R.string.tutorial_points_title_5), R.drawable.tut_points_5, getString(R.string.tutorial_points_desc_5));
                        break;
                }
            }
        }


        private void firstPromoTut(){
            if (getArguments() != null) {

                switch (getArguments().getInt(ARG_SECTION_NUMBER)){
                    case 1:
                        setFragmentSpecifics(getString(R.string.tutorial_fp_title_1), R.drawable.first_promo_1, getString(R.string.tutorial_fp_desc_1));
                        break;
                    case 2:
                        setFragmentSpecifics(getString(R.string.tutorial_fp_title_2), R.drawable.first_promo_2, getString(R.string.tutorial_fp_desc_2));

                        break;
                    case 3:
                        setFragmentSpecifics(getString(R.string.tutorial_fp_title_3), R.drawable.first_promo_3, getString(R.string.tutorial_fp_desc_3));

                        break;
                    case 4:
                        setFragmentSpecifics(getString(R.string.tutorial_fp_title_4), R.drawable.first_promo_4, getString(R.string.tutorial_fp_desc_4));
                        break;
                    case 5:
                        setFragmentSpecifics(getString(R.string.tutorial_fp_title_5), R.drawable.first_promo_5, getString(R.string.tutorial_fp_desc_5));
                        break;

                    case 6:
                        setFragmentSpecifics(getString(R.string.tutorial_fp_title_6), R.drawable.first_promo_6, getString(R.string.tutorial_fp_desc_6));
                        break;

                    case 7:
                        setFragmentSpecifics(getString(R.string.tutorial_fp_title_7), R.drawable.first_promo_7, getString(R.string.tutorial_fp_desc_7));
                        break;

                    case 8:
                        setFragmentSpecifics(getString(R.string.tutorial_fp_title_8), R.drawable.first_promo_8, getString(R.string.tutorial_fp_desc_8));
                        break;
                }
            }
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
            return PlaceholderFragment.newInstance(tutType, position + 1);
        }

        @Override
        public int getCount() {
            return mSize;
        }
    }
}