package com.oh.gameSdkTool.bean

import com.google.gson.Gson
import com.oh.gameSdkTool.CommandArgs
import com.ohuang.replacePackage.FileUtils
import java.io.File
import java.util.TreeMap

/**
 * apk 配置
 */
data class ApkConfigBean(
    var packageName: String,
    var iconImgPath: String, //图片绝对路径
    var iconSize: String,
    var appName: String,
    var versionCode: String,
    var versionName: String,
    var minSdkVersion: String,
    var targetSdkVersion: String,
    var abiNames: List<String>,
    var metaDataMap: Map<String, String>,
    var replaceStringManifest: List<ReplaceStringData>,
    var deleteFileList: List<String>, //相对路径
    var changeClassPackage: Map<OldName, NewName>,
    var renameResMap: Map<ResType, Map<OldName, NewName>>,
    var smaliClassSizeMB: Double,//限制smaliClass文件的大小,避免方法数量超出限制无法打包,推荐值30MB， 只有 maxMB >= 1.0 && maxMB <= 1000 生效
    var isOptimizeSmaliClass: Boolean,
    var deleteSmaliPaths: List<String>, //需要删除的smail的文件   /aa/bb   /aa/cc.smali
    var isDeleteSameNameSmali: Boolean, //是否删除相同名称的smali文件
    var deleteManifestNodeNames: Set<String>,//根据name删除的AndroidManifest.xml对应的节点
    var compileSdkInfo: CompileSdkInfo,
    var applicationSetAttributeMap: Map<String, String>,// application节点的属性修改
    var manifestNodeSetAttributeMapByName: Map<String, Map<String, String>> // 根据name修改的AndroidManifest.xml对应的节点的属性
)


fun CommandArgs.toApkConfigBean(): ApkConfigBean {
    return getApkConfigBean(apkConfig)
}

fun getApkConfigBean(apkConfigPath: String): ApkConfigBean {
    val file = File(apkConfigPath)
    val readText = FileUtils.readText(apkConfigPath)
    val gson = Gson()
    val fromJson = runCatching { gson.fromJson(readText, ApkConfig::class.java) }
        .onFailure {
            throw RuntimeException("$apkConfigPath  is not ApkConfig.json")
        }.getOrNull() ?: throw RuntimeException("$apkConfigPath  is not ApkConfig.json")

    var imagePath = ""
    if (fromJson.iconImgPath.isNotEmpty()) {
        imagePath = file.parentFile.absolutePath + "/" + fromJson.iconImgPath
        println("imagePath=$imagePath")
    }
    val abiNames = arrayListOf<String>()
    fromJson.abiNames.forEach {
        if (it.isNotEmpty()) {
            abiNames.add(it)
        }
    }
    return ApkConfigBean(
        packageName = fromJson.packageName,
        iconSize = fromJson.iconSize,
        appName = fromJson.appName,
        iconImgPath = imagePath,
        versionCode = fromJson.versionCode,
        versionName = fromJson.versionName,
        abiNames = abiNames,
        minSdkVersion = fromJson.minSdkVersion,
        targetSdkVersion = fromJson.targetSdkVersion,
        metaDataMap = fromJson.metaDataMap,
        replaceStringManifest = fromJson.replaceStringManifest,
        deleteFileList = fromJson.deleteFileList,
        changeClassPackage = fromJson.changeClassPackage,
        renameResMap = fromJson.renameResMap,
        smaliClassSizeMB=fromJson.smaliClassSizeMB,
        deleteSmaliPaths = fromJson.deleteSmaliPaths,
        isDeleteSameNameSmali = fromJson.isDeleteSameNameSmali,
        deleteManifestNodeNames = fromJson.deleteManifestNodeNames,
        isOptimizeSmaliClass=fromJson.isOptimizeSmaliClass,
        compileSdkInfo=fromJson.compileSdkInfo,
        applicationSetAttributeMap = fromJson.applicationSetAttributeMap,
        manifestNodeSetAttributeMapByName = fromJson.manifestNodeSetAttributeMapByName
    )
}