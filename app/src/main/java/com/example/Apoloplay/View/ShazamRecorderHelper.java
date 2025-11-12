package com.example.Apoloplay.View;

import android.content.Context;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;

public class ShazamRecorderHelper {

    public interface Callbacks {
        void onRecordingStarted();
        void onRecordingFinished(File fileOrNull);
    }

    private final Context ctx;
    private final ImageButton button;
    private final File outputFile;
    private final int recordMs;
    private final Callbacks callbacks;

    private final Handler handler = new Handler();
    private AudioRecorder recorder;
    private boolean busy = false;

    public ShazamRecorderHelper(Context ctx, ImageButton button, File outputFile, int recordMs, Callbacks cb) {
        this.ctx = ctx;
        this.button = button;
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
            if (button != null) button.setEnabled(false);
            recorder.startRecording();
            if (callbacks != null) callbacks.onRecordingStarted();
            handler.postDelayed(this::stop, recordMs);
        } catch (Exception e) {
            Toast.makeText(ctx, "Erro ao iniciar gravação", Toast.LENGTH_SHORT).show();
            cleanup(true);
            if (callbacks != null) callbacks.onRecordingFinished(null);
        }
    }

    public void stop() {
        try { if (recorder != null) recorder.stopRecording(); } catch (Exception ignore) {}
        if (button != null) button.setEnabled(true);

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
