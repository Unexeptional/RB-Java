package pl.rozbijbank.ui.fragment.list;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import pl.rozbijbank.R;
import pl.rozbijbank.databinding.BasicListBinding;
import pl.rozbijbank.databinding.ComplexListBinding;
import pl.rozbijbank.ui.fragment.BasicFragment;

/**
 * A abstract fragment representing a list of Items.
 * When in portrait orientation - linear layout manager.
 * When in landscape orientation - has a grid layout with 3 columns
 * <p/>
 */
public abstract class ComplexListFragment extends BasicFragment {

    protected ComplexListBinding mBinding;
    boolean isFilterActive;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ComplexListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding= DataBindingUtil.inflate(inflater, R.layout.complex_list,container, false);
        setLayoutManager();
        setAdapters();
        return mBinding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setClicks();
        setVisuals();

        mBinding.inactiveButton.setText(setButtonText());
        mBinding.activeList.setNestedScrollingEnabled(false);
        mBinding.inactiveList.setNestedScrollingEnabled(false);
        mBinding.swipeContainer.setOnRefreshListener(() ->
                new Handler().postDelayed(() ->
                mBinding.swipeContainer.setRefreshing(false), 2000));

        setObserver();
        setViewModel();
        getBundle();
        reloadItems();
    }

    private void setClicks(){
        mBinding.clearFilter.setOnClickListener(v -> {
            clearFilter();
            reloadItems();
        });
        mBinding.inactiveButton.setOnClickListener(v -> {
            boolean value= mBinding.inactiveListWrapper.getVisibility() == View.VISIBLE;

            setFinishedButton(value);

            if (!value)
                mBinding.nestedScrollView.postDelayed(() -> {
                    mBinding.nestedScrollView.scrollBy(0,300);
                }, 500);

        });
    }

    private void setFinishedButton(boolean value){
        if(value){
            mBinding.inactiveListWrapper.setVisibility(View.GONE);
            mBinding.inactiveButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.baseline_expand_less_white_24,0);
        }else{
            mBinding.inactiveListWrapper.setVisibility(View.VISIBLE);
            mBinding.inactiveButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.baseline_expand_more_white_24,0);
        }

        preferences.edit().putBoolean(getString(R.string.key_hide_) + getClass().getName() , value).apply();
    }

    private void setVisuals(){
        setFinishedButton(preferences.getBoolean(getString(R.string.key_hide_) + getClass().getName(), false));
    }


    private void setLayoutManager(){
        int orientation = this.getResources().getConfiguration().orientation;
        int mColumnCount;
        if (orientation == Configuration.ORIENTATION_PORTRAIT)
            mColumnCount = 0;
        else
            mColumnCount =3;


        if (mColumnCount <= 1){
            mBinding.activeList.setLayoutManager(new LinearLayoutManager(getActivity()));
            mBinding.inactiveList.setLayoutManager(new LinearLayoutManager(getActivity()));
        }
        else{
            mBinding.activeList.setLayoutManager(new GridLayoutManager(getActivity(), mColumnCount));
            mBinding.inactiveList.setLayoutManager(new GridLayoutManager(getActivity(), mColumnCount));
        }

    }


    //kids follow that
    public abstract void reloadItems();
    protected abstract void setAdapters();
    protected abstract void setViewModel();
    protected abstract void setObserver();
    protected abstract void getBundle();
    protected abstract void clearFilter();
    protected void setEmptyText(boolean value){
        if (value)
            mBinding.emptyListText.setVisibility(View.VISIBLE);
        else
            mBinding.emptyListText.setVisibility(View.GONE);
    }
    protected abstract String setButtonText();

}
