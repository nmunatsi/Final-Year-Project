package com.biust.ac.bw.panicbutton;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.mfcc.MFCC;

public class AutoDetectionActivity extends AppCompatActivity implements Runnable {
    private static final int SAMPLE_RATE = 16000;
    private static final int RECORDING_LENGTH = SAMPLE_RATE * 1;
    private static final int REQUEST_RECORD_AUDIO = 13;

    private static final String LABEL_FILENAME = "file:///android_asset/conv_actions_labels.txt";
    private static final String MODEL_FILENAME = "file:///android_asset/speech_commands_graph.pb";
    //private static final String MODEL_FILENAME = "file:///android_asset/conv_actions_frozen.pb";
    private static final String INPUT_DATA_NAME = "decoded_sample_data:0";
    private static final String INPUT_SAMPLE_RATE_NAME = "decoded_sample_data:1";
    private static final String OUTPUT_NODE_NAME = "labels_softmax";

    private TensorFlowInferenceInterface mInferenceInterface;

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private List<String> mLabels = new ArrayList<String>();

    private Button mButton;
    private TextView mTextView;

    //audio prosesing
    private float[] floatBuffer ;
    private AudioDispatcher dispatcher;
    private MFCC mfcc ;

    //nwww
    private int[] pridiction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_detection);



        mButton = findViewById(R.id.button);
        mTextView = findViewById(R.id.textview);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // record 1 second of audio then send it to the model for recognition
                mButton.setText("Listening...");
                Thread thread = new Thread(AutoDetectionActivity.this);
                thread.start();
            }
        });
        String actualFilename = LABEL_FILENAME.split("file:///android_asset/")[1];
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(getAssets().open(actualFilename)));
            String line;
            while ((line = br.readLine()) != null) {
                mLabels.add(line);
            }
            br.close();
        } catch (IOException e) {
            throw new RuntimeException("Problem reading label file!", e);
        }

        requestMicrophonePermission();

    }

    private void requestMicrophonePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                    new String[]{android.Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
        }
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                bufferSize);

        if (record.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(LOG_TAG, "Audio Record can't initialize!");
            return;
        }
        record.startRecording();

        Log.v(LOG_TAG, "Start recording");

        long shortsRead = 0;
        int recordingOffset = 0;
        short[] audioBuffer = new short[bufferSize / 2];
        short[] recordingBuffer = new short[RECORDING_LENGTH];
        while (shortsRead < RECORDING_LENGTH) { // 1 second of recording
            int numberOfShort = record.read(audioBuffer, 0, audioBuffer.length);

            shortsRead += numberOfShort;
            System.arraycopy(audioBuffer, 0, recordingBuffer, recordingOffset, numberOfShort);
            recordingOffset += numberOfShort;
        }
        record.stop();
        record.release();

        Log.v(LOG_TAG, String.format("Recording stopped. total read: %d", shortsRead));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mButton.setText("Recognizing...");
            }
        });

        Log.i("RECORDING_LENGTH", "run: "+RECORDING_LENGTH);

        /*float[] floatInputBuffer = new float[RECORDING_LENGTH];

        // We need to feed in float values between -1.0f and 1.0f, so divide the signed 16-bit inputs.
        for (int i = 0; i < RECORDING_LENGTH; ++i) {
            floatInputBuffer[i] = recordingBuffer[i] / 32767.0f;
        }


        AssetManager assetManager = getAssets();
        mInferenceInterface = new TensorFlowInferenceInterface(assetManager, MODEL_FILENAME);

        int[] sampleRate = new int[] {SAMPLE_RATE};
        mInferenceInterface.feed(INPUT_SAMPLE_RATE_NAME, sampleRate);

        mInferenceInterface.feed(INPUT_DATA_NAME, floatInputBuffer, RECORDING_LENGTH, 1);

        String[] outputScoresNames = new String[] {OUTPUT_NODE_NAME};
        mInferenceInterface.run(outputScoresNames);

        float[] outputScores = new float[mLabels.size()];
        mInferenceInterface.fetch(OUTPUT_NODE_NAME, outputScores);

        float max = outputScores[0];
        int idx = 0;
        for (int i=1; i<outputScores.length; i++) {
            if (outputScores[i] > max) {
                max = outputScores[i];
                idx = i;
            }
        }
        final String result = mLabels.get(idx);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mButton.setText("Start");
                mTextView.setText(result);
            }
        });*/

    }



    /*public void MFCCForSineTest(int sampleRate,int bufferSize,int bufferOverlap){
        try {
            floatBuffer = TestUtilities.audioBufferSine();
            dispatcher = AudioDispatcherFactory.fromFloatArray(floatBuffer, sampleRate, bufferSize, bufferOverlap);
            mfcc = new MFCC(bufferSize, sampleRate, 40, 50, 300, 3000);
            Log.i("mfcc", "MFCCForSineTest: "+mfcc);
            dispatcher.addAudioProcessor(mfcc);
            dispatcher.addAudioProcessor(new AudioProcessor() {

                @Override
                public void processingFinished() {
                }

                @Override
                public boolean process(AudioEvent audioEvent) {
                    return true;
                }
            });
            dispatcher.run();
        }catch (Exception e){
            String TAGE= "Unsupported";
            Log.e(TAGE, "MFCCForSineTest: "+e.getMessage(),e );
        }

    }*/
}