package alpha.breathe;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;


public class AutoUpdateService extends Service {

    public static final String ACTION_LOCATION_BROADCAST = AutoUpdateService.class.getName() + "LocationBroadcast";
    private LocationManager locationManager;
    private LocationManager networkLocationManager;
    private LocationListener locationListener;
    private LocationListener netLocationListener;
    String lat;
    String lng;
    String netLat;
    String netLng;
    String provider = "Unknown";

    int gpsFlag;

    int locationPeriod = 60 * 1000;

    public static final String CHANNEL_ID = "autoLocationNotify";
    private static final String TAG = "AutoUpdateService";
    private static Timer timer1;
    public static boolean runFlag;

    private void sendBroadcastMessage(String lat, String lng, Date time) {
            Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
            intent.putExtra("lat", lat);
            intent.putExtra("lng", lng);
            intent.putExtra("time", time.toString());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Created Auto Update Service ");
    }

    String token;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        createNotificationChannel();
        Log.e(TAG, "Starting Location Service. AutoUpdateService.");

        String title = intent.getStringExtra("notificationExtraTitle");
        token = intent.getStringExtra("userToken");
        //locationPeriod = intent.getIntExtra("interval",60000);
        //timeoutPeriod = locationPeriod + 10 * 1000;

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        networkLocationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.e(TAG, "GPS Location Changed");
                lat = location.getLatitude() + "";
                lng = location.getLongitude() + "";
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d("Location", "StatusChanged");
            }
            @Override
            public void onProviderEnabled(String provider) {
                Log.d("Location", "Prov Enabled");
            }
            @Override
            public void onProviderDisabled(String provider) {
                Log.d("Location", "Prov Disabled");
            }
        };

        netLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.e(TAG, "Net Location Changed");
                netLat = location.getLatitude() + "";
                netLng = location.getLongitude() + "";
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d("Location", "StatusChanged");
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d("Location", "Prov Enabled");
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d("Location", "Prov Disabled");
            }
        };

        Intent openAppIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, openAppIntent, 0);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return START_STICKY;
        }

        networkLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,locationPeriod,0,netLocationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,locationPeriod,0,locationListener);
        timer1=new Timer();
        timer1.scheduleAtFixedRate(new getLastLocation(),0, locationPeriod);
        runFlag = true;

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(String.format(Locale.getDefault(), getResources().getString(R.string.notification_text)))
                .setContentTitle(String.format(Locale.getDefault(), getResources().getString(R.string.notification_title)))
                .setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);
        return START_REDELIVER_INTENT; //Set different if service gets killed.
    }

    class getLastLocation extends TimerTask {
        @Override
        public void run() {
            Log.d("Auto Update Timer","Auto Update Timer Triggered");
            if (lat != null && lng != null){
                provider = "GPS";
                gpsFlag = 1;
                Log.e("Auto Update Timer","Timer Using GPS. Lat Lng not null");
                Date currentTime = Calendar.getInstance().getTime();
                sendLocation(lat,lng,currentTime,token);

                lat = null;
                lng = null;
                netLat = null;
                netLng = null;

            }

            else{
                Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (gpsLocation != null && gpsFlag == 1){
                    provider = "GPS";
                    lat = gpsLocation.getLatitude()+"";
                    lng = gpsLocation.getLongitude()+"";
                    Date currentTime = Calendar.getInstance().getTime();
                    sendBroadcastMessage(lat,lng,currentTime);
                    sendLocation(lat,lng,currentTime,token);

                    Log.e(TAG,"Using Previous GPS");
                    netLat = null;
                    netLng = null;
                    lat = null;
                    lng = null;

                }

                else if(netLat != null && netLng != null){
                provider = "Network";
                gpsFlag = 0;
                Log.e("Auto Update Timer","Timer Using Network. netLat netLng not null");
                Date currentTime = Calendar.getInstance().getTime();
                sendBroadcastMessage(lat,lng,currentTime);

                sendLocation(netLat,netLng,currentTime,token);

                netLat = null;
                netLng = null;
                lat = null;
                lng = null;

      }
                else{
                    Log.d(TAG,"Neither Net or GPS or previous locations.");
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Stopping Location Sharing Service. Called Destroy");

        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
            runFlag = false;
            Log.d(TAG, "Removed Location Listener");
        }

        if (networkLocationManager != null) {
            networkLocationManager.removeUpdates(netLocationListener);
            runFlag = false;
            Log.d(TAG, "Removed Net Location Listener");
        }

        timer1.cancel();

        stopSelf();
        Log.e(TAG, "Stop Self Called.");

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Breathe Notification",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
        Log.e(TAG, "Bound Notification for Location Service.");

    }


    private void sendLocation(String lat, String lng, Date timeStamp, String token){
        DatabaseService dbService = new DatabaseService();
        dbService.addToQueueDatabase(AutoUpdateService.this,token,lat,lng,timeStamp,null,null);

    }
}
