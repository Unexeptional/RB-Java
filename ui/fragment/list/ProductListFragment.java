package pl.rozbijbank.ui.fragment.list;

import android.content.Context;
import android.os.Bundle;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import pl.rozbijbank.R;
import pl.rozbijbank.callback.ProductClickCallback;
import pl.rozbijbank.db.entity.ProductEntity;
import pl.rozbijbank.ui.adapter.ProductViewAdapter;
import pl.rozbijbank.utilities.Constants;
import pl.rozbijbank.viewModel.ProductListViewModel;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link ProductClickCallback}
 * interface.
 */
public class ProductListFragment extends BasicListFragment {

    public ProductViewAdapter bankViewAdapter;
    private ProductClickCallback mListener;
    private ProductListViewModel viewModel;
    private Observer<List<ProductEntity>> observer;
    private int productType;
    private long bankId= 0;
    private LiveData<List<ProductEntity>> liveData;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ProductListFragment() {
    }

    public static ProductListFragment newInstance(long bankId, int productType) {
        ProductListFragment fragment = new ProductListFragment();
        Bundle args = new Bundle();
        args.putLong(Constants.KEY_BANK_ID, bankId);
        args.putInt(Constants.KEY_PRODUCT_TYPE, productType);
        fragment.setArguments(args);
        return fragment;
    }

    //from basic man
    @Override
    public void reloadItems(){
        /*if(liveData!=null)
            liveData.removeObservers(this);

        getBundle();

        if(bankId!=0)
            liveData= viewModel.getBankProducts(bankId);
        else
            liveData= viewModel.getAllProducts();


        liveData.observe(this, observer);*/
    }

    @Override
    protected void setAdapter() {
        bankViewAdapter = new ProductViewAdapter(mListener);
        mBinding.activeList.setAdapter(bankViewAdapter);
    }

    @Override
    protected void setViewModel() {
       /* viewModel = ViewModelProviders.of(ProductListFragment.this).get(ProductListViewModel.class);

        observer = products -> {
            List<ProductEntity> productEntities= new ArrayList<>();
            if (products != null) {
                mBinding.setIsLoading(false);
                for (ProductEntity productEntity: products)
                    if(productEntity.getProductType()/10==productType)
                        productEntities.add(productEntity);

                bankViewAdapter.setProductList(productEntities);
            } else {
                mBinding.setIsLoading(true);
            }

            mBinding.executePendingBindings();
        };*/
    }

    @Override
    protected void getBundle() {
        if(getArguments()!=null){
            bankId= getArguments().getLong(Constants.KEY_BANK_ID, 0);
            productType= getArguments().getInt(Constants.KEY_PRODUCT_TYPE);
        }
    }

    @Override
    protected void clearFilter() {

    }

    @Override
    protected void setEmptyText() {
        mBinding.emptyListText.setText(getString(R.string.no_products));
    }

    @NotNull
    @Override
    public String toString() {
        return Constants.TAG_BANK_LIST_FRAGMENT;
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
