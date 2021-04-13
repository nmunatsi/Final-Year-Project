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

    private StorageReference mStorageRef;

    private FirebaseUser mAuth;
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rcording);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PackageManager.PERMISSION_GRANTED);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Audios");

        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance().getCurrentUser();

        mProgress = new ProgressDialog(this);

        mRecordBtn = findViewById(R.id.recordBtn);
        mStopBtn = findViewById(R.id.stopBtn);
        mRecordMsg = findViewById(R.id.recordMsg);

        fileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        fileName += "/DVMLAudios/AudioRecorded.mp3";


        mRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startRecording();
                    mRecordMsg.setText("Recording Started ...");
                } catch (IOException e) {
                    e.printStackTrace();
                }

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

    private void startRecording() throws IOException {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mediaRecorder.setOutputFile(fileName);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        mediaRecorder.prepare();
        mediaRecorder.start();

        Log.d("mediaRecorder value", mediaRecorder.toString());
    }

    private void stopRecording(String fileName) {
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        uploadAudio(fileName);
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