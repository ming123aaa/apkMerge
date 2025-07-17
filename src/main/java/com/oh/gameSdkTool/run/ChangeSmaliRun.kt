package com.oh.gameSdkTool.run

import com.google.gson.Gson
import com.oh.gameSdkTool.CommandArgs
import com.oh.gameSdkTool.CommandRun
import com.oh.gameSdkTool.ReplaceAPk

import com.oh.gameSdkTool.bean.toApkConfigBean

/**
 * 修改smali环境
 */
class ChangeSmaliRun : CommandRun() {
    override fun isRun(commandArgs: CommandArgs): Boolean {
        return commandArgs.changeSmali.isNotEmpty()
    }

    override fun run(commandArgs: CommandArgs) {
        val changeSmali = commandArgs.changeSmali
        val apkConfig = commandArgs.apkConfig
        println("changeSmali=$changeSmali")
        println("apkConfig=$apkConfig")
        if (changeSmali.isEmpty() || apkConfig.isEmpty()) {
            println("请检查是否通过-apkConfig -changeSmali设置了路径")
            return
        }
        val toApkConfigBean = commandArgs.toApkConfigBean()
        ReplaceAPk.replaceApK(
            rootPath = changeSmali,
            apkConfigBean = toApkConfigBean
        )
        ReplaceAPk.optimizeAndroidManifest(changeSmali)
    }
}