package pl.rozbijbank.viewModel;

import android.app.Application;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import pl.rozbijbank.db.entity.ProductEntity;
import pl.rozbijbank.db.repository.ProductRepository;
import pl.rozbijbank.other.MyApplication;

public class ProductListViewModel extends AndroidViewModel {

    private final MediatorLiveData<List<ProductEntity>> allProducts;

    private ProductRepository mRepository;

    public ProductListViewModel(Application application) {
        super(application);
        allProducts = new MediatorLiveData<>();
        allProducts.setValue(null);

        mRepository = ((MyApplication) application).getProductRepository();

        LiveData<List<ProductEntity>> allProducts = mRepository.getAllProducts();

        // observe the changes of the products from the database and forward them
        this.allProducts.addSource(allProducts, this.allProducts::setValue);
    }

    public LiveData<List<ProductEntity>> getAllProducts() { return allProducts; }

    public LiveData<List<ProductEntity>> getBankProducts(long bankId) {
        return mRepository.getBankProducts(bankId);
    }
}
