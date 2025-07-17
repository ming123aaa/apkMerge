package com.oh.gameSdkTool.channelApk

import com.beust.jcommander.JCommander
import com.oh.gameSdkTool.CommandArgs
import com.oh.gameSdkTool.Decompile
import com.oh.gameSdkTool.ReplaceAPk
import com.oh.gameSdkTool.bean.ChannelConfigItemBean
import com.ohuang.apkMerge.mergeApkSmali
import com.ohuang.apkMerge.tryCatch
import com.ohuang.replacePackage.FileUtils
import com.ohuang.replacePackage.copyPathAllFile

import java.io.File

class MergeListChannelApkBuild : BaseChannelApkBuild() {

    var mergeDir = "merge_list"

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
        data.listMergeConfigs.forEachIndexed { index, mergreConfig ->
            tryCatch {

                if (mergreConfig.channelApkFile.isNotEmpty()) { //存在apk的话先走合并流程
                    val file = File(mergreConfig.channelApkFile)
                    if (!mergreConfig.enable) {
                        return@tryCatch
                    }
                    if (!file.exists()) {
                        return@tryCatch
                    }
                    val channelSmali = "$realBuildPath/channelSmali$index"
                    FileUtils.delete(File(channelSmali))
                    Decompile.apkToSmali(commandArgs, channelSmali, file.absolutePath)
                    val childCommandArgs = CommandArgs()
                    val jCommander = JCommander.newBuilder()
                        .addObject(childCommandArgs)
                        .build()
                    jCommander.parse(*mergreConfig.extraCmd.toTypedArray())
                    mergeApkSmali(
                        channelSmali = channelSmali,
                        baseSmali = baseApkSmaliPath,
                        commandArgs = childCommandArgs
                    )
                }
            }

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