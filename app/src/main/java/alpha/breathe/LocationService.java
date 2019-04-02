package alpha.breathe;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.Calendar;
import java.util.Date;

import androidx.annotation.Nullable;

public class LocationService extends Service {

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    public String latitude;
    public String longitude;
    public Date timeStamp;

    public String getLatitude(){
        return latitude;
    }
    public String getLongitude(){
        return longitude;
    }
    public Date getTime(){
        return timeStamp;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.e("SERVICE", "STARTED LOCATION SERVICE");

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.e("SERVICE", "GOT RESULT");

                if (locationResult == null) {
                    Log.e("SERVICE", "RESULT NULL");

                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    Log.e("SERVICE", "RESULT NOT NULL");

                    latitude = location.getLatitude()+"";
                    longitude = location.getLongitude()+"";
                    timeStamp = Calendar.getInstance().getTime();

                    Intent in = new Intent();
                    in.putExtra("lat",latitude);
                    in.putExtra("lng",longitude);
                    in.putExtra("timeStamp",timeStamp.toString());
                    in.putExtra("MYPROVIDER",location.getProvider());
                    in.putExtra("locationObject",location);

                    in.setAction("LOCATION");
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(in);

                }
            }

        };

        createLocationRequest();

        return START_REDELIVER_INTENT;


    }

    public class ServiceBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    protected void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,mLocationCallback,null);


    }

}
