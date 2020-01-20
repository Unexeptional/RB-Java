package pl.rozbijbank.viewModel;

import android.app.Application;

import java.util.Date;
import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import pl.rozbijbank.db.entity.PromoEntity;
import pl.rozbijbank.db.entity.TaskEntity;
import pl.rozbijbank.db.repository.PromoRepository;
import pl.rozbijbank.other.MyApplication;

public class PromoSViewModel extends AndroidViewModel {

    private final PromoRepository dataRepository;

    public PromoSViewModel(Application application) {
        super(application);
        dataRepository = ((MyApplication) application).getPromoRepository();
    }

    public void updatePromoUserData(long participationId, Date date, long id,boolean completed) { dataRepository.updatePromoUserData(participationId, date, id, completed); }

}
