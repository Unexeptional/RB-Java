package pl.rozbijbank.db.converter;

import android.net.Uri;

import androidx.room.TypeConverter;

public class UriTypeConverter {

    @TypeConverter
    public static Uri toUri(String value) {return value == null ? null : Uri.parse(value);}

    @TypeConverter
    public static String toString(Uri uri){ return uri == null ? null : uri.toString();}

}