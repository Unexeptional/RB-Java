package pl.rozbijbank.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import pl.rozbijbank.db.entity.TimestampEntity;

@Dao
public interface TimestampDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(TimestampEntity timestampEntity);

    @Query("select * from table_timestamp LIMIT 1")
    TimestampEntity getTimestamp();

}
