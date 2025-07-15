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

class MergeChannelApkBuild : BaseChannelApkBuild() {

    var mergeDir = "merge"

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
        if (data.channelApkFile.exists()) { //存在apk的话先走合并流程
            val channelSmali = "$realBuildPath/channelSmali"
            FileUtils.delete(File(channelSmali))
            Decompile.apkToSmali(commandArgs, channelSmali, data.channelApkFile.absolutePath)
            val childCommandArgs = CommandArgs()
            val jCommander = JCommander.newBuilder()
                .addObject(childCommandArgs)
                .build()
            jCommander.parse(*data.extraCmd.toTypedArray())
            mergeApkSmali(
                channelSmali = channelSmali,
                baseSmali = baseApkSmaliPath,
                commandArgs = childCommandArgs
            )
        }
        ReplaceAPk.replaceApK(baseApkSmaliPath, data.apkConfig)
        ReplaceAPk.optimizeAndroidManifest(baseApkSmaliPath)
        val smaliToApk =
            Decompile.smaliToApk(commandArgs, baseApkSmaliPath, "$realBuildPath/outApk", data.signConfig)
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
        val smali = "$realBuildPath/baseSmali"
        copyPathAllFile(sourceRootPath = baseSmali, targetRootPath = smali)
        return smali
    }
}