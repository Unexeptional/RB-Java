package pl.rozbijbank.ui.fragment;


import org.apache.commons.validator.routines.EmailValidator;

abstract class AuthFragment extends BasicFragment {

    boolean validateEmail(String email){
        return EmailValidator.getInstance().isValid(email);
    }

    boolean validatePassword(String password){
        return password!=null && !password.equals("") && password.length()>5;
    }

}
