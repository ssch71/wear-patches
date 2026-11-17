/*
 * Copyright (C) 2026 ssch71
 */
@file:SuppressLint("RestrictedApi")

package app.ssch71.extension.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.compose.remote.creation.compose.action.RemoteAction
import androidx.compose.remote.creation.compose.capture.RemoteImageVector
import androidx.compose.remote.creation.compose.capture.toRemoteImageVector
import androidx.compose.remote.creation.compose.layout.RemoteAlignment
import androidx.compose.remote.creation.compose.layout.RemoteArrangement
import androidx.compose.remote.creation.compose.layout.RemoteBox
import androidx.compose.remote.creation.compose.layout.RemoteColumn
import androidx.compose.remote.creation.compose.layout.RemoteComposable
import androidx.compose.remote.creation.compose.layout.RemoteRow
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.background
import androidx.compose.remote.creation.compose.modifier.clickable
import androidx.compose.remote.creation.compose.modifier.clip
import androidx.compose.remote.creation.compose.modifier.fillMaxSize
import androidx.compose.remote.creation.compose.modifier.fillMaxHeight
import androidx.compose.remote.creation.compose.modifier.fillMaxWidth
import androidx.compose.remote.creation.compose.modifier.padding
import androidx.compose.remote.creation.compose.modifier.size
import androidx.compose.remote.creation.compose.shapes.RemoteCircleShape
import androidx.compose.remote.creation.compose.state.rc
import androidx.compose.remote.creation.compose.state.rdp
import androidx.compose.remote.creation.compose.state.rf
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.glance.wear.AssociateWithGlanceWearWidget
import androidx.glance.wear.GlanceWearWidget
import androidx.glance.wear.GlanceWearWidgetService
import androidx.glance.wear.WearWidgetBrush
import androidx.glance.wear.WearWidgetData
import androidx.glance.wear.WearWidgetDocument
import androidx.glance.wear.core.ContainerInfo
import androidx.glance.wear.core.WearWidgetParams
import androidx.glance.wear.verticalGradient
import androidx.wear.compose.remote.material3.RemoteIcon
import androidx.wear.compose.remote.material3.RemoteMaterialTheme
import androidx.wear.compose.remote.material3.RemoteText

private const val MAIN_ACTIVITY = "com.hyundai.airlab.shucle.presenter.MainActivity"
private const val PENDING_INTENT_ACTION =
    "androidx.compose.remote.creation.compose.action.PendingIntentAction"

const val EXTRA_FAVORITE_ID = "wear_favorite_id"
const val EXTRA_DEST_NAME = "wear_dest_name"
const val EXTRA_DEST_LAT = "wear_dest_lat"
const val EXTRA_DEST_LNG = "wear_dest_lng"

// 셔클 아이콘 배경(#2462F7) 기반, 블러처럼 번지는 그라데이션
private val BG_TOP = Color(0xFF4A86FF)
private val BG_MID = Color(0xFF2462F7)
private val BG_BOTTOM = Color(0xFF1846C4)
private val CIRCLE = Color(0x40001A66)
private val ON_BG = Color(0xFFFFFFFF)

@AssociateWithGlanceWearWidget(ShucleWidget::class)
class ShucleWidgetService : GlanceWearWidgetService() {
    override val widget: GlanceWearWidget = ShucleWidget()
}

class WidgetItem(val label: String, val icon: RemoteImageVector?, val action: RemoteAction)

class ShucleWidget : GlanceWearWidget() {
    override suspend fun provideWidgetData(
        context: Context,
        params: WearWidgetParams,
    ): WearWidgetData {
        val large = params.containerType == ContainerInfo.CONTAINER_TYPE_LARGE
        val slots = if (large) 4 else 2
        val rows = if (large) 2 else 1

        val homeIcon = vector(context, "ico_home_fill")
        val placeIcon = vector(context, "ico_star_fill")
        val items = FavoriteCache.load(context).take(slots).map { fav ->
            WidgetItem(
                label = fav.name.ifBlank { if (fav.type == "HOME") "집" else "장소" },
                icon = if (fav.type == "HOME") homeIcon else placeIcon,
                action = pendingIntentAction { openShucle(context, fav) },
            )
        }

        // 칸(2열 x rows행)에 꽉 맞는 원 지름
        val cellW = (params.widthDp - 8f) / 2f
        val cellH = (params.heightDp - 8f) / rows
        val circleDp = (minOf(cellW, cellH) - 4f).toInt().coerceAtLeast(40)

        return WearWidgetDocument(
            background = WearWidgetBrush.verticalGradient(listOf(BG_TOP.rc, BG_MID.rc, BG_BOTTOM.rc))
        ) {
            ShucleWidgetContent(items, circleDp)
        }
    }
}

