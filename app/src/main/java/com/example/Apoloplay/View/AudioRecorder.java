package com.example.Apoloplay.View;

import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class AudioRecorder {

    private static final String TAG = "Shazam-Recorder";

    private MediaRecorder recorder;
    private final File outputFile;
    private boolean isRecording = false;

    public AudioRecorder(File outputFile) {
        this.outputFile = outputFile;
    }

    public void startRecording() {
        if (isRecording) return;

        recorder = new MediaRecorder();
        try {
            // --- Fonte de áudio ---

            int source = MediaRecorder.AudioSource.MIC;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                source = MediaRecorder.AudioSource.UNPROCESSED;
            } else {
                source = MediaRecorder.AudioSource.VOICE_RECOGNITION;
            }
            recorder.setAudioSource(source);

            // --- Formato e codec ---
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioSamplingRate(44100);
            recorder.setAudioEncodingBitRate(128_000); // 128 kbps é ideal
            recorder.setAudioChannels(1);              // mono: melhor fingerprint

            recorder.setOutputFile(outputFile.getAbsolutePath());

            recorder.prepare();
            recorder.start();
            isRecording = true;

            Log.d(TAG, "Recording started -> " + outputFile.getAbsolutePath());
        } catch (IOException | IllegalStateException e) {
            Log.e(TAG, "startRecording failed", e);
            safeCleanup(true);
        } catch (Exception e) {
            Log.e(TAG, "Unexpected recorder error", e);
            safeCleanup(true);
        }
    }

    public void stopRecording() {
        if (!isRecording || recorder == null) return;

        try {
            recorder.stop();
            Log.d(TAG, "Recording stopped.");
        } catch (RuntimeException e) {
            Log.e(TAG, "stopRecording runtime error", e);
            safeCleanup(true);
            return;
        } finally {
            safeCleanup(false);
        }
    }

    private void safeCleanup(boolean deleteFile) {
        try {
            if (recorder != null) {
                recorder.reset();
                recorder.release();
            }
        } catch (Exception ignored) {}
        recorder = null;
        isRecording = false;
        if (deleteFile && outputFile.exists()) {
            boolean deleted = outputFile.delete();
            Log.d(TAG, "Deleted invalid audio file: " + deleted);
        }
    }

    public boolean isRecording() {
        return isRecording;
    }
}
