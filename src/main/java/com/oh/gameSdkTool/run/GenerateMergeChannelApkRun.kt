package com.oh.gameSdkTool.run

import com.oh.gameSdkTool.CommandArgs
import com.oh.gameSdkTool.CommandRun
import com.oh.gameSdkTool.Decompile
import com.oh.gameSdkTool.ReplaceAPk
import com.oh.gameSdkTool.bean.toApkConfigBean

import com.ohuang.apkMerge.mergeApkSmali
import com.ohuang.replacePackage.FileUtils
import java.io.File

/**
 * 合并渠道并生成apk
 */
class GenerateMergeChannelApkRun : CommandRun() {
    override fun isRun(commandArgs: CommandArgs): Boolean {
        return commandArgs.isGenerateMergeChannelApk
    }

    override fun run(commandArgs: CommandArgs) {
        if (!checkArgs(commandArgs)) {
            return
        }
        val baseApk = commandArgs.baseApk
        val channelApk = commandArgs.channelApk
        val outPath = commandArgs.outPath

        val buildPath = "$outPath/build"
        val outApkPath = "$outPath/out.apk"
        val file = File(buildPath)
        println("----删除$file 和$outApkPath")
        FileUtils.delete(file)
        file.mkdirs()
        val apkFile = File(outApkPath)
        FileUtils.delete(apkFile)
        val baseSmaliPath = "$buildPath/baseSmaliPath"
        val channelSmaliPath = "$buildPath/channelSmaliPath"
        println("---反编译:$baseApk")
        Decompile.apkToSmali(commandArgs = commandArgs, smaliPath = baseSmaliPath, apkPath = baseApk)
        println("---反编译:$channelApk")
        Decompile.apkToSmali(commandArgs = commandArgs, smaliPath = channelSmaliPath, apkPath = channelApk)
        println("---开始合并smali环境")
        mergeApkSmali(
            channelSmaliPath,
            baseSmaliPath,
            commandArgs = commandArgs
        )

        println("---开始修改smali环境")
        val toApkConfigBean = commandArgs.toApkConfigBean()
        ReplaceAPk.replaceApK(
            rootPath = baseSmaliPath,
            apkConfigBean = toApkConfigBean
        )
        ReplaceAPk.optimizeAndroidManifest(baseSmaliPath)
        println("---生成apk")
        val smaliToApk = Decompile.smaliToApk(commandArgs, baseSmaliPath, buildPath)
        val outApk = File(outApkPath)
        outApk.delete()
        smaliToApk.renameTo(outApk)
        println("apk生成完成:$outApk")

    }

    private fun checkArgs(commandArgs: CommandArgs): Boolean {
        if (commandArgs.baseApk.isEmpty()) {
            println("没有使用 -baseApk 命令")
            return false
        }
        if (commandArgs.channelApk.isEmpty()) {
            println("没有使用 -channelApk 命令")
            return false
        }
        if (commandArgs.apkConfig.isEmpty()) {
            println("没有使用 -apkConfig 命令")
            return false
        }
        if (commandArgs.signConfig.isEmpty()) {
            println("没有使用 -signConfig 命令")
            return false
        }
        if (commandArgs.libs.isEmpty()) {
            println("没有使用 -libs 命令")
            return false
        }
        if (commandArgs.outPath.isEmpty()) {
            println("没有使用 -out 命令")
            return false
        }
        return true
    }
}