private fun vector(context: Context, name: String): RemoteImageVector? = try {
    val id = context.resources.getIdentifier(name, "drawable", context.packageName)
    if (id == 0) null
    else ImageVector.vectorResource(context.theme, context.resources, id).toRemoteImageVector()
} catch (_: Throwable) {
    null
}

/** 셔클을 목적지 정보와 함께 열기. 예약 화면 진입은 다음 단계 패치에서 처리 */
private fun openShucle(context: Context, fav: FavoriteCache.Item): PendingIntent {
    val intent = Intent(Intent.ACTION_MAIN)
        .setComponent(ComponentName(context.packageName, MAIN_ACTIVITY))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        .putExtra(EXTRA_FAVORITE_ID, fav.id)
        .putExtra(EXTRA_DEST_NAME, fav.name)
        .putExtra(EXTRA_DEST_LAT, fav.lat)
        .putExtra(EXTRA_DEST_LNG, fav.lng)
    return PendingIntent.getActivity(
        context,
        fav.id.hashCode(),
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )
}

/** PendingIntentAction 공개 API가 알파마다 바뀌어서 생성자를 직접 사용 */
private fun pendingIntentAction(pendingIntent: () -> PendingIntent): RemoteAction {
    val cls = Class.forName(PENDING_INTENT_ACTION)
    for (ctor in cls.declaredConstructors.sortedBy { it.parameterTypes.size }) {
        val types = ctor.parameterTypes
        if (types.isEmpty()) continue
        val first: Any = when {
            types[0] == PendingIntent::class.java -> pendingIntent()
            Function0::class.java.isAssignableFrom(types[0]) -> pendingIntent
            else -> continue
        }
        val args = arrayOfNulls<Any>(types.size)
        args[0] = first
        for (i in 1 until types.size) {
            args[i] = when {
                List::class.java.isAssignableFrom(types[i]) -> emptyList<Any>()
                types[i] == Int::class.javaPrimitiveType -> 0
                types[i] == Boolean::class.javaPrimitiveType -> false
                else -> null
            }
        }
        try {
            ctor.isAccessible = true
            return ctor.newInstance(*args) as RemoteAction
        } catch (_: ReflectiveOperationException) {
        } catch (_: IllegalArgumentException) {
        }
    }
    error("No usable constructor in $PENDING_INTENT_ACTION")
}

@RemoteComposable
@Composable
private fun FavoriteCircle(item: WidgetItem, circleDp: Int, cellModifier: RemoteModifier) {
    // 칸 전체가 터치 영역
    RemoteBox(
        modifier = cellModifier.clickable(item.action),
        contentAlignment = RemoteAlignment.Center,
    ) {
        RemoteBox(
            modifier = RemoteModifier
                .size(circleDp.rdp)
                .clip(RemoteCircleShape)
                .background(CIRCLE.rc),
            contentAlignment = RemoteAlignment.Center,
        ) {
            RemoteColumn(horizontalAlignment = RemoteAlignment.CenterHorizontally) {
                item.icon?.let {
                    RemoteIcon(
                        imageVector = it,
                        contentDescription = null,
                        modifier = RemoteModifier.size((circleDp * 0.30f).toInt().rdp),
                        tint = ON_BG.rc,
                    )
                }
                RemoteText(text = item.label.rs, color = ON_BG.rc, maxLines = 1)
            }
        }
    }
}

@RemoteComposable
@Composable
fun ShucleWidgetContent(items: List<WidgetItem>, circleDp: Int) {
    RemoteMaterialTheme {
        if (items.isEmpty()) {
            RemoteBox(
                modifier = RemoteModifier.fillMaxSize().padding(12.rdp),
                contentAlignment = RemoteAlignment.Center,
            ) {
                RemoteText(text = "셔클을 한 번 열어 즐겨찾기를 불러오세요".rs, color = ON_BG.rc)
            }
        } else {
            RemoteColumn(
                modifier = RemoteModifier.fillMaxSize().padding(4.rdp),
                verticalArrangement = RemoteArrangement.SpaceEvenly,
                horizontalAlignment = RemoteAlignment.CenterHorizontally,
            ) {
                items.chunked(2).forEach { row ->
                    RemoteRow(
                        modifier = RemoteModifier.fillMaxWidth().weight(1f.rf),
                        verticalAlignment = RemoteAlignment.CenterVertically,
                    ) {
                        row.forEach {
                            FavoriteCircle(
                                it, circleDp,
                                RemoteModifier.weight(1f.rf).fillMaxHeight(),
                            )
                        }
                    }
                }
            }
        }
    }
}
