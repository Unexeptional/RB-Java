package pl.rozbijbank.ui.dialog;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import org.apache.commons.validator.routines.EmailValidator;

import java.util.Objects;

import androidx.appcompat.app.AlertDialog;
import pl.rozbijbank.R;

public class ChangeEmailDialog {

    private Activity activity;
    private AlertDialog dialog;
    private String code;

    public ChangeEmailDialog(Activity activity, String code) {
        this.activity = activity;
        this.code= code;
    }

    public void showDialog() {

        AlertDialog.Builder builder  = new AlertDialog.Builder(activity);

        //...that's the layout i told you will inflate later
        View dialogView = View.inflate(activity, R.layout.change_email_dialog, null);
        builder.setView(dialogView);

        TextInputEditText codeEdit= dialogView.findViewById(R.id.code_input);
        TextInputEditText newEmailEdit= dialogView.findViewById(R.id.new_email_input);
        TextInputEditText confirmEmailEdit= dialogView.findViewById(R.id.confirm_email_input);

        Button accept= dialogView.findViewById(R.id.btn_accept);
        Button decline= dialogView.findViewById(R.id.btn_decline);

        accept.setOnClickListener(v ->{
            String passedCode= Objects.requireNonNull(codeEdit.getText()).toString();
            String newEmail= Objects.requireNonNull(newEmailEdit.getText()).toString();
            String confEmail= Objects.requireNonNull(confirmEmailEdit.getText()).toString();

            if(!validateEmail(newEmail)){
                newEmailEdit.setError(activity.getString(R.string.error_invalid_email));
                return;
            }

            if(!passedCode.equals(code)){
                codeEdit.setError(activity.getString(R.string.error_wrong_code));
                return;
            }

            if(newEmail.equals(confEmail)){
                Toast.makeText(activity, "make a call", Toast.LENGTH_SHORT).show();
                //onresponse toast that changed and close dialog
            } else
                confirmEmailEdit.setError(activity.getString(R.string.error_password_not_the_same));


        });

        decline.setOnClickListener(v -> dialog.dismiss());


        //...finaly show it
        dialog= builder.create();
        dialog.show();
    }

    private boolean validateEmail(String email){
        return EmailValidator.getInstance().isValid(email);
    }


    public void hideDialog(){
        dialog.dismiss();
    }

}