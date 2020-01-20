package pl.rozbijbank.ui.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import pl.rozbijbank.R;
import pl.rozbijbank.databinding.FragmentLoginBinding;
import pl.rozbijbank.networking.ApiClient;
import pl.rozbijbank.networking.ApiSessionInterface;
import pl.rozbijbank.networking.RetrofitCallback;
import pl.rozbijbank.networking.pojo.session.EmailPojo;
import pl.rozbijbank.networking.pojo.session.ForgotPasswordPojo;
import pl.rozbijbank.networking.pojo.session.LoginPojo;
import pl.rozbijbank.utilities.Constants;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LoginFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends AuthFragment {

    private FragmentLoginBinding mBinding;
    private AlertDialog forgotPasswordDialog;
    private OnFragmentInteractionListener mListener;

    public LoginFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     **/
    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mBinding= DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false);

        setClicks();

        return mBinding.getRoot();
    }

    private void setClicks(){
        mBinding.controller.setOnClickListener(v -> mListener.orSignUp());

        mBinding.caption.setOnClickListener(v -> login());

        mBinding.focusHider.setOnClickListener(v -> login());

        mBinding.skip.setOnClickListener(v -> mListener.onFinish());

        mBinding.forgotPassword.setOnClickListener(v -> {
            String email= Objects.requireNonNull(mBinding.emailInputEdit.getText()).toString();
            if(validateEmail(email))
                sendCodeByEmail(email);
             else
                setEmailError(getString(R.string.error_invalid_email));
        });
    }

    private void login(){
        String email= Objects.requireNonNull(mBinding.emailInputEdit.getText()).toString();
        String password= Objects.requireNonNull(mBinding.passwordInputEdit.getText()).toString();

        if(validateEmail(email)){
            if(validatePassword(password))
                mListener.onLogin(new LoginPojo(email, password));
            else
                mBinding.passwordInputEdit.setError(getString(R.string.error_password_short));
        } else
            setEmailError(getString(R.string.error_invalid_email));
    }

    //FORGOT PASSWORD
    private void sendCodeByEmail(String email ){
        ApiSessionInterface apiSessionInterface = ApiClient.getClient().create(ApiSessionInterface.class);
        Call<Void> call= apiSessionInterface.forgotPasswordgetCode(new EmailPojo(email));
        call.enqueue(new RetrofitCallback<>(getActivity(), new Callback<Void>() {
            @Override
            public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                if(response.code()==200){
                    Toast.makeText(getActivity(), getString(R.string.toast_code_sent), Toast.LENGTH_SHORT).show();
                    showForgotPasswordDialog(email);

                }

            }

            @Override
            public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
            }
        }));
    }

    private void showForgotPasswordDialog(String email) {

        AlertDialog.Builder builder  = new AlertDialog.Builder(getActivity());

        View dialogView = View.inflate(getActivity(), R.layout.forgot_password_dialog, null);
        builder.setView(dialogView);

        TextInputEditText codeEdit= dialogView.findViewById(R.id.code_input);
        TextInputEditText newPassword= dialogView.findViewById(R.id.password_input_edit);
        TextInputEditText confirmPassword= dialogView.findViewById(R.id.confirm_password_input_edit);
        Button accept= dialogView.findViewById(R.id.btn_accept);
        Button decline= dialogView.findViewById(R.id.btn_decline);

        accept.setOnClickListener(v ->{
            String passedCode= Objects.requireNonNull(codeEdit.getText()).toString();
            String newPass= Objects.requireNonNull(newPassword.getText()).toString();
            String confirmPass= Objects.requireNonNull(confirmPassword.getText()).toString();

            if(!validatePassword(newPass)){
                newPassword.setError(getString(R.string.error_password_short));
                return;
            }

            if(newPass.equals(confirmPass))
                forgotPasswordToApi(email,passedCode, newPass, codeEdit);
            else
                confirmPassword.setError(getString(R.string.error_password_not_the_same));


        });

        decline.setOnClickListener(v -> forgotPasswordDialog.dismiss());

        forgotPasswordDialog= builder.create();
        forgotPasswordDialog.show();
    }

    private void forgotPasswordToApi(String email, String code, String newPass , TextInputEditText codeEdit){
        ApiSessionInterface apiSessionInterface = ApiClient.getClient().create(ApiSessionInterface.class);

        Call<Void> call= apiSessionInterface.forgotPassword(new ForgotPasswordPojo(email, code, newPass));
        call.enqueue(new RetrofitCallback<>(getActivity(), new Callback<Void>() {
            @Override
            public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                if(response.code()==200){
                    Toast.makeText(getActivity(), getString(R.string.password_change_success), Toast.LENGTH_SHORT).show();
                    forgotPasswordDialog.dismiss();
                }else
                    codeEdit.setError(getString(R.string.error_wrong_code));
            }

            @Override
            public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
            }
        }));
    }


    //ACTIVITY USES THAT
    public void setEmailError(String error){
        mBinding.emailInputEdit.setError(error);
    }

    //LISTENER
    public interface OnFragmentInteractionListener {
        void orSignUp();
        void onLogin(LoginPojo loginPojo);
        void onFinish();
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
        return Constants.TAG_LOGIN_FRAGMENT;
    }
}
