package pl.rozbijbank.db.dao;

import java.util.Date;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;
import pl.rozbijbank.db.entity.PromoEntity;

import static androidx.room.OnConflictStrategy.IGNORE;
import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface PromoDao {

    //C
    @Insert (onConflict = IGNORE)
    long insert(PromoEntity promoEntity);

    @Insert(onConflict = IGNORE)
    void insertAll(List<PromoEntity> products);

    //R
    @Query("select * from table_promos where id = :promoId")
    LiveData<PromoEntity> loadPromo(long promoId);

    @Query("select * from table_promos where id = :promoId")
    PromoEntity getPromo(long promoId);

    @Query("select participation_id from table_promos where id = :promoId")
    long getPromoParticipate(long promoId);

    @Query("SELECT * FROM table_promos where participation_id = 0 ORDER BY end_date ASC")
    LiveData<List<PromoEntity>> getNoParticipatePromos();

    @Query("select * from table_promos where participation_id != 0 ORDER BY end_date ASC")
    LiveData<List<PromoEntity>>  getParticipatePromos();

    @Query("select * from table_promos where participation_id != 0 ORDER BY end_date ASC")
    List<PromoEntity>  getParticipatePromosUltra();

    @Query("select * from table_promos where bank_id = :bankId AND participation_id = 0 ORDER BY end_date ASC")
    LiveData<List<PromoEntity>> getNPBankPromos(long bankId);

    @Query("select * from table_promos where bank_id = :bankId AND participation_id = 0 AND end_date > :date ORDER BY end_date ASC")
    LiveData<List<PromoEntity>>  getNPActual(long bankId, long date);

    @Query("select * from table_promos where bank_id = :bankId AND participation_id = 0 AND end_date < :date ORDER BY end_date ASC")
    LiveData<List<PromoEntity>>  getNPActive(long bankId, long date);

    @Query("select * from table_promos where participation_id = 0 AND end_date > :date ORDER BY end_date ASC")
    LiveData<List<PromoEntity>>  getNPActual(long date);

    @Query("select * from table_promos where participation_id = 0 AND end_date < :date ORDER BY end_date ASC")
    LiveData<List<PromoEntity>>  getNPActive(long date);

    @RawQuery(observedEntities = PromoEntity.class)
    LiveData<List<PromoEntity>> getRawPromos(SupportSQLiteQuery query);

    @Query("SELECT * FROM table_promos ")
    LiveData<List<PromoEntity>> getAll();

    //U
    @Query("UPDATE table_promos SET " +
            "title= :title, " +
            "description= :description, " +
            "grace_period= :gracePeriod, " +
            "warning= :warning, " +
            "start_date= :startDate, " +
            "end_date= :endDate, " +
            "banner= :banner, " +
            "uri= :uri, " +
            "regulations_uri= :regulationsUri, " +
            "points=:points,  " +
            "bank_id=:bankId  " +
            "WHERE id=:id")
    void updatePromoHead(String title, String description, String gracePeriod,
                         String warning, Date startDate, Date endDate, String banner,
                         String uri, String regulationsUri, int points, long id, long bankId);

    @Query("UPDATE table_promos SET participation_id= :participationId, contract_signing_date=:date, completed=:completed  WHERE id=:id")
    void updatePromoUserData(long participationId, Date date, long id, boolean completed);

    //D
    @Delete
    void delete(PromoEntity promoEntity);

    @Query("DELETE FROM table_promos")
    void deleteAll();

    @Query("UPDATE table_promos SET participation_id= 0, contract_signing_date=null, completed=0")
    void deleteUserData();

    //trigger update
    @Query("SELECT * FROM table_promos LIMIT 1 ")
    PromoEntity triggerUpdate();
}
