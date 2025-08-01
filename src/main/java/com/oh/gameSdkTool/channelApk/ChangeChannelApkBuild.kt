package com.oh.gameSdkTool.channelApk

import com.oh.gameSdkTool.CommandArgs
import com.oh.gameSdkTool.Decompile
import com.oh.gameSdkTool.ReplaceAPk
import com.oh.gameSdkTool.bean.ChannelConfigItemBean
import com.ohuang.replacePackage.FileUtils
import com.ohuang.replacePackage.copyPathAllFile
import java.io.File

class ChangeChannelApkBuild : BaseChannelApkBuild() {
    var changeDir = "change"

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
        val realBuildPath = "$buildPath/$changeDir/${data.channelName}"
        ReplaceAPk.replaceApK(baseApkSmaliPath, data.apkConfig)
        ReplaceAPk.optimizeAndroidManifest(baseApkSmaliPath)
        val smaliToApk =
            Decompile.smaliToApk(commandArgs, baseApkSmaliPath, "$realBuildPath/outApk", data.signConfig)
        val outApkFile = File(outApkPath)
        if (outApkFile.exists()) {
            FileUtils.delete(outApkFile)
        }
        smaliToApk.renameTo(outApkFile)
        val delete = FileUtils.delete(File("$realBuildPath/outApk"))
        println("delete:{$delete} $realBuildPath ")
    }

    override fun getBuildSmaliPath(baseSmali: String, buildPath: String, data: ChannelConfigItemBean): String {
        val realBuildPath = "$buildPath/$changeDir/${data.channelName}"
        FileUtils.delete(File(realBuildPath))
        if (File(realBuildPath).exists()){
            throw RuntimeException("$realBuildPath cant delete" )
        }
        val smali = "$realBuildPath/baseSmali"
        copyPathAllFile(sourceRootPath = baseSmali, targetRootPath = smali)


        return smali
    }
}