package com.oh.gameSdkTool.run

import com.google.gson.Gson
import com.oh.gameSdkTool.CommandArgs
import com.oh.gameSdkTool.CommandRun
import com.oh.gameSdkTool.bean.ChannelConfig
import com.oh.gameSdkTool.bean.ChannelConfigItem
import com.oh.gameSdkTool.bean.MergeConfig
import com.ohuang.replacePackage.FileUtils
import java.io.File

/**
 * 生成ChannelConfig.json模板
 */
class OutChannelConfigRun: CommandRun()  {
    override fun isRun(commandArgs: CommandArgs): Boolean {
        return commandArgs.isOUtChannelConfig
    }

    override fun run(commandArgs: CommandArgs) {
        val outPath = commandArgs.outPath
        if (outPath.isEmpty()){
            println("请设置 -outPath")
            return
        }
        val file = File(outPath)
        file.mkdirs()
        val channelConfig = ChannelConfig()
        channelConfig.outDirFile="outApk"
        channelConfig.buildDirFile="build"
        val item = ChannelConfigItem()
        item.channelName="渠道名称"
        item.apkConfigFile="apkConfig.json的相对路径"
        item.signConfigFile="signConfig.json的相对路径"
        item.mode="修改渠道包的模式 默认skip模式  skip,simple,simple_fast,merge,merge_list,merge_reverse"
        item.extraCmd= arrayListOf("-useChannelRes","-useChannelCode")
        item.channelApkFile="合并渠道包框架的apk"
        item.listMergeConfigs= listOf(MergeConfig().apply {
            extraCmd= arrayListOf("-useChannelRes","-useChannelCode")
            channelApkFile="合并渠道包框架的apk"
        })
        channelConfig.items= arrayListOf(item)

        val toJson = Gson().toJson(channelConfig)
        FileUtils.writeText(File(file.absolutePath+"/ChannelConfig.json"),toJson)
    }
}