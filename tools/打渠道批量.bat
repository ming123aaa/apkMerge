chcp 65001
set /p var=请输入主包apk路径----

set /p var1=请输入channelConfig.json配置路径----

java -Dfile.encoding=utf-8 -jar  "%cd%\jar\gameSdkTool.jar" -libs "%cd%\libs" -baseApk %var%  -channelConfig %var1% -generateMultipleChannelApk

pause