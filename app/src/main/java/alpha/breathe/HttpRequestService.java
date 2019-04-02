package alpha.breathe;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpRequestService extends Service {

    String messageUrl = App.getResourses().getString(R.string.messageUrl);

    private static final String TAG = "HttpRequestService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "Created HttpService");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "Started Http Service");

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"Destroyed HTTP Service");
        super.onDestroy();

    }

    private void sendBroadcastMessage(String response) {
        Intent intent = new Intent();
        intent.setAction("httpThing");
        intent.putExtra("response", response);
        LocalBroadcastManager.getInstance(HttpRequestService.this).sendBroadcast(intent);
    }

    private void sendToast(String toastMessage) {
        Intent intent = new Intent();
        intent.setAction("toastBroadcastReceiver");
        intent.putExtra("toastMessage", toastMessage);
        LocalBroadcastManager.getInstance(HttpRequestService.this).sendBroadcast(intent);
    }


    public void sendCheckin(Context context,String id,String lat, String lng, String message,String time,String userToken, String imagePath){

        Log.e(TAG, "sendCheckin Called");

        Callback sendCheckinCallback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "SEND MESSAGE onFailure() Request was: " + call + "Failed sending message");
                sendBroadcastMessage("Sending Message Failed. Saved locally. Will be sent when internet is connected.");
                sendToast("Failed to Send");
                DatabaseService dbService = new DatabaseService();
                dbService.setSendingFlagOff(context,id);

            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String responseBody = response.body().string();
                int responseCode = response.code();

                Log.e(TAG, "SEND MESSAGE onResponse() AUTH CALLBACK Response was: " + responseBody );
                sendBroadcastMessage("Response to Send Message Request: " +responseBody);

                sendToast("Response code: "+ responseCode);

                if (responseCode < 400){

                    DatabaseService dbService = new DatabaseService();
                    dbService.setSendingFlagOff(context,id);
                    dbService.transferFromQueueDatabase(context,id);
                }

            }
        };

        sendCheckinHttp(lat,lng,message,time,userToken,imagePath, sendCheckinCallback);

    }

    private void sendCheckinHttp(String latitude, String longitude, String message,String timeStamp, String userToken,String imagePath, Callback callback) {

        final OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .addNetworkInterceptor(new StethoInterceptor())
                .build();
        Log.d(TAG, "sendCheckinHttp Called" );

        Request request;

        if (imagePath!= null){
            File imageFile = new File(imagePath);

            Log.d(TAG, "Image Check In" );

            String imageName = userToken + timeStamp + "_IMG" + ".jpg";

            MultipartBody multiBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("attachment",imageName,RequestBody.create(
                            MediaType.parse("image/jpeg"), imageFile))
                    .addFormDataPart("lat",latitude)
                    .addFormDataPart("lng",longitude)
                    .addFormDataPart("timestamp",timeStamp)
                    .addFormDataPart("token",userToken)
                    .build();
            Log.d(TAG, "Built Multipart Body" );

            request = new Request.Builder()
                    .url(messageUrl)
                    .post(multiBody)
                    .build();
            Log.d(TAG, "Built Request" );


        }else if (message!= null){
            Log.d(TAG, "Message Checkin" );

            RequestBody formBody = new FormBody.Builder()
                    .add("lat",latitude)
                    .add("lng",longitude)
                    .add("note",message)
                    .add("timestamp", timeStamp)
                    .add("token", userToken)

                    .build();
            Log.d(TAG, "Built formBody" );


            request = new Request.Builder()
                    .url(messageUrl)
                    .post(formBody)
                    .build();
            Log.d(TAG, "Built Request" );


        }else{
            Log.d(TAG, "Location Checkin" );

            RequestBody formBody = new FormBody.Builder()
                    .add("lat",latitude)
                    .add("lng",longitude)
                    .add("timestamp", timeStamp)
                    .add("token", userToken)

                    .build();
            Log.d(TAG, "Built formBody" );


            request = new Request.Builder()
                    .url(messageUrl)
                    .post(formBody)
                    .build();
            Log.d(TAG, "Built Request" );
        }

        Call call = client.newCall(request);
        call.enqueue(callback);
        Log.d(TAG, "Request Called." );

    }


    public void sendDelete(String userToken){
        Log.d("key","senddelete activated");

        Callback sendDeleteCallback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure() Request was: " + call + "Failed sending message");
                sendToast("On Failure");


            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String authResponse = response.body().string();
                Log.d(TAG, "onResponse() AUTH CALLBACK Response was: " + authResponse );
                sendBroadcastMessage("Response to Request Command: " + authResponse);
                sendToast(authResponse);

            }
        };

        sendDeleteHttp(userToken, sendDeleteCallback);

    }


    private void sendDeleteHttp(String userToken, Callback callback) {

        final OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .addNetworkInterceptor(new StethoInterceptor())
                .build();
        String url = App.getResourses().getString(R.string.deleteURL);
        Log.d(TAG,"Sending Delete Command to Server with token: " + userToken);

        RequestBody formBody = new FormBody.Builder()
                .add("token", userToken)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        Call call = client.newCall(request);

        call.enqueue(callback);
        Log.d(TAG, "Called Delete Request." );

    }




}
