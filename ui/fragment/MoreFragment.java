package pl.rozbijbank.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import pl.rozbijbank.R;
import pl.rozbijbank.databinding.FragmentMoreBinding;
import pl.rozbijbank.utilities.Constants;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MoreFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MoreFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MoreFragment extends BasicFragment {

    private FragmentMoreBinding mBinding;

    private OnFragmentInteractionListener mListener;

    public MoreFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     **/
    public static MoreFragment newInstance() {
        MoreFragment fragment = new MoreFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mBinding= DataBindingUtil.inflate(inflater, R.layout.fragment_more, container, false);

        mBinding.setCallback(mListener);

        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        String token= PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getString(R.string.key_active_user_token), "");

        if(token !=null && !token.isEmpty())
            mBinding.setIsAuthorized(true);
        else
            mBinding.setIsAuthorized(false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @NotNull
    @Override
    public String toString() {
        return Constants.TAG_MORE_FRAGMENT;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {
        void startSettingsFragment();
        void startLoginFragment();
        void startRegisterFragment();
        void startAccountFragment();
        void controlOfExpenditure();
        void startTutorialsFragment();
        void startRewardsFragment();
        void startAboutFragment();
        void rateThisApp();
        void sendFeedback();
        void exportDB();
        void importDB();
        void privacyPolicy();
        void getPromosData();
    }
}
