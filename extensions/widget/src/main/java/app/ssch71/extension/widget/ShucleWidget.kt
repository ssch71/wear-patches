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
import androidx.compose.remote.creation.compose.action.rememberPendingIntentAction
import androidx.compose.remote.creation.compose.layout.RemoteAlignment
import androidx.compose.remote.creation.compose.layout.RemoteArrangement
import androidx.compose.remote.creation.compose.layout.RemoteColumn
import androidx.compose.remote.creation.compose.layout.RemoteComposable
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.fillMaxSize
import androidx.compose.remote.creation.compose.modifier.fillMaxWidth
import androidx.compose.remote.creation.compose.modifier.padding
import androidx.compose.remote.creation.compose.state.rdp
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.runtime.Composable
import androidx.glance.wear.AssociateWithGlanceWearWidget
import androidx.glance.wear.GlanceWearWidget
import androidx.glance.wear.GlanceWearWidgetService
import androidx.glance.wear.WearWidgetBrush
import androidx.glance.wear.WearWidgetData
import androidx.glance.wear.WearWidgetDocument
import androidx.glance.wear.color
import androidx.glance.wear.core.ContainerInfo
import androidx.glance.wear.core.WearWidgetParams
import androidx.wear.compose.remote.material3.RemoteButton
import androidx.wear.compose.remote.material3.RemoteColorScheme
import androidx.wear.compose.remote.material3.RemoteMaterialTheme
import androidx.wear.compose.remote.material3.RemoteText

private const val MAIN_ACTIVITY = "com.hyundai.airlab.shucle.presenter.MainActivity"
const val EXTRA_WEAR_CALL = "wear_call"

@AssociateWithGlanceWearWidget(ShucleWidget::class)
class ShucleWidgetService : GlanceWearWidgetService() {
    override val widget: GlanceWearWidget = ShucleWidget()
}

class ShucleWidget : GlanceWearWidget() {
    override suspend fun provideWidgetData(
        context: Context,
        params: WearWidgetParams,
    ): WearWidgetData {
        val large = params.containerType == ContainerInfo.CONTAINER_TYPE_LARGE
        return WearWidgetDocument(
            background = WearWidgetBrush.color(RemoteColorScheme().surfaceContainer)
        ) {
            ShucleWidgetContent(large)
        }
    }
}

/** 셔클 자신을 extra와 함께 열기. 실제 호출 처리는 다음 단계 패치에서 */
private fun callPendingIntent(context: Context, target: String): PendingIntent {
    val intent = Intent(Intent.ACTION_MAIN)
        .setComponent(ComponentName(context.packageName, MAIN_ACTIVITY))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        .putExtra(EXTRA_WEAR_CALL, target)
    return PendingIntent.getActivity(
        context,
        target.hashCode(),
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )
}

@RemoteComposable
@Composable
private fun CallButton(label: String, target: String) {
    RemoteButton(
        onClick = rememberPendingIntentAction { ctx -> callPendingIntent(ctx, target) },
        modifier = RemoteModifier.fillMaxWidth(),
    ) {
        RemoteText(text = label.rs)
    }
}

@RemoteComposable
@Composable
fun ShucleWidgetContent(large: Boolean) {
    RemoteMaterialTheme {
        RemoteColumn(
            modifier = RemoteModifier.fillMaxSize().padding(8.rdp),
            verticalArrangement = RemoteArrangement.spacedBy(6.rdp),
            horizontalAlignment = RemoteAlignment.CenterHorizontally,
        ) {
            CallButton("집으로", "home")
            if (large) {
                CallButton("학교로", "school")
            }
        }
    }
}
