package com.example.Apoloplay.View;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Apoloplay.R;
import com.example.Apoloplay.data.ServiceLocator;
import com.example.Apoloplay.data.model.ShazamResponse;
import com.example.Apoloplay.domain.model.Music;
import com.example.Apoloplay.ui.main.MainViewModel;
import com.example.Apoloplay.ui.main.ShazamViewModel;
import com.example.Apoloplay.ui.main.ShazamViewModelFactory;
import com.example.Apoloplay.ui.player.PlayerViewModel;
import com.example.Apoloplay.ui.trending.CarouselViewModel;
import com.example.Apoloplay.ui.trending.TrendingCarouselAdapter;
import com.example.Apoloplay.utils.Result;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_CODE = 1337;
    private static final int PERMISSION_CODE_RECORD = 101;
    private static final String CLIENT_ID = "7b7105fe7abf4c13911b13a910f79cff";
    private static final String REDIRECT_URI = "com.example.apoloplay://callback";
    public static final String EXTRA_INITIAL_QUERY = "EXTRA_INITIAL_QUERY";

    private static final int TICK_MS = 16;
    private static final int STEP_PX = 4;
    private static final int PAUSE_MS = 1000;
    private static final int SYNC_TICKS = 25;
    private static final int CENTER_TOLERANCE_PX = 6;

    private PlayerViewModel playerVm;
    private MainViewModel mainVm;
    private CarouselViewModel carouselVm;
    private ShazamViewModel shazamVm;

    private View root, searchBar;
    private EditText searchInput;
    private ImageButton searchIcon, shazamButton;

    private RecyclerView rvTrending;
    private TrendingCarouselAdapter adapter;
    private LinearLayoutManager lm;
    private PagerSnapHelper snap;
    private boolean snapAttached = false;

    private final android.os.Handler auto = new android.os.Handler();
    private boolean autoRunning = false;
    private boolean userInteracting = false;
    private int tickCounter = 0;
    private Integer lastPausedCenterPos = null;
    private Integer lastAppliedIndex = null;

    private Runnable pendingAfterLogin;

    private AudioRecorder audioRecorder;
    private File recordedAudioFile;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_auth);

        playerVm   = new ViewModelProvider(this).get(PlayerViewModel.class);
        mainVm     = new ViewModelProvider(this).get(MainViewModel.class);
        carouselVm = new ViewModelProvider(this).get(CarouselViewModel.class);

        ServiceLocator sl = ServiceLocator.getInstance();
        ShazamViewModelFactory shazamFactory = sl.provideShazamViewModelFactory();
        shazamVm = new ViewModelProvider(this, shazamFactory).get(ShazamViewModel.class);

        root        = findViewById(R.id.root);
        searchBar   = findViewById(R.id.search_bar_container);
        searchInput = findViewById(R.id.searchInput);
        searchIcon  = findViewById(R.id.btn_search_icon);
        shazamButton= findViewById(R.id.btn_shazam);

        rvTrending = findViewById(R.id.rv_trending);
        lm = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        rvTrending.setLayoutManager(lm);
        rvTrending.setHasFixedSize(true);
        rvTrending.setOverScrollMode(View.OVER_SCROLL_NEVER);

        adapter = new TrendingCarouselAdapter(this::openDetails);
        rvTrending.setAdapter(adapter);

        snap = new PagerSnapHelper();
        attachSnap();

        rvTrending.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override public void onScrollStateChanged(@NonNull RecyclerView rv, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE && snapAttached) {
                    View s = snap.findSnapView(lm);
                    int n = adapter.logicalSize();
                    if (s != null && n > 0) {
                        int pos = rvTrending.getChildAdapterPosition(s);
                        if (pos != RecyclerView.NO_POSITION) {
                            int idx = pos % n;
                            carouselVm.setIndex(idx);
                            lastAppliedIndex = idx;
                        }
                    }
                }
            }
        });

        rvTrending.setOnTouchListener((v, e) -> {
            int a = e.getActionMasked();
            if (a == MotionEvent.ACTION_DOWN) {
                userInteracting = true;
                stopAuto();
                attachSnap();
            } else if (a == MotionEvent.ACTION_UP || a == MotionEvent.ACTION_CANCEL) {
                userInteracting = false;
                rvTrending.postDelayed(() -> {
                    detachSnap();
                    lastPausedCenterPos = null;
                    startAutoIfOk();
                }, 450);
            }
            return false;
        });

        mainVm.getTrending().observe(this, list -> {
            adapter.submit(list);
            carouselVm.bindList(list);
            if (list != null && !list.isEmpty()) {
                if (lastAppliedIndex == null) {
                    int idx = 0;
                    com.example.Apoloplay.ui.trending.CarouselUiState st = carouselVm.getState().getValue();
                    if (st != null) idx = st.getCurrentIndex();

                    final int anchor = anchorOf(idx);
                    rvTrending.post(() -> lm.scrollToPosition(anchor));
                    lastAppliedIndex = idx;
                }
                detachSnap();
                lastPausedCenterPos = null;
                startAutoIfOk();
            } else {
                stopAuto();
                lastAppliedIndex = null;
            }
        });

        searchInput.setOnEditorActionListener((v, actionId, ev) -> {
            boolean enter = actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (ev != null && ev.getKeyCode() == KeyEvent.KEYCODE_ENTER && ev.getAction() == KeyEvent.ACTION_UP);
            if (enter) {
                String q = searchInput.getText().toString().trim();
                if (!q.isEmpty()) openSearchWithQuery(q);
                return true;
            }
            return false;
        });
        if (searchIcon != null) {
            searchIcon.setOnClickListener(v -> {
                String q = searchInput.getText().toString().trim();
                if (!q.isEmpty()) openSearchWithQuery(q);
            });
        }

        ImageButton btnOpenPlaylists = findViewById(R.id.btn_open_playlists);
        if (btnOpenPlaylists != null) {
            btnOpenPlaylists.setOnClickListener(v ->
                    ensureLoginThen(() -> {
                        playerVm.connect(this);
                        goToPlaylists();
                    })
            );
        }

        if (shazamButton != null) {
            shazamButton.setOnClickListener(v -> checkRecordPermissionAndStartRecognition());
        }

        shazamVm.getIsLoading().observe(this, this::updateShazamLoadingState);
        shazamVm.getRecognitionResult().observe(this, this::handleShazamRecognitionResult);

        installKeyboardVisibilityListener();
    }

    @Override protected void onStart() {
        super.onStart();
        ensureLoginThen(() -> {
            List<Music> cur = mainVm.getTrending().getValue();
            if (cur == null || cur.isEmpty()) {
                mainVm.loadTrending();
            } else {
                if (lastAppliedIndex == null) {

                    int idx = 0;
                    com.example.Apoloplay.ui.trending.CarouselUiState st = carouselVm.getState().getValue();
                    if (st != null) idx = st.getCurrentIndex();

                    final int anchor = anchorOf(idx);
                    rvTrending.post(() -> lm.scrollToPosition(anchor));
                    lastAppliedIndex = idx;
                }
                detachSnap();
                lastPausedCenterPos = null;
                startAutoIfOk();
            }
        });
    }

    @Override protected void onResume() {
        super.onResume();
        detachSnap();
        lastPausedCenterPos = null;
        startAutoIfOk();
    }

    @Override protected void onPause() {
        super.onPause();
        stopAuto();
        if (audioRecorder != null) audioRecorder.stopRecording();
    }
    @Override protected void onStop()  {
        super.onStop();
        stopAuto();
    }

    private void checkRecordPermissionAndStartRecognition() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_CODE_RECORD);
        } else {
            startShazamRecording();
        }
    }

    private void startShazamRecording() {
        if (audioRecorder == null) {
            recordedAudioFile = new File(getCacheDir(), "shazam_recording.wav");
            audioRecorder = new AudioRecorder(recordedAudioFile);
        }

        Toast.makeText(this, "A gravar 5 segundos de áudio...", Toast.LENGTH_SHORT).show();
        shazamButton.setEnabled(false);

        audioRecorder.startRecording();

        auto.postDelayed(this::stopShazamRecordingAndRecognize, 5000);
    }

    private void stopShazamRecordingAndRecognize() {
        audioRecorder.stopRecording();
        shazamButton.setEnabled(true);

        if (recordedAudioFile != null && recordedAudioFile.exists()) {
            shazamVm.startRecognition(recordedAudioFile);
        } else {
            Toast.makeText(this, "Erro: Não foi possível gravar o ficheiro de áudio.", Toast.LENGTH_LONG).show();
        }
    }

    private void updateShazamLoadingState(Boolean isLoading) {
        if (isLoading) {
            shazamButton.setEnabled(false);
            Toast.makeText(this, "Reconhecimento em curso...", Toast.LENGTH_SHORT).show();
        } else {
            shazamButton.setEnabled(true);
        }
    }

    private void handleShazamRecognitionResult(Result<ShazamResponse> result) {
        if (result == null) return;

        if (result instanceof Result.Success) {
            ShazamResponse response = ((Result.Success<ShazamResponse>) result).data;
            if (response != null && response.getTrack() != null) {
                String title = response.getTrack().getTitle();
                String artist = response.getTrack().getSubtitle();
                Toast.makeText(this, "Música Encontrada: " + title + " por " + artist, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Música não reconhecida.", Toast.LENGTH_LONG).show();
            }
        } else if (result instanceof Result.Error) {
            String errorMsg = ((Result.Error<ShazamResponse>) result).exception.getMessage();
            Toast.makeText(this, "Erro no reconhecimento: " + errorMsg, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE_RECORD) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startShazamRecording();
            } else {
                Toast.makeText(this, "Permissão de áudio necessária para o Shazam.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            if (!autoRunning || adapter.getItemCount() == 0) return;
            if (userInteracting) { scheduleNext(TICK_MS); return; }

            rvTrending.scrollBy(STEP_PX, 0);
            tickCounter++;

            if (tickCounter % SYNC_TICKS == 0) {
                int n = adapter.logicalSize();
                if (n > 0) {
                    int pos = lm.findFirstVisibleItemPosition();
                    if (pos >= 0) {
                        int idx = pos % n;
                        carouselVm.setIndex(idx);
                        lastAppliedIndex = idx;
                    }
                }
            }

            if (PAUSE_MS > 0) {
                int centeredPos = getCenteredAdapterPosition();
                if (centeredPos != -1 && !centeredPosEqualsLast(centeredPos)) {
                    lastPausedCenterPos = centeredPos;

                    int n = adapter.logicalSize();
                    if (n > 0) {
                        int idx = centeredPos % n;
                        carouselVm.setIndex(idx);
                        lastAppliedIndex = idx;
                    }

                    scheduleNext(PAUSE_MS);
                    return;
                }
            }

            scheduleNext(TICK_MS);
        }
    };

    private boolean centeredPosEqualsLast(int centeredPos) {
        return lastPausedCenterPos != null && lastPausedCenterPos == centeredPos;
    }

    private int getCenteredAdapterPosition() {
        if (rvTrending.getChildCount() == 0) return -1;

        int rvLeft = rvTrending.getPaddingLeft();
        int rvRight = rvTrending.getWidth() - rvTrending.getPaddingRight();
        int rvCenter = rvLeft + (rvRight - rvLeft) / 2;

        int bestChildIdx = -1;
        int bestDist = Integer.MAX_VALUE;

        for (int i = 0; i < rvTrending.getChildCount(); i++) {
            View child = rvTrending.getChildAt(i);
            if (child == null) continue;
            int childCenter = (child.getLeft() + child.getRight()) / 2;
            int dist = Math.abs(childCenter - rvCenter);
            if (dist < bestDist) {
                bestDist = dist;
                bestChildIdx = rvTrending.getChildAdapterPosition(child);
            }
        }

        if (bestChildIdx == RecyclerView.NO_POSITION) return -1;
        return (bestDist <= CENTER_TOLERANCE_PX) ? bestChildIdx : -1;
    }

    private void scheduleNext(int delay) {
        auto.removeCallbacks(tick);
        auto.postDelayed(tick, delay);
    }

    private void startAutoIfOk() {
        if (autoRunning || adapter.getItemCount() == 0) return;
        autoRunning = true;
        scheduleNext(TICK_MS);
    }

    private void stopAuto() {
        autoRunning = false;
        auto.removeCallbacks(tick);
    }

    private void attachSnap() {
        if (!snapAttached) { snap.attachToRecyclerView(rvTrending); snapAttached = true; }
    }

    private void detachSnap() {
        if (snapAttached) { snap.attachToRecyclerView(null); snapAttached = false; }
    }

    private int anchorOf(int logicalIdx) {
        int n = adapter.logicalSize();
        if (n <= 0) return 0;
        int total = adapter.getItemCount();
        int blocks = total / n;
        int mid = Math.max(0, blocks / 2);
        return mid * n + (logicalIdx % n);
    }

    private void openDetails(Music m) {
        Intent i = new Intent(this, DetailsActivity.class);
        i.putExtra("MUSIC_DETAILS", m);
        startActivity(i);
    }

    private void openSearchWithQuery(String q) {
        Intent i = new Intent(this, SearchActivity.class);
        i.putExtra(EXTRA_INITIAL_QUERY, q);
        startActivity(i);
    }

    private void goToPlaylists() { startActivity(new Intent(this, PlaylistsActivity.class)); }

    private void ensureLoginThen(Runnable afterLogin) {
        String token = ServiceLocator.sessionProvider().getUserAccessToken();
        if (token == null || token.isEmpty()) {
            pendingAfterLogin = afterLogin;
            startSpotifyLogin();
        } else afterLogin.run();
    }

    private void startSpotifyLogin() {
        AuthorizationRequest req = new AuthorizationRequest.Builder(
                CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI
        )
                .setScopes(new String[]{
                        "playlist-read-private",
                        "playlist-modify-public",
                        "playlist-modify-private",
                        "user-read-email",
                        "user-read-private"
                })
                .build();
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
                detachSnap();
                lastPausedCenterPos = null;
                startAutoIfOk();
            } else if (r.getType() == AuthorizationResponse.Type.ERROR) {
                Toast.makeText(this, "Erro ao iniciar sessão: " + r.getError(), Toast.LENGTH_SHORT).show();
                pendingAfterLogin = null;
            }
        }
    }

    private void installKeyboardVisibilityListener() {
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
        int marginPx = dp(12);
        int overlap = (barBottomOnScreen + marginPx) - visibleFrame.bottom;
        if (kbVisible && overlap > 0)
            searchBar.animate().translationY(-overlap).alpha(1f).setDuration(140).start();
        else
            searchBar.animate().translationY(0f).alpha(1f).setDuration(140).start();
    }

    private int dp(int v){ return Math.round(getResources().getDisplayMetrics().density * v); }
}