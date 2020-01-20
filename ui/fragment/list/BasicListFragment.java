package pl.rozbijbank.ui.fragment.list;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import pl.rozbijbank.R;
import pl.rozbijbank.databinding.BasicListBinding;
import pl.rozbijbank.ui.fragment.BasicFragment;

/**
 * A abstract fragment representing a list of Items.
 * When in portrait orientation - linear layout manager.
 * When in landscape orientation - has a grid layout with 3 columns
 * <p/>
 */
abstract class BasicListFragment extends BasicFragment {

    BasicListBinding mBinding;
    boolean isFilterActive;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BasicListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setViewModel();
        getBundle();
        reloadItems();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setClicks();
        setEmptyText();
        mBinding.swipeContainer.setOnRefreshListener(() ->
                new Handler().postDelayed(() ->
                mBinding.swipeContainer.setRefreshing(false), 2000));
    }

    private void setClicks(){
        mBinding.clearFilter.setOnClickListener(v -> {
            clearFilter();
            reloadItems();
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding= DataBindingUtil.inflate(inflater, R.layout.basic_list,container, false);
        setLayoutManager();
        setAdapter();
        return mBinding.getRoot();
    }

    private void setLayoutManager(){
        int orientation = this.getResources().getConfiguration().orientation;
        int mColumnCount;
        if (orientation == Configuration.ORIENTATION_PORTRAIT)
            mColumnCount = 0;
        else
            mColumnCount =3;


        if (mColumnCount <= 1)
            mBinding.activeList.setLayoutManager(new LinearLayoutManager(getActivity()));
        else
            mBinding.activeList.setLayoutManager(new GridLayoutManager(getActivity(), mColumnCount));

    }


    //kids follow that
    public abstract void reloadItems();
    protected abstract void setAdapter();
    protected abstract void setViewModel();
    protected abstract void getBundle();
    protected abstract void clearFilter();
    protected abstract void setEmptyText();

}
