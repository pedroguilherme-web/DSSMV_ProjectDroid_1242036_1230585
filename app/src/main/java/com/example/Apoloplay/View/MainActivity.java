// com/example/Apoloplay/View/MainActivity.java
package com.example.Apoloplay.View;

import android.content.Intent;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Apoloplay.R;
import com.example.Apoloplay.data.ServiceLocator;
import com.example.Apoloplay.domain.model.Music;
import com.example.Apoloplay.ui.main.MainViewModel;
import com.example.Apoloplay.ui.player.PlayerViewModel;
import com.example.Apoloplay.ui.trending.CarouselViewModel;
import com.example.Apoloplay.ui.trending.TrendingCarouselAdapter;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_CODE = 1337;
    private static final String CLIENT_ID = "7b7105fe7abf4c13911b13a910f79cff";
    private static final String REDIRECT_URI = "com.example.apoloplay://callback";
    public static final String EXTRA_INITIAL_QUERY = "EXTRA_INITIAL_QUERY";

    // 🎚️ Auto-scroll config
    private static final int TICK_MS = 16;          // ~60fps
    private static final int STEP_PX = 4;           // velocidade por frame
    private static final int PAUSE_MS = 1000;        // ⏸️ pausa quando o card está no centro (0 = sem pausa)
    private static final int SYNC_TICKS = 25;       // quantos ticks até sincronizar índice (~400ms)
    private static final int CENTER_TOLERANCE_PX = 6; // quão perto do centro tem de estar p/ pausar

    // VMs
    private PlayerViewModel playerVm;
    private MainViewModel mainVm;
    private CarouselViewModel carouselVm;

    // UI
    private View root, searchBar;
    private EditText searchInput;
    private ImageButton searchIcon;

    // Carrossel
    private RecyclerView rvTrending;
    private TrendingCarouselAdapter adapter;
    private LinearLayoutManager lm;
    private PagerSnapHelper snap;
    private boolean snapAttached = false;

    // Motor
    private final android.os.Handler auto = new android.os.Handler();
    private boolean autoRunning = false;
    private boolean userInteracting = false;
    private int tickCounter = 0;
    private Integer lastPausedCenterPos = null; // evita pausar várias vezes no mesmo card

    private Runnable pendingAfterLogin;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_auth);

        // VMs
        playerVm   = new ViewModelProvider(this).get(PlayerViewModel.class);
        mainVm     = new ViewModelProvider(this).get(MainViewModel.class);
        carouselVm = new ViewModelProvider(this).get(CarouselViewModel.class);

        // UI
        root        = findViewById(R.id.root);
        searchBar   = findViewById(R.id.search_bar_container);
        searchInput = findViewById(R.id.searchInput);
        searchIcon  = findViewById(R.id.btn_search_icon);

        // Carrossel
        rvTrending = findViewById(R.id.rv_trending);
        lm = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        rvTrending.setLayoutManager(lm);
        rvTrending.setHasFixedSize(true);
        rvTrending.setOverScrollMode(View.OVER_SCROLL_NEVER);

        adapter = new TrendingCarouselAdapter(this::openDetails);
        rvTrending.setAdapter(adapter);

        snap = new PagerSnapHelper();
        attachSnap(); // ligado para interação manual

        // Listener de scroll → sincroniza índice lógico quando snap está ativo
        rvTrending.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override public void onScrollStateChanged(RecyclerView rv, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE && snapAttached) {
                    View s = snap.findSnapView(lm);
                    int n = adapter.logicalSize();
                    if (s != null && n > 0) {
                        int pos = rvTrending.getChildAdapterPosition(s);
                        if (pos != RecyclerView.NO_POSITION) {
                            carouselVm.setIndex(pos % n);
                        }
                    }
                }
            }
        });

        // Toque → pausa motor & liga snap para o utilizador arrastar
        rvTrending.setOnTouchListener((v, e) -> {
            int a = e.getActionMasked();
            if (a == MotionEvent.ACTION_DOWN) {
                userInteracting = true;
                stopAuto();
                attachSnap();
            } else if (a == MotionEvent.ACTION_UP || a == MotionEvent.ACTION_CANCEL) {
                userInteracting = false;
                // dá tempo ao snap de assentar e a possíveis cliques
                rvTrending.postDelayed(() -> {
                    detachSnap();
                    // limpar último card pausado para permitir pausar no card atual
                    lastPausedCenterPos = null;
                    startAutoIfOk();
                }, 450);
            }
            return false;
        });

        // Dados
        mainVm.getTrending().observe(this, list -> {
            adapter.submit(list);
            carouselVm.bindList(list);
            if (list != null && !list.isEmpty()) {
                Integer idx = carouselVm.getCurrentIndex().getValue();
                if (idx == null) idx = 0;
                final int anchor = anchorOf(idx);
                rvTrending.post(() -> lm.scrollToPosition(anchor));
                detachSnap();
                lastPausedCenterPos = null;
                startAutoIfOk();
            }
        });

        // Pesquisa
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

        installKeyboardVisibilityListener();
    }

    @Override protected void onStart() {
        super.onStart();
        ensureLoginThen(() -> {
            List<Music> cur = mainVm.getTrending().getValue();
            if (cur == null || cur.isEmpty()) {
                mainVm.loadTrending();
            } else {
                Integer idx = carouselVm.getCurrentIndex().getValue();
                if (idx == null) idx = 0;
                final int anchor = anchorOf(idx);
                rvTrending.post(() -> lm.scrollToPosition(anchor));
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

    @Override protected void onPause() { super.onPause(); stopAuto(); }
    @Override protected void onStop()  { super.onStop();  stopAuto(); }

    // ---------------- Auto-scroll com pausa no centro ----------------

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            if (!autoRunning || adapter.getItemCount() == 0) return;
            if (userInteracting) { scheduleNext(TICK_MS); return; }

            // scroll contínuo por px
            rvTrending.scrollBy(STEP_PX, 0);
            tickCounter++;

            // sincroniza índice lógico periodicamente
            if (tickCounter % SYNC_TICKS == 0) {
                int n = adapter.logicalSize();
                if (n > 0) {
                    int pos = lm.findFirstVisibleItemPosition();
                    if (pos >= 0) carouselVm.setIndex(pos % n);
                }
            }

            // pausa APENAS quando um item está centrado
            if (PAUSE_MS > 0) {
                int centeredPos = getCenteredAdapterPosition();
                if (centeredPos != -1 && !centeredPosEqualsLast(centeredPos)) {
                    lastPausedCenterPos = centeredPos;
                    int n = adapter.logicalSize();
                    if (n > 0) carouselVm.setIndex(centeredPos % n);
                    scheduleNext(PAUSE_MS); // respira no centro
                    return; // não agendar tick normal agora
                }
            }

            // sem pausa → segue o fluxo normal
            scheduleNext(TICK_MS);
        }
    };

    private boolean centeredPosEqualsLast(int centeredPos) {
        // Evita pausar múltiplas vezes no mesmo card enquanto continua centrado
        return lastPausedCenterPos != null && lastPausedCenterPos == centeredPos;
    }

    /** Devolve a posição do adapter do item cujo centro está mais próximo do centro do RecyclerView,
     *  desde que dentro de uma tolerância; senão devolve -1. */
    private int getCenteredAdapterPosition() {
        if (rvTrending.getChildCount() == 0) return -1;

        // centro “visual” do RV (desconta paddings)
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

    // --------------- Helpers ---------------

    /** Mapeia um índice lógico (0..N-1) para uma posição no “bloco” do meio (lista virtual infinita). */
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

    // --------------- Auth ---------------

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

    // --------------- Teclado ---------------

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
