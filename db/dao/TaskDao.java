package pl.rozbijbank.db.dao;

import java.util.Date;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import pl.rozbijbank.db.entity.TaskEntity;

import static androidx.room.OnConflictStrategy.IGNORE;
import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface TaskDao {

    @Insert(onConflict = IGNORE)
    void insert(TaskEntity taskEntity);

    @Insert(onConflict = IGNORE)
    void insertAll(List<TaskEntity> products);

    @Query("UPDATE table_tasks SET " +
            "user_start_date= :userStartDate, " +
            "user_end_date=:userEndDate, " +
            "completed=:completed, " +
            "actualAmount=:actualAmount, " +
            "participation_id=:participationId  " +
            "WHERE id=:id")
    void updateTaskUserData(Date userStartDate,
                            Date userEndDate,
                            boolean completed,
                            double actualAmount,
                            long participationId,
                            long id);

    @Query("UPDATE table_tasks SET " +
            "start_date= :startDate, " +
            "end_date=:endDate, " +
            "title=:title, " +
            "description=:description, " +
            "note=:note, " +
            "task_type=:taskType, " +
            "month_after_signing=:monthAfterSigning, " +
            "days_after_signing=:daysAfterSigning, " +
            "amount=:amount, " +
            "uri=:uri " +
            "WHERE id=:id")
    void updateTaskHead(Date startDate,
                        Date endDate,
                        String title,
                        String description,
                        String note,
                        short taskType,
                        short monthAfterSigning,
                        int daysAfterSigning,
                        double amount,
                        String uri,
                        long id);

    @Delete
    void delete(TaskEntity taskEntity);

    @Query("DELETE FROM table_tasks")
    void deleteAll();

    @Query("UPDATE table_tasks SET " +
            "user_start_date= null, " +
            "user_end_date=null, " +
            "completed=0, " +
            "actualAmount=0, " +
            "participation_id=0  ")
    void deleteUserData();

    @Query("select * from table_tasks where id = :taskId")
    LiveData<TaskEntity> getTask(long taskId);

    //LISTS
    @Query("SELECT * FROM table_tasks " +
            "WHERE table_tasks.promo_id IN (SELECT id FROM table_promos WHERE table_promos.participation_id!=0) " + taskOrder)
    LiveData<List<TaskEntity>> getAll();

    @Query("SELECT * FROM table_tasks WHERE table_tasks.promo_id IN (SELECT id FROM table_promos WHERE table_promos.participation_id!=0)" +
            " AND bank_id = :bankId" + taskOrder)
    LiveData<List<TaskEntity>> getBankTasks(long bankId);

    @Query("SELECT * FROM table_tasks WHERE table_tasks.promo_id IN (SELECT id FROM table_promos WHERE table_promos.participation_id!=0)" +
            " AND bank_id = :bankId AND user_end_date BETWEEN :fromDate AND :toDate" + taskOrder)
    LiveData<List<TaskEntity>> getBankDateTasks(long bankId, long fromDate, long toDate);

    @Query("SELECT * FROM table_tasks WHERE table_tasks.promo_id IN (SELECT id FROM table_promos WHERE table_promos.participation_id!=0)" +
            " AND user_end_date BETWEEN :fromDate AND :toDate" + taskOrder)
    LiveData<List<TaskEntity>> getDateTasks(long fromDate, long toDate);

    //exclusive for MyPromoActivity
    @Query("SELECT * FROM table_tasks WHERE promo_id = :promoId" + taskOrder)
    LiveData<List<TaskEntity>> getPromoTasks(long promoId);

    @Query("SELECT * FROM table_tasks WHERE promo_id = :promoId" + taskOrder)
    List<TaskEntity> getPromoTasksUltra(long promoId);

    String taskOrder= " ORDER BY completed ASC, user_end_date ASC, user_start_date ASC, month_after_signing ASC, days_after_signing ASC, end_date ASC";
}
