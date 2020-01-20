package pl.rozbijbank.ui.fragment.list;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import pl.rozbijbank.R;
import pl.rozbijbank.callback.BankClickCallback;
import pl.rozbijbank.db.entity.BankEntity;
import pl.rozbijbank.db.entity.ProductEntity;
import pl.rozbijbank.utilities.Constants;
import pl.rozbijbank.ui.adapter.BankViewAdapter;
import pl.rozbijbank.viewModel.BankListViewModel;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link BankClickCallback}
 * interface.
 */
public class BankListFragment extends BasicListFragment {

    private BankViewAdapter bankViewAdapter;
    private BankClickCallback mListener;
    private BankListViewModel viewModel;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BankListFragment() {
    }

    public static BankListFragment newInstance() {
        BankListFragment fragment = new BankListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void reloadItems(){
        viewModel.getAllBanks().observe(this, banks -> {

            if (banks != null) {
                for (BankEntity bankEntity: banks){
                    viewModel.getBankProducts(bankEntity.getId()).observe(this, productEntities -> {
                        if (productEntities!=null){
                            if(productEntities.isEmpty())
                                bankEntity.setActive(false);
                            else
                                bankEntity.setActive(true);

                            mBinding.setIsLoading(false);
                            bankViewAdapter.setBankList(banks);
                        }
                    });

                }
            } else {
                mBinding.setIsLoading(true);
            }

            mBinding.executePendingBindings();
            mBinding.swipeContainer.setRefreshing(false);
        });
    }

    @Override
    protected void setAdapter() {
        bankViewAdapter = new BankViewAdapter(mListener);
        mBinding.activeList.setAdapter(bankViewAdapter);
    }

    @Override
    protected void setViewModel() {
        viewModel = ViewModelProviders.of(BankListFragment.this).get(BankListViewModel.class);
    }

    @Override
    protected void getBundle() {
        //empty for now
    }

    @Override
    protected void clearFilter() {

    }

    @Override
    protected void setEmptyText() {
        mBinding.emptyListText.setText(getString(R.string.no_banks));
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
        if (context instanceof BankClickCallback) {
            mListener = (BankClickCallback) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement BankClickCallback");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
