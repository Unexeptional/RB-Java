package pl.rozbijbank.viewModel;

import android.app.Application;

import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import pl.rozbijbank.db.entity.PromoEntity;
import pl.rozbijbank.db.entity.TaskEntity;
import pl.rozbijbank.db.repository.PromoRepository;
import pl.rozbijbank.other.MyApplication;

public class PromoViewModel extends AndroidViewModel {

    private final LiveData<PromoEntity> mObservablePromo;
    private final PromoRepository repository;

    private final LiveData<List<TaskEntity>> mObservableTasks;

    //public and used by data binding with layout
    public ObservableField<PromoEntity> promo= new ObservableField<>();

    private PromoViewModel(@NonNull Application application, PromoRepository dataRepository, final long promoId){
        super(application);
        repository= dataRepository;
        mObservablePromo = repository.loadPromo(promoId);
        mObservableTasks = repository.getPromoTasks(promoId);
    }

    public LiveData<PromoEntity> getObservablePromo() {
        return mObservablePromo;
    }

    public LiveData<List<TaskEntity>> getTasks() {
        return mObservableTasks;
    }

    public void setPromo(PromoEntity promo) {
        this.promo.set(promo);
    }


    public void updatePromoUserData(long participationId, Date date, long id, boolean completed) { repository.updatePromoUserData(participationId, date, id, completed); }


    /**
     * A creator is used to inject the product ID into the ViewModel
     * <p>
     * This creator is to showcase how to inject dependencies into ViewModels. It's not
     * actually necessary in this case, as the product ID can be passed in a public method.
     */
    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final Application mApplication;

        private final long mProductId;

        private final PromoRepository mRepository;

        public Factory(@NonNull Application application, long productId) {
            mApplication = application;
            mProductId = productId;
            mRepository = ((MyApplication) application).getPromoRepository();
        }

        //Nie wiem co to
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            //noinspection unchecked
            return (T) new PromoViewModel(mApplication, mRepository, mProductId);
        }
    }
}
