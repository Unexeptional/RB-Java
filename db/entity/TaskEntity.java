package pl.rozbijbank.db.entity;

import java.util.Date;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import pl.rozbijbank.db.converter.DateTypeConverter;
import pl.rozbijbank.db.model.Task;
import pl.rozbijbank.networking.pojo.TaskPojo;
import pl.rozbijbank.networking.pojo.participation.ParticipationTask;

@Entity(tableName = "table_tasks",
        foreignKeys = @ForeignKey(entity = PromoEntity.class,
                parentColumns = "id",
                childColumns = "promo_id",
                onDelete = ForeignKey.CASCADE),
        indices = {
                @Index("bank_id"),
                @Index("promo_id"),
                @Index(value = "id", unique = true)})   //helping to search;

public class TaskEntity extends BasicEntity implements Task {

    //U CANT CHANGE THAT
    @ColumnInfo(name = "promo_id")
    private long promoId;
    @ColumnInfo(name = "bank_id")
    private long bankId;
    //HEAD
    @ColumnInfo(name = "start_date")
    private Date startDate;
    @ColumnInfo(name = "end_date")
    private Date endDate;
    private String title;
    private String description;
    private String note;
    @ColumnInfo(name = "task_type")
    private short taskType;
    @ColumnInfo(name = "month_after_signing")
    private short monthAfterSigning;
    @ColumnInfo(name = "days_after_signing")
    private int daysAfterSigning;
    private double amount;
    private String uri;

    //USER DATA
    @ColumnInfo(name = "participation_id")
    private long participationId;
    @ColumnInfo(name = "user_start_date")
    private Date userStartDate;
    @ColumnInfo(name = "user_end_date")
    private Date userEndDate;
    private boolean completed;
    private double actualAmount;

    //constructors

    public TaskEntity() {
    }

    //only for header(NO USER DATA!)
    @Ignore
    public TaskEntity(TaskPojo taskPojo) {
        super(taskPojo.getId());
        this.promoId = taskPojo.getPromoId();
        this.bankId = taskPojo.getBankId();
        this.startDate = DateTypeConverter.toDate(taskPojo.getStartDate());
        this.endDate = DateTypeConverter.toDate(taskPojo.getEndDate());
        this.title = taskPojo.getTitle();
        this.description = taskPojo.getDescription();
        this.note = taskPojo.getNote();
        this.taskType = taskPojo.getTaskType();
        this.monthAfterSigning = taskPojo.getMonthAfterSigning();
        this.daysAfterSigning = taskPojo.getDaysAfterSigning();
        this.amount = taskPojo.getAmount();
        this.uri= taskPojo.getUri();
    }

    //JUST USER DATA NO HEAD
    @Ignore
    public TaskEntity(ParticipationTask participationTask) {
        super(participationTask.getTaskId());
        this.completed= participationTask.isCompleted();
        this.actualAmount = participationTask.getActualAmount();
        this.participationId= participationTask.getParticipationId();
        this.userStartDate= DateTypeConverter.toDate(participationTask.getStartDate());
        this.userEndDate= DateTypeConverter.toDate(participationTask.getEndDate());
    }

    //must include ALL DATA!!
    @Ignore
    public TaskEntity(Task task) {
        super(task.getId());
        this.promoId= task.getPromoId();
        this.bankId= task.getBankId();
        this.startDate= task.getStartDate();
        this.endDate= task.getEndDate();
        this.title= task.getTitle();
        this.description= task.getDescription();
        this.note = task.getNote();
        this.taskType= task.getTaskType();
        this.monthAfterSigning = task.getMonthAfterSigning();
        this.amount= task.getAmount();
        this.completed= task.isCompleted();
        this.actualAmount = task.getActualAmount();
        this.participationId= task.getParticipationId();
        this.userStartDate= task.getUserStartDate();
        this.userEndDate= task.getUserEndDate();
        this.uri= task.getUri();
        this.daysAfterSigning= task.getDaysAfterSigning();
    }


    //getters
    @Override
    public long getPromoId() {
        return promoId;
    }

    @Override
    public long getBankId() {
        return bankId;
    }

    @Override
    public Date getStartDate() {
        return startDate;
    }

    @Override
    public Date getEndDate() {
        return endDate;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getNote() {
        return note;
    }

    @Override
    public short getTaskType() {
        return taskType;
    }

    @Override
    public short getMonthAfterSigning() {
        return monthAfterSigning;
    }

    @Override
    public int getDaysAfterSigning() {
        return daysAfterSigning;
    }

    @Override
    public double getAmount() {
        return amount;
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }

    @Override
    public double getActualAmount() {
        return actualAmount;
    }

    @Override
    public long getParticipationId() {
        return participationId;
    }

    @Override
    public Date getUserStartDate() {
        return userStartDate;
    }

    @Override
    public Date getUserEndDate() {
        return userEndDate;
    }

    @Override
    public String getUri() {
        return uri;
    }

    //setter
    public void setPromoId(long promoId) {
        this.promoId = promoId;
    }

    public void setBankId(long bankId) {
        this.bankId = bankId;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setTaskType(short taskType) {
        this.taskType = taskType;
    }

    public void setMonthAfterSigning(short monthAfterSigning) {
        this.monthAfterSigning = monthAfterSigning;
    }

    public void setDaysAfterSigning(int daysAfterSigning) {
        this.daysAfterSigning = daysAfterSigning;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void setActualAmount(double actualAmount) {
        this.actualAmount = actualAmount;
    }

    public void setParticipationId(long participationId) {
        this.participationId = participationId;
    }

    public void setUserStartDate(Date userStartDate) {
        this.userStartDate = userStartDate;
    }

    public void setUserEndDate(Date userEndDate) {
        this.userEndDate = userEndDate;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
