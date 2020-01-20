package pl.rozbijbank.db.entity;

import androidx.room.Entity;
import androidx.room.Ignore;

@Entity(tableName = "table_timestamp")
public class TimestampEntity extends BasicEntity {
    private long timestamp;

    public TimestampEntity() {
    }

    @Ignore
    public TimestampEntity(long id, long timestamp) {
        super(id);
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
