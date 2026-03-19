
## 工具介绍

通过使用gui界面来操作[apkMerge_gui](https://github.com/ming123aaa/apkMerge_gui)

[aarMergeTool](https://github.com/ming123aaa/aarMergeTool) :支持aar合并,apk转aar
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
常用用法已经写成.bat文件了，可以直接使用 [tools](tools)
其中[tools/jar/gameSdkTool.jar](tools/jar/gameSdkTool.jar)可能不是最新版，需要替换成最新版

注意:如果开启了windows实时保护功能,可能会让程序执行起来很慢,需要将操作的文件夹设置为windows安全中心的排除项。
打开“设置” > “更新和安全” > “Windows 安全中心” > “病毒和威胁防护”。
在“病毒和威胁防护设置”下点击“管理设置”。
滚动到“排除项”，然后点击“添加或删除排除项”。你可以添加特定的文件夹、文件类型或进程。


## 运行参数
[CommandArgs.java](src/main/java/com/oh/gameSdkTool/CommandArgs.java)


## 配置文件

### ApkConfig.json
用于修改apk内容的配置文件
[ApkConfig.kt](src/main/java/com/oh/gameSdkTool/bean/ApkConfig.kt)


### SignConfig.json
apk签名配置文件
[SignConfig.kt](src/main/java/com/oh/gameSdkTool/bean/SignConfig.kt)

### ChannelConfig.json
apk渠道配置文件,用于批量生成apk
[ChannelConfig.kt](src/main/java/com/oh/gameSdkTool/bean/ChannelConfig.kt)

## 合并魔法
### 合并时application只能有一个,用于保存被替换的application

<meta-data
android:name="Application_Name"
android:value="" />
合并后Application_Name会记录application,(多个application会用,分割。从头部开始添加)

### 合并时替换启动activity
如果合并的其中一个AndroidManifest.xml存在以下节点
 <meta-data
android:name="Launcher_Activity_Name"
android:value="" />
代表取消会在另一个AndroidManifest.xml的中activity的android.intent.category.LAUNCHER
android:value的值也会被替换成删除的activity类名(多个activity会用,隔开。从头部开始添加)


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

## 使用文档

### 可用命令 (Action命令)

以下是工具支持的主要操作命令及其功能：

| 命令 | 描述 |
|------|------|
| `-generateMergeChannelApk` | 合并渠道后生成apk 需要配合命令`-baseApk`、`-channelApk`、`-out`、`-apkConfig`、`-signConfig`、`-libs`一起使用 |
| `-generateMultipleChannelApk` | 批量生成渠道apk 需要配合命令`-baseApk`、`-channelConfig`、`-libs`一起使用 |
| `-mergeSmali` | 合并smali环境 需要配合`-baseSmali`和`-channelSmali`一起使用 |
| `-decompile` | 反编译apk，配合`-baseApk`、`-out`、`-libs`一起使用 |
| `-sign` | 给apk签名，配合`-libs`、`-out`、`-signConfig`和`-baseApk`一起使用 |
| `-toApk` | smali环境生成apk，配合`-libs`、`-out`、`-signConfig`和`-baseSmali`一起使用 |
| `-changeApk` | 需要修改apk的路径，配合`-apkConfig`、`-out`、`-signConfig`、`-libs`一起使用，可用于换包名、图标、名称 |
| `-changeSmali` | 修改smali环境，配合`-apkConfig`一起使用，可用于换包名、图标、名称 |
| `-apk2aar`, `-apkToAar` | 将apk转化为aar，配合命令`-basePath`、`-out`、`-libs`、`-aarConfig` |
| `-mergeAar` | 合合并aar，配合命令`-out`、`-libs`、`-aarConfig` |
| `-toUnzip` | 解压apk，需要配合`-basePath`、`-out` |
| `-toZip` | 压缩apk，需要配合`-basePath`、`-out` |
| `-test` | 测试运行 |
| `-outApkConfig` | 输出ApkConfig.json的模板，需要使用`-out`设置输出路径的文件夹 |
| `-outSignConfig` | 输出signConfig.json的模板，需要使用`-out`设置输出路径的文件夹 |
| `-outChannelConfig` | 输出ChannelConfig.json的模板，需要使用`-out`设置输出路径的文件夹 |
| `-showSmaliInfo` | 查看smali_classes信息，配合`-basePath`使用 |
| `-runCmdForWriteLog` | 调用命令并输出日志，配合`-out`设置输出文件 |

### 基础参数说明

| 参数 | 描述 |
|------|------|
| `-libs` | 运行环境路径，需包含所需的jar/可执行程序(如apktool.jar、zipalign.exe、apksigner.jar) |
| `-out`, `-o`, `-outPath` | 输出路径 |
| `-baseApk`, `-basePath` | 主包路径 |
| `-channelApk`, `-channelPath` | 渠道框架包路径 |
| `-baseSmali` | 主包smali环境路径，运行后会将框架打入这个smali环境 |
| `-channelSmali` | 渠道框架smali环境路径 |
| `-javaPath` | java.exe 路径，可不填 |
| `-log` | 打印运行日志 |
| `-help`, `-h` | 查看帮助 |
| `-apkConfig` | 设置ApkConfig配置文件路径 |
| `-signConfig` | 设置签名配置文件路径 |
| `-channelConfig` | 设置ChannelConfig.json配置文件路径 |
| `-aarConfig` | 设置aarConfig.json配置文件路径 |

### 合并/修改高级参数说明

| 参数 | 描述 |
|------|------|
| `-channelRes`, `-isCoverFile`, `-useChannelRes` | 优先使用渠道包内的res、assets、AndroidManifest.xml等，等价于开启以下5个`-isUseChannelFile*`命令 |
| `-isUseChannelFileAssets` | 优先使用渠道包内的assets |
| `-isUseChannelFileLib` | 优先使用渠道包内的lib |
| `-isUseChannelFileRes` | 优先使用渠道包内的res |
| `-isUseChannelFileManifest` | 优先使用渠道包内的AndroidManifest.xml的内容 |
| `-isUseChannelFileOther` | 优先使用渠道包内的其他文件 (指kotlin、unknown等) |
| `-channelCode`, `-useChannelCode` | 优先使用渠道的代码 |
| `-keepActivityTheme` | 合并前将Application theme添加到没有theme的Activity上 |
| `-useChannelApktoolYml` | 使用渠道包的apktool.yml |
| `-replaceApplication` | 合并AndroidManifest.xml替换Application类。若配合`-useChannelRes`命令使用，application会替换为变成主包的，否则替换成渠道包的 |
| `-changeNotRSmali` | 修改没有引用R.class的id值，通过修改0X7f开头的值实现 |
| `-isRenameRes` | res出现重名情况，是否重命名。apk合并时使用 |
| `-isReNameStyle` | 不重命名style，需要配合`-isRenameRes`使用 |
| `-reNameAttr` | 重命名attr |
| `-isRenameClassPackage` | class出现重名情况，是否修改package。apk合并时使用 |
| `-notUseDefaultKeepClassPackage` | 禁用默认的keep class规则。apk合并时使用 |

