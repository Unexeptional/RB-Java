package pl.rozbijbank.db.converter;

import java.util.Date;

import androidx.room.TypeConverter;

public class DateTypeConverter {

    @TypeConverter
    public static Date toDate(Long value) {
        if(value!=null)
            return value == 0 ? null : new Date(value);
        else
            return null;
    }

    @TypeConverter
    public static Long toLong(Date value) {
        if(value!=null)
            return value.getTime();
        else
            return 0L;
    }
}