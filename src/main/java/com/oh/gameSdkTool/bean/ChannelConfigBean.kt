package com.oh.gameSdkTool.bean

import com.google.gson.Gson
import com.ohuang.replacePackage.FileUtils
import java.io.File
import java.lang.RuntimeException

data class ChannelConfigBean(
    val buildDirFile: File,
    val outDirFile: File, val items: List<ChannelConfigItemBean>
)


data class ChannelConfigItemBean(
    val channelName: String,  //渠道的名称
    val apkConfig: ApkConfigBean,
    var signConfig: SignConfigBean,
    var mode: String,
    var extraCmd: List<String>,
    var channelApkFile: File,
    var listMergeConfigs: List<MergeConfig>,
    val channelEnable: Boolean,
    val channelGroupName: String,
    val apkName: String
)


fun getChannelConfigBean(channelConfigFile: File): ChannelConfigBean {
    if (!channelConfigFile.exists()) {
        throw RuntimeException()
    }
    val readText = FileUtils.readText(channelConfigFile.absolutePath)
    val gson = Gson()
    val channelConfig = runCatching{gson.fromJson(readText, ChannelConfig::class.java)}
        .onFailure { throw RuntimeException("$channelConfigFile  is not ChannelConfig.json") }
        .getOrNull() ?: throw RuntimeException("$channelConfigFile  is not ChannelConfig.json")
    return channelConfig.toBean(channelConfigFile.parent)
}

fun ChannelConfig.toBean(rootPath: String): ChannelConfigBean {
    var channelConfigItemBeanList = items.toBean(rootPath).filter { it.channelEnable }//获取可用渠道配置列表
    val enableList = enableChannelNames.filter { it.isNotEmpty() } //启用的渠道名列表
    if (enableList.isNotEmpty()) {
        channelConfigItemBeanList = channelConfigItemBeanList.filter {
            enableList.contains(it.channelName) || (it.channelGroupName.isNotEmpty() && enableList.contains(it.channelGroupName))
        }
    }
    val disableList=disableChannelNames.filter { it.isNotEmpty() }  //禁用的渠道名列表
    if (disableList.isNotEmpty()){
        channelConfigItemBeanList = channelConfigItemBeanList.filter {
            !(disableChannelNames.contains(it.channelName) || (it.channelGroupName.isNotEmpty() && disableChannelNames.contains(it.channelGroupName)))
        }
    }
    println("获取到的启用的渠道名-----")
    channelConfigItemBeanList.forEach {
        println("渠道名:${it.channelName}")
    }
    return ChannelConfigBean(
        buildDirFile = File("$rootPath/$buildDirFile"),
        outDirFile = File("$rootPath/$outDirFile"), items = channelConfigItemBeanList
    )
}


fun List<ChannelConfigItem>.toBean(rootPath: String): List<ChannelConfigItemBean> {
    return filter { it.channelName.isNotEmpty()&&it.channelEnable }.map { it.toBean(rootPath) }
}

fun ChannelConfigItem.toBean(rootPath: String): ChannelConfigItemBean {
    return ChannelConfigItemBean(
        channelName = channelName, apkConfig = getApkConfigBean("$rootPath/$apkConfigFile"),
        signConfig = getSignConfigBean("$rootPath/$signConfigFile"),
        mode = mode,
        extraCmd = extraCmd,
        channelApkFile = File("$rootPath/$channelApkFile"),
        listMergeConfigs = listMergeConfigs.map {
            it.channelApkFile = "$rootPath/${it.channelApkFile}"
            return@map it
        },
        channelEnable = channelEnable,
        channelGroupName = channelGroupName,
        apkName = apkName
    )
}