package pl.rozbijbank.db.entity;


import java.util.Date;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import pl.rozbijbank.db.converter.DateTypeConverter;
import pl.rozbijbank.db.model.Product;
import pl.rozbijbank.networking.pojo.ProductPojo;

@Entity(tableName = "table_products",
        foreignKeys = @ForeignKey(entity = BankEntity.class,
                parentColumns = "id",
                childColumns = "bank_id",
                onDelete = ForeignKey.CASCADE),
        indices = {
                @Index(value = "id", unique = true),
                @Index("bank_id")})
public class ProductEntity extends BasicEntity implements Product {

    //fields
    @ColumnInfo(name = "bank_id")
    private long bankId;
    @ColumnInfo(name = "product_type_id")
    private int productType;
    private String title;
    private String description;
    private boolean inactive;
    @ColumnInfo(name = "start_date")
    private Date startDate;
    @ColumnInfo(name = "end_date")
    private Date endDate;
    private int color;

    public ProductEntity() {
    }

    @Ignore
    public ProductEntity(ProductPojo productPojo) {
        super(productPojo.getId());
        this.bankId = productPojo.getBankId();
        this.productType = productPojo.getProductType();
        this.title = productPojo.getTitle();
        this.description = productPojo.getDescription();
        this.inactive = productPojo.isInactive();
        this.startDate = DateTypeConverter.toDate(productPojo.getStartDate());
        this.endDate = DateTypeConverter.toDate(productPojo.getEndDate());
        this.color = productPojo.getColor();
    }

    @Ignore
    public ProductEntity(long id, long bankId, int productType, String title, boolean inactive) {
        super(id);
        this.bankId = bankId;
        this.productType = productType;
        this.title = title;
        this.inactive=inactive;
    }

    @Ignore
    public ProductEntity(long bankId, int productType, String title, String description, boolean inactive, Date startDate, Date endDate, int color) {
        this.bankId = bankId;
        this.productType = productType;
        this.title = title;
        this.description = description;
        this.inactive = inactive;
        this.startDate = startDate;
        this.endDate = endDate;
        this.color = color;
    }

    @Ignore
    public ProductEntity(long bankId, int productType, String title, boolean inactive) {
        this.bankId = bankId;
        this.productType = productType;
        this.title = title;
        this.inactive=inactive;
    }

    @Override
    public long getBankId() {
        return bankId;
    }

    @Override
    public int getProductType() {
        return productType;
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
    public boolean isInactive() {
        return inactive;
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
    public int getColor() {
        return color;
    }

    public void setBankId(long bankId) {
        this.bankId = bankId;
    }

    public void setProductType(int productType) {
        this.productType = productType;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setInactive(boolean inactive) {
        this.inactive = inactive;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
