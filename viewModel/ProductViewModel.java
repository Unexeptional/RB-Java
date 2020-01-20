package pl.rozbijbank.viewModel;

import android.app.Application;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import pl.rozbijbank.db.entity.ProductEntity;
import pl.rozbijbank.db.entity.TaskEntity;
import pl.rozbijbank.db.repository.ProductRepository;
import pl.rozbijbank.db.repository.TaskRepository;
import pl.rozbijbank.other.MyApplication;

public class ProductViewModel extends AndroidViewModel {

    private final ProductRepository dataRepository;

    public ProductViewModel(Application application) {
        super(application);
        dataRepository = ((MyApplication) application).getProductRepository();
    }

    public LiveData<ProductEntity> getProduct(long productId){
        return dataRepository.getProduct(productId);
    }

    public void insert(ProductEntity productEntity){
        dataRepository.insert(productEntity);
    }

    public void update(ProductEntity productEntity){
        dataRepository.update(productEntity);
    }

    public void deleteAll(){
        dataRepository.deleteAll();
    }

    public void delete(ProductEntity productEntity){
        dataRepository.delete(productEntity);
    }

}
