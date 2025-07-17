chcp 65001
set /p var=请输入框架包smali环境----

set /p var2=请输入主包smali环境----

java -Dfile.encoding=utf-8 -jar  "%cd%\jar\gameSdkTool.jar" -channelSmali %var% -baseSmali %var2% -mergeSmali

pause