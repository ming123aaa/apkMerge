package com.oh.gameSdkTool.run

import com.oh.gameSdkTool.CommandArgs
import com.oh.gameSdkTool.CommandRun
import com.oh.gameSdkTool.Decompile
import com.oh.gameSdkTool.ReplaceAPk
import com.oh.gameSdkTool.bean.toAarConfigData
import com.oh.gameSdkTool.bean.toApkConfig
import com.ohuang.aar.aarSmaliToAAr
import com.ohuang.aar.copyToAArSmali
import com.ohuang.aar.setAarSmali
import com.ohuang.apkMerge.getManifestPackage
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
            var aarName = if (aarConfigBean.outAArName.isNotBlank()) {
                aarConfigBean.outAArName.replace(".", "_") + ".aar"
            } else {
                "out.aar"
            }
            var aarPath = "$outPath/$aarName"
            var baseSmali = "$buildDir/baseSmali"
            var newSmali = "$buildDir/newSmali"
            var buildRJarDir = "$buildDir/buildRJarDir"
            val classBuildDir = "${buildDir}/classBuildDir"
            Decompile.apkToSmali(commandArgs,baseSmali, baseApk)

            copyToAArSmali( baseSmali, newSmali)
            val packageName=if (aarConfigBean.packageName.isBlank()) {
                getManifestPackage("$newSmali/AndroidManifest.xml") ?: "com.xxx.yyy"
            }else{
                aarConfigBean.packageName
            }
            setAarSmali(commandArgs,newSmali,packageName ,buildRJarDir)
            ReplaceAPk.replaceApK(newSmali, aarConfigBean.toApkConfig())
            if (File(aarPath).exists()) {
                FileUtils.delete(File(aarPath))
            }
            aarSmaliToAAr(commandArgs, newSmali, aarPath,classBuildDir)
            if (File(aarPath).exists()) {
                println("生成aar成功-$aarPath")
            } else {
                println("生成aar失败-$aarPath")
            }
        }
    }

    private fun checkArgs(commandArgs: CommandArgs): Boolean {
        println("Apk To Aar")


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