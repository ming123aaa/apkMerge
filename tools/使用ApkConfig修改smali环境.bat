chcp 65001
set /p var=请输入smali环境路径----

set /p var2=请输入ApkConfig.json路径----

java -Dfile.encoding=utf-8 -jar  "%cd%\jar\gameSdkTool.jar" -changeSmali %var% -apkConfig %var2%

pause