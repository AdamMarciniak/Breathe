package alpha.breathe;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import alpha.breathe.MainDatabaseContract.*;

public class MainDBHelper extends SQLiteOpenHelper {


    @Override
    public SQLiteDatabase getWritableDatabase() {
        return super.getWritableDatabase();
    }

    public static final String DATABASE_NAME = "mainDatabase6.db";
    public static final int DATABASE_VERSION = 3;

    public MainDBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.e("DB", "onCreate of DB ");
        final String SQL_CREATE_MAIN_DB_TABLE = "CREATE TABLE " +
                CheckinEntry.TABLE_NAME + " (" +
                CheckinEntry.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CheckinEntry.COLUMN_TOKEN + " TEXT NOT NULL, " +
                CheckinEntry.COLUMN_LAT + " TEXT NOT NULL, " +
                CheckinEntry.COLUMN_LNG + " TEXT NOT NULL, " +
                CheckinEntry.COLUMN_MSG + " TEXT , " +
                CheckinEntry.COLUMN_TIME + " TEXT NOT NULL, " +
                CheckinEntry.COLUMN_IMAGEPATH + " TEXT , " +
                CheckinEntry.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP " +
                ");";


        db.execSQL(SQL_CREATE_MAIN_DB_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e("DB", "onUpgrade of DB ");

        db.execSQL("DROP TABLE IF EXISTS " + CheckinEntry.TABLE_NAME);
        onCreate(db);
    }
}
