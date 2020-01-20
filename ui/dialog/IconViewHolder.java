package pl.rozbijbank.ui.dialog;

import androidx.recyclerview.widget.RecyclerView;
import pl.rozbijbank.databinding.PickIconItemBinding;

class IconViewHolder extends RecyclerView.ViewHolder {

    final PickIconItemBinding binding;

    IconViewHolder(PickIconItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }
}

