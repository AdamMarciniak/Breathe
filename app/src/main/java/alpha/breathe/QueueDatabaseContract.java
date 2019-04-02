package alpha.breathe;

import android.provider.BaseColumns;

public class QueueDatabaseContract {

    private QueueDatabaseContract(){

    }

    public static final class QueueCheckinEntry implements BaseColumns{
        public static final String TABLE_NAME = "locationList";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_TOKEN = "token";
        public static final String COLUMN_LAT = "latitude";
        public static final String COLUMN_LNG = "longitude";
        public static final String COLUMN_MSG = "message";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_TIME = "time";
        public static final String COLUMN_IMAGEPATH = "imagePath";
        public static final String COLUMN_SENDING = "sending";


    }
}
