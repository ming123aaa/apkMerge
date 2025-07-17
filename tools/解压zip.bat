chcp 65001
set /p var=zip路径----

set /p var2=输出的文件夹----

java -Dfile.encoding=utf-8 -jar  "%cd%\jar\gameSdkTool.jar" -basePath %var% -out %var2%  -toUnzip

pause