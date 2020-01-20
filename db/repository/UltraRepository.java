package pl.rozbijbank.db.repository;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import pl.rozbijbank.db.TBRoomDatabase;
import pl.rozbijbank.db.converter.DateTypeConverter;
import pl.rozbijbank.db.entity.BankEntity;
import pl.rozbijbank.db.entity.ProductEntity;
import pl.rozbijbank.db.entity.PromoEntity;
import pl.rozbijbank.db.entity.TaskEntity;
import pl.rozbijbank.db.entity.TimestampEntity;
import pl.rozbijbank.db.model.Product;
import pl.rozbijbank.networking.ApiClient;
import pl.rozbijbank.networking.ApiInterface;
import pl.rozbijbank.networking.pojo.IdPojo;
import pl.rozbijbank.networking.pojo.ProductPojo;
import pl.rozbijbank.networking.pojo.addParticipationPojo;
import pl.rozbijbank.networking.pojo.participation.ParticipationPojo;
import pl.rozbijbank.networking.pojo.participation.ParticipationTask;
import pl.rozbijbank.other.AppExecutors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UltraRepository {

    private static volatile UltraRepository instance;
    private final TBRoomDatabase mDatabase;

    private UltraRepository(final TBRoomDatabase database) {
        mDatabase = database;
    }


    public void setDefaultDb(){
        new AppExecutors().diskIO().execute(() -> mDatabase.runInTransaction(() -> {
            mDatabase.productDao().deleteAll();
            mDatabase.promoDao().deleteUserData();
            mDatabase.taskDao().deleteUserData();
        }));

    }

    public void testBEzpieczneLogowanie(List<ProductEntity> products, List<ParticipationPojo> participations){
        new AppExecutors().diskIO().execute(() -> mDatabase.runInTransaction(() -> {
            mDatabase.productDao().deleteAll();
            mDatabase.productDao().insertAll(products);


            mDatabase.promoDao().deleteUserData();
            mDatabase.taskDao().deleteUserData();

            for (ParticipationPojo pp: participations) {

                mDatabase.promoDao().updatePromoUserData(pp.getId(), DateTypeConverter.toDate(pp.getContractSigningDate()),
                        pp.getPromoId(), pp.isCompleted());

                for (ParticipationTask pt : pp.getParticipationTasks())
                    mDatabase.taskDao().updateTaskUserData(
                            DateTypeConverter.toDate(pt.getStartDate()),
                            DateTypeConverter.toDate(pt.getEndDate()),
                            pt.isCompleted(),
                            pt.getActualAmount(),
                            pp.getId(),
                            pt.getTaskId());
            }

        }));
    }

    public void setDb(List<BankEntity> bankEntities, List<PromoEntity> promoEntities, List<TaskEntity> taskEntities){
        new AppExecutors().diskIO().execute(() -> mDatabase.runInTransaction(() -> {
            mDatabase.bankDao().insertAll(bankEntities);
            mDatabase.promoDao().insertAll(promoEntities);
            mDatabase.taskDao().insertAll(taskEntities);
        }));
    }

    public void addPromosManual(List<PromoEntity> promoEntities, List<TaskEntity> taskEntities){
        new AppExecutors().diskIO().execute(() -> mDatabase.runInTransaction(() -> {
            mDatabase.promoDao().insertAll(promoEntities);
            mDatabase.taskDao().insertAll(taskEntities);
        }));
    }

    public void addPromoManual(PromoEntity promoEntitity, List<TaskEntity> taskEntities){
        new AppExecutors().diskIO().execute(() -> mDatabase.runInTransaction(() -> {
            mDatabase.promoDao().insert(promoEntitity);
            mDatabase.taskDao().insertAll(taskEntities);
        }));
    }

    //Trigger onchanged after backup
    public void triggerRefreshAfterBackup(){
        new AppExecutors().diskIO().execute(() -> {
            ProductEntity productEntity= mDatabase.productDao().triggerUpdate();
            if(productEntity!=null)
                mDatabase.productDao().update(productEntity);
            else {
                productEntity = new ProductEntity(1, 1, 1, "", false);
                mDatabase.productDao().insert(productEntity);
                mDatabase.productDao().delete(productEntity);
            }

            PromoEntity promoEntity= mDatabase.promoDao().triggerUpdate();
            if (promoEntity!=null)
                mDatabase.promoDao().updatePromoUserData(
                        promoEntity.getParticipationId(),
                        promoEntity.getContractSigningDate(),
                        promoEntity.getId(),
                        promoEntity.isCompleted());
        });
    }

    //WHEN SIGN IN
    public void login(){
        getProducts();
        getParticipationsFromApi();
    }

    private void getProducts(){
        ApiInterface apiInterface= ApiClient.getClient().create(ApiInterface.class);
        Call<List<ProductPojo>> call= apiInterface.getProducts();
        call.enqueue(new Callback<List<ProductPojo>>() {
            @Override
            public void onResponse(@NotNull Call<List<ProductPojo>> call, @NotNull Response<List<ProductPojo>> response) {
                if(response.code()==200 && response.body()!=null){
                    List<ProductEntity> productEntities= new ArrayList<>();
                    for (ProductPojo productPojo: response.body())
                        productEntities.add(new ProductEntity(productPojo));

                    new AppExecutors().diskIO().execute(() -> mDatabase.runInTransaction(() -> {
                        mDatabase.productDao().deleteAll();
                        mDatabase.productDao().insertAll(productEntities);

                    }));

                }
            }

            @Override
            public void onFailure(@NotNull Call<List<ProductPojo>> call, @NotNull Throwable t) {
            }
        });
    }

    private void getParticipationsFromApi() {
        ApiInterface apiInterface= ApiClient.getClient().create(ApiInterface.class);
        Call<List<ParticipationPojo>> call= apiInterface.getParticipations();
        call.enqueue(new Callback<List<ParticipationPojo>>() {
            @Override
            public void onResponse(@NotNull Call<List<ParticipationPojo>> call, @NotNull Response<List<ParticipationPojo>> response) {
                if(response.code()==200 && response.body()!=null){
                    new AppExecutors().diskIO().execute(() -> mDatabase.runInTransaction(() -> {
                        mDatabase.promoDao().deleteUserData();
                        mDatabase.taskDao().deleteUserData();

                        for (ParticipationPojo pp: response.body()) {

                            mDatabase.promoDao().updatePromoUserData(pp.getId(), DateTypeConverter.toDate(pp.getContractSigningDate()),
                                    pp.getPromoId(), pp.isCompleted());

                            for (ParticipationTask pt : pp.getParticipationTasks())
                                mDatabase.taskDao().updateTaskUserData(
                                        DateTypeConverter.toDate(pt.getStartDate()),
                                        DateTypeConverter.toDate(pt.getEndDate()),
                                        pt.isCompleted(),
                                        pt.getActualAmount(),
                                        pp.getId(),
                                        pt.getTaskId());
                        }

                    }));


                }
            }

            @Override
            public void onFailure(@NotNull Call<List<ParticipationPojo>> call, @NotNull Throwable t) {
            }
        });
    }

    //REGISTER
    public void register(){
        addParticipationsToApi();
        addProductsToApi();
    }

    private void addProductsToApi() {
        new AppExecutors().diskIO().execute(() -> {
            for(ProductEntity productEntity:  mDatabase.productDao().getAllUltra()){
                createProductApi(productEntity);
            }
        });

    }

    private void createProductApi(ProductEntity productEntity){
        ApiInterface apiInterface= ApiClient.getClient().create(ApiInterface.class);
        Call<IdPojo> call= apiInterface.createProduct(new ProductPojo(productEntity));
        call.enqueue( new Callback<IdPojo>() {
            @Override
            public void onResponse(@NotNull Call<IdPojo> call, @NotNull Response<IdPojo> response) {
                if(response.code()==201){
                    if (response.body()!=null){
                        //set id
                        productEntity.setId(response.body().getId());
                        new AppExecutors().diskIO().execute(() -> mDatabase.productDao().update(productEntity));
                    }

                }

            }

            @Override
            public void onFailure(@NotNull Call<IdPojo> call, @NotNull Throwable t) {
            }
        });
    }

    private void addParticipationsToApi(){
        new AppExecutors().diskIO().execute(() -> {
            for(PromoEntity promoEntity:  mDatabase.promoDao().getParticipatePromosUltra()){
                addParticipationApi(promoEntity);
            }
        });

    }

    private void addParticipationApi(PromoEntity promoEntity){
        ApiInterface apiInterface= ApiClient.getClient().create(ApiInterface.class);

        Call<IdPojo> call= apiInterface.addParticipation(new addParticipationPojo(promoEntity.getId(),
                DateTypeConverter.toLong(promoEntity.getContractSigningDate()), promoEntity.isCompleted()));
        call.enqueue(new Callback<IdPojo>() {
            @Override
            public void onResponse(@NotNull Call<IdPojo> call, @NotNull Response<IdPojo> response) {
                if(response.code()== 201 && response.body()!=null){

                    new AppExecutors().diskIO().execute(() ->{
                        mDatabase.promoDao().updatePromoUserData(response.body().getId(), promoEntity.getContractSigningDate(),
                                promoEntity.getId(), promoEntity.isCompleted());

                        for (TaskEntity taskEntity: mDatabase.taskDao().getPromoTasksUltra(promoEntity.getId())){
                            taskEntity.setParticipationId(response.body().getId());
                            updateTaskOnApi(taskEntity);
                        }
                    });
                }
            }

            @Override
            public void onFailure(@NotNull Call<IdPojo> call, @NotNull Throwable t) {
            }
        });

    }

    private void updateTaskOnApi(TaskEntity taskEntity){

        ApiInterface apiInterface= ApiClient.getClient().create(ApiInterface.class);
        ParticipationTask participationTask= new ParticipationTask(
                taskEntity.isCompleted(),
                taskEntity.getActualAmount(),
                taskEntity.getId(),
                taskEntity.getParticipationId());

        if(taskEntity.getUserStartDate()!=null)
            participationTask.setStartDate(DateTypeConverter.toLong(taskEntity.getUserStartDate()));
        if(taskEntity.getUserEndDate()!=null)
            participationTask.setEndDate(DateTypeConverter.toLong(taskEntity.getUserEndDate()));

        Call<Void> call= apiInterface.updateParticipationTask(participationTask);
        call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                    if (response.code()==200)
                        new AppExecutors().diskIO().execute(() ->   mDatabase.taskDao().updateTaskUserData(
                                taskEntity.getStartDate(),
                                taskEntity.getUserEndDate(),
                                taskEntity.isCompleted(),
                                taskEntity.getActualAmount(),
                                taskEntity.getParticipationId(),
                                taskEntity.getId()
                        ));

                }

                @Override
                public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {

                }
            });

    }

    //timestamp handle
    public TimestampEntity getTimestamp(){
        return mDatabase.timestampDao().getTimestamp();
    }

    public void setTimestamp(long timestamp){
        new AppExecutors().diskIO().execute(() ->
                mDatabase.timestampDao().insert(new TimestampEntity(1, timestamp)));

    }

    //singleton assurance
    public static UltraRepository getInstance(TBRoomDatabase database){
        if(instance == null){
            synchronized (UltraRepository.class){
                if(instance == null)
                    instance= new UltraRepository(database);
            }
        }
        return instance;
    }
}
