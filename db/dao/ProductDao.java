package pl.rozbijbank.db.dao;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import pl.rozbijbank.db.entity.ProductEntity;

@Dao
public interface ProductDao {

    //CREATE
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ProductEntity productEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ProductEntity> products);

    //READ
    @Query("select * from table_products where id = :productId LIMIT 1")
    LiveData<ProductEntity> getProduct(long productId);

    @Query("SELECT * FROM table_products ORDER BY inactive ASC, bank_id ASC")
    LiveData<List<ProductEntity>> getAll();

    @Query("SELECT * FROM table_products ORDER BY inactive ASC, bank_id ASC")
    List<ProductEntity> getAllUltra();

    @Query("select * from table_products where bank_id = :bankId")
    LiveData<List<ProductEntity>> getBankProducts(long bankId);


    //UPDATE
    @Update (onConflict = OnConflictStrategy.REPLACE)
    void update(ProductEntity productEntity);

    //DELETE
    @Delete
    void delete(ProductEntity productEntity);

    @Query("DELETE FROM table_products")
    void deleteAll();



    //TRIGGER onchanged
    @Query("SELECT * FROM table_products ORDER BY inactive ASC, bank_id ASC LIMIT 1")
    ProductEntity triggerUpdate();

}
