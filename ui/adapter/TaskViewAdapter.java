package pl.rozbijbank.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import pl.rozbijbank.R;
import pl.rozbijbank.callback.TaskClickCallback;
import pl.rozbijbank.databinding.TaskItemBinding;
import pl.rozbijbank.db.entity.TaskEntity;
import pl.rozbijbank.db.model.Task;
import pl.rozbijbank.other.MyApplication;
import pl.rozbijbank.utilities.Constants;

/**
 * {@link RecyclerView.Adapter} that can display a {@link TaskEntity} and makes a call to the
 * specified {@link TaskClickCallback}.
 */
public class TaskViewAdapter extends RecyclerView.Adapter<TaskViewAdapter.TaskViewHolder> {

    private List<? extends Task> mTaskList;

    @Override
    public int getItemCount() {
        return mTaskList == null ? 0 : mTaskList.size();
    }

    @Override
    public long getItemId(int position) {
        return mTaskList.get(position).getId();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {

        final TaskItemBinding binding;

        TaskViewHolder(TaskItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NotNull
    private final TaskClickCallback mTaskClickCallback;

    public TaskViewAdapter(@NotNull TaskClickCallback clickCallback) {
        mTaskClickCallback = clickCallback;
        setHasStableIds(true);
    }

    public void setTaskList(final List<? extends Task> taskList) {
        if (mTaskList == null) {
            mTaskList = taskList;
            notifyItemRangeInserted(0, taskList.size());
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mTaskList.size();
                }

                @Override
                public int getNewListSize() {
                    return taskList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mTaskList.get(oldItemPosition).getId() ==
                            taskList.get(newItemPosition).getId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Task newTask = taskList.get(newItemPosition);
                    Task oldTask = mTaskList.get(oldItemPosition);

                    //only stuff that is on card
                    return newTask.getId() == oldTask.getId()
                            && Objects.equals(newTask.isCompleted(), oldTask.isCompleted())
                            && Objects.equals(newTask.getTitle(), oldTask.getTitle())
                            && Objects.equals(newTask.getTaskType(), oldTask.getTaskType())
                            && Objects.equals(newTask.getUri(), oldTask.getUri())
                            && Objects.equals(newTask.getDescription(), oldTask.getDescription())
                            && Objects.equals(newTask.getNote(), oldTask.getNote())
                            && Objects.equals(newTask.getUserEndDate(), oldTask.getUserEndDate())
                            && Objects.equals(newTask.getUserStartDate(), oldTask.getUserStartDate())
                            && Objects.equals(newTask.getParticipationId(), oldTask.getParticipationId())
                            && Objects.equals(newTask.getActualAmount(), oldTask.getActualAmount());
                }
            });
            mTaskList = taskList;
            result.dispatchUpdatesTo(this);
        }
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TaskItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.task_item,
                        parent, false);
        return new TaskViewHolder(binding);
    }

    //HERE THE MAGIC STARTS

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskItemBinding binding= holder.binding;
        TaskEntity taskEntity= new TaskEntity(mTaskList.get(position));
        binding.setTask(taskEntity);
        binding.executePendingBindings();

        setTaskVisuals(taskEntity, binding);
        setTaskClicks(taskEntity, binding);
        if (taskEntity.getTaskType()== Constants.TASK_TYPE_BOOL_LINKED)
            setLinkedTask(binding, taskEntity);
    }

    private boolean areClicksAllowed(TaskEntity taskEntity){
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.add(Calendar.HOUR, 2);

        return taskEntity.getUserStartDate() != null &&
                taskEntity.getUserEndDate() != null &&
                taskEntity.getParticipationId() >0 &&
                calendar.getTime().after(taskEntity.getUserStartDate());
    }

    private void setTaskClicks(TaskEntity taskEntity, TaskItemBinding binding){

        binding.taskItemShowUri.setOnClickListener(v ->
                mTaskClickCallback.openBrowser(taskEntity.getUri(), taskEntity.getPromoId()));


        if(areClicksAllowed(taskEntity)) {
            binding.buttonsLayout.setVisibility(View.VISIBLE);

            binding.done.setOnClickListener(v ->{
                if(!taskEntity.isCompleted()){
                    taskEntity.setCompleted(true);
                    mTaskClickCallback.updateTask(taskEntity, true);
                }
            });

            binding.undone.setOnClickListener(v ->{
                if(taskEntity.isCompleted()){
                    taskEntity.setCompleted(false);
                    mTaskClickCallback.updateTask(taskEntity, true);
                }
            });

            binding.plus.setOnClickListener(v ->{
                if(taskEntity.getActualAmount() < taskEntity.getAmount()){
                    taskEntity.setActualAmount(taskEntity.getActualAmount()+1);
                    if (taskEntity.getAmount()==taskEntity.getActualAmount()) {
                        taskEntity.setCompleted(true);
                        mTaskClickCallback.updateTask(taskEntity, true);
                    }else
                        mTaskClickCallback.updateTask(taskEntity, false);
                }
            });

            binding.minus.setOnClickListener(v ->{
                if(taskEntity.getActualAmount() > 0 ){
                    taskEntity.setActualAmount(taskEntity.getActualAmount()-1);
                    if (taskEntity.getAmount()>taskEntity.getActualAmount()) {
                        taskEntity.setCompleted(false);
                        mTaskClickCallback.updateTask(taskEntity, true);
                    }else
                        mTaskClickCallback.updateTask(taskEntity, false);
                }
            });


        }else
            binding.buttonsLayout.setVisibility(View.GONE);


    }

    private void setDates(TaskItemBinding binding, TaskEntity taskEntity){
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        Context context= MyApplication.getContext();

        if(taskEntity.getDaysAfterSigning()== 0 && taskEntity.getMonthAfterSigning() ==0){
            binding.startDate.setText(dateFormatGmt.format(taskEntity.getStartDate()));
            binding.endDate.setText(dateFormatGmt.format(taskEntity.getEndDate()));
        }else {
            if (taskEntity.getMonthAfterSigning() != 0) {
                if (taskEntity.getUserStartDate()!=null)
                    binding.startDate.setText(dateFormatGmt.format(taskEntity.getUserStartDate()));
                else{
                    String builder = context.getString(R.string.in) + " " +
                            taskEntity.getMonthAfterSigning() + " " + context.getString(R.string.task_item_month);
                    binding.startDate.setText(builder);
                }

                setEndDate(taskEntity, binding, context, dateFormatGmt);
            }else if (taskEntity.getDaysAfterSigning()!=0){
                if (taskEntity.getUserStartDate()!=null)
                    binding.startDate.setText(dateFormatGmt.format(taskEntity.getUserStartDate()));
                else{
                    String builder = context.getString(R.string.within) + " " +
                            taskEntity.getDaysAfterSigning() + " " + context.getString(R.string.days);
                    binding.startDate.setText(builder);
                }

                setEndDate(taskEntity, binding, context, dateFormatGmt);
            }
        }
    }


    private void setLinkedTask(TaskItemBinding binding, TaskEntity taskEntity){
        binding.taskItemShowUri.setText(MyApplication.getMyString(R.string.show_special_uri));
        binding.taskItemShowUri.setVisibility(View.VISIBLE);
        binding.taskItemShowUri.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.add(Calendar.HOUR, 2);

            if (taskEntity.getEndDate().after( calendar.getTime()))
                mTaskClickCallback.openBrowser(taskEntity.getPromoId());
            else
                Toast.makeText(MyApplication.getContext(), MyApplication.getMyString(R.string.ptomo_finished_warning), Toast.LENGTH_SHORT).show();
        });

    }

    private void setEndDate(TaskEntity taskEntity, TaskItemBinding binding, Context context, SimpleDateFormat dateFormatGmt){
        if (taskEntity.getUserEndDate()!=null)
            binding.endDate.setText(dateFormatGmt.format(taskEntity.getUserEndDate()));
        else{
            binding.endDate.setText(context.getString(R.string.task_item_after_contract_signing));
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private void setTaskVisuals(TaskEntity taskEntity, TaskItemBinding binding){

        int id= MyApplication.getBankIconId(taskEntity.getBankId());
        if(id!=0)
            Picasso.get().load(id).into(binding.taskItemBankIcon);

        binding.actualDoubleAmount.clearFocus();

        setDates(binding, taskEntity);

        switch (taskEntity.getTaskType()){
            case Constants.TASK_TYPE_BOOL:
                break;
            case Constants.TASK_TYPE_INT:
                binding.actualIntAmount.setText(String.valueOf(taskEntity.getActualAmount()));
              /*  IT IS CAUSING PROBLEMS
                if(taskEntity.getActualAmount()>= taskEntity.getAmount())
                    binding.done.callOnClick();
                else
                    binding.undone.callOnClick();*/
                break;
            case Constants.TASK_TYPE_DOUBLE:
                binding.actualDoubleAmount.setText(String.valueOf(taskEntity.getActualAmount()));
                binding.actualDoubleAmount.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        binding.actualDoubleAmount.setCompoundDrawablesWithIntrinsicBounds(0,
                                0, R.drawable.twotone_done_black_24,0);

                    }
                });

                binding.actualDoubleAmount.setOnTouchListener((v, event) -> {
                    final int DRAWABLE_RIGHT = 2;

                    if(event.getAction() == MotionEvent.ACTION_UP) {

                        if(  binding.actualDoubleAmount.getCompoundDrawables()[DRAWABLE_RIGHT]!=null)
                            if(event.getRawX() >= (  binding.actualDoubleAmount.getRight() - 50 -  binding.actualDoubleAmount.
                                    getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                                double amount;
                                try {
                                    amount= Double.parseDouble(Objects.requireNonNull(binding.actualDoubleAmount.getText()).toString());
                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();
                                    amount=0;
                                }

                                taskEntity.setActualAmount(amount);
                                mTaskClickCallback.updateTask(taskEntity, false);
                            }
                    }
                    return false;
                });
                break;
        }

    }
}
