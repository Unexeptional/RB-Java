package pl.rozbijbank.db.repository;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import pl.rozbijbank.db.TBRoomDatabase;
import pl.rozbijbank.db.entity.ProductEntity;
import pl.rozbijbank.other.AppExecutors;

public class ProductRepository {

    private static volatile ProductRepository instance;
    private final TBRoomDatabase mDatabase;

    private MediatorLiveData<List<ProductEntity>> allProducts;

    private ProductRepository(final TBRoomDatabase database) {
        mDatabase = database;

        allProducts = new MediatorLiveData<>();

        allProducts.addSource(mDatabase.productDao().getAll(),
                products -> {
                    if (mDatabase.getDatabaseCreated().getValue() != null) {
                        allProducts.postValue(products);
                    }
                });
    }

    //used by PromoListViewModel
    public LiveData<List<ProductEntity>> getAllProducts() {
        return allProducts;
    }

    public LiveData<List<ProductEntity>> getBankProducts(long bankId) {
        return mDatabase.productDao().getBankProducts(bankId);
    }

    //used by ProductViewModel
    public LiveData<ProductEntity> getProduct(long productId){
        return mDatabase.productDao().getProduct(productId);
    }

    public void insert(ProductEntity productEntity){
        new AppExecutors().diskIO().execute(() -> mDatabase.runInTransaction(() -> mDatabase.productDao().insert(productEntity)));
    }

    public void update(ProductEntity productEntity){
        new AppExecutors().diskIO().execute(() -> mDatabase.runInTransaction(() -> mDatabase.productDao().update(productEntity)));
    }

    public void deleteAll(){
        new AppExecutors().diskIO().execute(() -> mDatabase.runInTransaction(() -> mDatabase.productDao().deleteAll()));
    }

    public void delete(ProductEntity productEntity){
        new AppExecutors().diskIO().execute(() -> mDatabase.runInTransaction(() -> mDatabase.productDao().delete(productEntity)));
    }

    //singleton assurance
    public static ProductRepository getInstance(TBRoomDatabase database){
        if(instance == null){
            synchronized (ProductRepository.class){
                if(instance == null)
                    instance= new ProductRepository(database);
            }
        }
        return instance;
    }
}
