package pl.rozbijbank.db.repository;

import java.util.Date;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import pl.rozbijbank.db.TBRoomDatabase;
import pl.rozbijbank.db.entity.TaskEntity;
import pl.rozbijbank.other.AppExecutors;

public class TaskRepository {

    private static volatile TaskRepository instance;
    private final TBRoomDatabase mDatabase;

    private MediatorLiveData<List<TaskEntity>> allTasks;



    private TaskRepository(final TBRoomDatabase database) {
        mDatabase = database;

        allTasks = new MediatorLiveData<>();

        allTasks.addSource(mDatabase.taskDao().getAll(),
                taskEntities -> {
                    if (mDatabase.getDatabaseCreated().getValue() != null) {
                        allTasks.postValue(taskEntities);
                    }
                });
    }

    //Used by tasklistviewmodel
    public LiveData<List<TaskEntity>> getAllTasks() {
        return allTasks;
    }

    public LiveData<List<TaskEntity>> getBankTasks(long bankId) {
        return mDatabase.taskDao().getBankTasks(bankId);
    }

    public LiveData<List<TaskEntity>> getPromoTasks(long promoId) {
        return mDatabase.taskDao().getPromoTasks(promoId);
    }

    public LiveData<List<TaskEntity>> getDateTasks(long fromDate, long toDate) {
        return mDatabase.taskDao().getDateTasks(fromDate, toDate);
    }

    public LiveData<List<TaskEntity>> getBankDateTasks(long bankId, long fromDate, long toDate) {
        return mDatabase.taskDao().getBankDateTasks(bankId, fromDate, toDate);
    }

    //used by viewModel
    public void insert(TaskEntity taskEntity){
        new AppExecutors().diskIO().execute(() -> mDatabase.runInTransaction(() ->
                mDatabase.taskDao().insert(taskEntity)));

    }

    public void updateTaskHead(TaskEntity taskEntity){
        new AppExecutors().diskIO().execute(() -> mDatabase.runInTransaction(() ->
                mDatabase.taskDao().updateTaskHead(
                        taskEntity.getStartDate(),
                        taskEntity.getEndDate(),
                        taskEntity.getTitle(),
                        taskEntity.getDescription(),
                        taskEntity.getNote(),
                        taskEntity.getTaskType(),
                        taskEntity.getMonthAfterSigning(),
                        taskEntity.getDaysAfterSigning(),
                        taskEntity.getAmount(),
                        taskEntity.getUri(),
                        taskEntity.getId()
                )));
    }

    public void updateTaskUserData(TaskEntity taskEntity){
        new AppExecutors().diskIO().execute(() -> mDatabase.runInTransaction(() ->
                mDatabase.taskDao().updateTaskUserData(
                        taskEntity.getUserStartDate(),
                        taskEntity.getUserEndDate(),
                        taskEntity.isCompleted(),
                        taskEntity.getActualAmount(),
                        taskEntity.getParticipationId(),
                        taskEntity.getId()
                        )));
    }




    //singleton assurance
    public static TaskRepository getInstance(TBRoomDatabase database){
        if(instance == null){
            synchronized (TaskRepository.class){
                if(instance == null)
                    instance= new TaskRepository(database);
            }
        }
        return instance;
    }
}
