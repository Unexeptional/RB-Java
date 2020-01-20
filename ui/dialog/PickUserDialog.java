package pl.rozbijbank.ui.dialog;

import android.content.Intent;
import android.view.View;
import android.widget.Button;

import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import pl.rozbijbank.R;
import pl.rozbijbank.callback.PickUserCallback;
import pl.rozbijbank.db.model.User;
import pl.rozbijbank.ui.activity.LoginActivity;
import pl.rozbijbank.viewModel.UserListViewModel;

public class PickUserDialog {

    private AppCompatActivity activity;
    private AlertDialog dialog;
    private PickUserCallback callback;

    public PickUserDialog(AppCompatActivity activity, PickUserCallback callback) {
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
        PickUserViewAdapter adapter= new PickUserViewAdapter(callback);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(activity, 3));

        selectNOne.setText(activity.getString(R.string.new_user));
        selectNOne.setOnClickListener(v -> activity.startActivity(new Intent(activity, LoginActivity.class)));
        //get items
        UserListViewModel viewModel = ViewModelProviders.of(activity).get(UserListViewModel.class);

        viewModel.getUsers().observe(activity, users -> {
            if (users != null) {
                adapter.setUserList(users);
            }
        });

        dialog = builder.create();
        dialog.show();
    }

    public void hideDialog(){
        dialog.dismiss();
    }

    public class PickUserViewAdapter extends BasicPickAdapter  {

        private List<? extends User> mUserList;

        @Nullable
        private final PickUserCallback mPickUserCallback;

        PickUserViewAdapter(@Nullable PickUserCallback clickCallback) {
            mPickUserCallback = clickCallback;
            setHasStableIds(true);
        }

        void setUserList(final List<? extends User> userList) {
            if (mUserList == null) {
                mUserList = userList;
                notifyItemRangeInserted(0, userList.size());
            } else {
                DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                    @Override
                    public int getOldListSize() {
                        return mUserList.size();
                    }

                    @Override
                    public int getNewListSize() {
                        return userList.size();
                    }

                    @Override
                    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                        return mUserList.get(oldItemPosition).getId() ==
                                userList.get(newItemPosition).getId();
                    }

                    @Override
                    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                        User newUser = userList.get(newItemPosition);
                        User oldUser = mUserList.get(oldItemPosition);
                        return newUser.getId() == oldUser.getId()
                                && Objects.equals(newUser.getEmail(), oldUser.getEmail())
                                && Objects.equals(newUser.getToken(), oldUser.getToken())
                                && Objects.equals(newUser.getAvatar(), oldUser.getAvatar())
                                && Objects.equals(newUser.getPoints(), oldUser.getPoints());
                    }
                });
                mUserList = userList;
                result.dispatchUpdatesTo(this);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull IconViewHolder holder, int position) {
            holder.binding.executePendingBindings();
            String name= mUserList.get(position).getAvatar();

            int id= getDrawable(name);

            if(id!=0)
                Picasso.get().load(id).into(holder.binding.pickIcon);

            if(mPickUserCallback!=null)
                holder.binding.pickIcon.setOnClickListener(v -> {
                    mPickUserCallback.onUserItemPick(mUserList.get(position));
                    dialog.dismiss();
                });
        }


        @Override
        public int getItemCount() {
            return mUserList == null ? 0 : mUserList.size();
        }
    }
}