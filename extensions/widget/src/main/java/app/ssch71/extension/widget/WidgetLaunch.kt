/*
 * Copyright (C) 2026 ssch71
 */
package app.ssch71.extension.widget

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.SystemClock

object WidgetLaunch {
    private const val TIMEOUT_MS = 30_000L
    private const val COLD_START_REDELIVER_MS = 3_000L
    const val DRT_HOME = "shucle://drthome"

    @Volatile private var pendingId: String? = null
    @Volatile private var pendingAt = 0L
    private val main = Handler(Looper.getMainLooper())

    private fun take(intent: Intent?): Boolean {
        val id = intent?.getStringExtra(EXTRA_FAVORITE_ID) ?: return false
        pendingId = id
        pendingAt = SystemClock.elapsedRealtime()
        intent.removeExtra(EXTRA_FAVORITE_ID)
        return true
    }

    /** MainActivity.onCreate 시작. 콜드 스타트면 홈 준비 후 딥링크를 한 번 더 보냄 */
    @JvmStatic
    fun onActivityCreated(activity: Activity) {
        if (!take(activity.intent)) return
        main.postDelayed({
            if (pendingId == null || activity.isFinishing || activity.isDestroyed) return@postDelayed
            try {
                activity.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(DRT_HOME))
                        .setComponent(ComponentName(activity, activity.javaClass))
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )
            } catch (_: Throwable) {
            }
        }, COLD_START_REDELIVER_MS)
    }

    /** MainActivity.onNewIntent 시작 (셔클이 이미 떠 있을 때) */
    @JvmStatic
    fun onNewIntent(intent: Intent?) {
        take(intent)
    }

    /** (Favorite, 클릭람다, ...) 형태 칩 컴포저블 진입부. 매 리컴포지션마다 불리니 가볍게 */
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
