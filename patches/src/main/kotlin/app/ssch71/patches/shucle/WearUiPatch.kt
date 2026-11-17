/*
 * Copyright (C) 2026 ssch71
 */
package app.ssch71.patches.shucle

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.intOption
import app.morphe.patcher.patch.patch
import app.ssch71.patches.shared.Constants.COMPATIBILITY_SHUCLE

private const val EXTENSION_CLASS = "Lapp/ssch71/extension/wear/WearUi;"

@Suppress("unused")
val wearUiPatch = patch(
    name = "Wear UI",
    description = "원형 워치 화면에 맞게 density를 낮추고 원형 인셋 패딩을 적용합니다.",
    default = true
) {
    compatibleWith(COMPATIBILITY_SHUCLE)

    extendWith("extensions/extension.mpe")

    val targetWidthDp by intOption(
        key = "targetWidthDp",
        default = 320,
        values = mapOf(
            "크게 (260dp)" to 260,
            "보통 (300dp)" to 300,
            "작게 (320dp)" to 320,
            "아주 작게 (360dp)" to 360,
        ),
        title = "화면 폭",
        description = "폰 UI를 몇 dp 폭으로 보여줄지. 클수록 글씨는 작아지고 한 화면에 더 많이 들어갑니다.",
        required = true,
        validator = { it != null && it in 180..480 },
    )

    val verticalInsetPercent by intOption(
        key = "verticalInsetPercent",
        default = 10,
        values = mapOf(
            "없음 (0%)" to 0,
            "얇게 (5%)" to 5,
            "보통 (10%)" to 10,
            "원형 내접 (15%)" to 15,
        ),
        title = "상하 여백",
        description = "원형 화면 위아래 여백 비율. 작을수록 세로 공간이 늘지만 모서리가 잘릴 수 있습니다.",
        required = true,
        validator = { it != null && it in 0..20 },
    )

    execute {
        // 메서드 시작 시점이라 로컬 v0, v1은 비어 있음
        ShucleMainActivityOnCreateFingerprint.method.addInstructions(
            0,
            """
                const/16 v0, ${targetWidthDp!!}
                const/16 v1, ${verticalInsetPercent!!}
                invoke-static {v0, v1}, ${EXTENSION_CLASS}->configure(II)V
                invoke-static/range {p0 .. p0}, ${EXTENSION_CLASS}->onCreate(Landroid/app/Activity;)V
            """
        )
    }
}
