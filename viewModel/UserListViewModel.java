package pl.rozbijbank.viewModel;

import android.app.Application;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import pl.rozbijbank.db.entity.UserEntity;
import pl.rozbijbank.db.repository.UserRepository;
import pl.rozbijbank.other.MyApplication;

public class UserListViewModel extends AndroidViewModel {

    private final MediatorLiveData<List<UserEntity>> allUsers;

    public UserListViewModel(Application application) {
        super(application);
        allUsers = new MediatorLiveData<>();
        allUsers.setValue(null);

        UserRepository mRepository = ((MyApplication) application).getUserRepository();

        LiveData<List<UserEntity>> allTasks = mRepository.getUsers();
        // observe the changes of the products from the database and forward them
        this.allUsers.addSource(allTasks, this.allUsers::setValue);

    }

    public LiveData<List<UserEntity>> getUsers() { return allUsers; }
    
}
