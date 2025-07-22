package com.oh.gameSdkTool.run

import com.oh.gameSdkTool.CommandArgs
import com.oh.gameSdkTool.CommandRun
import com.oh.gameSdkTool.Decompile
import com.oh.gameSdkTool.ReplaceAPk
import com.oh.gameSdkTool.bean.ApkConfigBean
import com.oh.gameSdkTool.bean.toAarConfigData
import com.oh.gameSdkTool.bean.toApkConfig
import com.ohuang.apkMerge.tryCatch
import com.ohuang.replacePackage.FileUtils
import java.io.File

class ApkToAarRun: CommandRun()  {
    override fun isRun(commandArgs: CommandArgs): Boolean {
        return commandArgs.isApk2Aar
    }

    override fun run(commandArgs: CommandArgs) {
        if(!checkArgs(commandArgs)){
            return
        }
        tryCatch(true){
            val baseApk = commandArgs.basePath
            val outPath = commandArgs.outPath
            val aarConfigBean = commandArgs.toAarConfigData()
            if (!File(baseApk).exists()){
                println("$baseApk 不存在")
                return@tryCatch
            }
            val buildDir= "${outPath}/build"
            if (File(buildDir).exists()){
                println("删除-$buildDir")
                FileUtils.delete(File(buildDir))
            }
            Decompile.apkToSmali(commandArgs, "$buildDir/baseSmali", baseApk)
            ReplaceAPk.replaceApK("$buildDir/baseSmali", aarConfigBean.toApkConfig())
            val aarBuild= "$buildDir/aarBuild"

        }
    }

    private fun checkArgs(commandArgs: CommandArgs): Boolean {
        println("Apk To Aar 功能暂时不正常")

        return false
        if (commandArgs.basePath.isEmpty()) {
            println("没有使用 -basePath 命令")
            return false
        }

        if (commandArgs.outPath.isEmpty()) {
            println("没有使用 -out 命令")
            return false
        }
        if (commandArgs.libs.isEmpty()) {
            println("没有使用 -libs 命令")
            return false
        }
        if (commandArgs.aarConfig.isEmpty()) {
            println("没有使用 -aarConfig 命令")
            return false
        }
        return true
    }
}