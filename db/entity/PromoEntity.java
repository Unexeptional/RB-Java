package pl.rozbijbank.db.entity;

import java.util.Date;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import pl.rozbijbank.db.converter.DateTypeConverter;
import pl.rozbijbank.db.model.Promo;
import pl.rozbijbank.networking.pojo.PromoPojo;
import pl.rozbijbank.networking.pojo.PromoTasksPojo;

@Entity(tableName = "table_promos",
        foreignKeys = @ForeignKey(entity = BankEntity.class,
                parentColumns = "id",
                childColumns = "bank_id",
                onDelete = ForeignKey.CASCADE),
        indices = {
                @Index("bank_id"),
                @Index("participation_id"),
                @Index(value = "id", unique = true)})   //helping to search;

public class PromoEntity extends BasicEntity implements Promo {

    //u don't change that
    @ColumnInfo(name = "bank_id")
    private long bankId;
    //head
    private String title;
    private String description;
    @ColumnInfo(name = "grace_period")
    private String gracePeriod;
    private String warning;
    @ColumnInfo(name = "start_date")
    private Date startDate;
    @ColumnInfo(name = "end_date")
    private Date endDate;
    @ColumnInfo(name = "banner")
    private String banner;
    @ColumnInfo(name = "uri")
    private String uri;
    @ColumnInfo(name = "regulations_uri")
    private String regulationsUri;
    private int points;

    //user data
    @ColumnInfo(name = "participation_id")
    private long participationId;
    @ColumnInfo(name = "contract_signing_date")
    private Date contractSigningDate;
    private boolean completed;


    //constructor


    public PromoEntity() {
    }

    //JUST HEAD NO USER DATA
    @Ignore
    public PromoEntity(PromoTasksPojo promoPojo) {
        super(promoPojo.getId());
        this.bankId = promoPojo.getBankId();
        this.title = promoPojo.getTitle();
        this.description = promoPojo.getDescription();
        this.gracePeriod =  promoPojo.getGracePeroid();
        this.warning =  promoPojo.getWarning();
        this.startDate = DateTypeConverter.toDate(promoPojo.getStartDate());
        this.endDate =  DateTypeConverter.toDate(promoPojo.getEndDate());
        this.banner =  promoPojo.getBanner();
        this.uri =  promoPojo.getUri();
        this.regulationsUri =  promoPojo.getRegulationsUri();
        this.points=  promoPojo.getPoints();
    }

    @Override
    public long getBankId() {
        return bankId;
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
    public String getGracePeriod() {
        return gracePeriod;
    }

    @Override
    public String getWarning() {
        return warning;
    }

    @Override
    public long getParticipationId() {
        return participationId;
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
    public Date getContractSigningDate() {
        return contractSigningDate;
    }

    @Override
    public String getBanner() {
        return banner;
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public String getRegulationsUri() {
        return regulationsUri;
    }

    @Override
    public int getPoints() {
        return points;
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }


    //setters
    public void setBankId(long bankId) {
        this.bankId = bankId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setGracePeriod(String gracePeriod) {
        this.gracePeriod = gracePeriod;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }

    public void setParticipationId(long participationId) {
        this.participationId = participationId;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setContractSigningDate(Date contractSigningDate) {
        this.contractSigningDate = contractSigningDate;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setRegulationsUri(String regulationsUri) {
        this.regulationsUri = regulationsUri;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
