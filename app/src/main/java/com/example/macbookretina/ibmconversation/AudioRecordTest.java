package com.example.macbookretina.ibmconversation;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.media.RemoteControlClient.OnPlaybackPositionUpdateListener;
import java.io.DataInputStream;

/**
 * @author <a href="http://www.benmccann.com">Ben McCann</a>
 */
public class AudioRecordTest {

    private MediaRecorder recorder = new MediaRecorder();
    final String path;

    /**
     * Creates a new audio recording at the given path (relative to root of SD card).
     */
    public AudioRecordTest(String path) {
        this.path = path;
    }

    /**
     * Starts a new recording.
     */
    public void start() throws IOException {
        File output = new File(path);
        if (output != null) {
            output.delete();
        }

        String state = android.os.Environment.getExternalStorageState();
        if(!state.equals(android.os.Environment.MEDIA_MOUNTED))  {
            throw new IOException("SD Card is not mounted.  It is " + state + ".");
        }

        // make sure the directory we plan to store the recording in exists
        File directory = new File(path).getParentFile();
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Path to file could not be created.");
        }

        recorder = new MediaRecorder();

        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(path);
        recorder.prepare();
        recorder.start();
    }

    public void audioPlay(String filePath) {
        MediaPlayer m = new MediaPlayer();
        try {
            m.setDataSource(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            m.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        m.start();
        m.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer m) {
                m.release();
            }
        });
    }

    public void playMedia(byte[] buffer) throws IOException {
        File output = new File(path);
        output.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(output);
        System.out.println(buffer[0]);
        fos.write(buffer);
        fos.close();

        MediaPlayer mediaPlayer = new MediaPlayer();

        FileInputStream myFile = new FileInputStream(output);
        mediaPlayer.setDataSource(myFile.getFD());

        mediaPlayer.prepare();
        mediaPlayer.start();
    }

    public void playMedia2(byte[] buffer) {
        File output = new File(path);
        if (output != null) {
            output.delete();
        }

        String state = android.os.Environment.getExternalStorageState();
        if(!state.equals(android.os.Environment.MEDIA_MOUNTED))  {
            System.out.println("HEY");
        }

        // make sure the directory we plan to store the recording in exists
        File directory = new File(path).getParentFile();
        if (!directory.exists() && !directory.mkdirs()) {
            System.out.println("Wrong");
        }
        int minBufferSize = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        int bufferSize = 512;
        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, 8000, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize, AudioTrack.MODE_STREAM);
        String filepath = path;
        int count = 0;
        byte[] data = new byte[bufferSize];
        try {
            FileInputStream fileInputStream = new FileInputStream(filepath);
            DataInputStream dataInputStream = new DataInputStream(fileInputStream);
            audioTrack.play();

            while ((count = dataInputStream.read(data, 0, bufferSize)) > -1 ) {
                audioTrack.write(data, 0, count);
            }
            audioTrack.stop();
            audioTrack.release();
            dataInputStream.close();
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Stops a recording that has been previously started.
     */
    public void stop() throws IOException {
        recorder.stop();
        recorder.release();
    }

}