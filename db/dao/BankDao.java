package pl.rozbijbank.db.dao;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import pl.rozbijbank.db.entity.BankEntity;

@Dao
public interface  BankDao {
    //C
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<BankEntity> products);

    @Insert
    long insert(BankEntity bankEntity);

    //R

    @Query("SELECT * FROM table_banks")
    LiveData<List<BankEntity>> getBanks();

    @Query("select * from table_banks where id = :bankId LIMIT 1")
    LiveData<BankEntity> getBank(long bankId);

    //U
    @Update
    void update(BankEntity bankEntity);

    //D
    @Delete
    void delete(BankEntity bankEntity);

  /*@Query("DELETE FROM table_banks")
    void deleteAll();*/

}
