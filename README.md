
## 工具介绍

通过使用gui界面来操作[apkMerge_gui](https://github.com/ming123aaa/apkMerge_gui)

[aarMergeTool](https://github.com/ming123aaa/aarMergeTool) :支持aar合并,apk2aar
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

## ApkConfig.json
[ApkConfig.kt](src/main/java/com/oh/gameSdkTool/bean/ApkConfig.kt)


## SignConfig.json
[SignConfig.kt](src/main/java/com/oh/gameSdkTool/bean/SignConfig.kt)

## ChannelConfig.json
[ChannelConfig.kt](src/main/java/com/oh/gameSdkTool/bean/ChannelConfig.kt)
## 合并时application只能有一个

<meta-data
android:name="Application_Name"
android:value="" />
合并后Application_Name会记录application,多个application会用,分割

## 合并时替换启动activity
如果合并的其中一个AndroidManifest.xml存在以下节点
 <meta-data
android:name="Launcher_Activity_Name"
android:value="" />
就代表会删除另一个AndroidManifest.xml的中启动activity（android.intent.category.LAUNCHER） , 且android:value的值也会被替换成删除的activity类名(多个activity会用,隔开)


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

### 可用命令

以下是工具支持的主要命令及其功能：

| 命令 | 描述 |
|------|------|
| `-generateMergeChannelApk` | 合并渠道并生成APK |
| `-generateMultipleChannelApk` | 批量生成渠道APK |
| `-mergeSmali` | 合并两个包的smali环境 |
| `-decompile` | 反编译APK |
| `-sign` | 为APK签名 |
| `-toApk` | 将smali环境生成APK |
| `-changeApk` | 修改APK并重新生成 |
| `-changeSmali` | 修改smali环境 |
| `-apk2aar`/`-apkToAar` | 将APK转换为AAR |
| `-mergeAar` | 合并AAR文件 |
| `-toUnzip` | 解压文件 |
| `-toZip` | 压缩文件 |

### 常用参数说明

| 参数 | 描述 | 必要 |
|------|------|------|
| `-libs` | 运行环境路径，包含所需的jar和可执行程序 | 是 |
| `-out`/`-o`/`-outPath` | 输出路径 | 是 |
| `-baseApk`/`-basePath` | 主包路径 | 部分命令 |
| `-channelApk`/`-channelPath` | 渠道框架包路径 | 部分命令 |
| `-apkConfig` | ApkConfig配置文件路径 | 部分命令 |
| `-signConfig` | 签名配置文件路径 | 部分命令 |
| `-channelConfig` | ChannelConfig配置文件路径 | 部分命令 |
| `-aarConfig` | aarConfig配置文件路径 | 部分命令 |
| `-baseSmali` | 主包smali环境路径 | 部分命令 |
| `-channelSmali` | 渠道框架smali环境路径 | 部分命令 |


### 合并apk参数

| 参数 | 描述 |
|------|------|
| `-isRenameRes` | 资源重名时是否重命名 |
| `-isRenameClassPackage` | 类重名时是否修改包名 |
| `-replaceApplication` | 合并AndroidManifest.xml时替换Application类 |
| `-useChannelRes` | 优先使用渠道包内的资源文件 |
| `-channelCode` | 优先使用渠道的代码 |

