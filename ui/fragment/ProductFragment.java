package pl.rozbijbank.ui.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import pl.rozbijbank.R;
import pl.rozbijbank.callback.PickBankCallback;
import pl.rozbijbank.databinding.ProductFragmentBinding;
import pl.rozbijbank.db.entity.ProductEntity;
import pl.rozbijbank.networking.ApiClient;
import pl.rozbijbank.networking.ApiInterface;
import pl.rozbijbank.networking.RetrofitCallback;
import pl.rozbijbank.networking.pojo.IdPojo;
import pl.rozbijbank.networking.pojo.ProductPojo;
import pl.rozbijbank.other.MyApplication;
import pl.rozbijbank.ui.activity.SecondActivity;
import pl.rozbijbank.ui.dialog.PickBankDialog;
import pl.rozbijbank.utilities.Constants;
import pl.rozbijbank.viewModel.ProductViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link ProductFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProductFragment extends BasicFragment {

    private ProductFragmentBinding mBinding;
    private ProductViewModel viewModel;
    private ProductEntity activeProduct;
    private int generalProductType;
    private boolean modify;
    private ApiInterface apiInterface;


    public ProductFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     **/
    public static ProductFragment newInstance(int productType, long bankId, long productId) {
        ProductFragment fragment = new ProductFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.KEY_PRODUCT_TYPE, productType);
        args.putLong(Constants.KEY_BANK_ID, bankId);
        args.putLong(Constants.KEY_PRODUCT_ID, productId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences= PreferenceManager.getDefaultSharedPreferences(getActivity());
        viewModel = ViewModelProviders.of(this).get(ProductViewModel.class);
        apiInterface= ApiClient.getClient().create(ApiInterface.class);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding= DataBindingUtil.inflate(inflater, R.layout.product_fragment, container, false);



        if(getArguments()!=null){
            activeProduct= new ProductEntity();
            generalProductType = getArguments().getInt(Constants.KEY_PRODUCT_TYPE);
            activeProduct.setBankId(getArguments().getLong(Constants.KEY_BANK_ID, 0));

            long productId= getArguments().getLong(Constants.KEY_PRODUCT_ID, 0);
            if(productId!=0)
                getProduct(productId);
            else
                setNoProductVisual();

        }

        setClicks();

        return mBinding.getRoot();
    }

    private void getProduct(long productId){
        modify=true;
        viewModel.getProduct(productId).observe(this, productEntity -> {
            if(productEntity!=null){
                activeProduct= productEntity;

                setProductVisuals();
            }

        });
    }

    private void setProductVisuals( ){
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("UTC"));

        mBinding.setProduct(activeProduct);

        mBinding.closedProduct.setChecked(activeProduct.isInactive());

        if(activeProduct.getBankId()>0) {
            int id = MyApplication.getBankIconId(activeProduct.getBankId());
            if (id != 0)
                Picasso.get().load(id).into(mBinding.pickColor);
        }

        mBinding.productTypeSpinner.setSelection(activeProduct.getProductType()%10);
        mBinding.closedProduct.setChecked(activeProduct.isInactive());

        if (activeProduct.getStartDate()!=null)
            mBinding.productStartDate.setText(dateFormatGmt.format(activeProduct.getStartDate()));
        if (activeProduct.getEndDate()!=null)
            mBinding.productEndDate.setText(dateFormatGmt.format(activeProduct.getEndDate()));

    }

    private void setNoProductVisual(){
        mBinding.btnDelete.setVisibility(View.GONE);
    }

    private void setClicks(){
        mBinding.productStartDate.setOnClickListener(v -> showDatePicker(false));
        mBinding.productEndDate.setOnClickListener(v -> showDatePicker(true));

        mBinding.btnCancel.setOnClickListener(v ->
                Objects.requireNonNull(getActivity()).finish());
        mBinding.btnSave.setOnClickListener(v -> onSave());

        mBinding.btnDelete.setOnClickListener(v -> showDeleteDialog());

        mBinding.pickColor.setOnClickListener(v -> new PickBankDialog((SecondActivity) getActivity(), pickBankCallback).showDialog());

        mBinding.closedProduct.setOnCheckedChangeListener((buttonView, isChecked) -> {
            activeProduct.setInactive(isChecked);
            if (isChecked)
                mBinding.productEndDateLayout.setVisibility(View.VISIBLE);
            else{
                mBinding.productEndDateLayout.setVisibility(View.GONE);
                activeProduct.setEndDate(null);
            }   

        });

        setProductTypeSpinner();
    }

    private PickBankCallback pickBankCallback= bank -> {
        activeProduct.setBankId(bank.getId());
        int id = MyApplication.getBankIconId(bank.getId());
        if (id != 0){
            mBinding.pickColor.setBackgroundColor(getResources().getColor(R.color.white));
            Picasso.get().load(id).into(mBinding.pickColor);
        }
    };

    private void showDatePicker(boolean endDateMode){
        Calendar calendar= Calendar.getInstance(TimeZone.getTimeZone("UTC"));


        DatePickerDialog.OnDateSetListener dateSetListener = (view1, year, month, day) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            Date date = calendar.getTime();

            setDates(endDateMode, date);


        };


        new DatePickerDialog(Objects.requireNonNull(getActivity()), dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();

    }

    //this sh** with errors when enddate<startdate
    private void setDates(boolean endDateMode, Date date){
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("UTC"));

        if (endDateMode){
            if(activeProduct.getStartDate()!=null)
                if (date.before(activeProduct.getStartDate())){
                    mBinding.productEndDateLayout.setError(getString(R.string.error_open_date_after_close_date));
                    date=null;
                }

            activeProduct.setEndDate(date);
            if(date!=null){
                mBinding.productEndDateLayout.setError(null);
                mBinding.productEndDate.setText(dateFormatGmt.format(date));
            }
        } else{
            if(activeProduct.getEndDate()!=null)
                if (date.after(activeProduct.getEndDate())){
                    mBinding.productStartDateLayout.setError(getString(R.string.error_open_date_after_close_date));
                    date=null;
                }


            activeProduct.setStartDate(date);
            if(date!=null){
                mBinding.productStartDateLayout.setError(null);
                mBinding.productStartDate.setText(dateFormatGmt.format(date));
            }
        }
    }

    private void setProductTypeSpinner(){
        Spinner spinner= mBinding.productTypeSpinner;

        @ArrayRes int arrayResId;
        switch (generalProductType){
            case 1:
                arrayResId= R.array.AccountTypes;
                break;
            case 2:
                arrayResId= R.array.CardTypes;
                break;
            case 3:
                arrayResId= R.array.DepositTypes;
                break;
            default:
                arrayResId= 0;

        }
        ArrayAdapter<CharSequence> adapterDate= ArrayAdapter.
                createFromResource(Objects.requireNonNull(getActivity()), arrayResId, android.R.layout.simple_spinner_item);
        adapterDate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapterDate);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[]  array= getResources().getStringArray(arrayResId);

                activeProduct.setProductType(generalProductType * 10 + position);
                if (canRewriteName(array)){
                    mBinding.productTitle.setText(array[position]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private boolean canRewriteName(String [] array){
        return (mBinding.productTitle.getText()!=null
                && mBinding.productTitle.getText().toString().equals(""))
                || titleFromArray(array);
    }

    private boolean titleFromArray(String [] array){
        for (String s : array) {
            if (s.equals(Objects.requireNonNull(mBinding.productTitle.getText()).toString()))
                return true;
        }
        return false;
    }

    private void onSave(){

        //CHECK
        if(mBinding.productTitle.getText()==null || mBinding.productTitle.getText().toString().equals("")){
            mBinding.productTitle.setError(getString(R.string.enter_valid_name));
            return;
        }

        if(activeProduct.isInactive())
            if (activeProduct.getEndDate()==null){
                mBinding.productEndDate.setError(getString(R.string.pick_date));
                return;
            }

        if(activeProduct.getBankId()==0){
            mBinding.pickColor.setBackgroundColor(getResources().getColor(R.color.red));
            return;
        }

        //SET
        activeProduct.setTitle(mBinding.productTitle.getText().toString());

        if(mBinding.productDesc.getText()!=null)
            activeProduct.setDescription(mBinding.productDesc.getText().toString());


        if(modify)
            updateProductApi();
        else
           createProductApi();

    }

    //CREATE
    private void createProductApi(){
        if (Objects.equals(preferences.getString(getString(R.string.key_active_user_token), ""), "")){
            createProductRoom();
        }else{
            Call<IdPojo> call= apiInterface.createProduct(new ProductPojo(activeProduct));
            call.enqueue(new RetrofitCallback<>(getActivity(), new Callback<IdPojo>() {
                @Override
                public void onResponse(@NotNull Call<IdPojo> call, @NotNull Response<IdPojo> response) {
                    if(response.code()==201){
                        if (response.body()!=null){
                            activeProduct.setId(response.body().getId());
                            createProductRoom();
                        }

                    }
                }

                @Override
                public void onFailure(@NotNull Call<IdPojo> call, @NotNull Throwable t) {
                }
            }));
        }
    }

    private void createProductRoom(){
        viewModel.insert(activeProduct);
        Objects.requireNonNull(getActivity()).finish();
    }

    //UPDATE
    private void updateProductApi(){
        if (Objects.equals(preferences.getString(getString(R.string.key_active_user_token), ""), "")){
            updateProductRoom();
        }else{
            Call<Void> call= apiInterface.updateProduct(activeProduct.getId(), new ProductPojo(activeProduct));
            call.enqueue(new RetrofitCallback<>(getActivity(), new Callback<Void>() {
                @Override
                public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                    if(response.code()==200)
                        updateProductRoom();
                    else if(response.code()==400)
                        if(getActivity()!=null)
                            getActivity().finish();
                }

                @Override
                public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
                }
            }));
        }
    }

    private void updateProductRoom(){
        viewModel.update(activeProduct);
        Objects.requireNonNull(getActivity()).finish();
    }

    //DELETE
    private void showDeleteDialog(){
        new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                .setTitle(getString(R.string.warning_title_delete_product))
                .setPositiveButton(android.R.string.yes, (dialog, which) ->
                        deleteProductApi())

                .setNegativeButton(android.R.string.no, null)
                .show();
    }


    private void deleteProductApi(){
        if (Objects.equals(preferences.getString(getString(R.string.key_active_user_token), ""), "")){
            deleteProductRoom();
        }else{
            Call<Void> call= apiInterface.deleteProduct(activeProduct.getId());
            call.enqueue(new RetrofitCallback<>(getActivity(), new Callback<Void>() {
                @Override
                public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                    if(response.code()==200)
                        deleteProductRoom();

                    Toast.makeText(getActivity(), String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
                }
            }));
        }
    }

    private void deleteProductRoom(){
        viewModel.delete(activeProduct);
        Objects.requireNonNull(getActivity()).finish();
    }

    @NotNull
    @Override
    public String toString() {
        return Constants.TAG_PRODUCT_FRAGMENT;
    }

}
