/*
 * Copyright (C) 2026 ssch71
 */
package app.ssch71.extension.widget

import android.app.Application
import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** 셔클이 서버에서 받은 즐겨찾기를 위젯용으로 저장. 셔클 클래스는 리플렉션으로만 접근 */
object FavoriteCache {
    private const val PREFS = "ssch71_wear_widget"
    private const val KEY = "favorites"
    private const val KEY_DRT_IDS = "drt_ids"

    data class Item(
        val id: String,
        val name: String,
        val type: String,
        val lat: Double,
        val lng: Double,
        val firstSeen: Long,
    )

    /** dgd.a(ridergwv1.Place$Favorite) 같은 매퍼의 반환 직전에 호출됨 */
    @JvmStatic
    fun record(favorite: Any?) {
        if (favorite == null) return
        try {
            val context = application() ?: return
            val id = favorite.call("getId") as? String ?: return
            val type = favorite.call("getType")?.toString() ?: return
            if (type == "TRIP") return
            val place = favorite.call("getPlace") ?: return
            val lngLat = place.call("getLngLat") ?: return
            val lat = lngLat.call("getLatitude") as? Double ?: return
            val lng = lngLat.call("getLongitude") as? Double ?: return
            val name = (favorite.call("getDisplayName") as? String)?.takeIf { it.isNotBlank() }
                ?: (place.call("getDisplayName") as? String).orEmpty()

            synchronized(this) {
                val items = load(context).associateBy { it.id }.toMutableMap()
                val old = items[id]
                items[id] = Item(id, name, type, lat, lng, old?.firstSeen ?: System.currentTimeMillis())
                save(context, items.values)
            }
        } catch (_: Throwable) {
        }
    }

    /** AppConfig.ModalityMenu 생성자 시작에서 호출. DRT 메뉴의 서비스 ID 저장 */
    @JvmStatic
    fun recordMenu(ids: Any?, menuType: Any?) {
        try {
            if (menuType?.toString() != "MENU_TYPE_DRT") return
            val list = (ids as? List<*>)?.mapNotNull { (it as? String)?.takeIf(String::isNotBlank) }
            if (list.isNullOrEmpty()) return
            val context = application() ?: return
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(KEY_DRT_IDS, list.joinToString(",")).apply()
        } catch (_: Throwable) {
        }
    }

    fun drtIds(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_DRT_IDS, null)

    /** 집이 먼저, 나머지는 처음 등록된 순서 */
    fun load(context: Context): List<Item> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null)
            ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            (0 until array.length()).map { i ->
                val o = array.getJSONObject(i)
                Item(
                    o.getString("id"), o.getString("name"), o.getString("type"),
                    o.getDouble("lat"), o.getDouble("lng"), o.optLong("firstSeen"),
                )
            }.sortedWith(compareBy<Item>({ if (it.type == "HOME") 0 else 1 }, { it.firstSeen }))
        } catch (_: Throwable) {
            emptyList()
        }
    }

    private fun save(context: Context, items: Collection<Item>) {
        val array = JSONArray()
        items.forEach {
            array.put(
                JSONObject()
                    .put("id", it.id).put("name", it.name).put("type", it.type)
                    .put("lat", it.lat).put("lng", it.lng).put("firstSeen", it.firstSeen)
            )
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY, array.toString()).apply()
    }

    private fun application(): Context? = try {
        Class.forName("android.app.ActivityThread")
            .getMethod("currentApplication").invoke(null) as? Application
    } catch (_: Throwable) {
        null
    }

    private fun Any.call(name: String): Any? = javaClass.getMethod(name).invoke(this)
}
