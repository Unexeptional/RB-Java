package pl.rozbijbank.ui.dialog;

import android.view.View;
import android.widget.Button;

import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import pl.rozbijbank.R;
import pl.rozbijbank.callback.PickBankCallback;
import pl.rozbijbank.db.entity.BankEntity;
import pl.rozbijbank.db.model.Bank;
import pl.rozbijbank.viewModel.BankListViewModel;

public class PickBankDialog {

    private AppCompatActivity activity;
    private AlertDialog dialog;
    private PickBankCallback callback;

    public PickBankDialog(AppCompatActivity activity, PickBankCallback callback) {
        this.activity = activity;
        this.callback = callback;
    }

    public void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        View dialogView = View.inflate(activity, R.layout.dialog_pick_single, null);
        builder.setView(dialogView);

        //bind views
        RecyclerView recyclerView= dialogView.findViewById(R.id.pick_single);
        Button selectNOne= dialogView.findViewById(R.id.select_none);
        //setRecycler
        PickBankViewAdapter adapter= new PickBankViewAdapter(callback);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(activity, 3));

        selectNOne.setOnClickListener(v -> {
            callback.onBankItemPick(new BankEntity("", 0, false));
            dialog.dismiss();
        });

        //get items
        LiveData<List<BankEntity>> liveData=  ViewModelProviders.of(activity).get(BankListViewModel.class).getAllBanks();

        liveData.observe(activity, banks -> {
            if (banks != null) {
                liveData.removeObservers(activity);
                adapter.setBankList(banks);
            }
        });

        dialog = builder.create();
        dialog.show();
    }

    public void hideDialog(){
        dialog.dismiss();
    }

    private class PickBankViewAdapter extends BasicPickAdapter {

        private List<? extends Bank> mBankList;

        @Nullable
        private final PickBankCallback mPickBankCallback;

        PickBankViewAdapter(@Nullable PickBankCallback clickCallback) {
            mPickBankCallback = clickCallback;
            setHasStableIds(true);
        }

        void setBankList(final List<? extends Bank> bankList) {
            if (mBankList == null) {
                mBankList = bankList;
                notifyItemRangeInserted(0, bankList.size());
            } else {
                DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                    @Override
                    public int getOldListSize() {
                        return mBankList.size();
                    }

                    @Override
                    public int getNewListSize() {
                        return bankList.size();
                    }

                    @Override
                    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                        return mBankList.get(oldItemPosition).getId() ==
                                bankList.get(newItemPosition).getId();
                    }

                    @Override
                    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                        Bank newBank = bankList.get(newItemPosition);
                        Bank oldBank = mBankList.get(oldItemPosition);
                        return newBank.getId() == oldBank.getId()
                                && Objects.equals(newBank.getTitle(), oldBank.getTitle());
                    }
                });
                mBankList = bankList;
                result.dispatchUpdatesTo(this);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull IconViewHolder holder, int position) {
            holder.binding.executePendingBindings();
            String name= "bank_" + String.valueOf(mBankList.get(position).getId());

            int id= getDrawable(name);

            if(id!=0)
                Picasso.get().load(id).into(holder.binding.pickIcon);

            if(mPickBankCallback!=null) {
                holder.binding.pickIcon.setOnClickListener(v -> {
                    mPickBankCallback.onBankItemPick(mBankList.get(position));
                    dialog.dismiss();
                });
            }
        }


        @Override
        public int getItemCount() {
            return mBankList == null ? 0 : mBankList.size();
        }

        @Override
        public long getItemId(int position) {
            return mBankList.get(position).getId();
        }
    }
}