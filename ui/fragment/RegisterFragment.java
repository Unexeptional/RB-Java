package pl.rozbijbank.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import pl.rozbijbank.R;
import pl.rozbijbank.databinding.FragmentRegisterBinding;
import pl.rozbijbank.networking.pojo.session.RegisterPojo;
import pl.rozbijbank.utilities.Constants;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RegisterFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RegisterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisterFragment extends AuthFragment {

    private FragmentRegisterBinding mBinding;

    private OnFragmentInteractionListener mListener;

    public RegisterFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     **/
    public static RegisterFragment newInstance() {
        RegisterFragment fragment = new RegisterFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mBinding= DataBindingUtil.inflate(inflater, R.layout.fragment_register, container, false);
        setClicks();

        return mBinding.getRoot();
    }

    private void setClicks(){
        mBinding.controller.setOnClickListener(v -> mListener.orSignIn());

        mBinding.caption.setOnClickListener(v -> register());

        mBinding.focusHider.setOnClickListener(v -> register());

        mBinding.skip.setOnClickListener(v -> mListener.onFinish());
    }

    private void register(){
        String email= Objects.requireNonNull(mBinding.emailInputEditRegister.getText()).toString();
        String password= Objects.requireNonNull(mBinding.passwordInputEditRegister.getText()).toString();
        String confirm= Objects.requireNonNull(mBinding.confirmPasswordInputEdit.getText()).toString();
        String recCode= Objects.requireNonNull(mBinding.recommendationCodeInput.getText()).toString();

        if(validateEmail(email)){
            if(validatePassword(password, confirm))
                mListener.onRegister(new RegisterPojo(email, password, recCode));
        } else
            setEmailError(getString(R.string.error_invalid_email));
    }

    public void setEmailError(String error){
        mBinding.emailInputEditRegister.setError(error);
    }

    public void setCodeError(String error){
        mBinding.recommendationCodeInput.setError(error);
    }



    private boolean validatePassword(String password, String confirm){
        if(validatePassword(password))
            if (validatePassword(confirm))
                if (password.equals(confirm))
                    return true;
                else
                    mBinding.confirmPasswordInputEdit.setError(getString(R.string.error_password_not_the_same));
            else
                mBinding.confirmPasswordInputEdit.setError(getString(R.string.error_password_not_the_same));
        else
            mBinding.passwordInputEditRegister.setError(getString(R.string.error_password_short));

        return false;
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
        return Constants.TAG_REGISTER_FRAGMENT;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {
        void orSignIn();
        void onRegister(RegisterPojo registerPojo);
        void onFinish();
    }
}
