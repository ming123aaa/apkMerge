package com.oh.gameSdkTool.run

import com.oh.gameSdkTool.CommandArgs
import com.oh.gameSdkTool.CommandRun
import com.oh.gameSdkTool.Decompile
import com.ohuang.replacePackage.FileUtils
import java.io.File

/**
 * 生成apk
 */
class ToApkRun: CommandRun()  {
    override fun isRun(commandArgs: CommandArgs): Boolean {
        return commandArgs.isToApk
    }

    override fun run(commandArgs: CommandArgs) {
        val baseSmali = commandArgs.baseSmali
        val signConfig = commandArgs.signConfig
        val outPath = commandArgs.outPath
        val libs = commandArgs.libs
        val buildPath = "$outPath/build"
        val outApkPath = "$outPath/out.apk"
        val file = File(buildPath)
        println("----删除$file 和$outApkPath")
        FileUtils.delete(file)
        file.mkdirs()
        val apkFile = File(outApkPath)
        FileUtils.delete(apkFile)
        if (libs.isEmpty()){
            println("请通过-libs设置环境")
            return
        }
        if (baseSmali.isEmpty()){
            println("请通过-baseSmali")
            return
        }
        if (signConfig.isEmpty()){
            println("请通过-signConfig签名配置")
            return
        }
        if (outPath.isEmpty()){
            println("请通过-out设置输出的apk路径")
            return
        }
        val smaliToApk = Decompile.smaliToApk(commandArgs, baseSmali, buildPath)
        val outApk = File(outApkPath)
        outApk.delete()
        smaliToApk.renameTo(outApk)
        println("apk生成完成:$outApk")
    }
}