package com.oh.gameSdkTool.channelApk

import com.beust.jcommander.JCommander
import com.oh.gameSdkTool.CommandArgs
import com.oh.gameSdkTool.Decompile
import com.oh.gameSdkTool.ReplaceAPk
import com.oh.gameSdkTool.bean.ChannelConfigItemBean
import com.ohuang.apkMerge.mergeApkSmali
import com.ohuang.replacePackage.FileUtils
import com.ohuang.replacePackage.copyPathAllFile
import java.io.File

class MergeReverseChannelApkBuild : BaseChannelApkBuild() {

    var mergeDir = "merge_reverser"

    override fun decompile(commandArgs: CommandArgs, baseApk: String, buildPath: String): String {
        return CacheDecompile.decompile(commandArgs = commandArgs, baseApkPath = baseApk, buildPath = buildPath)
    }

    override fun build(
        commandArgs: CommandArgs,
        baseApkSmaliPath: String,
        buildPath: String,
        outApkPath: String,
        data: ChannelConfigItemBean
    ) {
        val realBuildPath = "$buildPath/$mergeDir/${data.channelName}"
        val realBaseSmali = "$realBuildPath/baseSmali"
        if (data.channelApkFile.exists()) { //存在apk的话先走合并流程

            FileUtils.delete(File(realBaseSmali))
            Decompile.apkToSmali(commandArgs, realBaseSmali, data.channelApkFile.absolutePath)
            val childCommandArgs = CommandArgs()
            val jCommander = JCommander.newBuilder()
                .addObject(childCommandArgs)
                .build()
            jCommander.parse(*data.extraCmd.toTypedArray())
            val isChannelCode = childCommandArgs.isChannelCode
            val isChannelRes = childCommandArgs.isChannelRes
            val isUseChannelApktoolYml = childCommandArgs.isUseChannelApktoolYml
            val isReplaceApplication = childCommandArgs.isReplaceApplication
            println("isChannelCode=$isChannelCode isChannelRes=$isChannelRes isUseChannelApktoolYml=$isUseChannelApktoolYml")
            mergeApkSmali(
                channelSmali = baseApkSmaliPath,
                baseSmali =realBaseSmali ,
                commandArgs = childCommandArgs
            )
        }else{
            println("${data.channelApkFile}  不存在")
            return
        }
        ReplaceAPk.replaceApK(realBaseSmali, data.apkConfig)
        ReplaceAPk.optimizeAndroidManifest(realBaseSmali)
        val smaliToApk =
            Decompile.smaliToApk(commandArgs, realBaseSmali, "$realBuildPath/outApk", data.signConfig)
        val outApkFile = File(outApkPath)
        if (outApkFile.exists()) {
            FileUtils.delete(outApkFile)
        }
        smaliToApk.renameTo(outApkFile)
        Thread.sleep(1000)
        val delete = FileUtils.delete(File("$realBuildPath/outApk"))
        println("delete:{$delete} $realBuildPath/outApk ")
    }

    override fun getBuildSmaliPath(baseSmali: String, buildPath: String, data: ChannelConfigItemBean): String {

        val realBuildPath = "$buildPath/$mergeDir/${data.channelName}"
        FileUtils.delete(File(realBuildPath))
        if (File(realBuildPath).exists()) {
            throw RuntimeException("$realBuildPath  cant delete")
        }
        val smali = "$realBuildPath/channelSmali"
        copyPathAllFile(sourceRootPath = baseSmali, targetRootPath = smali)
        return smali
    }
}