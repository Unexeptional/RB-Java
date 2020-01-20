package pl.rozbijbank.db.repository;

import java.util.Date;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.sqlite.db.SupportSQLiteQuery;
import pl.rozbijbank.db.TBRoomDatabase;
import pl.rozbijbank.db.entity.PromoEntity;
import pl.rozbijbank.db.entity.TaskEntity;
import pl.rozbijbank.other.AppExecutors;

public class PromoRepository {

    private static volatile PromoRepository instance;
    private final TBRoomDatabase mDatabase;

    //PromoListVM
    private MediatorLiveData<List<PromoEntity>> noParticipatePromos;
    private MediatorLiveData<List<PromoEntity>> participatePromos;



    private PromoRepository(final TBRoomDatabase database) {
        mDatabase = database;

        noParticipatePromos = new MediatorLiveData<>();
        participatePromos = new MediatorLiveData<>();

        //list observation
        participatePromos.addSource(mDatabase.promoDao().getParticipatePromos(),
                promoEntities -> {
                    if (mDatabase.getDatabaseCreated().getValue() != null) {
                        participatePromos.postValue(promoEntities);
                    }
                });

        noParticipatePromos.addSource(mDatabase.promoDao().getNoParticipatePromos(),
                promoEntities -> {
                    if (mDatabase.getDatabaseCreated().getValue() != null) {
                        noParticipatePromos.postValue(promoEntities);
                    }
                });
    }

    //used by PromoListViewModel
    public LiveData<List<PromoEntity>> getParticipatePromos() {
        return participatePromos;
    }

    public LiveData<List<PromoEntity>> getNoParticipatePromos() {
        return noParticipatePromos;
    }

    public LiveData<List<PromoEntity>> getNPBankPromos(long bankId) {
        return mDatabase.promoDao().getNPBankPromos(bankId);
    }

    public LiveData<List<PromoEntity>> getNPActive(long bankId, long date) {
        return mDatabase.promoDao().getNPActive(bankId, date);
    }

    public LiveData<List<PromoEntity>> getNPActual(long bankId, long date) {
        return mDatabase.promoDao().getNPActual(bankId, date);
    }

    public LiveData<List<PromoEntity>> getNPActive(long date) {
        return mDatabase.promoDao().getNPActive(date);
    }

    public LiveData<List<PromoEntity>> getNPActual(long date) {
        return mDatabase.promoDao().getNPActual(date);
    }

    public LiveData<List<PromoEntity>> getRawPromos(SupportSQLiteQuery query) {
        return mDatabase.promoDao().getRawPromos(query);
    }

    public LiveData<List<PromoEntity>> getAll() {
        return mDatabase.promoDao().getAll();
    }

    //PromoViewModel
    public LiveData<PromoEntity> loadPromo(final long promoId) {
        return mDatabase.promoDao().loadPromo(promoId);
    }

    public PromoEntity getPromo(final long promoId){
        return mDatabase.promoDao().getPromo(promoId);
    }

    public void updatePromoUserData(long participationId, Date date, long id, boolean completed){
        new AppExecutors().diskIO().execute(() -> mDatabase.runInTransaction(() ->
                mDatabase.promoDao().updatePromoUserData(participationId, date, id, completed)));

    }

    public void updatePromoHead(PromoEntity promoEntity){
        new AppExecutors().diskIO().execute(() -> mDatabase.runInTransaction(() ->
                mDatabase.promoDao().updatePromoHead(
                        promoEntity.getTitle(),
                        promoEntity.getDescription(),
                        promoEntity.getGracePeriod(),
                        promoEntity.getWarning(),
                        promoEntity.getStartDate(),
                        promoEntity.getEndDate(),
                        promoEntity.getBanner(),
                        promoEntity.getUri(),
                        promoEntity.getRegulationsUri(),
                        promoEntity.getPoints(),
                        promoEntity.getId(),
                        promoEntity.getBankId()
                )));

    }

    public LiveData<List<TaskEntity>> getPromoTasks(long promoId) {
        return mDatabase.taskDao().getPromoTasks(promoId);
    }


    //Used straight from that baby
    public void insert(PromoEntity promoEntity){
        new AppExecutors().diskIO().execute(() -> mDatabase.runInTransaction(() -> mDatabase.promoDao().insert(promoEntity)));
    }

    public boolean isPromoParticipated(long promoId){
        return mDatabase.promoDao().getPromoParticipate(promoId)!= 0;
    }




    //singleton assurance
    public static PromoRepository getInstance(TBRoomDatabase database){
        if(instance == null){
            synchronized (PromoRepository.class){
                if(instance == null)
                    instance= new PromoRepository(database);
            }
        }
        return instance;
    }
}
