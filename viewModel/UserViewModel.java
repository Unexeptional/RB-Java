package pl.rozbijbank.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import pl.rozbijbank.db.entity.UserEntity;
import pl.rozbijbank.db.repository.UserRepository;
import pl.rozbijbank.other.MyApplication;

public class UserViewModel extends AndroidViewModel {

    private final UserRepository dataRepository;

    public UserViewModel(Application application) {
        super(application);
        dataRepository = ((MyApplication) application).getUserRepository();
    }

    public LiveData<UserEntity> getUser(String userId){
        return dataRepository.getUser(userId);
    }

    public void insert(UserEntity userEntity){
        dataRepository.insert(userEntity);
    }

    public void delete(UserEntity userEntity){
        dataRepository.delete(userEntity);
    }

    public void update(UserEntity userEntity){
        dataRepository.update(userEntity);
    }

}
