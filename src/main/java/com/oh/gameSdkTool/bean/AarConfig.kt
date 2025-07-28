package com.oh.gameSdkTool.bean

import com.google.gson.Gson
import com.oh.gameSdkTool.CommandArgs
import com.ohuang.replacePackage.FileUtils
import java.io.File
import java.util.TreeMap

class AarConfig {
    var outAArName: String = "" //输出的aar名称
    var packageName: String = "" // 包名或命名空间
    var aarPathList: List<String> = emptyList() //需要合并的aar文件路径   示例 xxxx.aar
    var metaDataMap: Map<String, String> = TreeMap<String, String>() // meta-data修改
    var replaceStringManifest: List<ReplaceStringData> = emptyList() // AndroidManifest.xml 字符串替换   用于复杂的数据替换
    var deleteFileList: List<String> = emptyList() //需要删除的文件， 示例 res/mipmap-anydpi
    var changeClassPackage: Map<OldName, NewName> = emptyMap() // 修改class所在的包名  com.xxx.yyy 中间用.隔开
    var deleteSmaliPaths: List<String> = emptyList() //需要删除的smail的文件   com/google   com/xxx/R.smali
    var isDeleteSameNameSmali: Boolean = true  //是否删除相同名称的smali文件
    var deleteManifestNodeNames: Set<String> = emptySet() //根据name删除的AndroidManifest.xml对应的节点
    var smaliClassSizeMB: Long =
        30 //限制smaliClass文件的大小,避免方法数量超出限制无法打包,推荐值30MB， 若smaliClassSizeMB<=0或smaliClassSizeMB>=1000将不限制文件大小
}

data class AarConfigData(
    var outAArName: String,
    var packageName: String,
    var aarPathList: List<File>, //需要合并的aar文件路径   示例 xxxx.aar
    var metaDataMap: Map<String, String>, // meta-data修改
    var replaceStringManifest: List<ReplaceStringData>, // AndroidManifest.xml 字符串替换   用于复杂的数据替换
    var deleteFileList: List<String>, //需要删除的文件， 示例 res/mipmap-anydpi
    var changeClassPackage: Map<OldName, NewName>, // 修改class所在的包名  com.xxx.yyy 中间用.隔开
    var smaliClassSizeMB: Long,//限制smaliClass文件的大小,避免方法数量超出限制无法打包,推荐值50MB， 若smaliClassSizeMB<=0或smaliClassSizeMB>=1000将不限制文件大小

    var deleteSmaliPaths: List<String>, //需要删除的smail的文件   com/google   com/xxx/R.smali
    var isDeleteSameNameSmali: Boolean,  //是否删除相同名称的smali文件
    var deleteManifestNodeNames: Set<String>, //根据name删除的AndroidManifest.xml对应的节点)

)

fun AarConfigData.toApkConfig(): ApkConfigBean {
    return ApkConfigBean(
        packageName = packageName,
        iconImgPath = "",
        iconSize = "",
        appName = "",
        versionCode = "",
        versionName = "",
        minSdkVersion = "",
        targetSdkVersion = "",
        abiNames = emptyList(),
        metaDataMap = metaDataMap,
        replaceStringManifest = replaceStringManifest,
        deleteFileList = deleteFileList,
        changeClassPackage = changeClassPackage,
        renameResMap = emptyMap(),
        deleteSmaliPaths = deleteSmaliPaths,
        isDeleteSameNameSmali = isDeleteSameNameSmali,
        deleteManifestNodeNames = deleteManifestNodeNames,
        smaliClassSizeMB = smaliClassSizeMB
    )
}

fun CommandArgs.toAarConfigData(): AarConfigData {
    return createAarConfigData(aarConfig)
}

private fun createAarConfigData(path: String): AarConfigData {
    var gson = Gson()
    val file = File(path)
    val readText = FileUtils.readText(path)
    val fromJson = runCatching { gson.fromJson(readText, AarConfig::class.java) }
        .onFailure {
            throw RuntimeException("$path  is not AarConfig.json")
        }.getOrNull() ?: throw RuntimeException("$path  is not AarConfig.json")
    val aarFileList = fromJson.aarPathList.map { File(file.parentFile.absolutePath + "/" + it) }
    return AarConfigData(
        outAArName = fromJson.outAArName,
        packageName = fromJson.packageName,
        aarPathList = aarFileList,
        metaDataMap = fromJson.metaDataMap,
        replaceStringManifest = fromJson.replaceStringManifest,
        deleteFileList = fromJson.deleteFileList,
        changeClassPackage = fromJson.changeClassPackage,
        deleteSmaliPaths = fromJson.deleteSmaliPaths,
        isDeleteSameNameSmali = fromJson.isDeleteSameNameSmali,
        deleteManifestNodeNames = fromJson.deleteManifestNodeNames, smaliClassSizeMB = fromJson.smaliClassSizeMB
    )
}