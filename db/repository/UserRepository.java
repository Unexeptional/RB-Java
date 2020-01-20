package pl.rozbijbank.db.repository;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import pl.rozbijbank.db.TBRoomDatabase;
import pl.rozbijbank.db.entity.UserEntity;
import pl.rozbijbank.other.AppExecutors;

public class UserRepository {

    private static volatile UserRepository instance;
    private final TBRoomDatabase mDatabase;

    private MediatorLiveData<List<UserEntity>> allUsers;

    private UserRepository(final TBRoomDatabase database) {
        mDatabase = database;

        allUsers = new MediatorLiveData<>();

        allUsers.addSource(mDatabase.userDao().getUsers(),
                users -> {
                    if (mDatabase.getDatabaseCreated().getValue() != null) {
                        allUsers.postValue(users);
                    }
                });
    }

    //used by UserListViewModel
    public LiveData<List<UserEntity>> getUsers() {
        return allUsers;
    }

    //used by UserViewModel
    public LiveData<UserEntity> getUser(String userId){
        return mDatabase.userDao().getUser(userId);
    }

    public void insert(UserEntity userEntity){
        new AppExecutors().diskIO().execute(() -> mDatabase.runInTransaction(() -> mDatabase.userDao().insert(userEntity)));
    }

    public void delete(UserEntity userEntity){
        new AppExecutors().diskIO().execute(() -> mDatabase.runInTransaction(() -> mDatabase.userDao().delete(userEntity)));
    }

    public void update(UserEntity userEntity){
        new AppExecutors().diskIO().execute(() -> mDatabase.runInTransaction(() -> mDatabase.userDao().update(userEntity)));
    }

    //singleton assurance
    public static UserRepository getInstance(TBRoomDatabase database){
        if(instance == null){
            synchronized (UserRepository.class){
                if(instance == null)
                    instance= new UserRepository(database);
            }
        }
        return instance;
    }
}
