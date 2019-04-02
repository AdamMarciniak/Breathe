package alpha.breathe;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import alpha.breathe.QueueDatabaseContract.*;

public class QueueDBHelper extends SQLiteOpenHelper {


    @Override
    public SQLiteDatabase getWritableDatabase() {
        return super.getWritableDatabase();
    }

    public static final String DATABASE_NAME = "queueDatabase6.db";
    public static final int DATABASE_VERSION = 3;

    public QueueDBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.e("DB", "onCreate of DB ");
        final String SQL_CREATE_QUEUE_DB_TABLE = "CREATE TABLE " +
                QueueCheckinEntry.TABLE_NAME + " (" +
                QueueCheckinEntry.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                QueueCheckinEntry.COLUMN_TOKEN + " TEXT NOT NULL, " +
                QueueCheckinEntry.COLUMN_LAT + " TEXT NOT NULL, " +
                QueueCheckinEntry.COLUMN_LNG + " TEXT NOT NULL, " +
                QueueCheckinEntry.COLUMN_MSG + " TEXT , " +
                QueueCheckinEntry.COLUMN_TIME + " TEXT NOT NULL, " +
                QueueCheckinEntry.COLUMN_SENDING + " INTEGER NOT NULL, " +
                QueueCheckinEntry.COLUMN_IMAGEPATH + " TEXT , " +
                QueueCheckinEntry.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP " +
                ");";

        db.execSQL(SQL_CREATE_QUEUE_DB_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e("DB", "onUpgrade of DB ");

        db.execSQL("DROP TABLE IF EXISTS " + QueueCheckinEntry.TABLE_NAME);
        onCreate(db);
    }
}
