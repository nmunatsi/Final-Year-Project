package com.biust.ac.bw.panicbutton;

public class TestUtilities {

    /**
     * Constructs and returns a buffer of a two seconds long pure sine of 440Hz
     * sampled at 44.1kHz.
     *
     * @return A buffer of a two seconds long pure sine (440Hz) sampled at
     * 44.1kHz.
     */
    public static float[] audioBufferSine() {
        final double sampleRate = 44100.0;
        final double f0 = 440.0;
        final double amplitudeF0 = 0.5;
        final double seconds = 4.0;
        final float[] buffer = new float[(int) (seconds * sampleRate)];
        for (int sample = 0; sample < buffer.length; sample++) {
            final double time = sample / sampleRate;
            buffer[sample] = (float) (amplitudeF0 * Math.sin(2 * Math.PI * f0 * time));
        }
        return buffer;
    }
}