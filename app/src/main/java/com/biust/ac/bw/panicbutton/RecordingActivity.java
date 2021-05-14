package com.biust.ac.bw.panicbutton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RecordingActivity extends AppCompatActivity {
    MediaRecorder mediaRecorder;

    Button mRecordBtn;
    Button mStopBtn;
    TextView mRecordMsg;

    String fileName;
    String userId;

    private String mFileName = null;
    private String mFilePath = null;

    private DBHelper mDatabase;

    private StorageReference mStorageRef;

    private FirebaseUser mAuth;
    private DatabaseReference myRef;

    ProgressDialog mProgress;

    private static final String LOG_TAG = "RecordingService";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rcording);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PackageManager.PERMISSION_GRANTED);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Audios");

        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance().getCurrentUser();

        mProgress = new ProgressDialog(this);

        mRecordBtn = findViewById(R.id.recordBtn);
        mStopBtn = findViewById(R.id.stopBtn);
        mRecordMsg = findViewById(R.id.recordMsg);


        mRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecording();
                mRecordMsg.setText("Recording Started ...");


            }
        });

        mStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording(fileName);
                mRecordMsg.setText("Recording stopped");
            }
        });

    }

    private void startRecording() {
        setFileNameAndPath();
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mediaRecorder.setOutputFile(mFilePath);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();

            //startTimer();
            //startForeground(1, createNotification());

        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
            Log.e(LOG_TAG, e.getMessage());
        }
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    }

    private void stopRecording(String fileName) {
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        uploadAudio(mFilePath);
    }

    private void uploadAudio(String fileName) {
        mProgress.setMessage("Uploading Audio...");
        mProgress.show();

        StorageReference filepath = mStorageRef.child("Audios").child(userId);
        Uri uri = Uri.fromFile(new File(fileName));

        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        myRef.child(userId).child(currentDate).child(currentTime).push().setValue(uri);
                    }
                });
                mProgress.dismiss();
                Toast.makeText(RecordingActivity.this, "Recording Uploaded", Toast.LENGTH_SHORT).show();
                mRecordMsg.setText("Tap and Hold button to record");
            }
        });

    }

    public void setFileNameAndPath() {
        int count = 0;
        File f;

        do {
            count++;

            mFileName = getString(R.string.default_file_name)
                    + "_" + count + ".wav";
            mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            mFilePath += "/DVMLAudios/" + mFileName;

            f = new File(mFilePath);
        } while (f.exists() && !f.isDirectory());
    }

    public void updateUI(FirebaseUser account) {

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth;
        updateUI(currentUser);
        userId = currentUser.getUid();
    }

}