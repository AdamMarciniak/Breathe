package alpha.breathe;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.androidnetworking.AndroidNetworking;
import com.crashlytics.android.Crashlytics;
import com.facebook.stetho.Stetho;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.gson.JsonObject;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import com.karumi.dexter.listener.single.PermissionListener;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;

import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import androidx.exifinterface.media.ExifInterface;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.fabric.sdk.android.Fabric;
import io.fotoapparat.Fotoapparat;
import io.fotoapparat.view.CameraView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    static int[][] noteStates;
    static int[] noteButtonColorStates;

    private MapView mapView;

    String TAG = "Main Activity";

    @BindString(R.string.authGoogleUrl) String AUTH_GOOGLE_URL;
    @BindString(R.string.authWhoAmIUrl) String AUTH_WHOAMI_URL;
    @BindView(R.id.id_next_button) Button next_button;
    @BindView(R.id.id_back_button) Button back_button;
    @BindView(R.id.id_auto_location_image) ImageView autoLocationImage;
    @BindView(R.id.id_tutLayout) View tutView;
    @BindView(R.id.id_mainActivity) View mainView;
    @BindView(R.id.id_leave_note_image) ImageView leave_note_button;
    @BindView(R.id.id_textLogView) TextView logText;
    @BindView(R.id.id_httpText) TextView httpText;
    @BindView(R.id.id_infoTextView) TextView infoTextView;
    @BindView(R.id.id_photoView) CameraView photoView;
    @BindView(R.id.id_share_button) Button shareButton;
    @BindView(R.id.id_openCamerButton2) Button openCameraButton2;
    @BindView(R.id.id_deleteDB) Button deleteDB;
    @BindView(R.id.id_imageCardView) View imageCardView;
    @BindView(R.id.id_photoPopup) ImageView photoPopup;
    @BindView(R.id.id_messageCardView) View messageCardView;
    @BindView(R.id.id_messageTextView) TextView messageTextView;
    @BindView(R.id.id_responseTextView) TextView responseTextView;
    @BindView(R.id.id_imageNoteUsernameTextview) TextView imageNoteUsername;
    @BindView(R.id.id_messageNoteUsernameTextview2) TextView messageNoteUsername;




    String absoluteImageFolder;

    @OnClick(R.id.id_imageCardView) void clearImageView(){

        AnimatorListenerAdapter animatorListener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                imageCardView.setVisibility(View.GONE);

            }
        };

        imageCardView.animate()
                .alpha(0.0f)
                .setDuration(200)
                .setListener(animatorListener);



    }

    @OnClick(R.id.id_responseTextView) void clearTextView(){
        responseTextView.setText("");
        responseTextView.setVisibility(View.INVISIBLE);
        responseTextView.setEnabled(false);
    }


    @OnClick(R.id.id_messageCardView) void clearMessageView(){

        AnimatorListenerAdapter animatorListener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                messageCardView.setVisibility(View.GONE);

            }
        };

        messageCardView.animate()
                .alpha(0.0f)
                .setDuration(200)
                .setListener(animatorListener);




    }


    int deleteDBCounter = 0;
    @OnClick(R.id.id_deleteDB) void deleteDatabase() {

        deleteDBCounter += 1;

        if (deleteDBCounter == 5) {

            HttpRequestService httpRequest = new HttpRequestService();
            httpRequest.sendDelete(userToken);


            cameraCoordinates.clear();
            messageCoordinates.clear();
            DatabaseService dbService = new DatabaseService();

            deleteDBCounter = 0;


            Log.e(TAG, "RELOADING MAP MARKERS");


            File folder = new File(getFilesDir(),"photoStorage");
            String[] files;
            files = folder.list();

            for (String filePath : files){
                File myFile = new File(folder, filePath);
                Boolean deleted = myFile.delete();
                if (deleted){
                    Log.e(TAG, "File deleted:" + myFile.getAbsolutePath() );
                }
            }
            dbService.clearDatabase(MainActivity.this);

            reloadMapMarkers();

        }

    }

    @OnClick(R.id.id_openCamerButton2) void openCamera(){

        Log.e(TAG, "Asking Camera To Open. Location Permission Flag:" + locationPermissionFlag);

        if (!locationPermissionFlag){
            Log.e(TAG, "Asking to Enable Location Permissions");

            Toast.makeText(MainActivity.this,"Please enable location permissions",Toast.LENGTH_LONG).show();

        }

        else if (globalLat.equals("0.00") || globalLong.equals("0.00")){
            Toast.makeText(MainActivity.this,"Location not found yet. Wait a bit.",Toast.LENGTH_LONG).show();
            Log.e(TAG, "Asking to wait for location to be found");

        }
        else{

            Dexter.withActivity(MainActivity.this)
                    .withPermissions(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .withListener(new MultiplePermissionsListener() {
                        @Override public void onPermissionsChecked(MultiplePermissionsReport report) {
                            locationPermissionFlag = true;
                            cameraPermissionFlag = true;

                            Log.e(TAG, "Location and Camera Permission Granted. Opening Camera.");

                            if (report.areAllPermissionsGranted()){
                                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                                    // Create the File where the photo should go
                                    File photoFile ;
                                    try {

                                        photoFile = createImageFile();


                                        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                                        StrictMode.setVmPolicy(builder.build());
                                        if (photoFile != null) {
                                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                                            Log.e(TAG, "Starting Camera Intent for Result");

                                            startActivityForResult(cameraIntent, 100);
                                        }
                                    } catch (IOException ex) {
                                        // Error occurred while creating the File
                                        Log.i(TAG, "Creating Camera Photo File Failed:" + ex.toString());
                                    }
                                    // Continue only if the File was successfully created
                                }
                            }

                        }

                        @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                            Log.e(TAG, "Showing Camera Permission Rationale");
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Access Camera Permission")
                                    .setMessage("Camera permission is required to send photos.")
                                    .setNegativeButton(android.R.string.cancel,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int which) {
                                                    dialog.dismiss();
                                                    token.cancelPermissionRequest();
                                                    cameraPermissionFlag = false;
                                                }
                                            })
                                    .setPositiveButton(android.R.string.ok,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int which) {
                                                    dialog.dismiss();
                                                    token.continuePermissionRequest();
                                                    cameraPermissionFlag = true;
                                                }
                                            })
                                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {
                                            token.cancelPermissionRequest();
                                            cameraPermissionFlag = false;
                                            Log.e(TAG, "Camera Permission Dismissed");

                                        }
                                    }).show();

                        }
                    })
                    .onSameThread()
                    .check();

        }

    }
    @OnClick(R.id.id_auto_location_image) void autoLocationToggle(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            Log.e(TAG, "Asking for Foreground Permission");

            Dexter.withActivity(MainActivity.this)
                    .withPermission(Manifest.permission.FOREGROUND_SERVICE)
                    .withListener(new PermissionListener() {
                        @Override public void onPermissionGranted(PermissionGrantedResponse response) {
                            Log.e(TAG, "Foreground Granted.");
                            enableAutoLocationSharing();
                        }
                        @Override public void onPermissionDenied(PermissionDeniedResponse response) {
                            Log.e(TAG, "Foreground Denied");
                        }
                        @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                            Log.e(TAG, "rationale");
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Location permission")
                                    .setMessage("Location permission is needed to share location, messages and photos.")
                                    .setNegativeButton(android.R.string.cancel,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int which) {
                                                    dialog.dismiss();
                                                    token.cancelPermissionRequest();
                                                }
                                            })
                                    .setPositiveButton(android.R.string.ok,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int which) {
                                                    dialog.dismiss();
                                                    token.continuePermissionRequest();
                                                }
                                            })
                                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {
                                            token.cancelPermissionRequest();
                                        }
                                    }).show();
                            Log.e(TAG, "Showing Foreground Rationale.");

                        }
                    })
                    .onSameThread()
                    .check();

        }else{
            Log.e(TAG, "Foreground Permissions not needed.");
            enableAutoLocationSharing();
        }

    }
    @OnClick(R.id.id_share_button) void shareUrlExternal(){
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = "Hey, see my location at: " + getResources().getString(R.string.travellerUrl) + webUrlSecret;
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "My Location");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }
    @OnClick(R.id.id_leave_note_image) void startNoteActivity(){


        if (!locationPermissionFlag){
            Toast.makeText(this,"Please enable location permissions",Toast.LENGTH_LONG).show();

        }

        else if (globalLat.equals("0.00") || globalLong.equals("0.00")){
            Toast.makeText(this,"Location not found yet. Wait a bit.",Toast.LENGTH_LONG).show();
        }

        else{

            globalLat = locationComponent.getLastKnownLocation().getLatitude()+"";
            globalLong = locationComponent.getLastKnownLocation().getLongitude()+"";
            globalTime = Calendar.getInstance().getTime().toString();


            Intent myIntent = new Intent(getApplicationContext(), NoteActivity.class);
            myIntent.putExtra("noteCardTop",leave_note_button.getTop());
            myIntent.putExtra("userToken",userToken);
            myIntent.putExtra("lat",globalLat);
            myIntent.putExtra("lng",globalLong);
            myIntent.putExtra("timeStamp",globalTime);
            MainActivity.this.startActivity(myIntent);
        }


    }

    BroadcastReceiver bReceiver;
    BroadcastReceiver toastReceiver;
    BroadcastReceiver databaseUpdateReceiver;


    View[] notes;

    GoogleSignInOptions gso;
    GoogleSignInClient mGoogleSignInClient;

    Boolean locationPermissionFlag = false;
    Boolean cameraPermissionFlag = false;

    String googleID;
    String userToken;
    String webUrlSecret;
    String userName;
    String userEmail;
    String mCurrentPhotoPath;

    double dbLat;
    double dbLng;
    String dbMessage;
    String dbTime;
    String imagePath;
    String dbResponse;

    String globalLat = "123.123";
    String globalLong = "123.1230";
    String globalTime = Calendar.getInstance().getTime().toString();

    LocationComponent locationComponent;


    int stateNumber = 0;
    int sharingFlag;

    ArrayList<Feature> cameraCoordinates = new ArrayList<>();
    ArrayList<Feature> messageCoordinates = new ArrayList<>();

    GeoJsonSource cameraSource;
    GeoJsonSource messageSource;

    MapboxMap mapboxMap;
    MapboxMap.OnMapClickListener listener;


    Boolean cameraFlag;
    Boolean firstTimeFlag = true;

    SharedPreferences settings;

    Fotoapparat photoInstance;

    private void enableAutoLocationSharing(){

        SharedPreferences settings = getSharedPreferences("breathePrefs", 0);
        SharedPreferences.Editor editor = settings.edit();
        sharingFlag = settings.getInt("sharingFlag",0);

        if (!isMyServiceRunning(AutoUpdateService.class)){

            Log.e(TAG, "Enabled Location Sharing");
            (new Handler()).postDelayed(MainActivity.this::checkIfLocationEnabled, 1000);
            autoLocationImage.setImageResource(R.drawable.sharing_on);
            sharingFlag = 1;

            editor.putInt("sharingFlag", sharingFlag);
            editor.apply();
            Intent intent = new Intent(MainActivity.this, AutoUpdateService.class);
            intent.putExtra("userToken",userToken);
            intent.putExtra("locFlag",true);
            startService(intent);
        }
        else{
            Log.e(TAG, "Disabled Location Sharing");
            autoLocationImage.setImageResource(R.drawable.sharing_off);
            sharingFlag = 0;
            Intent intent = new Intent(MainActivity.this, AutoUpdateService.class);
            editor.putInt("sharingFlag", sharingFlag);
            editor.apply();
            stopService(intent);

        }
    }


    private void logUser() {
        // TODO: Use the current user's information
        // You can call any combination of these three methods
        Crashlytics.setUserIdentifier(webUrlSecret);
        Crashlytics.setUserEmail(userEmail);
        Crashlytics.setUserName(userName);
    }





    private void reloadMapMarkers(){

        DatabaseService dbService = new DatabaseService();

        ArrayList<JSONObject> jsonArray = dbService.broadcastDatabaseValues(MainActivity.this);

        Log.e(TAG, "RELOADING MAP MARKERS" );
        Log.e(TAG, jsonArray.toString() );



        if (!jsonArray.isEmpty()){

            cameraCoordinates.clear();
            messageCoordinates.clear();

            Log.e(TAG, "Size of jsonArray: " + jsonArray.size() );


            for (JSONObject entry : jsonArray){

                Log.e(TAG, entry.toString() );


                try{

                    dbLat = Double.parseDouble(entry.getString("lat"));
                    dbLng = Double.parseDouble(entry.getString("lng"));
                    dbTime = entry.getString("time");

                    if (entry.has("imagePath")){
                        imagePath = entry.getString("imagePath");
                        dbLat = Double.parseDouble(entry.getString("lat"));
                        dbLng = Double.parseDouble(entry.getString("lng"));


                        Log.d(TAG, "Adding Image to coordinates ");
                        Log.d(TAG, "Image Path is: " + imagePath);


                        JsonObject cameraJson = new JsonObject();
                        cameraJson.addProperty("imagePath",imagePath);
                        cameraJson.addProperty("response",dbResponse);
                        cameraJson.addProperty("time",dbTime);

                        cameraCoordinates.add(Feature.fromGeometry(
                                Point.fromLngLat(dbLng,dbLat),cameraJson));
                    }

                    if (entry.has("message")){

                        if (!entry.getString("message").equals("")) {
                            dbLat = Double.parseDouble(entry.getString("lat"));
                            dbLng = Double.parseDouble(entry.getString("lng"));
                            dbMessage = entry.getString("message");
                            JsonObject messageJson = new JsonObject();
                            messageJson.addProperty("message", dbMessage);
                            messageJson.addProperty("time", dbTime);
                            messageJson.addProperty("response", dbResponse);
                            Log.d(TAG, "Adding message to coordinates ");
                            Log.d(TAG, "Message is: " + dbMessage);

                            messageCoordinates.add(Feature.fromGeometry(
                                    Point.fromLngLat(dbLng, dbLat), messageJson));
                        }
                    }



                }catch(JSONException e){
                    e.printStackTrace();
                }
            }

        }

        Log.e(TAG, "setting sources" );
        cameraSource.setGeoJson(FeatureCollection.fromFeatures(cameraCoordinates));
        messageSource.setGeoJson(FeatureCollection.fromFeatures(messageCoordinates));
        Log.e(TAG, "setting sources done" );

    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {

        MainActivity.this.mapboxMap = mapboxMap;
        Log.e(TAG, "map ready");
        mapboxMap.getUiSettings().setRotateGesturesEnabled(false);

        Log.e(TAG, "map ready");

        Log.e(TAG, "Broadcast Receiver done" );
        databaseUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.e(TAG, "Received database Ready. Reloading map markers" );
                reloadMapMarkers();

            }
        };


        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(databaseUpdateReceiver, new IntentFilter("databaseReady"));


        mapboxMap.setStyle(Style.OUTDOORS,
                new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {


                        Log.e(TAG, "beforegeojsonset: " );

                         cameraSource = new GeoJsonSource("camera-source", FeatureCollection.fromFeatures(cameraCoordinates));
                         messageSource = new GeoJsonSource("message-source", FeatureCollection.fromFeatures(messageCoordinates));
                        Log.e(TAG, "onStyleLoaded: " );


                        if (mapboxMap.getStyle().getSource("camera-source") == null){
                            mapboxMap.getStyle().addSource(cameraSource);
                            Log.e(TAG, "CAMERA SOURCE WAS NULL" );

                        }

                        if (mapboxMap.getStyle().getSource("message-source") == null){
                            mapboxMap.getStyle().addSource(messageSource);
                            Log.e(TAG, "MESSAGE SOURCE WAS NULL" );


                        }

                        reloadMapMarkers();
                        Log.e(TAG, "Sources added: " );


                        Log.e(TAG, "Map Markers reloaded" );


                        Log.e(TAG, "Setting sources. " );


                        mapboxMap.getUiSettings().setTiltGesturesEnabled(false);

                        Log.e(TAG, "makinggeosources" );

                        Log.e(TAG, "adding sources." );

                        style.addImage("camera-marker-image", BitmapFactory.decodeResource(
                                MainActivity.this.getResources(), R.drawable.camera_marker));

                        style.addImage("message-marker-image", BitmapFactory.decodeResource(
                                MainActivity.this.getResources(), R.drawable.message_marker));

                        style.addImage("route-marker-image", BitmapFactory.decodeResource(
                                MainActivity.this.getResources(), R.drawable.route_line));


                            style.addLayer(new SymbolLayer("camera-marker-layer", "camera-source")
                                    .withProperties(PropertyFactory.iconImage("camera-marker-image"),
                                            PropertyFactory.iconSize(1f),PropertyFactory.iconIgnorePlacement(true)
                                    ));

                            style.addLayer(new SymbolLayer("message-marker-layer", "message-source")
                                    .withProperties(PropertyFactory.iconImage("message-marker-image"),
                                            PropertyFactory.iconSize(1f),PropertyFactory.iconIgnorePlacement(true)
                                    ));






                        listener = new MapboxMap.OnMapClickListener() {
                            @Override
                            public boolean onMapClick(@NonNull LatLng point) {

                                Log.e(TAG, "onMapClick: " );
                                PointF screenPoint = mapboxMap.getProjection().toScreenLocation(point);
                                List<Feature> features = mapboxMap.queryRenderedFeatures(screenPoint, "camera-marker-layer","message-marker-layer");
                                if (!features.isEmpty()) {


                                    Feature selectedFeature = features.get(0);
                                    Log.e(TAG, "ALL FEATURES CLICKED: " + features.toString() );
                                    Log.e(TAG, "FEATURES: " + features.toString() );
                                    //Log.e(TAG, "properties: " + selectedFeature.properties());


                                    if (selectedFeature.hasProperty("imagePath")){

                                        String imageFilePath = selectedFeature.getStringProperty("imagePath");

                                        if(imageFilePath!=null && !imageFilePath.equals(""))
                                        {
                                            File myFile = new File(imageFilePath);
                                            Log.e(TAG, "Set File" );
                                            if(myFile.exists())
                                            {
                                                Log.e(TAG, "File Exists" );

                                                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                                                Bitmap bitmap = BitmapFactory.decodeFile(myFile.getAbsolutePath(),bmOptions);
                                                Drawable dr = new BitmapDrawable(getResources(), bitmap);


                                                photoPopup.setImageDrawable(dr);


                                                imageNoteUsername.setText(userName);
                                                imageCardView.setAlpha(0);
                                                imageCardView.setVisibility(View.VISIBLE);

                                                photoPopup.setVisibility(View.VISIBLE);
                                                infoTextView.setVisibility(View.INVISIBLE);
                                                infoTextView.setText(dbResponse);


                                                imageCardView.animate()
                                                        .alpha(1.0f)
                                                        .setDuration(100)
                                                        .setListener(null);

                                            }
                                        }
                                    }

                                    if (selectedFeature.hasProperty("message")){

                                        if (!selectedFeature.getStringProperty("message").equals("null")){

                                            String dbMessage = selectedFeature.getStringProperty("message");

                                            if(dbMessage!=null && !dbMessage.equals(""))
                                            {
                                                messageCardView.setAlpha(0);
                                                messageNoteUsername.setText(userName);

                                                messageCardView.setVisibility(View.VISIBLE);
                                                messageTextView.setText(dbMessage);
                                                messageTextView.setVisibility(View.VISIBLE);

                                                messageCardView.animate()
                                                        .alpha(1.0f)
                                                        .setDuration(200)
                                                        .setListener(null);

                                            }

                                        }
                                        }
                                    }

                                else{
                                    Log.e(TAG, "onMapClick: empty click " );


                                }
                                return false;
                            }
                        };


                        mapboxMap.addOnMapClickListener(listener);





                        Dexter.withActivity(MainActivity.this)
                                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                                .withListener(new PermissionListener() {
                                    @Override public void onPermissionGranted(PermissionGrantedResponse response) {
                                        locationPermissionFlag = true;
                                        Log.e(TAG, "granted");


                                        enableLocationComponent(style,mapboxMap);

                                        locationPermissionFlag = true;
                                        Log.e(TAG, "onPermissionGranted:1 " );

                                    }
                                    @Override public void onPermissionDenied(PermissionDeniedResponse response) {
                                        Log.e(TAG, "denied");

                                        locationPermissionFlag = false;

                                    }
                                    @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                        Log.e(TAG, "rationale");
                                        new AlertDialog.Builder(MainActivity.this)
                                                .setTitle("Location permission")
                                                .setMessage("Location permission is needed to share location, messages and photos.")
                                                .setNegativeButton(android.R.string.cancel,
                                                        new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog,
                                                                                int which) {
                                                                dialog.dismiss();
                                                                token.cancelPermissionRequest();
                                                                locationPermissionFlag = false;
                                                            }
                                                        })
                                                .setPositiveButton(android.R.string.ok,
                                                        new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog,
                                                                                int which) {
                                                                dialog.dismiss();
                                                                token.continuePermissionRequest();
                                                            }
                                                        })
                                                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                                    @Override
                                                    public void onDismiss(DialogInterface dialog) {
                                                        token.cancelPermissionRequest();
                                                        locationPermissionFlag = false;
                                                    }
                                                }).show();

                                    }
                                })
                                .check();
                    }
                });
    }

    private void enableLocationComponent(@NonNull Style loadedMapStyle, MapboxMap mapBoxMap) {


                // Get an instance of the component
                locationComponent = mapBoxMap.getLocationComponent();

                // Activate with options
                locationComponent.activateLocationComponent(this, loadedMapStyle);

                // Enable to make component visible
                locationComponent.setLocationComponentEnabled(true);


                // Set the component's camera mode
                locationComponent.setCameraMode(CameraMode.TRACKING_GPS_NORTH);

                // Set the component's render mode
                locationComponent.setRenderMode(RenderMode.COMPASS);


        }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.DefaultTheme);
        super.onCreate(savedInstanceState);
        Stetho.initializeWithDefaults(this);
        Fabric.with(this, new Crashlytics());
        Mapbox.getInstance(this, getString(R.string.mapBoxApiToken));
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);





        mapView = findViewById(R.id.mapView);

        mapView.onCreate(savedInstanceState);

        imageCardView.setVisibility(View.GONE);
        messageCardView.setVisibility(View.GONE);



        AndroidNetworking.initialize(getApplicationContext());
        setAutoLocationButtonState();



        getWindow().setStatusBarColor(getColor(R.color.app_blue));



        photoPopup.setVisibility(View.INVISIBLE);

        settings = getSharedPreferences("breathePrefs", 0);
        webUrlSecret = settings.getString("webUrlSecret","0");
        cameraFlag = false;
        File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "drawable");


        sharingFlag = settings.getInt("sharingFlag",0);


        tutView.setVisibility(View.GONE);

        //showShowcase();



    }


    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();

        setTheme(R.style.DefaultTheme);
        restorePreferences();

        deleteDBCounter = 0;
        if (firstTimeFlag) {
            Log.d("first", "First time flag");
            setViewAndChildrenEnabled(mainView, false);
            setTutorialNoteStates();
            showTutorial();
        }else{
            Log.e(TAG, "tutorial done" );
            mapView.getMapAsync(this);
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {

                        globalLat = intent.getStringExtra("lat");
                        globalLong = intent.getStringExtra("lng");
                        globalTime = intent.getStringExtra("time");

                    }
                }, new IntentFilter(AutoUpdateService.ACTION_LOCATION_BROADCAST)
        );


        toastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                responseTextView.setEnabled(true);



                String toastMessage = intent.getStringExtra("toastMessage");
                Toast.makeText(MainActivity.this,toastMessage,Toast.LENGTH_LONG).show();

                responseTextView.setText("");
                //responseTextView.setVisibility(View.VISIBLE);



            }
        };




        bReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.e(TAG, "Received Location Broadcast" );
                globalLat = intent.getStringExtra("lat");
                globalLong = intent.getStringExtra("lng");
                globalTime = intent.getStringExtra("timeStamp");

            }
        };

        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(bReceiver, new IntentFilter("LOCATION"));
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(toastReceiver, new IntentFilter("toastReceiver"));



        (new Handler()).postDelayed(MainActivity.this::checkIfLocationEnabled, 1000);

        photoView.setVisibility(View.INVISIBLE);


        setAutoLocationButtonState();


        Intent serviceIntent = new Intent(this, LocationService.class);

        try{
            startService(serviceIntent);

        }catch(RuntimeException e){
            e.printStackTrace();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d("first","Main Pause");
        SharedPreferences settings = getSharedPreferences("breathePrefs", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("tutFlag", firstTimeFlag);
        editor.putString("lat",globalLat);
        editor.putString("lng",globalLong);
        // Commit the edits!
        editor.apply();
        mapView.onPause();

        if (mapboxMap != null){
            mapboxMap.removeOnMapClickListener(listener);

        }




    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "Called onStop");
        SharedPreferences settings = getSharedPreferences("breathePrefs", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("tutFlag", firstTimeFlag);

        LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(bReceiver);
        LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(databaseUpdateReceiver);
        LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(toastReceiver);



        // Commit the edits!
        editor.apply();
        mapView.onStop();



    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "Called onDestroy");

        mapView.onDestroy();



    }

    private void setTutorialNoteStates(){

        noteButtonColorStates = new int[]{
                R.color.blue,
                R.color.white,
                R.color.dark_grey,
                R.color.dark_grey,

        };

        noteStates = new int[][]{
                new int[]{
                        26 * 4,
                        26 * 3,
                        26 * 2,
                        26,
                },
                {
                        26 * 4 + 4000,
                        26 * 3,
                        26 * 2,
                        26,
                },
                {
                        26 * 4 + 4000,
                        26 * 3 + 4000,
                        26 * 2,
                        26,
                },
                {
                        26 * 4 + 4000,
                        26 * 3 + 4000,
                        26 * 2 + 4000,
                        26,
                },
        };
    }



    private void setAutoLocationButtonState(){
        if (isMyServiceRunning(AutoUpdateService.class)){
            autoLocationImage.setImageResource(R.drawable.sharing_on);

        }
        else{
            autoLocationImage.setImageResource(R.drawable.sharing_off);
        }
    }

    private void restorePreferences(){
        settings = getSharedPreferences("breathePrefs", 0);
        firstTimeFlag = settings.getBoolean("tutFlag", true);
        googleID = settings.getString("googleID","");
        userToken = settings.getString("userToken","");
        sharingFlag = settings.getInt("sharingFlag",0);
        webUrlSecret = settings.getString("webUrlSecret","0");
        userName = settings.getString("userName","none");
        userEmail = settings.getString("userEmail","none");
        globalLat = settings.getString("lat","0.00");
        globalLong = settings.getString("lng","0.00");


        Log.d("main","User ID: " + webUrlSecret);
        Log.d("IDs","googleID IS    " + googleID);
        Log.d("IDs","userID IS" + userToken);
        logUser();
    }

    public void animateNotes(int stateNum) {

        notes[stateNum].clearAnimation();

        for (int i = 0; i < 4; i++) {

            final ObjectAnimator animation = ObjectAnimator.ofFloat(notes[i], "translationY", (float) noteStates[stateNum][i]);
            animation.setDuration(300);
            animation.start();
            Log.d("anims", String.format("%d    %d", i, noteStates[stateNum][i]));

        }

        next_button.setTextColor(getColor(noteButtonColorStates[stateNum]));
        back_button.setTextColor(getColor(noteButtonColorStates[stateNum]));

        if (stateNum == 0) {
            next_button.setVisibility(View.VISIBLE);
            next_button.setText("Let's go!");
            back_button.setVisibility(View.INVISIBLE);
            Log.d("anims", "State0");
        } else if (stateNum == 3) {

            back_button.setVisibility(View.VISIBLE);
            back_button.setEnabled(true);
            next_button.setVisibility(View.INVISIBLE);
            findViewById(R.id.sign_in_button).setEnabled(true);

            Log.d("anims", "State3");
        } else {
            back_button.setVisibility(View.VISIBLE);
            back_button.setEnabled(true);
            next_button.setVisibility(View.VISIBLE);
            next_button.setText(getString(R.string.next));
            Log.d("anims", "StateOthers");

        }

    }

    void showTutorial(){

        stateNumber = 0;

        tutView.setVisibility(View.VISIBLE);

        Log.d("main","showtutorial");
        next_button = findViewById(R.id.id_next_button);
        back_button = findViewById(R.id.id_back_button);

        next_button.setEnabled(true);
        next_button.setPaintFlags(next_button.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        next_button.setText("Let's go!");
        back_button.setVisibility(View.GONE);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        notes = new View[]{
                findViewById(R.id.id_tut_note_1),
                findViewById(R.id.id_tut_note_2),
                findViewById(R.id.id_tut_note_3),
                findViewById(R.id.id_tut_note_4),
        };


        animateNotes(0);

        Log.d("but","NextButtonReady");
        back_button.setOnClickListener((View v) -> animateNotes(--stateNumber));
        next_button.setOnClickListener((View v) -> animateNotes(++stateNumber));

        findViewById(R.id.sign_in_button).setOnClickListener((View v) -> signIn());

    }

    private void showShowcase(){





        final TapTargetSequence targetSequence = new TapTargetSequence(this)
                .targets(
                        TapTarget.forView(findViewById(R.id.id_openCamerButton2), "Take a photo!", "Pin a photo at your current location.")
                                .dimColor(android.R.color.black)
                                .outerCircleColor(R.color.app_blue)
                                .textColor(android.R.color.white)
                                .transparentTarget(true)
                                .targetRadius(100)

                        ,
                        TapTarget.forView(findViewById(R.id.id_leave_note_image), "Leave a note!", "Write a note and it will be pinned to your current location.")
                                .dimColor(android.R.color.black)
                                .outerCircleColor(R.color.app_blue)
                                .textColor(android.R.color.white)
                                .transparentTarget(true)
                                .targetRadius(100)

                        ,
                        TapTarget.forView(findViewById(R.id.id_share_button), "Share your live map.", "Use the share button to send a link to your live map using your favorite messaging apps.")
                                .dimColor(android.R.color.black)
                                .outerCircleColor(R.color.app_blue)
                                .textColor(android.R.color.white)
                                .transparentTarget(true)
                        .targetRadius(100)
                        ,
                        TapTarget.forView(findViewById(R.id.id_auto_location_image), "Auto Location Sharing", "Enable Auto Location Sharing to update your live map periodically.")
                                .dimColor(android.R.color.black)
                                .outerCircleColor(R.color.app_blue)
                                .transparentTarget(true)
                                .textColor(android.R.color.white)
                                .targetRadius(200)

                )
                .listener(new TapTargetSequence.Listener() {
                    // This listener will tell us when interesting(tm) events happen in regards
                    // to the sequence
                    @Override
                    public void onSequenceFinish() {
                        // Yay
                        Log.e(TAG, "onSequenceFinish: " );
                        firstTimeFlag = false;
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                        // Perform action for the current target
                        Log.e(TAG, "onSequenceStep: " );
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        // Boo
                        Log.e(TAG, "onSequenceCanceled: " );
                    }
                });

        targetSequence.start();
        }


    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 1);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            //updateUI(account);
            sendAuthHttp(AUTH_GOOGLE_URL, account.getIdToken(), authcallback);
            googleID = account.getIdToken();
            Log.d("IDs", googleID);
            SharedPreferences.Editor editor = getSharedPreferences("breathePrefs", MODE_PRIVATE).edit();
            editor.putBoolean("tutFlag", false);
            editor.putString("googleID",googleID);
            editor.apply();


        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("SignIn", "signInResult:failed code=" + e.getStatusCode());
            Log.w("SignIn", "signInResult:failed message=" + e.getMessage());

        }
    }

    private void sendAuthHttp(String url, String body, Callback callback) {

        final OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("token", body)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        Call call = client.newCall(request);

        call.enqueue(callback);
    }


    Callback authcallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            Log.d("HttpService", "onFailure() Request was: " + call);
            Toast.makeText(MainActivity.this,"Unable to Sign in. Check your internet connection.",Toast.LENGTH_LONG).show();


        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {

            String authResponse = response.body().string();

            Log.d("HttpService", "onResponse() BEFORE WHO AM I AUTH CALLBACK Response was: " + authResponse);
            SharedPreferences.Editor editor = getSharedPreferences("breathePrefs", MODE_PRIVATE).edit();
            if (response.code() < 400){
                userToken = authResponse;
                editor.putString("userToken",authResponse);
                editor.apply();
                sendAuthHttp(AUTH_WHOAMI_URL, authResponse, whoamicallback);
            }else{
                Toast.makeText(MainActivity.this,"Unable to Sign in. We are having some difficulties on our side",Toast.LENGTH_LONG).show();
            }


        }
    };

    Callback whoamicallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            Log.d("HttpService", "onFailure() Request was: " + call);
            Toast.makeText(MainActivity.this,"Unable to Sign in. Check your internet connection.",Toast.LENGTH_LONG).show();


        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {

            String whoamiresponse = response.body().string();

            if (response.code() < 400) {


                Log.d("HttpService", "onResponse() WHOAMI Response was: " + whoamiresponse);
                try {
                    JSONObject jsonObj = new JSONObject(whoamiresponse);
                    webUrlSecret = jsonObj.getString("secret");
                    userName = jsonObj.getString("name");
                    userEmail = jsonObj.getString("email");
                    Log.d("main", "User ID: " + webUrlSecret);

                    SharedPreferences.Editor editor = getSharedPreferences("breathePrefs", MODE_PRIVATE).edit();
                    editor.putString("webUrlSecret", webUrlSecret);
                    editor.putString("userName", userName);
                    editor.putString("userEmail", userEmail);
                    editor.apply();


                } catch (org.json.JSONException e) {
                    Log.d("HttpService", "Json error" + e.toString());

                }

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        tutView.setVisibility(View.GONE);
                        setViewAndChildrenEnabled(mainView,true);
                        back_button.setVisibility(View.GONE);
                        next_button.setVisibility(View.GONE);

                        for (View note : notes){
                            note.clearAnimation();
                            note.setVisibility(View.GONE);
                        }


                    }
                });
            }else{
                Toast.makeText(MainActivity.this,"Unable to Sign in. We are having some difficulties on our side",Toast.LENGTH_LONG).show();
            }




        }
    };

    private static void setViewAndChildrenEnabled(View view, boolean enabled) {
        view.setEnabled(enabled);
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                setViewAndChildrenEnabled(child, enabled);
            }
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    private void checkIfLocationEnabled(){
        LocationManager locationManager = (LocationManager) MainActivity.this.getSystemService(Context.LOCATION_SERVICE);
        if( !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("GPS not enabled.")  // GPS not found
                    .setMessage("Please enable your location.") // Want to enable?
                    .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            MainActivity.this.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
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

    private File createImageFile() throws IOException {
        // Create an image file name
        String currentTime = getLocalToUTCDate(Calendar.getInstance().getTime());

        String imageFileName = "JPEG_" +currentTime +  "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == 1) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }

        if (requestCode == 100 && resultCode == RESULT_OK) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {

                    try {

                        Date timeStamp = Calendar.getInstance().getTime();

                        Bitmap mImageBitmap = MediaStore.Images.Media.getBitmap(MainActivity.this.getContentResolver(), Uri.parse(mCurrentPhotoPath));
                        mImageBitmap = rotateImageIfRequired(MainActivity.this, mImageBitmap, Uri.parse(mCurrentPhotoPath));

                        //create a file to write bitmap data
                        File path = new File(getFilesDir(), "photoStorage");

                        if (!path.exists()) {
                            path.mkdirs();
                        }

                        File photoFile = new File(path, userToken + timeStamp + "_IMG" + ".jpg");
                        photoFile.createNewFile();

                        JsonObject json = new JsonObject();
                        json.addProperty("lat", globalLat);
                        json.addProperty("lng", globalLong);
                        json.addProperty("imagePath", photoFile.getAbsolutePath());


                        cameraCoordinates.add(Feature.fromGeometry(
                                Point.fromLngLat(Double.parseDouble(globalLong), Double.parseDouble(globalLat)), json));


                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        mImageBitmap.compress(Bitmap.CompressFormat.JPEG, 25 /*ignored for PNG*/, bos);
                        byte[] bitmapdata = bos.toByteArray();

                        //write the bytes in file
                        FileOutputStream fos = new FileOutputStream(photoFile);
                        fos.write(bitmapdata);
                        fos.flush();
                        fos.close();


                        Log.e(TAG, "Sending photoFile to Database!"  + photoFile.getAbsolutePath());
                        DatabaseService dbService = new DatabaseService();
                        dbService.addToQueueDatabase(MainActivity.this, userToken, globalLat, globalLong, timeStamp, null, photoFile.getAbsolutePath());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    }

    private static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {

        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23)
            ei = new ExifInterface(input);
        else
            ei = new ExifInterface(selectedImage.getPath());

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);

        outState.putSerializable("photoPath",mCurrentPhotoPath);
        outState.putSerializable("userToken",userToken);
        outState.putSerializable("latitude",globalLat);
        outState.putSerializable("longitude",globalLong);
        outState.putSerializable("time",globalTime);
        outState.putInt("sharingFlag",sharingFlag);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        try {
            mCurrentPhotoPath = savedInstanceState.getSerializable("photoPath").toString();
            userToken = savedInstanceState.getSerializable("userToken").toString();
            globalLat = savedInstanceState.getSerializable("latitude").toString();
            globalLong = savedInstanceState.getSerializable("longitude").toString();
            globalTime = savedInstanceState.getSerializable("time").toString();
            sharingFlag = savedInstanceState.getInt("sharingFlag");

        } catch (NullPointerException e) {

        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }


    @Override
    public void onBackPressed() {

        if (imageCardView.getVisibility() == View.VISIBLE){
            imageCardView.setVisibility(View.GONE);
        }else if(messageCardView.getVisibility() == View.VISIBLE){
            messageCardView.setVisibility(View.GONE);


        }
        else{
            MainActivity.super.onBackPressed();
        }





    }

    public Bitmap screenShot(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(),
                view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    public static Bitmap blur(Context context, Bitmap image) {

        float BITMAP_SCALE = 0.4f;
        float BLUR_RADIUS = 7.5f;
        int width = Math.round(image.getWidth() * BITMAP_SCALE);
        int height = Math.round(image.getHeight() * BITMAP_SCALE);

        Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height,
                false);
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs,
                Element.U8_4(rs));
        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
        theIntrinsic.setRadius(BLUR_RADIUS);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);

        return outputBitmap;
    }

    private Bitmap getBlurredScreen(View view, Context context){
        return blur(context,screenShot(view));
    }
}

