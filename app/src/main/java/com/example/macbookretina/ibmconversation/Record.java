package com.example.macbookretina.ibmconversation;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.Vector;


import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder.AudioSource;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.Log;
import java.util.Vector;

class Record implements Runnable {
    AudioTrack oTrack = null;
    byte[] playData = null;
    AudioRecord aud = null;
    private static final int encoding = AudioFormat.ENCODING_PCM_16BIT;
    private static final int chan = AudioFormat.CHANNEL_IN_MONO;
    private static final int recsrc = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR_MR1) ? MediaRecorder.AudioSource.VOICE_RECOGNITION
            : MediaRecorder.AudioSource.DEFAULT;
    public int samplerate;
    private int bufferSize = 0;
    private int MAXQUEUE = 50;
    private static Vector<ByteBuffer> buffers = new Vector<ByteBuffer>();
    private volatile boolean isRecording = false;
    private final Object mutex = new Object();
    private final Object recordingStopped = new Object();
    public int maxamp = 0;
    private boolean done = false;

    public Record() {
        super();
    }

    public synchronized int getSize() {
        return buffers.size();
    }

    public synchronized ByteBuffer getBuffer() throws InterruptedException {
        while (buffers.size() == 0)
            wait();
        int N = Math.min(buffers.size(), 4);
        int capacity = 0;
        // Grab multiple buffers if available to prevent overflow
        for (int i = 0; i < N; i++) {
            capacity += buffers.elementAt(i).capacity();
        }
        ByteBuffer b = ByteBuffer.allocateDirect(capacity);
        for (int i = 0; i < N; i++) {
            b.put(buffers.firstElement());
            buffers.removeElementAt(0);
        }
        return b;
    }

    private synchronized void addBuffer(ByteBuffer buf, int samples) {
        // Log.i("AAA","addBuffer: size="+buffers.size()+", sample="+samples);
        ShortBuffer sb = buf.asShortBuffer();
        maxamp = 0;
        for (int i = 0; i < samples; i++) {
            int val = (int) Short.reverseBytes(sb.get(i));
            if (val > maxamp)
                maxamp = val;
        }
        if (buffers.size() == MAXQUEUE) {
            stopRecording();
            throw new IllegalStateException("addBuffer: BUFFER OVERFLOW");
        }
        buffers.addElement(buf);
        notify(); // notify waiting getBuffer
    }

    boolean initAudio() {
        aud = null;
        boolean success = false;
        int i = 0;
        int[] samplerates = { 8000,16000, 22050, 44100 }; // NOTE: emulator does not support 16kHz

        for (int j = 0; j < samplerates.length; ++j) {
            samplerate = samplerates[j];
            Log.i("TrulyHandsfreeSDK", "initAudio: trying samplerate=" + samplerate);
            bufferSize = AudioRecord.getMinBufferSize(samplerate, chan, encoding);
            if (bufferSize == AudioRecord.ERROR_BAD_VALUE)
                continue;
            Log.i("TrulyHandsfreeSDK",  "initAudio: (1) bufferSize=" + bufferSize);
            while (bufferSize < 4096) bufferSize *= 2;
            Log.i("TrulyHandsfreeSDK", "initAudio: (2) bufferSize=" + bufferSize);
            while (!success && i < 2) {
                i++;
                try {
                    aud = new AudioRecord(recsrc, samplerate, chan, encoding, bufferSize);
                    success = true;
                } catch (IllegalStateException e) {
                    aud = null;
                    e.printStackTrace();
                    Log.i("MYAUDIO", "Audio init failed: attempt " + i);
                    synchronized (this) {
                        try {
                            wait(250);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
            if (success)
                break;
        }
        return success;
    }

    public void run() {
        int numSamples = 0;
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        while (!done) {
            if (!initAudio())
                break;
            aud.startRecording();
            while (isRecording) {
                ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize * 2);
                numSamples = aud.read(buffer, bufferSize * 2);
                if (numSamples == AudioRecord.ERROR_INVALID_OPERATION) {
                    throw new IllegalStateException(
                            "read() returned AudioRecord.ERROR_INVALID_OPERATION");
                } else if (numSamples == AudioRecord.ERROR_BAD_VALUE) {
                    throw new IllegalStateException(
                            "read() returned AudioRecord.ERROR_BAD_VALUE");
                } else if (numSamples == AudioRecord.ERROR_INVALID_OPERATION) {
                    throw new IllegalStateException(
                            "read() returned AudioRecord.ERROR_INVALID_OPERATION");
                }
                if (numSamples > 0)
                    addBuffer(buffer, numSamples/2);
            }
            aud.stop();
            aud.release();
            aud = null;
            synchronized (recordingStopped) {
                recordingStopped.notifyAll();
            }
            synchronized (mutex) {
                try {
                    mutex.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void startRecording() {
        synchronized (mutex) {
            this.isRecording = true;
            mutex.notify();
        }
    }

    public void stopRecording() {
        synchronized (recordingStopped) {
            if (!this.isRecording)
                return;
            this.isRecording = false;
            try {
                recordingStopped.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isRecording() {
        synchronized (mutex) {
            return isRecording;
        }
    }

    public synchronized void exitAudio() {
        done = true;
        notifyAll();
    }
}
