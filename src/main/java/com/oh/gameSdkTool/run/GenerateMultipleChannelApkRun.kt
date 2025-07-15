package com.oh.gameSdkTool.run

import com.oh.gameSdkTool.CommandArgs
import com.oh.gameSdkTool.CommandRun
import com.oh.gameSdkTool.channelApk.ChannelApkBuild

/**
 * 批量生成渠道包
 */
class GenerateMultipleChannelApkRun:CommandRun() {
    override fun isRun(commandArgs: CommandArgs): Boolean {
        return commandArgs.isGenerateMultipleChannelApk
    }

    override fun run(commandArgs: CommandArgs) {
       if (!checkArgs(commandArgs)){
           return
       }
        println("GenerateMultipleChannelApkRun start")
        ChannelApkBuild.build(commandArgs)
        println("GenerateMultipleChannelApkRun end")
    }

    private fun checkArgs(commandArgs: CommandArgs): Boolean {
        if (commandArgs.baseApk.isEmpty()) {
            println("没有使用 -baseApk 命令")
            return false
        }
        if (commandArgs.channelConfig.isEmpty()) {
            println("没有使用 -channelConfig 命令")
            return false
        }
        if (commandArgs.libs.isEmpty()) {
            println("没有使用 -libs 命令")
            return false
        }

        return true
    }
}