package pl.rozbijbank.viewModel;

import android.app.Application;

import java.util.Date;
import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import pl.rozbijbank.db.entity.TaskEntity;
import pl.rozbijbank.db.repository.TaskRepository;
import pl.rozbijbank.other.MyApplication;

public class TaskViewModel extends AndroidViewModel {

    private final TaskRepository dataRepository;

    public TaskViewModel(Application application) {
        super(application);
        dataRepository = ((MyApplication) application).getTaskRepository();
    }

    public void insert(TaskEntity taskEntity){
        dataRepository.insert(taskEntity);
    }


    public void updateTaskHead(TaskEntity taskEntity){
        dataRepository.updateTaskHead(taskEntity);
    }

    public void updateTaskUserData(TaskEntity taskEntity){
        dataRepository.updateTaskUserData(taskEntity);
    }

}
