chcp 65001
set /p var=请输入apk路径----

set /p var2=请输入SignConfig路径----

set /p var3=请输入apk输出路径----

java -Dfile.encoding=utf-8 -jar  "%cd%\jar\gameSdkTool.jar" -libs "%cd%\libs" -baseApk %var%  -signConfig %var2% -out %var3% -sign

pause