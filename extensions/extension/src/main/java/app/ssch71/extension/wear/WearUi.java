/*
 * Copyright (C) 2026 ssch71
 */
package app.ssch71.extension.wear;

import android.app.Activity;
import android.app.Application;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Outline;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewOutlineProvider;

@SuppressWarnings({"unused", "deprecation"})
public final class WearUi {
    /** 원형 화면 내접 정사각형 좌우 인셋: (1 - 1/√2) / 2 ≈ 0.146 */
    private static final float ROUND_INSET_RATIO = 0.146f;

    private static float targetWidthDp = 320f;
    private static float verticalInsetRatio = 0.10f;

    private WearUi() {}

    /** 패치 옵션 값. onCreate 전에 호출됨 */
    public static void configure(int widthDp, int verticalInsetPercent) {
        if (widthDp > 0) targetWidthDp = widthDp;
        if (verticalInsetPercent >= 0) verticalInsetRatio = verticalInsetPercent / 100f;
    }

    public static void onCreate(Activity activity) {
        applyDensity(activity);
        if (Build.VERSION.SDK_INT >= 29) {
            activity.registerActivityLifecycleCallbacks(new Callbacks());
        }
    }

    private static boolean isRound(Resources res) {
        return res.getConfiguration().isScreenRound();
    }

    private static void applyDensity(Activity activity) {
        Resources res = activity.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = new Configuration(res.getConfiguration());

        int shortPx = Math.min(dm.widthPixels, dm.heightPixels);
        float contentPx = isRound(res) ? shortPx * (1f - 2f * ROUND_INSET_RATIO) : shortPx;
        float density = contentPx / targetWidthDp;
        int dpi = Math.round(density * 160f);

        dm.density = density;
        dm.scaledDensity = density * conf.fontScale;
        dm.densityDpi = dpi;

        conf.densityDpi = dpi;
        conf.screenWidthDp = Math.round(dm.widthPixels / density);
        conf.screenHeightDp = Math.round(dm.heightPixels / density);
        conf.smallestScreenWidthDp = Math.min(conf.screenWidthDp, conf.screenHeightDp);

        res.updateConfiguration(conf, dm);
    }

    private static void applyRoundInset(Activity activity) {
        Resources res = activity.getResources();
        if (!isRound(res)) return;
        final View content = activity.findViewById(android.R.id.content);
        if (content == null) return;
        DisplayMetrics dm = res.getDisplayMetrics();
        int shortPx = Math.min(dm.widthPixels, dm.heightPixels);
        int h = Math.round(shortPx * ROUND_INSET_RATIO);
        int v = Math.round(shortPx * verticalInsetRatio);
        content.setPadding(h, v, h, v);

        // 인셋 바깥은 베젤과 이어지게 검정
        activity.getWindow().getDecorView().setBackgroundColor(Color.BLACK);

        // 콘텐츠 영역을 둥근 창처럼 클리핑 (density 오버라이드와 무관하게 실제 px 기준)
        final float radius = shortPx * 0.08f;
        content.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(
                        view.getPaddingLeft(),
                        view.getPaddingTop(),
                        view.getWidth() - view.getPaddingRight(),
                        view.getHeight() - view.getPaddingBottom(),
                        radius);
            }
        });
        content.setClipToOutline(true);
    }

    private static final class Callbacks implements Application.ActivityLifecycleCallbacks {
        @Override public void onActivityPostCreated(Activity a, Bundle b) { applyRoundInset(a); }
        @Override public void onActivityCreated(Activity a, Bundle b) {}
        @Override public void onActivityStarted(Activity a) {}
        @Override public void onActivityResumed(Activity a) {}
        @Override public void onActivityPaused(Activity a) {}
        @Override public void onActivityStopped(Activity a) {}
        @Override public void onActivitySaveInstanceState(Activity a, Bundle b) {}
        @Override public void onActivityDestroyed(Activity a) { a.unregisterActivityLifecycleCallbacks(this); }
    }
}
