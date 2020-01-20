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
public class PromoListFragment extends ComplexListFragment {

    private PromoViewAdapter activeViewAdapter, inactiveViewAdapter;
    private PromoClickCallback mListener;
    private PromoListViewModel viewModel;
    private Set<String> stringSet;
    private LiveData<List<PromoEntity>> activeLiveData;
    private LiveData<List<PromoEntity>> inactiveLiveData;
    private Observer<List<PromoEntity>> observer;
    private Observer<List<PromoEntity>> inactiveObserver;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PromoListFragment() {
    }

    public static PromoListFragment newInstance( ) {
        PromoListFragment fragment = new PromoListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    //from basic
    @Override
    public void reloadItems() {
        getActive();
        getInactive();
    }

    private void getActive(){
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.add(Calendar.HOUR, 2);

        if(activeLiveData !=null)
            activeLiveData.removeObservers(this);

        StringBuilder builder= new StringBuilder();
        builder.append("select * from table_promos where participation_id = 0 ");


        getBundle();
        String order= preferences.getString(getString(R.string.key_promo_filter_sort_string), " ORDER BY end_date ASC");



        if(stringSet != null && !stringSet.isEmpty()){
            String joined = TextUtils.join(",", stringSet);
            builder.append("AND bank_id IN (").append(joined).append(") ");
            isFilterActive=true;


            builder.append("AND " + "end_date" + " > ").append(calendar.getTimeInMillis());
            builder.append(order);
            activeLiveData = viewModel.getRawPromos(new SimpleSQLiteQuery(builder.toString()));

        } else {
            isFilterActive = false;
            builder.append("AND " + "end_date" + " > ").append(calendar.getTimeInMillis());
            builder.append(order);
            activeLiveData = viewModel.getRawPromos(new SimpleSQLiteQuery(builder.toString()));
        }

        if(activeLiveData !=null)
            activeLiveData.observe(this, observer);
    }

    private void getInactive(){
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.add(Calendar.HOUR, 2);

        String order= preferences.getString(getString(R.string.key_promo_filter_sort_string), " ORDER BY end_date DESC");


        if(inactiveLiveData !=null)
            inactiveLiveData.removeObservers(this);

        StringBuilder builder= new StringBuilder();
        builder.append("select * from table_promos where participation_id = 0 ");

        getBundle();

        if(stringSet != null && !stringSet.isEmpty()){
            String joined = TextUtils.join(",", stringSet);
            builder.append("AND bank_id IN (").append(joined).append(") ");
            isFilterActive=true;
            builder.append("AND " + "end_date" + " < ").append(calendar.getTimeInMillis());
            builder.append(order);
            inactiveLiveData = viewModel.getRawPromos(new SimpleSQLiteQuery(builder.toString()));

        } else {
            isFilterActive = false;

            builder.append("AND " + "end_date" + " < ").append(calendar.getTimeInMillis());
            builder.append(order);
            inactiveLiveData = viewModel.getRawPromos(new SimpleSQLiteQuery(builder.toString()));
        }

        if(inactiveLiveData !=null)
            inactiveLiveData.observe(this, inactiveObserver);
    }

    @Override
    protected void setObserver(){
        observer= promoEntities -> {
            if (promoEntities != null) {
                mBinding.setIsLoading(false);

                activeViewAdapter.setPromoList(promoEntities);
                setEmptyText(promoEntities.isEmpty());

            } else {
                mBinding.setIsLoading(true);
            }

            mBinding.setIsFilterActive(isFilterActive);
            mBinding.executePendingBindings();
        };

        inactiveObserver= promoEntities -> {
            if (promoEntities != null) {
                mBinding.setIsLoading(false);

                inactiveViewAdapter.setPromoList(promoEntities);

                setEmptyText(promoEntities.isEmpty());

                if(promoEntities.isEmpty())
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
        viewModel = ViewModelProviders.of(this).get(PromoListViewModel.class);
    }

    @Override
    protected void getBundle() {
        stringSet= preferences.getStringSet(getString(R.string.key_promo_filter_bank_ids), new HashSet<>());
    }

    @Override
    protected void clearFilter() {
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(getActivity());
        preferences.edit().putInt(getString(R.string.key_promo_filter_content), 0).apply();
        preferences.edit().putStringSet(getString(R.string.key_promo_filter_bank_ids), new HashSet<>()).apply();
    }

    @Override
    protected void setEmptyText(boolean value) {
        super.setEmptyText(value);
        if (isFilterActive)
            mBinding.emptyListText.setText(R.string.no_promos_filter_active);
        else
            mBinding.emptyListText.setText(R.string.promos_empty);
    }

    @Override
    protected String setButtonText() {
        return getString(R.string.promo_list_button_text);
    }

    @NotNull
    @Override
    public String toString() {
        return Constants.TAG_PROMO_LIST_FRAGMENT;
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
