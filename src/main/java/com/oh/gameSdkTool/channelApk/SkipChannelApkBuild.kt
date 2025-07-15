package com.oh.gameSdkTool.channelApk

import com.oh.gameSdkTool.CommandArgs
import com.oh.gameSdkTool.bean.ChannelConfigItemBean

class SkipChannelApkBuild:BaseChannelApkBuild() {
    override fun decompile(commandArgs: CommandArgs, baseApk: String, buildPath: String): String {
        return ""
    }

    override fun build(
        commandArgs: CommandArgs,
        baseApkSmaliPath: String,
        buildPath: String,
        outApkPath: String,
        data: ChannelConfigItemBean
    ) {
      println("不生成 ${data.channelName} 渠道")
    }

    override fun getBuildSmaliPath(baseSmali: String, buildPath: String, data: ChannelConfigItemBean): String {
       return ""
    }
}