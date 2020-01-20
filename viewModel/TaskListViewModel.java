package pl.rozbijbank.viewModel;

import android.app.Application;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import pl.rozbijbank.db.entity.TaskEntity;
import pl.rozbijbank.db.repository.TaskRepository;
import pl.rozbijbank.other.MyApplication;

public class TaskListViewModel extends AndroidViewModel {

    private final MediatorLiveData<List<TaskEntity>> allTasks;

    private TaskRepository mRepository;

    public TaskListViewModel(Application application) {
        super(application);
        allTasks = new MediatorLiveData<>();
        allTasks.setValue(null);

        mRepository = ((MyApplication) application).getTaskRepository();

        LiveData<List<TaskEntity>> allTasks = mRepository.getAllTasks();
        // observe the changes of the products from the database and forward them
        this.allTasks.addSource(allTasks, this.allTasks::setValue);

    }

    public LiveData<List<TaskEntity>> getTasks() { return allTasks; }

    public LiveData<List<TaskEntity>> getTasks(long bankId) {
        return mRepository.getBankTasks(bankId);
    }

    public LiveData<List<TaskEntity>> getTasks(long fromDate, long toDate) {
        return mRepository.getDateTasks(fromDate, toDate);
    }

    public LiveData<List<TaskEntity>> getTasks(long bankId, long fromDate, long toDate) {
        return mRepository.getBankDateTasks(bankId, fromDate, toDate);
    }

    public LiveData<List<TaskEntity>> getPromoTasks(long promoId) {
        return mRepository.getPromoTasks(promoId);
    }

}
