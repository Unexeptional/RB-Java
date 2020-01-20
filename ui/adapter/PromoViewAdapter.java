package pl.rozbijbank.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import pl.rozbijbank.R;
import pl.rozbijbank.callback.PromoClickCallback;
import pl.rozbijbank.databinding.PromoItemBinding;
import pl.rozbijbank.db.entity.PromoEntity;
import pl.rozbijbank.db.model.Promo;
import pl.rozbijbank.other.MyApplication;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PromoEntity} and makes a call to the
 * specified {@link PromoClickCallback}.
 */
public class PromoViewAdapter extends RecyclerView.Adapter<PromoViewAdapter.PromoViewHolder> {

    private List<? extends Promo> mPromoList;

    @Nullable
    private final PromoClickCallback mPromoClickCallback;

    public PromoViewAdapter(@Nullable PromoClickCallback clickCallback) {
        mPromoClickCallback = clickCallback;
        setHasStableIds(true);
    }

    public void setPromoList(final List<? extends Promo> promoList) {
        if (mPromoList == null) {
            mPromoList = promoList;
            notifyItemRangeInserted(0, promoList.size());
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mPromoList.size();
                }

                @Override
                public int getNewListSize() {
                    return promoList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mPromoList.get(oldItemPosition).getId() ==
                            promoList.get(newItemPosition).getId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Promo newPromo = promoList.get(newItemPosition);
                    Promo oldPromo = mPromoList.get(oldItemPosition);

                    //only stuff that is on card
                    return newPromo.getId() == oldPromo.getId()
                            && Objects.equals(newPromo.getDescription(), oldPromo.getDescription())
                            && Objects.equals(newPromo.getWarning(), oldPromo.getWarning())
                            && Objects.equals(newPromo.isCompleted(), oldPromo.isCompleted())
                            && Objects.equals(newPromo.getEndDate(), oldPromo.getEndDate())
                            && Objects.equals(newPromo.getPoints(), oldPromo.getPoints())
                            && Objects.equals(newPromo.getParticipationId(), oldPromo.getParticipationId())
                            && Objects.equals(newPromo.getTitle(), oldPromo.getTitle());
                }
            });
            mPromoList = promoList;
            result.dispatchUpdatesTo(this);
        }
    }

    @NonNull
    @Override
    public PromoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PromoItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.promo_item,
                        parent, false);
        binding.setCallback(mPromoClickCallback);
        return new PromoViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PromoViewHolder holder, int position) {
        Promo promo= mPromoList.get(position);
        holder.binding.setPromo(promo);
        holder.binding.executePendingBindings();



        int id= MyApplication.getBankIconId(promo.getBankId());
        if(id!=0)
            Picasso.get().load(id).into(holder.binding.cardPromoBankIcon);


        setDate(promo, holder.binding);
    }

    private void setDate(Promo promo, PromoItemBinding binding){

        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date promoDate= promo.getEndDate();

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.add(Calendar.HOUR, 2);
        Date today= calendar.getTime();


        if(today.after(promoDate))
            binding.cardPromoEndDate.
                    setText(String.format("%s: %s",
                            MyApplication.getContext().getResources().getString(R.string.finished),
                            dateFormatGmt.format(promo.getEndDate())));

        else if(promoDate.getTime() - today.getTime() < 86400000)
            binding.cardPromoEndDate.setText(R.string.one_day_left);

        else if(promoDate.getTime() - today.getTime() < 172800000)
            binding.cardPromoEndDate.setText(R.string.two_days_left);

        else if(promoDate.getTime() - today.getTime() < 259200000)
            binding.cardPromoEndDate.setText(R.string.three_days_left);

        else if(promoDate.getTime() - today.getTime() < 604800000)
            binding.cardPromoEndDate.setText(R.string.week_left);
        else
            binding.cardPromoEndDate.
                    setText(String.format("%s  %s",
                            MyApplication.getContext().getResources().getString(R.string.end_date),
                            dateFormatGmt.format(promo.getEndDate())));

        if(today.after(promoDate))
            binding.cardPromoEndDate.setTextColor(Color.RED);
        else
            binding.cardPromoEndDate.setTextColor(Color.parseColor("#00CC00"));
    }

    @Override
    public int getItemCount() {
        return mPromoList == null ? 0 : mPromoList.size();
    }

    @Override
    public long getItemId(int position) {
        return mPromoList.get(position).getId();
    }

    static class PromoViewHolder extends RecyclerView.ViewHolder {

        final PromoItemBinding binding;

        PromoViewHolder(PromoItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
