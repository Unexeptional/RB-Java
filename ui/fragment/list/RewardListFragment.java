package pl.rozbijbank.ui.fragment.list;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import pl.rozbijbank.R;
import pl.rozbijbank.databinding.BasicListNewBinding;
import pl.rozbijbank.databinding.RewardItemBinding;
import pl.rozbijbank.networking.ApiClient;
import pl.rozbijbank.networking.ApiInterface;
import pl.rozbijbank.networking.RetrofitCallback;
import pl.rozbijbank.networking.pojo.OrderPojo;
import pl.rozbijbank.networking.pojo.RewardPojo;
import pl.rozbijbank.ui.fragment.BasicFragment;
import pl.rozbijbank.utilities.Constants;
import pl.rozbijbank.viewModel.UserViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A abstract fragment representing a list of Items.
 * When in portrait orientation - linear layout manager.
 * When in landscape orientation - has a grid layout with 3 columns
 * <p/>
 */
public class RewardListFragment extends BasicFragment {

    private BasicListNewBinding mBinding;
    private RewardAdapter adapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RewardListFragment() {
    }

    public static RewardListFragment newInstance() {
        RewardListFragment fragment = new RewardListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding= DataBindingUtil.inflate(inflater, R.layout.basic_list_new,container, false);

        setLayoutManager();
        adapter = new RewardAdapter();
        mBinding.list.setAdapter(adapter);

        mBinding.setIsLoading(true);
        getRewards();
        return mBinding.getRoot();
    }

    private void getRewards(){
        ApiInterface apiInterface= ApiClient.getClient().create(ApiInterface.class);

        Call<List<RewardPojo>> call= apiInterface.getRewards();
        call.enqueue(new Callback<List<RewardPojo>>() {
            @Override
            public void onResponse(@NotNull Call<List<RewardPojo>> call, @NotNull Response<List<RewardPojo>> response) {
                if(response.code()==200 && response.body()!=null){
                    //adapter.setRewards(response.body());
                    List<RewardPojo> rewards= new ArrayList<>();
                    for (RewardPojo rewardPojo: response.body()){
                        rewards.add(rewardPojo);
                        Log.i("kura", rewardPojo.getTitle());
                    }

                    adapter.setRewards(rewards);

                    mBinding.setIsLoading(response.body().isEmpty());
                }
            }

            @Override
            public void onFailure(@NotNull Call<List<RewardPojo>> call, @NotNull Throwable t) {
                mBinding.emptyRewards.setText(R.string.check_network_connection);
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setClicks();
    }

    private void setClicks(){
    }

    private void setLayoutManager(){
        int orientation = this.getResources().getConfiguration().orientation;
        int mColumnCount;
        if (orientation == Configuration.ORIENTATION_PORTRAIT)
            mColumnCount = 0;
        else
            mColumnCount =2;


        if (mColumnCount <= 1)
            mBinding.list.setLayoutManager(new LinearLayoutManager(getActivity()));
        else
            mBinding.list.setLayoutManager(new GridLayoutManager(getActivity(), mColumnCount));

    }

    private void showConfirmationDialog(long rewardId){
        new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                .setTitle(getString(R.string.warning_reward_order))
                .setMessage(getString(R.string.warning_reward_order_desc))
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    sendOrder(rewardId);
                })

                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void sendOrder(long rewardId){
        ApiInterface apiInterface= ApiClient.getClient().create(ApiInterface.class);
        Call<Void> call= apiInterface.sendOrded(new OrderPojo(1, rewardId));
        call.enqueue(new RetrofitCallback<>(getActivity(), new Callback<Void>() {
            @Override
            public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                if(response.code()==201){
                    Toast.makeText(getActivity(), getString(R.string.order_sent), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {

            }
        }));
    }


    @NotNull
    @Override
    public String toString() {
        return Constants.TAG_REWARDS_FRAGMENT;
    }

    public class RewardAdapter extends RecyclerView.Adapter<RewardAdapter.ViewHolder> {

        private List<RewardPojo> rewards;

        RewardAdapter() {
        }

        void setRewards(final List<RewardPojo> pojoList) {
            rewards = pojoList;
            notifyItemRangeInserted(0, pojoList.size());
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            RewardItemBinding binding = DataBindingUtil
                    .inflate(LayoutInflater.from(parent.getContext()), R.layout.reward_item,
                            parent, false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            RewardPojo reward= rewards.get(position);
            holder.binding.setReward(reward);
            holder.binding.executePendingBindings();

            byte[] data = android.util.Base64.decode(reward.getIcon(), android.util.Base64.DEFAULT);

            Glide.with(RewardListFragment.this).load(data).
                    into(holder.binding.rewardIcon);

            holder.binding.ordedButton.setOnClickListener(v -> {
                String token= preferences.getString(getString(R.string.key_active_user_token), "");
                if(token != null && !token.isEmpty()) {
                    String userId = preferences.getString(getString(R.string.key_active_user_id), "");
                    ViewModelProviders.of(RewardListFragment.this).get(UserViewModel.class).getUser(userId)
                            .observe(RewardListFragment.this, userEntity -> {
                                if (userEntity!=null){
                                    if (reward.getCost()<=userEntity.getPoints()){
                                        showConfirmationDialog(reward.getId());
                                    }else
                                        Toast.makeText(getActivity(), getString(R.string.warning_not_enough_points), Toast.LENGTH_SHORT).show();

                                }
                    });

                } else
                    Toast.makeText(getActivity(), getString(R.string.warning_not_logged_in), Toast.LENGTH_SHORT).show();
            });
        }


        @Override
        public int getItemCount() {
            return rewards == null ? 0 : rewards.size();
        }


        class ViewHolder extends RecyclerView.ViewHolder {

            final RewardItemBinding binding;

            ViewHolder(RewardItemBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
