package com.example.Apoloplay.View;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Apoloplay.domain.model.Music;
import com.example.Apoloplay.ui.trending.CarouselUiState;
import com.example.Apoloplay.ui.trending.CarouselViewModel;
import com.example.Apoloplay.ui.trending.TrendingCarouselAdapter;

import java.util.List;

public class TrendingCarouselHelper {

    private static final int TICK_MS = 16;
    private static final int STEP_PX = 4;
    private static final int PAUSE_MS = 1000;
    private static final int SYNC_TICKS = 25;
    private static final int CENTER_TOLERANCE_PX = 6;

    private final RecyclerView rv;
    private final LinearLayoutManager lm;
    private final TrendingCarouselAdapter adapter;
    private final CarouselViewModel vm;
    private final PagerSnapHelper snap = new PagerSnapHelper();
    private final Handler auto = new Handler();

    private boolean autoRunning = false;
    private boolean userInteracting = false;
    private boolean snapAttached = false;
    private int tickCounter = 0;
    private Integer lastPausedCenterPos = null;
    private Integer lastAppliedIndex = null;

    public TrendingCarouselHelper(RecyclerView rv, LinearLayoutManager lm,
                                  TrendingCarouselAdapter adapter, CarouselViewModel vm) {
        this.rv = rv; this.lm = lm; this.adapter = adapter; this.vm = vm;

        rv.setLayoutManager(lm);
        rv.setHasFixedSize(true);
        rv.setOverScrollMode(View.OVER_SCROLL_NEVER);
        rv.setAdapter(adapter);

        attachSnap();

        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override public void onScrollStateChanged(@NonNull RecyclerView rv, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE && snapAttached) {
                    View s = snap.findSnapView(lm);
                    int n = adapter.logicalSize();
                    if (s != null && n > 0) {
                        int pos = rv.getChildAdapterPosition(s);
                        if (pos != RecyclerView.NO_POSITION) {
                            int idx = pos % n;
                            vm.setIndex(idx);
                            lastAppliedIndex = idx;
                        }
                    }
                }
            }
        });

        rv.setOnTouchListener((v, e) -> {
            int a = e.getActionMasked();
            if (a == MotionEvent.ACTION_DOWN) {
                userInteracting = true;
                stopAuto();
                attachSnap();
            } else if (a == MotionEvent.ACTION_UP || a == MotionEvent.ACTION_CANCEL) {
                userInteracting = false;
                rv.postDelayed(() -> {
                    detachSnap();
                    lastPausedCenterPos = null;
                    startAutoIfOk();
                }, 450);
            }
            return false;
        });
    }

    public void bindAndCenter(List<Music> list) {
        adapter.submit(list);
        vm.bindList(list);
        if (list != null && !list.isEmpty()) {
            if (lastAppliedIndex == null) {
                int idx = 0;
                CarouselUiState st = vm.getState().getValue();
                if (st != null) idx = st.getCurrentIndex();
                final int anchor = anchorOf(idx);
                rv.post(() -> lm.scrollToPosition(anchor));
                lastAppliedIndex = idx;
            }
            detachSnap();
            lastPausedCenterPos = null;
            startAutoIfOk();
        } else {
            stopAuto();
            lastAppliedIndex = null;
        }
    }

    public void onResume() { detachSnap(); lastPausedCenterPos = null; startAutoIfOk(); }
    public void onPauseOrStop() { stopAuto(); }

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            if (!autoRunning || adapter.getItemCount() == 0) return;
            if (userInteracting) { scheduleNext(TICK_MS); return; }

            rv.scrollBy(STEP_PX, 0);
            tickCounter++;

            if (tickCounter % SYNC_TICKS == 0) {
                int n = adapter.logicalSize();
                if (n > 0) {
                    int pos = lm.findFirstVisibleItemPosition();
                    if (pos >= 0) vm.setIndex(pos % n);
                }
            }

            if (PAUSE_MS > 0) {
                int centeredPos = getCenteredAdapterPosition();
                if (centeredPos != -1 && !centeredPosEqualsLast(centeredPos)) {
                    lastPausedCenterPos = centeredPos;
                    int n = adapter.logicalSize();
                    if (n > 0) vm.setIndex(centeredPos % n);
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
        if (rv.getChildCount() == 0) return -1;

        int rvLeft = rv.getPaddingLeft();
        int rvRight = rv.getWidth() - rv.getPaddingRight();
        int rvCenter = rvLeft + (rvRight - rvLeft) / 2;

        int bestChildIdx = -1;
        int bestDist = Integer.MAX_VALUE;

        for (int i = 0; i < rv.getChildCount(); i++) {
            View child = rv.getChildAt(i);
            if (child == null) continue;
            int childCenter = (child.getLeft() + child.getRight()) / 2;
            int dist = Math.abs(childCenter - rvCenter);
            if (dist < bestDist) {
                bestDist = dist;
                bestChildIdx = rv.getChildAdapterPosition(child);
            }
        }
        if (bestChildIdx == RecyclerView.NO_POSITION) return -1;
        return (bestDist <= CENTER_TOLERANCE_PX) ? bestChildIdx : -1;
    }

    private void scheduleNext(int delay) { auto.removeCallbacks(tick); auto.postDelayed(tick, delay); }
    private void startAutoIfOk() { if (!autoRunning && adapter.getItemCount() > 0) { autoRunning = true; scheduleNext(TICK_MS); } }
    private void stopAuto() { autoRunning = false; auto.removeCallbacks(tick); }
    private void attachSnap() { if (!snapAttached) { snap.attachToRecyclerView(rv); snapAttached = true; } }
    private void detachSnap() { if (snapAttached) { snap.attachToRecyclerView(null); snapAttached = false; } }

    private int anchorOf(int logicalIdx) {
        int n = adapter.logicalSize();
        if (n <= 0) return 0;
        int total = adapter.getItemCount();
        int blocks = total / n;
        int mid = Math.max(0, blocks / 2);
        return mid * n + (logicalIdx % n);
    }
}
