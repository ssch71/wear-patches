/*
 * Copyright (C) 2026 ssch71
 */
package app.ssch71.extension.widget

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.os.SystemClock

object WidgetLaunch {
    private const val TIMEOUT_MS = 30_000L

    @Volatile private var pendingId: String? = null
    @Volatile private var pendingAt = 0L
    private val main = Handler(Looper.getMainLooper())

    /** MainActivity.onCreate 시작에서 호출 */
    @JvmStatic
    fun onActivityCreated(activity: Activity) {
        val intent = activity.intent ?: return
        val id = intent.getStringExtra(EXTRA_FAVORITE_ID) ?: return
        pendingId = id
        pendingAt = SystemClock.elapsedRealtime()
        intent.removeExtra(EXTRA_FAVORITE_ID)
    }

    /** (Favorite, 클릭람다, ...) 형태 칩 컴포저블 진입부에서 호출. 매 리컴포지션마다 불리니 가볍게 */
    @JvmStatic
    fun onFavoriteChip(favorite: Any?, onClick: Any?) {
        val id = pendingId ?: return
        if (favorite == null || onClick == null) return
        if (SystemClock.elapsedRealtime() - pendingAt > TIMEOUT_MS) {
            pendingId = null
            return
        }
        val favId = try {
            favorite.javaClass.getMethod("getId").invoke(favorite) as? String
        } catch (_: Throwable) {
            null
        } ?: return
        if (favId != id) return

        // Function1.invoke(Object)이 있는 람다일 때만 실행 (Function0 등 다른 콜백은 무시)
        val invoke = try {
            onClick.javaClass.getMethod("invoke", Any::class.java)
        } catch (_: Throwable) {
            null
        } ?: return

        pendingId = null
        main.post {
            try {
                invoke.invoke(onClick, favorite)
            } catch (_: Throwable) {
            }
        }
    }
}
