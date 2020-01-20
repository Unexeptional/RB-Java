package pl.rozbijbank.db;

import android.content.Context;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import pl.rozbijbank.db.converter.DateTypeConverter;
import pl.rozbijbank.db.converter.UriTypeConverter;
import pl.rozbijbank.db.dao.BankDao;
import pl.rozbijbank.db.dao.ProductDao;
import pl.rozbijbank.db.dao.PromoDao;
import pl.rozbijbank.db.dao.TaskDao;
import pl.rozbijbank.db.dao.TimestampDao;
import pl.rozbijbank.db.dao.UserDao;
import pl.rozbijbank.db.entity.BankEntity;
import pl.rozbijbank.db.entity.ProductEntity;
import pl.rozbijbank.db.entity.PromoEntity;
import pl.rozbijbank.db.entity.TaskEntity;
import pl.rozbijbank.db.entity.TimestampEntity;
import pl.rozbijbank.db.entity.UserEntity;
import pl.rozbijbank.other.AppExecutors;

@Database(entities = {
        BankEntity.class,
        PromoEntity.class,
        ProductEntity.class,
        TaskEntity.class,
        UserEntity.class,
        TimestampEntity.class
},

        version = 2)
@TypeConverters({
        DateTypeConverter.class,
        UriTypeConverter.class
})
public abstract class TBRoomDatabase extends RoomDatabase {

    private static volatile TBRoomDatabase INSTANCE;
    private final MutableLiveData<Boolean> mIsDatabaseCreated = new MutableLiveData<>();


    public static final String DATABASE_NAME = "tb_database";


    public abstract BankDao bankDao();
    public abstract PromoDao promoDao();
    public abstract ProductDao productDao();
    public abstract TaskDao taskDao();
    public abstract UserDao userDao();
    public abstract TimestampDao timestampDao();


    public static TBRoomDatabase getInstance(final Context context, final AppExecutors executors){
        if(INSTANCE== null){
            synchronized (TBRoomDatabase.class){
                if(INSTANCE== null){

                    INSTANCE = buildDatabase(context.getApplicationContext(), executors);
                    INSTANCE.updateDatabaseCreated(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }


    /**
     * Build the database. {@link Builder#build()} only sets up the database configuration and
     * creates a new instance of the database.
     * The SQLite database is only created when it's accessed for the first time.
     */
    private static TBRoomDatabase buildDatabase(final Context appContext, final AppExecutors executors) {
        return Room.databaseBuilder(appContext, TBRoomDatabase.class, DATABASE_NAME)
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        executors.diskIO().execute(() -> {

                            // Generate the data for pre-population
                            TBRoomDatabase database = TBRoomDatabase.getInstance(appContext, executors);

                            database.setDatabaseCreated();
                        });
                    }
                })
                .setJournalMode(JournalMode.TRUNCATE)
                .addMigrations(MIGRATION_1_2)
                .build();
    }

    /**
     * Check whether the database already exists and expose it via {@link #getDatabaseCreated()}
     */
    private void updateDatabaseCreated(final Context context) {
        if (context.getDatabasePath(DATABASE_NAME).exists()) {
            setDatabaseCreated();
        }
    }

    public LiveData<Boolean> getDatabaseCreated() {
        return mIsDatabaseCreated;
    }

    private void setDatabaseCreated(){
        mIsDatabaseCreated.postValue(true);
    }


    //MIGRATIONS
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE `table_timestamp` (`timestamp` INTEGER NOT NULL, "
                    + "`id` INTEGER NOT NULL, PRIMARY KEY(`id`))");
            //indexing
            //bank
            database.execSQL("CREATE UNIQUE INDEX index_table_banks_id ON  table_banks(id)");
            //promo
            database.execSQL("CREATE UNIQUE INDEX index_table_promos_id ON  table_promos(id)");
            database.execSQL("CREATE INDEX index_table_promos_participation_id ON  table_promos(participation_id)");
            database.execSQL("DROP INDEX index_table_promos_title");
            //product
            database.execSQL("DROP INDEX index_table_products_title");
            database.execSQL("CREATE UNIQUE INDEX index_table_products_id ON  table_products(id)");
            //task
            database.execSQL("DROP INDEX index_table_tasks_title");
            database.execSQL("CREATE UNIQUE INDEX index_table_tasks_id ON  table_tasks(id)");
            database.execSQL("CREATE INDEX index_table_tasks_bank_id ON  table_tasks(bank_id)");
        }
    };
}
