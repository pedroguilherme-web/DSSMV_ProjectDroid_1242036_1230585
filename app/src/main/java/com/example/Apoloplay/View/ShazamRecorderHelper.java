package com.example.Apoloplay.View;

import android.os.Handler;
import android.os.Looper;

import java.io.File;

public class ShazamRecorderHelper {

    public interface Callbacks {
        void onRecordingStarted();
        void onRecordingFinished(File fileOrNull);
        default void onError(Exception e) {} // opcional
    }

    private final File outputFile;
    private final int recordMs;
    private final Callbacks callbacks;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private AudioRecorder recorder;
    private boolean busy = false;

    public ShazamRecorderHelper(File outputFile, int recordMs, Callbacks cb) {
        this.outputFile = outputFile;
        this.recordMs = recordMs;
        this.callbacks = cb;
    }

    public boolean isBusy() { return busy; }

    public void start() {
        if (busy) return;
        busy = true;
        recorder = new AudioRecorder(outputFile);
        try {
            recorder.startRecording();
            if (callbacks != null) callbacks.onRecordingStarted();
            handler.postDelayed(this::stop, recordMs);
        } catch (Exception e) {
            cleanup(true);
            if (callbacks != null) {
                callbacks.onError(e);
                callbacks.onRecordingFinished(null);
            }
        }
    }

    public void stop() {
        try { if (recorder != null) recorder.stopRecording(); } catch (Exception ignore) {}
        File result = (outputFile.exists() && outputFile.length() > 0) ? outputFile : null;
        if (callbacks != null) callbacks.onRecordingFinished(result);
        cleanup(false);
    }

    public void cancelNow() {
        cleanup(true);
        if (callbacks != null) callbacks.onRecordingFinished(null);
    }

    private void cleanup(boolean deleteFile) {
        handler.removeCallbacksAndMessages(null);
        busy = false;
        recorder = null;
        if (deleteFile && outputFile.exists()) outputFile.delete();
    }
}
