/*
package com.biust.ac.bw.panicbutton;

import org.junit.Test;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.mfcc.MFCC;

public class MFCCTest {
    private static int counter = 0;

    @Test
    public void MFCCForSineTest(){
        int sampleRate = 44100;
        int bufferSize = 1024;
        int bufferOverlap = 128;
        final float[] floatBuffer = TestUtilities.audioBufferSine();
        final AudioDispatcher dispatcher = AudioDispatcherFactory.fromFloatArray(floatBuffer, sampleRate, bufferSize, bufferOverlap);
        final MFCC mfcc = new MFCC(bufferSize, sampleRate, 40, 50, 300, 3000);
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
    }

}*/
