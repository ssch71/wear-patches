/*
 * Copyright (C) 2026 ssch71
 */
package app.ssch71.patches.shucle

import app.morphe.patcher.patch.patch
import app.morphe.patcher.patch.resourceFile
import app.morphe.patcher.patch.resourceXml
import app.ssch71.patches.shared.Constants.COMPATIBILITY_SHUCLE

private const val WIDGET_SERVICE = "app.ssch71.extension.widget.ShucleWidgetService"

private val WIDGET_INFO = """
    <?xml version="1.0" encoding="utf-8"?>
    <wearwidget-provider
        description="집이나 학교로 셔클을 바로 호출합니다"
        group="shucle"
        icon="@mipmap/ic_launcher"
        label="셔클 호출"
        preferredType="SMALL">
        <container type="SMALL" previewImage="@mipmap/ic_launcher" />
        <container type="LARGE" previewImage="@mipmap/ic_launcher" />
    </wearwidget-provider>
""".trimIndent()

@Suppress("unused")
val wearWidgetPatch = patch(
    name = "Wear Widget",
    description = "Wear OS 7 위젯(반 크기 타일)으로 집/학교 호출 버튼을 추가합니다.",
    default = true
) {
    compatibleWith(COMPATIBILITY_SHUCLE)

    extendWith("extensions/widget.mpe")

    execute {
        resourceFile("res/xml/shucle_widget_info.xml", copy = false).writeText(WIDGET_INFO)

        resourceXml("AndroidManifest.xml").use { manifest ->
            val application = manifest.elements("application").first()

            application.appendChild("uses-library") {
                setAttribute("android:name", "wear-sdk")
                setAttribute("android:required", "false")
            }

            application.appendChild("service") {
                setAttribute("android:name", WIDGET_SERVICE)
                setAttribute("android:exported", "true")
                setAttribute("android:icon", "@mipmap/ic_launcher")
                setAttribute("android:label", "셔클 호출")
                setAttribute("android:permission", "com.google.android.wearable.permission.BIND_TILE_PROVIDER")
                appendChild("intent-filter") {
                    appendChild("action") {
                        setAttribute("android:name", "androidx.glance.wear.action.BIND_WIDGET_PROVIDER")
                    }
                }
                appendChild("meta-data") {
                    setAttribute("android:name", "androidx.glance.wear.widget.provider")
                    setAttribute("android:resource", "@xml/shucle_widget_info")
                }
            }
        }
    }
}
