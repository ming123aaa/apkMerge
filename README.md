
## 工具介绍
本项目用于合并多个apk为一个apk;修改apk的一些信息如:包名,图标,版本号等;支持批量生成渠道包。

支持合并多个aar为一个aar,apk转aar。目前aar支持还不够完善,部分aar可能会出现问题。

### 推荐使用界面操作。
使用界面的方式来操作 [apkMerge_gui](https://github.com/ming123aaa/apkMerge_gui)

支持 多渠道打包 apk合并 修改apk信息,该项目的jar和本项目的jar都要使用最新版。
### aar相关使用示例
aar合并 和 apk转aar 的功能使用请查看 [aarMergeTool](https://github.com/ming123aaa/aarMergeTool) 使用时需要替换为本项目最新jar。


### 通过行使用jar
常用用法已经写成.bat文件了，可以直接使用 [tools](tools)

其中[tools/jar/gameSdkTool.jar](tools/jar/gameSdkTool.jar)可能不是最新版，需要替换成最新版


### 注意事项
注意:如果开启了windows实时保护功能,可能会让程序执行起来很慢,需要将操作的文件夹设置为windows安全中心的排除项。

打开“设置” > “更新和安全” > “Windows 安全中心” > “病毒和威胁防护”。

在“病毒和威胁防护设置”下点击“管理设置”。

滚动到“排除项”，然后点击“添加或删除排除项”。你可以添加特定的文件夹、文件类型或进程。


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

### aarConfig.json
用于合并aar的配置文件
[AarConfig.kt](src/main/java/com/oh/gameSdkTool/bean/AarConfig.kt)


## 合并魔法
### 合并时application只能有一个,用于保存被替换的application
```xml
<meta-data
android:name="Application_Name"
android:value="这里是application类名"/>
```
合并后Application_Name会记录application。

多个application会用逗号,隔开。从头部开始添加

### 合并时替换启动activity
如果合并的其中一个AndroidManifest.xml存在以下节点
```xml
<meta-data
android:name="Launcher_Activity_Name"
android:value="这里是启动Activity类名" />
``` 

代表取消会在另一个AndroidManifest.xml的中activity的android.intent.category.LAUNCHER

android:value的值也会被替换成删除的activity类名(多个activity会用逗号,隔开。从头部开始添加)


## 合并前修改内容
配置文件在apk内部,以下是路径配置:
```kotlin
private const val assets_merge_ApkConfig = "/assets/mergeApkContent/ApkConfig.json" //存在则触发合并前修改内容
```
在apk合并前会根据ApkConfig.json修改内容。(主包不会生效)

## 合并时,冲突问题解决
解决文件冲突,可使用冲突重命名功能(-isRenameRes,-isRenameClassPackage),会自动的重命名res资源文件和class的类名。

若想让自定文件不被重命名,可通过在apk包内通过配置文件,设置想要被排除文件。
### keepClass规则，用于排除不修改的class


默认情况下会有自己的keepClassPackage，会排除kotlin,以及META-INF/services下面的class

配置文件在apk内部,以下是路径配置:
```kotlin
private const val keepClassJson="/assets/keepClassPackage.json"  //冲突不修改的class  Set<String>
```
文件数据结构如下:
```
 ["aaa.bbb","ccc,aaaa"]

```
### keepRes规则,用于排除不修改的res资源
配置文件在apk内部,以下是路径配置:
```kotlin
private const val keepResNameJson="/assets/keepResNamePackage.json" //冲突不修改的res  Map<resType,Set<name>>
```
文件数据结构如下:
```
{
"layout":["activity_main"],
"string":["app_name"]
}
```

## 使用文档

### 可用命令 (Action命令)

以下是工具支持的主要操作命令及其功能：

| 命令 | 描述                                                                                     |
|------|----------------------------------------------------------------------------------------|
| `-generateMergeChannelApk` | 合并渠道后生成apk 需要配合命令`-baseApk`、`-channelApk`、`-out`、`-apkConfig`、`-signConfig`、`-libs`一起使用 |
| `-generateMultipleChannelApk` | 批量生成渠道apk 需要配合命令`-baseApk`、`-channelConfig`、`-libs`一起使用                                |
| `-mergeSmali` | 合并smali环境 需要配合`-baseSmali`和`-channelSmali`一起使用                                         |
| `-decompile` | 反编译apk，配合`-baseApk`、`-out`、`-libs`一起使用                                                 |
| `-sign` | 给apk签名，配合`-libs`、`-out`、`-signConfig`和`-baseApk`一起使用                                   |
| `-toApk` | smali环境生成apk，配合`-libs`、`-out`、`-signConfig`和`-baseSmali`一起使用                           |
| `-changeApk` | 需要修改apk的路径，配合`-apkConfig`、`-out`、`-signConfig`、`-libs`一起使用，可用于换包名、图标、名称                |
| `-changeSmali` | 修改smali环境，配合`-apkConfig`一起使用，可用于换包名、图标、名称                                              |
| `-apk2aar`, `-apkToAar` | 将apk转化为aar，配合命令`-basePath`、`-out`、`-libs`、`-aarConfig`                                 |
| `-mergeAar` | 合并aar，配合命令`-out`、`-libs`、`-aarConfig`                                                  |
| `-toUnzip` | 解压apk/zip，需要配合`-basePath`、`-out`                                                           |
| `-toZip` | 压缩apk/zip，需要配合`-basePath`、`-out`                                                           |
| `-test` | 用于测试命令,请勿使用                                                                            |
| `-outApkConfig` | 输出ApkConfig.json的模板，需要使用`-out`设置输出路径的文件夹                                               |
| `-outSignConfig` | 输出signConfig.json的模板，需要使用`-out`设置输出路径的文件夹                                              |
| `-outChannelConfig` | 输出ChannelConfig.json的模板，需要使用`-out`设置输出路径的文件夹                                           |
| `-showSmaliInfo` | 查看smali_classes信息，配合`-basePath`使用                                                      |
| `-runCmdForWriteLog` | 运行命令写日志文件。只需输入一个命令,会运行该命令并输出日志文件,使用-out设置日志输出的文件                                       |

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

### 命令定义
查看命令定义class文件 [CommandArgs.java](src/main/java/com/oh/gameSdkTool/CommandArgs.java)

或者直接运行jar查看帮助信息
```shell
java -jar gameSdkTool.jar -h
```
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

生成后输出文件:[out](out)
