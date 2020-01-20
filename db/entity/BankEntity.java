package pl.rozbijbank.db.entity;


import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import pl.rozbijbank.db.model.Bank;

@Entity(tableName = "table_banks",
        indices = {@Index(value = "id", unique = true)})
public class BankEntity extends BasicEntity implements Bank {

    private String title;

    //its just 4 me
    private boolean active;

    //constructors
    public BankEntity() {
    }

    @Ignore
    public BankEntity(String title, long id, boolean active) {
        super(id);
        this.title = title;
        this.active= active;
    }

    @Ignore
    public BankEntity(long id, String title) {
        super(id);
        this.title = title;
    }

    //setter getter
    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void setTitle(String mName) {
        this.title = mName;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
