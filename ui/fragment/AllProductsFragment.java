package pl.rozbijbank.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import pl.rozbijbank.R;
import pl.rozbijbank.callback.ProductClickCallback;
import pl.rozbijbank.databinding.ActivityBankProductsBinding;
import pl.rozbijbank.db.entity.ProductEntity;
import pl.rozbijbank.ui.activity.SecondActivity;
import pl.rozbijbank.ui.adapter.ProductViewAdapter;
import pl.rozbijbank.ui.fragment.list.ComplexListFragment;
import pl.rozbijbank.utilities.Constants;
import pl.rozbijbank.viewModel.ProductListViewModel;

public class AllProductsFragment extends BasicFragment  {

    private boolean isTablet;
    private boolean isPortrait;
    private ActivityBankProductsBinding mBinding;
    private ProductListFragment accountsFragment, cardsFragment, depositsFragment;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     **/
    public static AllProductsFragment newInstance() {
        AllProductsFragment fragment = new AllProductsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isTablet= getResources().getBoolean(R.bool.isTablet);
        isPortrait= getResources().getBoolean(R.bool.isPortrait);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding= DataBindingUtil.inflate(inflater, R.layout.activity_bank_products, container, false);
        mBinding.fab.setVisibility(View.GONE);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.viewPager.setAdapter(new ViewPagerAdapter(getChildFragmentManager()));
        mBinding.viewPager.setOffscreenPageLimit(3);
        mBinding.tabLayout.setupWithViewPager(mBinding.viewPager);
        //in order to recreate after oreintation change
        setRetainInstance(true);
        reloadItems();
    }

    @NotNull
    @Override
    public String toString() {
        return Constants.TAG_ALL_PRODUCTS_FRAGMENT;
    }

    private void reloadItems(){
        ViewModelProviders.of(this).get(ProductListViewModel.class).
                getAllProducts().observe(this, productEntities -> {
            List<ProductEntity> accounts= new ArrayList<>();
            List<ProductEntity> cards= new ArrayList<>();
            List<ProductEntity> deposits= new ArrayList<>();

            if(productEntities!=null){
                for (ProductEntity productEntity: productEntities)
                    switch (productEntity.getProductType() / 10) {
                        case 1:
                            accounts.add(productEntity);
                            break;
                        case 2:
                            cards.add(productEntity);
                            break;
                        case 3:
                            deposits.add(productEntity);
                            break;
                        default:
                            accounts.add(productEntity);
                    }
            }
            if(accountsFragment!=null)
                accountsFragment.setItems(accounts);

            if(cardsFragment !=null)
                cardsFragment.setItems(cards);

            if(depositsFragment!=null)
                depositsFragment.setItems(deposits);
        });
    }

    public void newProduct(){
        Intent intent= new Intent(getActivity(), SecondActivity.class);
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
                    fragment = accountsFragment= ProductListFragment.newInstance();
                    break;
                case 1:
                    fragment = cardsFragment = ProductListFragment.newInstance();
                    break;
                case 2:
                    fragment = depositsFragment= ProductListFragment.newInstance();
                    break;
                default:
                    fragment = ProductListFragment.newInstance();
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

    /**
     * A fragment representing a list of Items.
     * <p/>
     * Activities containing this fragment MUST implement the {@link ProductClickCallback}
     * interface.
     */
    public static class ProductListFragment extends ComplexListFragment {

        private ProductViewAdapter activeViewAdapter, inactiveViewAdapter;
        private ProductClickCallback mListener;

        /**
         * Mandatory empty constructor for the fragment manager to instantiate the
         * fragment (e.g. upon screen orientation changes).
         */
        public ProductListFragment() {
        }

        public static ProductListFragment newInstance() {
           ProductListFragment fragment = new ProductListFragment();
            Bundle args = new Bundle();
            fragment.setArguments(args);
            return fragment;
        }

        @NotNull
        @Override
        public String toString() {
            return "";
        }


        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            //in order to recreate after oreintation change

            setRetainInstance(true);
        }

        //to jest bardzo podobne na każdym... ale nei takie samo
        public void setItems(List<ProductEntity> productEntities){
            List<ProductEntity> active= new ArrayList<>();
            List<ProductEntity> inactive= new ArrayList<>();

            setEmptyText(productEntities.isEmpty());

            for (ProductEntity productEntity: productEntities)
                if(productEntity.isInactive())
                    inactive.add(productEntity);
                else
                    active.add(productEntity);

            if(inactive.isEmpty())
                mBinding.inactiveButton.setVisibility(View.GONE);
            else
                mBinding.inactiveButton.setVisibility(View.VISIBLE);


            activeViewAdapter.setProductList(active);
            inactiveViewAdapter.setProductList(inactive);
        }

        @Override
        public void reloadItems() {

        }

        @Override
        protected void setAdapters() {
            activeViewAdapter = new ProductViewAdapter(mListener);
            mBinding.activeList.setAdapter(activeViewAdapter);

            inactiveViewAdapter = new ProductViewAdapter(mListener);
            mBinding.inactiveList.setAdapter(inactiveViewAdapter);
        }

        @Override
        protected void setViewModel() {

        }

        @Override
        protected void setObserver() {

        }

        @Override
        protected void getBundle() {

        }

        @Override
        protected void clearFilter() {

        }

        @Override
        protected void setEmptyText(boolean value) {
            super.setEmptyText(value);
            mBinding.emptyListText.setText(R.string.my_products_empty);
        }

        @Override
        protected String setButtonText() {
            return getString(R.string.closed);
        }

        //Listener
        @Override
        public void onAttach(@NonNull Context context) {
            super.onAttach(context);
            if (context instanceof ProductClickCallback) {
                mListener = (ProductClickCallback) context;
            } else {
                throw new RuntimeException(context.toString()
                        + " must implement ProductClickCallback");
            }
        }

        @Override
        public void onDetach() {
            super.onDetach();
            mListener = null;
        }
    }

}
