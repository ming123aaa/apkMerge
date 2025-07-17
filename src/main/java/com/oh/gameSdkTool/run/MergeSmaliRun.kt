package com.oh.gameSdkTool.run

import com.oh.gameSdkTool.CommandArgs
import com.oh.gameSdkTool.CommandRun
import com.oh.gameSdkTool.ReplaceAPk

import com.ohuang.apkMerge.mergeApkSmali

/**
 * 合并两个包smali环境
 */
class MergeSmaliRun : CommandRun() {
    override fun isRun(commandArgs: CommandArgs): Boolean {
        return commandArgs.isMergeSmali
    }

    override fun run(commandArgs: CommandArgs) {
        println("执行  -mergeSmali")
        val baseSmali = commandArgs.baseSmali
        val channelSmali = commandArgs.channelSmali

        println("baseSmali=$baseSmali")
        println("channelSmali=$channelSmali")
        if (baseSmali.isEmpty() || channelSmali.isEmpty()) {
            println("没有输入smali路径请使用  -baseSmali 和 -channelSmali 设置")
            return
        }

        mergeApkSmali(
            channelSmali,
            baseSmali,
            commandArgs = commandArgs
        )


        ReplaceAPk.optimizeAndroidManifest(baseSmali)
    }
}