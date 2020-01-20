package pl.rozbijbank.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;
import butterknife.BindView;
import butterknife.ButterKnife;
import pl.rozbijbank.R;
import pl.rozbijbank.utilities.Constants;

/**
 * A simple {@link androidx.fragment.app.Fragment} subclass. Shows when a fragment is being shown for the first time
 *
 * Activities that contain this fragment must implement the
 * {@link HelloFragment} interface
 * to handle interaction events.
 *
 * Use the {@link HelloFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HelloFragment extends BasicFragment {

    @BindView(R.id.section_label) TextView mLabel;
    @BindView(R.id.section_description) TextView mDescription;
    @BindView(R.id.background) RelativeLayout background;

    private String mTag;

    public HelloFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     *
     * @return A new instance of fragment HelloFragment.
     */
    public static HelloFragment newInstance() {
        HelloFragment fragment = new HelloFragment();
        Bundle bundle= new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            mTag = getArguments().getString(Constants.KEY_FRAGMENT_TAG);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView= inflater.inflate(R.layout.fragment_hello, container, false);
        ButterKnife.bind(this, rootView);

        background.setOnClickListener(view -> {
            if(getActivity()!=null)
                getActivity().getSupportFragmentManager().popBackStack();
        });

        setFragmentSpecifics("TITLE", "DESC");


        return rootView;
    }

    private void getFragmentSpecifics(){
        if (getArguments() != null) {

/*
            switch (mTag){
                case ConstantsTags.TAG_DEBT_LIST:// DEBT
                    setFragmentSpecifics(getString(R.string.nav_debt_list),  getString(R.string.hello_debt_list_description));
                    break;
                case ConstantsTags.TAG_NEW_TRANSACTION:// NT
                    setFragmentSpecifics(getString(R.string.hello_new_transaction_title),  getString(R.string.hello_new_transaction_description));
                    break;
                case ConstantsTags.TAG_BANK_LIST:// BANK LIST
                    setFragmentSpecifics(getString(R.string.hello_bank_list_title),  getString(R.string.hello_bank_list_description));
                    break;
                    //only one page
                case ConstantsTags.TAG_HOME:// HOME
                    setFragmentSpecifics(getString(R.string.nav_home),
                            getString(R.string.hello_home_description));
                    break;
                case ConstantsTags.TAG_BANK_DETAILS:// HOME
                    setFragmentSpecifics(getString(R.string.hello_bank_details_title),  getString(R.string.hello_bank_details_description));
                    break;
                case ConstantsTags.TAG_PLANNED_LIST:// HOME
                    setFragmentSpecifics(getString(R.string.nav_notifications),  getString(R.string.hello_planned_description));
                    break;
                case ConstantsTags.TAG_CATEGORY_LIST:// HOME
                    setFragmentSpecifics(getString(R.string.nav_category_list),  getString(R.string.hello_category_list_description));
                    break;
                case ConstantsTags.TAG_TRANSACTION_LIST:// HOME
                    setFragmentSpecifics(getString(R.string.nav_transaction_list),  getString(R.string.hello_transaction_list_description));
                    break;
            }
*/
        }
    }

    private void setFragmentSpecifics(String title , String description){
        mLabel.setText(title);
        mDescription.setText(description);
    }

    @NotNull
    @Override
    public String toString() {
        return Constants.TAG_HELLO_FRAGMENT;
    }

}
