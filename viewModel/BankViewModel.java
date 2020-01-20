package pl.rozbijbank.viewModel;

import android.app.Application;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import pl.rozbijbank.db.entity.BankEntity;
import pl.rozbijbank.db.entity.ProductEntity;
import pl.rozbijbank.db.repository.BankRepository;
import pl.rozbijbank.other.MyApplication;

public class BankViewModel extends AndroidViewModel {

    private final BankRepository dataRepository;

    public BankViewModel(Application application) {
        super(application);
        dataRepository = ((MyApplication) application).getBankRepository();
    }

    public LiveData<BankEntity> getBank(long bankId){
        return dataRepository.getBank(bankId);
    }

}
