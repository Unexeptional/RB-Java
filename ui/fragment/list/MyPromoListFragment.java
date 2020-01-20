package pl.rozbijbank.ui.fragment.list;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.sqlite.db.SimpleSQLiteQuery;
import pl.rozbijbank.R;
import pl.rozbijbank.callback.PromoClickCallback;
import pl.rozbijbank.db.entity.PromoEntity;
import pl.rozbijbank.ui.adapter.PromoViewAdapter;
import pl.rozbijbank.utilities.Constants;
import pl.rozbijbank.viewModel.PromoListViewModel;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link PromoClickCallback}
 * interface.
 */
public class MyPromoListFragment extends ComplexListFragment {

    private PromoViewAdapter activeViewAdapter, inactiveViewAdapter;
    private PromoClickCallback mListener;
    private Observer<List<PromoEntity>> observer;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MyPromoListFragment() {
    }

    public static MyPromoListFragment newInstance() {
        MyPromoListFragment fragment = new MyPromoListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    //from basic
    @Override
    public void reloadItems() {

     /*   if(liveData!=null)
            liveData.removeObservers(this);

        getBundle();

        liveData= viewModel.getParticipatePromos();

        if(liveData!=null)
            liveData.observe(this, observer);*/
    }

    @Override
    protected void setObserver(){
        observer= promoEntities -> {
            List<PromoEntity> active= new ArrayList<>();
            List<PromoEntity> inactive= new ArrayList<>();

            if (promoEntities != null) {
                mBinding.setIsLoading(false);

                for(PromoEntity promoEntity: promoEntities)
                    if(promoEntity.isCompleted())
                        inactive.add(promoEntity);
                    else
                        active.add(promoEntity);


                activeViewAdapter.setPromoList(active);
                inactiveViewAdapter.setPromoList(inactive);

                setEmptyText(promoEntities.isEmpty());

                if(inactive.isEmpty())
                    mBinding.inactiveButton.setVisibility(View.GONE);
                else
                    mBinding.inactiveButton.setVisibility(View.VISIBLE);


            } else {
                mBinding.setIsLoading(true);
            }

            mBinding.setIsFilterActive(isFilterActive);
            mBinding.executePendingBindings();
        };
    }

    @Override
    protected void setAdapters() {
        activeViewAdapter = new PromoViewAdapter(mListener);
        mBinding.activeList.setAdapter(activeViewAdapter);

        inactiveViewAdapter = new PromoViewAdapter(mListener);
        mBinding.inactiveList.setAdapter(inactiveViewAdapter);
    }

    @Override
    protected void setViewModel() {
        PromoListViewModel viewModel = ViewModelProviders.of(this).get(PromoListViewModel.class);
        viewModel.getParticipatePromos().observe(this, observer);
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
        mBinding.emptyListText.setText(R.string.my_promos_empty);
    }

    @Override
    protected String setButtonText() {
        return getString(R.string.completed);
    }

    @NotNull
    @Override
    public String toString() {
        return Constants.TAG_MY_PROMO_LIST_FRAGMENT;
    }

    //listener
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof PromoClickCallback) {
            mListener = (PromoClickCallback) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement PromoClickCallback");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
