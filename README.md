## 如何编译成可运行的jar
### 1.设置 Artifacts
打开idea  
1.file->project Structure

2.选中Artifacts  点+号添加

3.弹出选中 jar-> from modules with dependencies

4.选择模块 选择main类  选择META-INF的位置(一定要重新选择,不要用默认)

5.完成

### 运行Artifacts
build->build Artifacts->选中刚刚设置的Artifacts->等待jar生成


## 使用jar
查看命令帮助
java -jar gameSdkTool.jar -h

合并smali环境:

java -jar gameSdkTool.jar -baseSmali "主包smali路径" -channelSmali "渠道框架smali环境" -mergeSmali

可使用 -useChannelRes 覆盖冲突文件(默认不覆盖),-useChannelCode 渠道包的代码优先 等命令


修改smali环境(换包名、图标、应用名):
java -jar gameSdkTool.jar -changeSmali "需要修改的smali路径" -apkConfig "ApkConfig.json的路径"


常用用法已经写成.bat文件了，可以直接使用 [tools](tools)

对于批量打渠道包功能，可使用界面操作 项目地址[https://gitee.com/oneEyeTrue/kmp_apk-sdk-tool_gui](https://gitee.com/oneEyeTrue/kmp_apk-sdk-tool_gui)


## 运行参数
[CommandArgs.java](src/main/java/com/oh/gameSdkTool/CommandArgs.java)


## 配置文件

## ApkConfig.json
[ApkConfig.kt](src/main/java/com/oh/gameSdkTool/bean/ApkConfig.kt)


## SignConfig.json
[SignConfig.kt](src/main/java/com/oh/gameSdkTool/bean/SignConfig.kt)

## ChannelConfig.json
[ChannelConfig.kt](src/main/java/com/oh/gameSdkTool/bean/ChannelConfig.kt)


## 合并时替换启动activity
如果合并的其中一个AndroidManifest.xml存在以下节点
 <meta-data
android:name="Launcher_Activity_Name"
android:value="" />
就代表会删除另一个AndroidManifest.xml的中启动activity（android.intent.category.LAUNCHER） , 且android:value的值也会被替换成删除的activity类名


## 合并前修改内容

private const val assets_merge_ApkConfig = "/assets/mergeApkContent/ApkConfig.json" //存在则触发合并前修改内容
在apk合并前会根据ApkConfig.json修改内容。(主包不会生效)

## 合并时,若开启了文件冲突重命名功能(-isRenameRes,-isRenameClassPackage),可设置不排除文件

### keepClass规则
默认情况下会有自己的keepClassPackage，会保持kotlin， META-INF/services下面的class
通过以下文件设置:
private const val keepClassJson="/assets/keepClassPackage.json"  //冲突不修改的class  Set<String>
```
 ["aaa.bbb","ccc,aaaa"]

```
### keepRes规则
通过以下文件设置:
private const val keepResNameJson="/assets/keepResNamePackage.json" //冲突不修改的res  Map<resType,Set<name>>
```
{
"layout":["activity_main"],
"string":["app_name"]
}
```
