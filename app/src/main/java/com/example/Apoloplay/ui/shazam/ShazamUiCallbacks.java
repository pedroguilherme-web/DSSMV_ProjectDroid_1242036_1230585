package com.example.Apoloplay.ui.shazam;

import android.widget.ImageButton;

import com.example.Apoloplay.View.ShazamRecorderHelper;
import com.example.Apoloplay.ui.main.ShazamViewModel;

import java.io.File;
import java.util.function.Consumer;

public class ShazamUiCallbacks implements ShazamRecorderHelper.Callbacks {

    private final ShazamViewModel shazamVm;
    private final ImageButton shazamButton;
    private final Consumer<String> showToast;

    public ShazamUiCallbacks(ShazamViewModel vm,
                             ImageButton shazamButton,
                             Consumer<String> showToast) {
        this.shazamVm = vm;
        this.shazamButton = shazamButton;
        this.showToast = showToast;
    }

    @Override
    public void onRecordingStarted() {
        shazamVm.startRecording();
        if (shazamButton != null) shazamButton.setEnabled(false);
        if (showToast != null) showToast.accept("A gravar ~12 segundos…");
    }

    @Override
    public void onRecordingFinished(File f) {
        if (shazamButton != null) shazamButton.setEnabled(true);

        if (f != null) {
            shazamVm.startRecognition(f);
        } else if (showToast != null) {
            showToast.accept("Erro: áudio vazio ou inválido.");
        }
    }
}
