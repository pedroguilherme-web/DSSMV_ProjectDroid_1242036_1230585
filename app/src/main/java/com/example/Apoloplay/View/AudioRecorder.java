package com.example.Apoloplay.View;

import android.media.MediaRecorder;
import java.io.File;
import java.io.IOException;

public class AudioRecorder {

    private MediaRecorder recorder;
    private final File outputFile;
    private boolean isRecording = false;

    public AudioRecorder(File outputFile) {
        this.outputFile = outputFile;
    }

    public void startRecording() {
        if (isRecording) {
            return;
        }

        recorder = new MediaRecorder();

        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(outputFile.getAbsolutePath());

        try {
            recorder.prepare();
            recorder.start();
            isRecording = true;
        } catch (IOException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        if (!isRecording) {
            return;
        }

        try {
            recorder.stop();
            recorder.release();
        } catch (RuntimeException e) {
            e.printStackTrace();
            if (outputFile.exists()) {
                outputFile.delete();
            }
        } finally {
            recorder = null;
            isRecording = false;
        }
    }

    public boolean isRecording() {
        return isRecording;
    }
}