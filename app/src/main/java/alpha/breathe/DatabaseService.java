package alpha.breathe;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import androidx.annotation.Nullable;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class DatabaseService extends Service {

    String TAG = "DatabaseService";

    private static QueueDBHelper queueDBHelper;
    private static MainDBHelper mainDBHelper;



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void setSendingFlagOff(Context context,String id){

        Log.e(TAG, "Set Sending Flag Off");
        QueueDBHelper dbHelper = new QueueDBHelper(context);
        SQLiteDatabase mDatabase = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(QueueDatabaseContract.QueueCheckinEntry.COLUMN_SENDING,0);

        mDatabase.update(QueueDatabaseContract.QueueCheckinEntry.TABLE_NAME,cv,"id=? ",new String[]{id});
        mDatabase.close();



    }

    public void setSendingFlagOn(Context context,String id){

        Log.e(TAG, "Set Sending Flag On");
        QueueDBHelper dbHelper = new QueueDBHelper(context);
        SQLiteDatabase mDatabase = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(QueueDatabaseContract.QueueCheckinEntry.COLUMN_SENDING,1);

        mDatabase.update(QueueDatabaseContract.QueueCheckinEntry.TABLE_NAME,cv,"id=? ",new String[]{id});
        mDatabase.close();



    }

    private void addToMainDatabase(Context context,String token, String lat, String lng, Date timeStamp, String message, String imgPath){

        Log.e(TAG, "Adding to Main Database");
        MainDBHelper dbHelper = new MainDBHelper(context);
        SQLiteDatabase mDatabase = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(MainDatabaseContract.CheckinEntry.COLUMN_TOKEN,token);
        cv.put(MainDatabaseContract.CheckinEntry.COLUMN_LAT,lat);
        cv.put(MainDatabaseContract.CheckinEntry.COLUMN_LNG,lng);
        cv.put(MainDatabaseContract.CheckinEntry.COLUMN_TIME,getLocalToUTCDate(timeStamp));
        cv.put(MainDatabaseContract.CheckinEntry.COLUMN_MSG,message);
        cv.put(MainDatabaseContract.CheckinEntry.COLUMN_IMAGEPATH,imgPath);

        mDatabase.insert(QueueDatabaseContract.QueueCheckinEntry.TABLE_NAME,null,cv);
        mDatabase.close();
        Log.e(TAG, "Added to Main Database. Closed.");

        Intent intent = new Intent();
        intent.setAction("databaseReady");
        LocalBroadcastManager.getInstance(DatabaseService.this).sendBroadcast(intent);
    }

    public void addToQueueDatabase(Context context,String token, String lat, String lng, Date timeStamp, String message, String imgPath){

        addToMainDatabase(context,token,lat,lng,timeStamp,message,imgPath);

        Log.e(TAG, "Adding to Queue Database");
        QueueDBHelper dbHelper = new QueueDBHelper(context);
        SQLiteDatabase mDatabase = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(QueueDatabaseContract.QueueCheckinEntry.COLUMN_TOKEN,token);
        cv.put(QueueDatabaseContract.QueueCheckinEntry.COLUMN_LAT,lat);
        cv.put(QueueDatabaseContract.QueueCheckinEntry.COLUMN_LNG,lng);
        cv.put(QueueDatabaseContract.QueueCheckinEntry.COLUMN_TIME,getLocalToUTCDate(timeStamp));
        cv.put(QueueDatabaseContract.QueueCheckinEntry.COLUMN_MSG,message);
        cv.put(QueueDatabaseContract.QueueCheckinEntry.COLUMN_IMAGEPATH,imgPath);
        cv.put(QueueDatabaseContract.QueueCheckinEntry.COLUMN_SENDING,0);

        mDatabase.insert(QueueDatabaseContract.QueueCheckinEntry.TABLE_NAME,null,cv);
        mDatabase.close();
        Log.e(TAG, "Added to Queue Database. Closed.");

        //sendFromQueueDatabase(context);

         WorkManager mWorkManager;
         mWorkManager = WorkManager.getInstance();
         mWorkManager.enqueue(OneTimeWorkRequest.from(HttpWorker.class));



    }

    public void sendFromQueueDatabase(Context context){

        Log.e(TAG, "Sending from Queue Database");
        queueDBHelper = new QueueDBHelper(context);
        SQLiteDatabase mDatabase = queueDBHelper.getWritableDatabase();

        HttpRequestService httpService = new HttpRequestService();

        Cursor mCursor = mDatabase.query(QueueDatabaseContract.QueueCheckinEntry.TABLE_NAME,null,null,null,null,null,null);


        while (mCursor.moveToNext()) {

            int sendingFlag = mCursor.getInt(mCursor.getColumnIndex(QueueDatabaseContract.QueueCheckinEntry.COLUMN_SENDING));

            if (sendingFlag == 0){

                String lng = mCursor.getString(mCursor.getColumnIndex(QueueDatabaseContract.QueueCheckinEntry.COLUMN_LNG));
                String lat = mCursor.getString(mCursor.getColumnIndex(QueueDatabaseContract.QueueCheckinEntry.COLUMN_LAT));
                String message = mCursor.getString(mCursor.getColumnIndex(QueueDatabaseContract.QueueCheckinEntry.COLUMN_MSG));
                String token = mCursor.getString(mCursor.getColumnIndex(QueueDatabaseContract.QueueCheckinEntry.COLUMN_TOKEN));
                String time = mCursor.getString(mCursor.getColumnIndex(QueueDatabaseContract.QueueCheckinEntry.COLUMN_TIME));
                String id = mCursor.getInt(mCursor.getColumnIndex(QueueDatabaseContract.QueueCheckinEntry.COLUMN_ID))+"";
                String imagePath = mCursor.getString(mCursor.getColumnIndex(QueueDatabaseContract.QueueCheckinEntry.COLUMN_IMAGEPATH));
                setSendingFlagOn(context,id);


                httpService.sendCheckin(context,id,lat,lng,message,time,token,imagePath);
            }

        }

        mCursor.close();
        mDatabase.close();

    }

    public void transferFromQueueDatabase(Context context, String id){
        Log.e(TAG, "transferFromQueueDatabase: ");

        queueDBHelper = new QueueDBHelper(context);
        SQLiteDatabase mDatabase = queueDBHelper.getWritableDatabase();
        String queueTableName = QueueDatabaseContract.QueueCheckinEntry.TABLE_NAME;

        Cursor mCursor = mDatabase.rawQuery("SELECT * FROM " + queueTableName + " WHERE id=?", new String[]{id});

        if (mCursor != null)
        {
            Log.e(TAG, "Record Exists");
            Log.e(TAG, "Transferring ID" );

            mDatabase.delete(QueueDatabaseContract.QueueCheckinEntry.TABLE_NAME,"id=? ",new String[]{id});


        }
        else
        {
            Log.e(TAG, "Record Does not exist. Not transferring.");

            /* record not exist */
        }

        mDatabase.close();
        mCursor.close();




    }


    public void clearDatabase(Context context) {
        mainDBHelper = new MainDBHelper(context);
        SQLiteDatabase mDatabase = mainDBHelper.getWritableDatabase();
        String clearDBQuery = "DELETE FROM "+ MainDatabaseContract.CheckinEntry.TABLE_NAME;
        mDatabase.execSQL(clearDBQuery);
        mDatabase.close();

        queueDBHelper = new QueueDBHelper(context);
        SQLiteDatabase queuemDatabase = queueDBHelper.getWritableDatabase();
        String queueclearDBQuery = "DELETE FROM "+ QueueDatabaseContract.QueueCheckinEntry.TABLE_NAME;
        queuemDatabase.execSQL(queueclearDBQuery);
        queuemDatabase.close();


    }

    public ArrayList<JSONObject> broadcastDatabaseValues(Context context){

        mainDBHelper= new MainDBHelper(context);
        SQLiteDatabase mDatabase = mainDBHelper.getWritableDatabase();

        ArrayList<JSONObject> jsonArray = new ArrayList<>();

        Cursor mCursor = mDatabase.query(MainDatabaseContract.CheckinEntry.TABLE_NAME,null,null,null,null,null,null);
        while (mCursor.moveToNext()){
            JSONObject entry = new JSONObject();
            String lng = mCursor.getString(mCursor.getColumnIndex(MainDatabaseContract.CheckinEntry.COLUMN_LNG));
            String lat = mCursor.getString(mCursor.getColumnIndex(MainDatabaseContract.CheckinEntry.COLUMN_LAT));
            String message = mCursor.getString(mCursor.getColumnIndex(MainDatabaseContract.CheckinEntry.COLUMN_MSG));
            String time = mCursor.getString(mCursor.getColumnIndex(MainDatabaseContract.CheckinEntry.COLUMN_TIME));
            String imagePath = mCursor.getString(mCursor.getColumnIndex(MainDatabaseContract.CheckinEntry.COLUMN_IMAGEPATH));
            Log.e(TAG, "MESSAGE FROM DATABASE:  " + message );

            try {
                entry.put("lng", lng);
                entry.put("lat", lat);
                entry.put("message", message);
                entry.put("time", time);
                entry.put("imagePath", imagePath);


            }catch(JSONException e){
                e.printStackTrace();
            }

            jsonArray.add(entry);

        }
        mCursor.close();
        mDatabase.close();

        return jsonArray;



    }

    public String getLocalToUTCDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date time = calendar.getTime();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat outputFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        outputFmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        return outputFmt.format(time);
    }

}
