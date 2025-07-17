chcp 65001
set /p var=文件路径----

set /p var2=输出zip路径----

java -Dfile.encoding=utf-8 -jar  "%cd%\jar\gameSdkTool.jar" -basePath %var% -out %var2%  -toZip

pause