package pl.rozbijbank.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import org.jetbrains.annotations.NotNull;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProviders;
import pl.rozbijbank.R;
import pl.rozbijbank.callback.ProductClickCallback;
import pl.rozbijbank.databinding.ActivityBankProductsBinding;
import pl.rozbijbank.db.model.Product;
import pl.rozbijbank.ui.fragment.list.ProductListFragment;
import pl.rozbijbank.utilities.Constants;
import pl.rozbijbank.viewModel.BankViewModel;

public class BankProductsActivity extends BasicActivity implements
        ProductClickCallback {

    ActivityBankProductsBinding mBinding;
    private long bankId;


    @Override
    protected int provideActivityLayout() {
        return R.layout.activity_bank_products;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding= (ActivityBankProductsBinding) dataBinding;
        mBinding.viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        mBinding.viewPager.setOffscreenPageLimit(3);
        mBinding.tabLayout.setupWithViewPager(mBinding.viewPager);

        bankId= getIntent().getLongExtra(Constants.KEY_BANK_ID, 0);

        BankViewModel viewModel= ViewModelProviders.of(this).get(BankViewModel.class);
        viewModel.getBank(bankId).observe(this, bankEntity -> {
            if(bankEntity!=null){

            }

        });
        setClicks();

    }

    private void setClicks(){
        mBinding.fab.setOnClickListener(v ->{
            Intent intent= new Intent(BankProductsActivity.this, SecondActivity.class);
            switch (mBinding.viewPager.getCurrentItem()){
                case 0:
                    intent.putExtra(Constants.KEY_PRODUCT_TYPE, Constants.PRODUCT_TYPE_ACCOUNT);
                    break;
                case 1:
                    intent.putExtra(Constants.KEY_PRODUCT_TYPE, Constants.PRODUCT_TYPE_CARD);
                    break;
                case 2:
                    intent.putExtra(Constants.KEY_PRODUCT_TYPE, Constants.PRODUCT_TYPE_DEPOSIT);
                    break;

            }
            intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_PRODUCT_FRAGMENT);
            intent.putExtra(Constants.KEY_BANK_ID,bankId);
            startActivity(intent);
        });
    }

    @Override
    public void onProductItemClick(Product product) {
        Intent intent= new Intent(BankProductsActivity.this, SecondActivity.class);
        intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_PRODUCT_FRAGMENT);
        intent.putExtra(Constants.KEY_BANK_ID, bankId);
        intent.putExtra(Constants.KEY_PRODUCT_TYPE, (product.getProductType()/10));
        intent.putExtra(Constants.KEY_PRODUCT_ID, product.getId());
        startActivity(intent);
    }

    //ADAPTER
    private class ViewPagerAdapter extends FragmentPagerAdapter {

        ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @NotNull
        @Override
        public Fragment getItem(int position) {
            Fragment fragment;
            switch (position) {
                case 0:
                    fragment = ProductListFragment.newInstance(bankId, Constants.PRODUCT_TYPE_ACCOUNT);
                    break;
                case 1:
                    fragment = ProductListFragment.newInstance(bankId, Constants.PRODUCT_TYPE_CARD);
                    break;
                case 2:
                    fragment = ProductListFragment.newInstance(bankId, Constants.PRODUCT_TYPE_DEPOSIT);
                    break;
                default:
                    fragment = ProductListFragment.newInstance(bankId, Constants.PRODUCT_TYPE_ACCOUNT);
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.accounts);
                case 1:
                    return getString(R.string.cards);
                case 2:
                    return getString(R.string.savings);
            }
            return getString(R.string.no_title);
        }
    }
}
