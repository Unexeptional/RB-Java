package pl.rozbijbank.viewModel;

import android.app.Application;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import pl.rozbijbank.db.entity.BankEntity;
import pl.rozbijbank.db.entity.ProductEntity;
import pl.rozbijbank.db.repository.BankRepository;
import pl.rozbijbank.other.MyApplication;

public class BankListViewModel extends AndroidViewModel {

    private final MediatorLiveData<List<BankEntity>> allBanks;

    private BankRepository bankRepository;

    public BankListViewModel(Application application) {
        super(application);
        allBanks = new MediatorLiveData<>();
        allBanks.setValue(null);

        bankRepository = ((MyApplication) application).getBankRepository();

        LiveData<List<BankEntity>> allBanks = bankRepository.getAllBanks();

        // observe the changes of the products from the database and forward them
        this.allBanks.addSource(allBanks, this.allBanks::setValue);
    }

    public LiveData<List<BankEntity>> getAllBanks() { return allBanks; }

    public LiveData<List<ProductEntity>> getBankProducts(long bankId) {
        return bankRepository.getBankProducts(bankId);
    }
}
