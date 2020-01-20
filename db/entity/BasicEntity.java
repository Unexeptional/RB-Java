package pl.rozbijbank.db.entity;

import androidx.room.PrimaryKey;

abstract class BasicEntity {

    //fields
    @PrimaryKey(autoGenerate = true)
    private long id;

    BasicEntity() {
    }

    BasicEntity(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
