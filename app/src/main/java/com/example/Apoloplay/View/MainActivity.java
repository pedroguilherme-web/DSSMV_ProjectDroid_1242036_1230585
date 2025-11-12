package com.example.Apoloplay.View;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Apoloplay.R;
import com.example.Apoloplay.data.ServiceLocator;
import com.example.Apoloplay.domain.model.Music;
import com.example.Apoloplay.ui.main.MainViewModel;
import com.example.Apoloplay.ui.main.ShazamViewModel;
import com.example.Apoloplay.ui.player.PlayerViewModel;
import com.example.Apoloplay.ui.shazam.ShazamUiState;
import com.example.Apoloplay.ui.trending.CarouselViewModel;
import com.example.Apoloplay.ui.trending.TrendingCarouselAdapter;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // ---- Spotify Auth ----
    private static final int REQ_CODE = 1337;
    private static final String CLIENT_ID = "7b7105fe7abf4c13911b13a910f79cff";
    private static final String REDIRECT_URI = "com.example.apoloplay://callback";

    // ---- Search ----
    public static final String EXTRA_INITIAL_QUERY = "EXTRA_INITIAL_QUERY";

    private PlayerViewModel playerVm;
    private MainViewModel mainVm;
    private CarouselViewModel carouselVm;
    private ShazamViewModel shazamVm;

    // Views
    private View root, searchBar;
    private EditText searchInput;
    private ImageButton searchIcon, shazamButton, btnOpenPlaylists;

    // Carrossel
    private RecyclerView rvTrending;
    private TrendingCarouselAdapter adapter;
    private LinearLayoutManager lm;
    private TrendingCarouselHelper carouselHelper;

    // Shazam
    private ShazamRecorderHelper shazamHelper;
    private File recordedAudioFile;

    private Runnable pendingAfterLogin;

    // Permissões (microfone)
    private final ActivityResultLauncher<String> micPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) startShazamCapture();
                else toast("Permissão de áudio necessária para o Shazam.");
            });

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_auth);

        // VMs
        playerVm   = new ViewModelProvider(this).get(PlayerViewModel.class);
        mainVm     = new ViewModelProvider(this).get(MainViewModel.class);
        carouselVm = new ViewModelProvider(this).get(CarouselViewModel.class);
        shazamVm   = new ViewModelProvider(this).get(ShazamViewModel.class);

        // Views
        root            = findViewById(R.id.root);
        searchBar       = findViewById(R.id.search_bar_container);
        searchInput     = findViewById(R.id.searchInput);
        searchIcon      = findViewById(R.id.btn_search_icon);
        shazamButton    = findViewById(R.id.btn_shazam);
        btnOpenPlaylists= findViewById(R.id.btn_open_playlists);

        // Carrossel
        rvTrending = findViewById(R.id.rv_trending);
        lm = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        adapter = new TrendingCarouselAdapter(this::openDetails);
        carouselHelper = new TrendingCarouselHelper(rvTrending, lm, adapter, carouselVm);

        // Search
        if (searchIcon != null) searchIcon.setOnClickListener(v -> openSearchIfAny());
        if (searchInput != null) {
            searchInput.setOnEditorActionListener((v, actionId, ev) -> {
                boolean enter = actionId == EditorInfo.IME_ACTION_SEARCH ||
                        (ev != null && ev.getKeyCode() == KeyEvent.KEYCODE_ENTER && ev.getAction() == KeyEvent.ACTION_UP);
                if (enter) { openSearchIfAny(); return true; }
                return false;
            });
        }

        // Playlists
        if (btnOpenPlaylists != null) {
            btnOpenPlaylists.setOnClickListener(v -> ensureLoginThen(() -> {
                playerVm.connect(this);
                startActivity(new Intent(this, PlaylistsActivity.class));
            }));
        }

        // Shazam
        recordedAudioFile = new File(getCacheDir(), "shazam_recording.m4a");
        shazamHelper = new ShazamRecorderHelper(
                this, shazamButton, recordedAudioFile, 12_000,
                new ShazamRecorderHelper.Callbacks() {
                    @Override public void onRecordingStarted() {
                        shazamVm.startRecording();
                        toast("A gravar ~12 segundos…");
                    }
                    @Override public void onRecordingFinished(File f) {
                        if (f != null) shazamVm.startRecognition(f);
                        else toast("Erro: áudio vazio ou inválido.");
                    }
                });
        if (shazamButton != null) {
            shazamButton.setOnClickListener(v -> {
                if (shazamHelper.isBusy()) return;
                checkMicPermissionAndStart();
            });
        }

        // Observers
        mainVm.getTrending().observe(this, this::renderTrending);
        shazamVm.getState().observe(this, this::renderShazam);

        installKeyboardVisibilityListener();

        // arranque: login + trending
        ensureLoginThen(this::ensureTrendingLoaded);
    }

    // Ciclo de vida
    @Override protected void onResume() { super.onResume(); carouselHelper.onResume(); }
    @Override protected void onPause()  { super.onPause();  carouselHelper.onPauseOrStop(); }
    @Override protected void onStop()   { super.onStop();   carouselHelper.onPauseOrStop(); }
    @Override protected void onDestroy(){ super.onDestroy(); if (shazamHelper!=null) shazamHelper.cancelNow(); }

    // Render
    private void renderTrending(List<Music> list) { carouselHelper.bindAndCenter(list); }

    private void renderShazam(ShazamUiState state) {
        if (state == null) return;

        boolean enable = state.getStatus() == ShazamUiState.Status.IDLE
                || state.getStatus() == ShazamUiState.Status.DATA
                || state.getStatus() == ShazamUiState.Status.ERROR;
        if (shazamButton != null) shazamButton.setEnabled(enable);

        switch (state.getStatus()) {
            case RECORDING: toastOnce("A gravar áudio…"); break;
            case LOADING:   toastOnce("A reconhecer…");   break;
            case DATA:
                Music m = state.getMusic();
                toastOnce(m != null ? "Música: " + m.getTitle() + " · " + m.getArtist()
                        : "Música não reconhecida.");
                break;
            case ERROR:
                toastOnce("Erro: " + (state.getError() != null ? state.getError() : "desconhecido"));
                break;
            default: break; // IDLE
        }
    }

    // Spotify Auth
    private void ensureLoginThen(Runnable afterLogin) {
        String token = ServiceLocator.sessionProvider().getUserAccessToken();
        if (token == null || token.isEmpty()) {
            startSpotifyLogin();
            pendingAfterLogin = afterLogin;
        } else afterLogin.run();
    }

    private void startSpotifyLogin() {
        AuthorizationRequest req = new AuthorizationRequest.Builder(
                CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI)
                .setScopes(new String[]{
                        "playlist-read-private",
                        "playlist-modify-public",
                        "playlist-modify-private",
                        "user-read-email",
                        "user-read-private"
                }).build();
        AuthorizationClient.openLoginActivity(this, REQ_CODE, req);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQ_CODE) {
            AuthorizationResponse r = AuthorizationClient.getResponse(resultCode, intent);
            if (r.getType() == AuthorizationResponse.Type.TOKEN) {
                ServiceLocator.sessionProvider().setUserAccessToken(r.getAccessToken());
                if (pendingAfterLogin != null) { pendingAfterLogin.run(); pendingAfterLogin = null; }
                carouselHelper.onResume();
            } else if (r.getType() == AuthorizationResponse.Type.ERROR) {
                toast("Erro ao iniciar sessão: " + r.getError());
                pendingAfterLogin = null;
            }
        }
    }

    // Search
    private void openSearchIfAny() {
        String q = (searchInput != null) ? searchInput.getText().toString().trim() : "";
        if (!q.isEmpty()) {
            Intent i = new Intent(this, SearchActivity.class);
            i.putExtra(EXTRA_INITIAL_QUERY, q);
            startActivity(i);
        }
    }

    // Shazam permissões
    private void checkMicPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            startShazamCapture();
        } else {
            micPermission.launch(Manifest.permission.RECORD_AUDIO);
        }
    }

    private void startShazamCapture() {
        toastOnce("A gravar ~12 segundos...");
        shazamHelper.start();
    }

    // Trending boot
    private void ensureTrendingLoaded() {
        List<Music> cur = mainVm.getTrending().getValue();
        if (cur == null || cur.isEmpty()) mainVm.loadTrending();
        else carouselHelper.onResume();
    }

    // Navegação / util
    private void openDetails(Music m) {
        Intent i = new Intent(this, DetailsActivity.class);
        i.putExtra("MUSIC_DETAILS", m);
        startActivity(i);
    }

    private void installKeyboardVisibilityListener() {
        if (root == null || searchBar == null) return;
        root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override public void onGlobalLayout() {
                Rect r = new Rect();
                root.getWindowVisibleDisplayFrame(r);
                int screenHeight = root.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;
                boolean kbVisible = keypadHeight > screenHeight * 0.15;
                adjustSearchBarForVisibleFrame(r, kbVisible);
            }
        });
    }

    private void adjustSearchBarForVisibleFrame(Rect visibleFrame, boolean kbVisible) {
        int[] loc = new int[2];
        searchBar.getLocationOnScreen(loc);
        int barBottomOnScreen = loc[1] + searchBar.getHeight();
        int marginPx = Math.round(getResources().getDisplayMetrics().density * 12);
        int overlap = (barBottomOnScreen + marginPx) - visibleFrame.bottom;
        if (kbVisible && overlap > 0)
            searchBar.animate().translationY(-overlap).alpha(1f).setDuration(140).start();
        else
            searchBar.animate().translationY(0f).alpha(1f).setDuration(140).start();
    }

    private String lastToastMsg = null;
    private void toastOnce(String msg){ if (msg!=null && !msg.equals(lastToastMsg)) { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show(); lastToastMsg = msg; } }
    private void toast(String msg){ Toast.makeText(this, msg, Toast.LENGTH_SHORT).show(); }
}
