package pl.rozbijbank.viewModel;

import android.app.Application;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.sqlite.db.SupportSQLiteQuery;
import pl.rozbijbank.db.entity.PromoEntity;
import pl.rozbijbank.db.repository.PromoRepository;
import pl.rozbijbank.other.MyApplication;

public class PromoListViewModel extends AndroidViewModel {

    // MediatorLiveData can observe other LiveData objects and react on their emissions.
    private final MediatorLiveData<List<PromoEntity>> noParticipatePromos;
    private final MediatorLiveData<List<PromoEntity>> participatePromos;

    private PromoRepository mRepository;

    public PromoListViewModel(Application application) {
        super(application);


        // set by default null, until we get data from the database.
        noParticipatePromos = new MediatorLiveData<>();
        participatePromos = new MediatorLiveData<>();

        noParticipatePromos.setValue(null);
        participatePromos.setValue(null);

        mRepository = ((MyApplication) application).getPromoRepository();


        LiveData<List<PromoEntity>> allPromos = mRepository.getNoParticipatePromos();
        // observe the changes of the products from the database and forward them
        this.noParticipatePromos.addSource(allPromos, this.noParticipatePromos::setValue);

        LiveData<List<PromoEntity>> participatePromos = mRepository.getParticipatePromos();
        // observe the changes of the products from the database and forward them
        this.participatePromos.addSource(participatePromos, this.participatePromos::setValue);

    }

    //used By UI
    public LiveData<List<PromoEntity>> getNoParticipatePromos() { return noParticipatePromos; }

    public LiveData<List<PromoEntity>> getParticipatePromos() { return participatePromos; }

/*     public LiveData<List<PromoEntity>> getPromos(long bankId) {
        return mRepository.getNPBankPromos(bankId);
    }

    public LiveData<List<PromoEntity>> getNPActive(long bankId, long date) {
        return mRepository.getNPActive(bankId, date);
    }

    public LiveData<List<PromoEntity>> getNPActual(long bankId, long date) {
        return mRepository.getNPActual(bankId, date);
    }*/

    public LiveData<List<PromoEntity>> getNPActive(long date) {
        return mRepository.getNPActive(date);
    }

    public LiveData<List<PromoEntity>> getNPActual(long date) {
        return mRepository.getNPActual(date);
    }

    public LiveData<List<PromoEntity>> getRawPromos(SupportSQLiteQuery query) {
        return mRepository.getRawPromos(query);
    }

    public LiveData<List<PromoEntity>> getAll( ) {
        return mRepository.getAll();
    }
}
