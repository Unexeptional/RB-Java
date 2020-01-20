package pl.rozbijbank.ui.dialog;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import pl.rozbijbank.R;
import pl.rozbijbank.databinding.PickIconItemBinding;
import pl.rozbijbank.other.MyApplication;

abstract class BasicPickAdapter extends RecyclerView.Adapter<IconViewHolder> {


    @NonNull
    @Override
    public IconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PickIconItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.pick_icon_item,
                        parent, false);
        return new IconViewHolder(binding);
    }

    protected int getDrawable(String iconName){
        if (iconName != null && iconName.length() > 0 && iconName.charAt(iconName.length() - 1) == 'x') {
            iconName = iconName.substring(0, iconName.length() - 1);
        }
        return MyApplication.getDrawableId(iconName);

    }
}


