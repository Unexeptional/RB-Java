package pl.rozbijbank.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
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
import pl.rozbijbank.callback.ProductClickCallback;
import pl.rozbijbank.databinding.ProductItemBinding;
import pl.rozbijbank.db.entity.ProductEntity;
import pl.rozbijbank.db.model.Product;
import pl.rozbijbank.other.MyApplication;

/**
 * {@link RecyclerView.Adapter} that can display a {@link ProductEntity} and makes a call to the
 * specified {@link ProductClickCallback}.
 */
public class ProductViewAdapter extends RecyclerView.Adapter<ProductViewAdapter.ProductViewHolder> {

    private List<? extends Product> mProductList;

    @Nullable
    private final ProductClickCallback mProductClickCallback;

    public ProductViewAdapter(@Nullable ProductClickCallback clickCallback) {
        mProductClickCallback = clickCallback;
        setHasStableIds(true);
    }

    public void setProductList(final List<? extends Product> productList) {
        if (mProductList == null) {
            mProductList = productList;
            notifyItemRangeInserted(0, productList.size());
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mProductList.size();
                }

                @Override
                public int getNewListSize() {
                    return productList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mProductList.get(oldItemPosition).getId() ==
                            productList.get(newItemPosition).getId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Product newProduct = productList.get(newItemPosition);
                    Product oldProduct = mProductList.get(oldItemPosition);
                    return newProduct.getId() == oldProduct.getId()
                            && Objects.equals(newProduct.getDescription(), oldProduct.getDescription())
                            && Objects.equals(newProduct.getBankId(), oldProduct.getBankId())
                            && Objects.equals(newProduct.getStartDate(), oldProduct.getStartDate())
                            && Objects.equals(newProduct.getEndDate(), oldProduct.getEndDate())
                            && Objects.equals(newProduct.getProductType(), oldProduct.getProductType())
                            && Objects.equals(newProduct.isInactive(), oldProduct.isInactive())
                            && Objects.equals(newProduct.getTitle(), oldProduct.getTitle());
                }
            });
            mProductList = productList;
            result.dispatchUpdatesTo(this);
        }
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ProductItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.product_item,
                        parent, false);
        binding.setCallback(mProductClickCallback);
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {

        Product product= mProductList.get(position);
        holder.binding.setProduct(product);
        holder.binding.executePendingBindings();

        setDates(product, holder.binding);

        int id= MyApplication.getBankIconId(mProductList.get(position).getBankId());
        if(id!=0)
            Picasso.get().load(id).into(holder.binding.bankItemIcon);

        int id2= getTypeIcon(product.getProductType());
        if(id2!=0)
            Picasso.get().load(id2).into(holder.binding.typeIcon);
    }

    private void setDates(Product product, ProductItemBinding binding){
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("UTC"));

        if(product.getStartDate()!=null)
            binding.prodStartDate.setText(dateFormatGmt.format(product.getStartDate()));

        if(product.getEndDate()!=null)
            binding.prodEndDate.setText(dateFormatGmt.format(product.getEndDate()));

    }

    private static int getTypeIcon(int productType){
        String iconName= "settings_" + String.valueOf(productType);

        if (iconName.length() > 0 && iconName.charAt(iconName.length() - 1) == 'x') {
            iconName = iconName.substring(0, iconName.length() - 1);
        }
        return MyApplication.getDrawableId(iconName);

    }

    @Override
    public int getItemCount() {
        return mProductList == null ? 0 : mProductList.size();
    }

    @Override
    public long getItemId(int position) {
        return mProductList.get(position).getId();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {

        final ProductItemBinding binding;

        ProductViewHolder(ProductItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
