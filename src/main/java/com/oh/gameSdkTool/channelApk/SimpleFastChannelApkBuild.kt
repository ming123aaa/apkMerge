package com.oh.gameSdkTool.channelApk

import com.oh.gameSdkTool.CommandArgs
import com.oh.gameSdkTool.Decompile
import com.oh.gameSdkTool.ReplaceAPk
import com.oh.gameSdkTool.bean.ChannelConfigItemBean
import com.ohuang.replacePackage.FileUtils
import com.ohuang.replacePackage.copyPathAllFile
import java.io.File

class SimpleFastChannelApkBuild : BaseChannelApkBuild() {

    var fastDir = "simpleFast"

    override fun decompile(commandArgs: CommandArgs, baseApk: String, buildPath: String): String {
       return CacheDecompile.decompileNoCode(commandArgs = commandArgs, baseApkPath = baseApk, buildPath = buildPath)
    }

    override fun build(
        commandArgs: CommandArgs,
        baseApkSmaliPath: String,
        buildPath: String,
        outApkPath: String,
        data: ChannelConfigItemBean
    ) {
        ReplaceAPk.replaceApK(baseApkSmaliPath,data.apkConfig)
        ReplaceAPk.optimizeAndroidManifest(baseApkSmaliPath)
        val smaliToApk = Decompile.smaliToApk(commandArgs, baseApkSmaliPath, "$buildPath/$fastDir/outApk", data.signConfig)
        val outApkFile=File(outApkPath)
        if (outApkFile.exists()){
            FileUtils.delete(outApkFile)
        }
        smaliToApk.renameTo(outApkFile)
        FileUtils.delete(File("$buildPath/$fastDir/outApk"))
    }

    override fun getBuildSmaliPath(baseSmali: String, buildPath: String, data: ChannelConfigItemBean): String {
        val smali = "$buildPath/$fastDir/baseSmali"
        val androidXml = "$buildPath/$fastDir/baseSmali/AndroidManifest.xml"
        if (File(smali).exists()) {
            FileUtils.writeText(File(androidXml),FileUtils.readText("$baseSmali/AndroidManifest.xml"))
//            copyPathAllFile(sourceRootPath= "$baseSmali/AndroidManifest.xml", targetRootPath = androidXml, isCover = true)
        } else {
            copyPathAllFile(sourceRootPath = baseSmali, targetRootPath = smali)
        }

        return smali
    }
}