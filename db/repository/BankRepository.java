package pl.rozbijbank.db.repository;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import pl.rozbijbank.db.TBRoomDatabase;
import pl.rozbijbank.db.entity.BankEntity;
import pl.rozbijbank.db.entity.ProductEntity;
import pl.rozbijbank.other.AppExecutors;

public class BankRepository {

    private static volatile BankRepository instance;
    private final TBRoomDatabase mDatabase;

    private MediatorLiveData<List<BankEntity>> allBanks;

    private BankRepository(final TBRoomDatabase database) {
        mDatabase = database;

        allBanks = new MediatorLiveData<>();

        allBanks.addSource(mDatabase.bankDao().getBanks(),
                bankEntities -> {
                    if (mDatabase.getDatabaseCreated().getValue() != null) {
                        allBanks.postValue(bankEntities);
                    }
                });
    }

    //used by BankListViewModel
    public LiveData<List<BankEntity>> getAllBanks() {
        return allBanks;
    }

    //used by BankViewModel
    public LiveData<BankEntity> getBank(long bankId){
        return mDatabase.bankDao().getBank(bankId);
    }

    public LiveData<List<ProductEntity>> getBankProducts(long bankId) {
        return mDatabase.productDao().getBankProducts(bankId);
    }

    public void update(BankEntity bankEntity){
        new AppExecutors().diskIO().execute(() -> mDatabase.runInTransaction(() -> mDatabase.bankDao().update(bankEntity)));
    }

    public void insert(BankEntity bankEntity){
        new AppExecutors().diskIO().execute(() -> mDatabase.runInTransaction(() -> mDatabase.bankDao().insert(bankEntity)));
    }

    //singleton assurance
    public static BankRepository getInstance(TBRoomDatabase database){
        if(instance == null){
            synchronized (BankRepository.class){
                if(instance == null)
                    instance= new BankRepository(database);
            }
        }
        return instance;
    }
}
