package pl.rozbijbank.db.dao;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import pl.rozbijbank.db.entity.UserEntity;

@Dao
public interface UserDao {

    @Insert
    long insert(UserEntity userEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<UserEntity> users);

    @Delete
    void delete(UserEntity userEntity);

    @Update
    void update(UserEntity userEntity);

    @Query("SELECT * FROM table_active_users WHERE userId = :userId LIMIT 1")
    LiveData<UserEntity> getUser(String userId);


    @Query("SELECT * FROM table_active_users")
    LiveData<List<UserEntity>> getUsers();

}
