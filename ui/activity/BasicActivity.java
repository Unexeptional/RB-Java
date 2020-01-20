package pl.rozbijbank.ui.activity;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import pl.rozbijbank.R;

//Does provide same layout for everyone
public abstract class BasicActivity extends AbstractActivity  {

    protected ViewDataBinding dataBinding;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBinding= DataBindingUtil.setContentView(this, provideActivityLayout());

        //StatusColorControl
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }
    }

    protected abstract @LayoutRes
    int provideActivityLayout();
}
