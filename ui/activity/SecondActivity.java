package pl.rozbijbank.ui.activity;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import pl.rozbijbank.R;
import pl.rozbijbank.databinding.ActivitySecondBinding;
import pl.rozbijbank.ui.fragment.ProductFragment;
import pl.rozbijbank.ui.fragment.SettingsFragment;
import pl.rozbijbank.ui.fragment.UserAccountFragment;
import pl.rozbijbank.ui.fragment.list.RewardListFragment;
import pl.rozbijbank.utilities.Constants;

public class SecondActivity extends BasicActivity {

    ActivitySecondBinding mBinding;

    @Override
    protected int provideActivityLayout() {
        return R.layout.activity_second;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding= (ActivitySecondBinding) dataBinding;

        getStuffFromIntent();
    }

    private void getStuffFromIntent(){
        if (getIntent()!=null){
            String fragmentTag= getIntent().getStringExtra(Constants.KEY_FRAGMENT_TAG);

            switch (fragmentTag){
                case Constants.TAG_SETTINGS_FRAGMENT:
                    startFragment(SettingsFragment.newInstance());
                    break;
                case Constants.TAG_ACCOUNT_FRAGMENT:
                    startFragment(UserAccountFragment.newInstance());
                    break;
                case Constants.TAG_TUTORIALS_FRAGMENT:
                    startFragment(UserAccountFragment.newInstance());
                    break;
                case Constants.TAG_ABOUT_APP_FRAGMENT:
                    startFragment(UserAccountFragment.newInstance());
                    break;
                case Constants.TAG_REWARDS_FRAGMENT:
                    startFragment(RewardListFragment.newInstance());
                    break;
                case Constants.TAG_PRODUCT_FRAGMENT:
                    startFragment(ProductFragment.newInstance(
                            getIntent().getIntExtra(Constants.KEY_PRODUCT_TYPE, 0),
                            getIntent().getLongExtra(Constants.KEY_BANK_ID, 0),
                            getIntent().getLongExtra(Constants.KEY_PRODUCT_ID, 0)));
                    break;

            }
        }
    }

    private void startFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        fragmentTransaction.replace(R.id.fragment_container, fragment, fragment.toString()).commit();
    }


}
