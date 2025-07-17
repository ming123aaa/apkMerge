chcp 65001
set /p var=请输入主包apk路径----

set /p var1=请输入渠道框架apk路径----

set /p var2=请输入ApkConfig.json路径----

set /p var3=请输入SignConfig.json路径----

set /p var4=请输入文件输出路径----

java -Dfile.encoding=utf-8 -jar  "%cd%\jar\gameSdkTool.jar" -libs "%cd%\libs" -baseApk %var% -channelApk %var1% -apkConfig %var2% -signConfig %var3% -out %var4% -generateMergeChannelApk -channelRes

pause