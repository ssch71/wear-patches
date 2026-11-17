/*
 * Copyright (C) 2026 ssch71
 */
package app.ssch71.patches.shucle

import app.morphe.patcher.Fingerprint

object ShucleMainActivityOnCreateFingerprint : Fingerprint(
    definingClass = "Lcom/hyundai/airlab/shucle/presenter/MainActivity;",
    name = "onCreate",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
)

/** ridergwv1.Place$Favorite(서버 프로토) → 도메인 Favorite 매퍼. 프로토 클래스명이라 업데이트에도 안정적 */
object FavoriteMapperFingerprint : Fingerprint(
    returnType = "Lcom/hyundai/airlab/shucle/core/domain/model/Favorite;",
    parameters = listOf("Lridergwv1/Place\$Favorite;"),
)
