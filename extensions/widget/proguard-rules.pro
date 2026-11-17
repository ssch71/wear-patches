# 매니페스트에서 이름으로 참조되는 진입점은 유지
-keep class app.ssch71.extension.widget.ShucleWidgetService { *; }
-keep class app.ssch71.extension.widget.ShucleWidget { *; }
# 나머지(androidx, kotlin, kotlinx)는 전부 이 패키지로 이동
-repackageclasses 'app.ssch71.extension.widget.r'
-allowaccessmodification
-dontwarn **
