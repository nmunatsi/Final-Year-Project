package com.biust.ac.bw.panicbutton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Homepage extends AppCompatActivity implements LocationListener,Runnable{

    Button toRecord;
    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected Context context;
    TextView txtLat;
    String lat, standUp;
    String provider;
    protected String latitude, longitude, userId;
    protected boolean gps_enabled, network_enabled;
    FirebaseDatabase db;
    DatabaseReference reference;

    //ml things
    private static final String MODEL_FILENAME = "file:///android_asset/model.tflite";
    private static final String LABEL_FILENAME = "file:///android_asset/conv_actions_labels.txt";
    private static final String INPUT_DATA_NAME = "decoded_sample_data:0";
    private static final String INPUT_SAMPLE_RATE_NAME = "decoded_sample_data:1";
    private static final String OUTPUT_NODE_NAME = "labels_softmax";
    
    private TensorFlowInferenceInterface mInferenceInterface;
    private List<String> mLabels = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(Homepage.this));
        }

        toRecord = findViewById(R.id.toRecord);

        txtLat = (TextView) findViewById(R.id.textview1);

        FirebaseUser auth=FirebaseAuth.getInstance().getCurrentUser();

        Log.d("auth", "onCreate: "+auth);

        userId= auth.getUid();

        db= FirebaseDatabase.getInstance();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) this);

        toRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(Homepage.this, AutoDetectionActivity.class));
                //standUp=getHelloWorld();
                //txtLat.setText(standUp);
             startActivity(new Intent(Homepage.this, WAVRecordingActivity.class));
            }
        });

    }

    private String getHelloWorld() {
            Python phython = Python.getInstance();
            PyObject pythonFile= phython.getModule("hello");
            return pythonFile.callAttr("helloWorld").toString();
    }

    @Override
    public void onLocationChanged(Location location) {
        txtLat = (TextView) findViewById(R.id.textview1);
        latitude= String.valueOf(location.getLatitude());
        longitude= String.valueOf(location.getLongitude());
        txtLat.setText("Latitude:" + latitude + ", Longitude:" + longitude);
        reference= db.getReference().child(userId);

        Map<String, Object> map = new HashMap<>();
        map.put("Latitude", latitude);
        map.put("Longitude", longitude);

        reference.updateChildren(map);

    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude","disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude","enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude","status");
    }

    @Override
    public void run() {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}