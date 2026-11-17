# 매니페스트에서 이름으로 참조되는 진입점은 유지
-keep class app.ssch71.extension.widget.ShucleWidgetService { *; }
-keep class app.ssch71.extension.widget.ShucleWidget { *; }
# 나머지(androidx, kotlin, kotlinx)는 전부 이 패키지로 이동
-repackageclasses 'app.ssch71.extension.widget.r'
-allowaccessmodification
-dontwarn **
# 리플렉션으로 생성하는 액션 클래스는 이름과 생성자 유지
-keep class androidx.compose.remote.creation.compose.action.PendingIntentAction { <init>(...); }
# 셔클 바이트코드에서 직접 호출
-keep class app.ssch71.extension.widget.FavoriteCache { *; }
