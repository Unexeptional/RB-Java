package pl.rozbijbank.ui.dialog;

import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pl.rozbijbank.R;
import pl.rozbijbank.callback.CheckBankCallback;
import pl.rozbijbank.databinding.PickIconItemBinding;
import pl.rozbijbank.db.entity.BankEntity;
import pl.rozbijbank.db.model.Bank;
import pl.rozbijbank.viewModel.BankListViewModel;

public class CheckBankDialog {

    private AppCompatActivity activity;
    private AlertDialog dialog;
    private CheckBankCallback callback;

    public CheckBankDialog(AppCompatActivity activity, CheckBankCallback callback) {
        this.activity = activity;
        this.callback = callback;
    }

    public void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        List<CheckedItem> checkedItems= new ArrayList<>();

        View dialogView = View.inflate(activity, R.layout.dialog_check, null);
        builder.setView(dialogView);

        //bind views
        RecyclerView recyclerView= dialogView.findViewById(R.id.check_rv);
        Button selectNone= dialogView.findViewById(R.id.select_none);
        Button selectAll= dialogView.findViewById(R.id.select_all);
        Button ok= dialogView.findViewById(R.id.btn_ok);

        //setRecycler
        CheckBankViewAdapter adapter= new CheckBankViewAdapter(callback);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(activity, 3));


        ok.setOnClickListener(v -> dialog.dismiss());
        selectNone.setOnClickListener(v -> {
            for (CheckedItem checkedItem: checkedItems)
                checkedItem.setChecked(false);
            adapter.setItems(checkedItems);
            //aby wywołać bindviewholder
            recyclerView.setAdapter(adapter);
            callback.sendItems(checkedItems);
        });
        selectAll.setOnClickListener(v -> {
            for (CheckedItem checkedItem: checkedItems)
                checkedItem.setChecked(true);
            adapter.setItems(checkedItems);
            //aby wywołać bindviewholder
            recyclerView.setAdapter(adapter);
            callback.sendItems(checkedItems);
        });

        //get items
        LiveData<List<BankEntity>> liveData=  ViewModelProviders.of(activity).get(BankListViewModel.class).getAllBanks();

        liveData.observe(activity, banks -> {
            if (banks != null) {
                for (Bank bank: banks)
                    checkedItems.add(new CheckedItem(bank.getId()));

                Set<String> stringSet= new HashSet<>(checkedItems.size());
                stringSet= PreferenceManager.getDefaultSharedPreferences(activity).getStringSet(activity.getString(R.string.key_promo_filter_bank_ids), stringSet);
                assert stringSet != null;
                for (String string: stringSet){

                    for (CheckedItem checkedItem: checkedItems){
                        if (Long.parseLong(string)== checkedItem.getId())
                            checkedItem.setChecked(true);
                    }
                }

                liveData.removeObservers(activity);

                adapter.setItems(checkedItems);
            }
        });

        dialog = builder.create();
        dialog.show();
    }

    public void hideDialog(){
        dialog.dismiss();
    }

    public class CheckedItem{
        private long id;
        private boolean checked;

        CheckedItem(long id) {
            this.id = id;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public boolean isChecked() {
            return checked;
        }

        public void setChecked(boolean checked) {
            this.checked = checked;
        }
    }

    private class CheckBankViewAdapter extends BasicPickAdapter {

        private List<CheckedItem> mCheckedItems;

        @Nullable
        private final CheckBankCallback mCheckBankCallback;

        CheckBankViewAdapter(@Nullable CheckBankCallback clickCallback) {
            mCheckBankCallback = clickCallback;
            setHasStableIds(true);
        }

        void setItems(final List<CheckedItem> checkedItems) {
            if (mCheckedItems == null) {
                mCheckedItems = checkedItems;
                notifyItemRangeInserted(0, checkedItems.size());
            } else {
                DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                    @Override
                    public int getOldListSize() {
                        return mCheckedItems.size();
                    }

                    @Override
                    public int getNewListSize() {
                        return checkedItems.size();
                    }

                    @Override
                    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                        return mCheckedItems.get(oldItemPosition).getId() ==
                                checkedItems.get(newItemPosition).getId();
                    }

                    @Override
                    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                        CheckedItem oldItem = checkedItems.get(newItemPosition);
                        CheckedItem newItem = mCheckedItems.get(oldItemPosition);
                        return oldItem.getId() == newItem.getId()
                                && oldItem.isChecked() == newItem.isChecked();
                    }
                });
                mCheckedItems = checkedItems;
                result.dispatchUpdatesTo(this);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull IconViewHolder holder, int position) {
            CheckedItem checkedItem= mCheckedItems.get(position);
            holder.binding.executePendingBindings();

            String name= "bank_" + String.valueOf(checkedItem.getId());
            int id= getDrawable(name);

            if(id!=0)
                Picasso.get().load(id).into(holder.binding.pickIcon);

            setVisuals(checkedItem, holder.binding);

            if(mCheckBankCallback!=null)
                holder.binding.pickIcon.setOnClickListener(v ->{
                    if(checkedItem.isChecked())
                        checkedItem.setChecked(false);
                    else
                        checkedItem.setChecked(true);
                    setVisuals(checkedItem, holder.binding);
                    callback.sendItems(mCheckedItems);
                });

        }

        private void setVisuals(CheckedItem checkedItem, PickIconItemBinding binding){
            if (checkedItem.isChecked()){
                binding.itemCheckedBackground.setVisibility(View.VISIBLE);
                binding.itemCheckedForeground.setVisibility(View.VISIBLE);
            } else{
                binding.itemCheckedForeground.setVisibility(View.GONE);
                binding.itemCheckedBackground.setVisibility(View.GONE);
            }

        }


        @Override
        public int getItemCount() {
            return mCheckedItems == null ? 0 : mCheckedItems.size();
        }

        @Override
        public long getItemId(int position) {
            return mCheckedItems.get(position).getId();
        }
    }
}