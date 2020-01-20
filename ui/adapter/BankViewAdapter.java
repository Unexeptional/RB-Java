package pl.rozbijbank.ui.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import pl.rozbijbank.R;
import pl.rozbijbank.callback.BankClickCallback;
import pl.rozbijbank.databinding.BankItemBinding;
import pl.rozbijbank.db.entity.BankEntity;
import pl.rozbijbank.db.model.Bank;
import pl.rozbijbank.other.MyApplication;

import java.util.List;
import java.util.Objects;

/**
 * {@link RecyclerView.Adapter} that can display a {@link BankEntity} and makes a call to the
 * specified {@link BankClickCallback}.
 */
public class BankViewAdapter extends RecyclerView.Adapter<BankViewAdapter.BankViewHolder> {

    private List<? extends Bank> mBankList;

    @Nullable
    private final BankClickCallback mBankClickCallback;

    public BankViewAdapter(@Nullable BankClickCallback clickCallback) {
        mBankClickCallback = clickCallback;
        setHasStableIds(true);
    }

    public void setBankList(final List<? extends Bank> bankList) {
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
                            && Objects.equals(newBank.isActive(), oldBank.isActive()
                            && Objects.equals(newBank.getTitle(), oldBank.getTitle()));
                }
            });
            mBankList = bankList;
            result.dispatchUpdatesTo(this);
        }
    }

    @NonNull
    @Override
    public BankViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        BankItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.bank_item,
                        parent, false);
        binding.setCallback(mBankClickCallback);
        return new BankViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BankViewHolder holder, int position) {
        holder.binding.setBank(mBankList.get(position));
        holder.binding.executePendingBindings();

        int id= MyApplication.getBankIconId(mBankList.get(position).getId());
        if(id!=0)
            Picasso.get().load(id).into(holder.binding.bankItemIcon);
    }


    @Override
    public int getItemCount() {
        return mBankList == null ? 0 : mBankList.size();
    }

    @Override
    public long getItemId(int position) {
        return mBankList.get(position).getId();
    }

    static class BankViewHolder extends RecyclerView.ViewHolder {

        final BankItemBinding binding;

        BankViewHolder(BankItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
