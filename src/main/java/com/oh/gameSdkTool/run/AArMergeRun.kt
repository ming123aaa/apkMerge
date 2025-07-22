package com.oh.gameSdkTool.run

import com.oh.gameSdkTool.CommandArgs
import com.oh.gameSdkTool.CommandRun
import com.oh.gameSdkTool.ReplaceAPk
import com.oh.gameSdkTool.bean.toAarConfigData
import com.oh.gameSdkTool.bean.toApkConfig
import com.ohuang.aar.aarDirToArrSmali
import com.ohuang.aar.aarRSmaliChangePackage
import com.ohuang.aar.aarSmaliToAAr
import com.ohuang.aar.mergeAarSmali
import com.ohuang.apkMerge.AndroidManifest
import com.ohuang.apkMerge.getManifestPackage
import com.ohuang.apkMerge.tryCatch
import com.ohuang.replacePackage.FileUtils
import com.ohuang.replacePackage.ZipUtil
import java.io.File

class AArMergeRun : CommandRun() {
    override fun isRun(commandArgs: CommandArgs): Boolean {
        return commandArgs.isMergeAar
    }

    override fun run(commandArgs: CommandArgs) {
        if (!checkArgs(commandArgs)) {
            return
        }
        tryCatch(true) {
            val outPath = commandArgs.outPath
            val buildDir = "${outPath}/build"
            val aarConfigBean = commandArgs.toAarConfigData()
            var aarName = if (aarConfigBean.outAArName.isNotBlank()) {
                aarConfigBean.outAArName.replace(".", "_") + ".aar"
            } else {
                "out.aar"
            }

            val aarPath = "${outPath}/${aarName}"
            if (File(buildDir).exists()) {
                println("删除-$buildDir")
                FileUtils.delete(File(buildDir))
            }
            if (aarConfigBean.packageName.isBlank()) {
                println("AarConfig.json 中的packageName字段不能为空")
                return
            }
            var index = 0
            val baseSmali: String = "${buildDir}/baseSmali"
            val oldPackage= HashSet<String>()
            aarConfigBean.aarPathList.forEach {
                mergeToBaseSmali(commandArgs, index, it, oldPackage, buildDir)
                index++
            }
            ReplaceAPk.replaceApK(baseSmali, aarConfigBean.toApkConfig())
            if (File(aarPath).exists()) {
                FileUtils.delete(File(aarPath))
            }
            val classBuildDir: String = "${buildDir}/classBuildDir"
            aarRSmaliChangePackage(baseSmali, oldPackage, aarConfigBean.packageName)
            aarSmaliToAAr(commandArgs, baseSmali, aarPath,classBuildDir)
            if (File(aarPath).exists()) {
                println("生成aar成功-$aarPath")
            } else {
                println("生成aar失败-$aarPath")
            }

        }
    }

    private fun mergeToBaseSmali(
        commandArgs: CommandArgs,
        index: Int,
        aarFile: File,
        oldPackages: HashSet<String>,
        buildPath: String
    ) {
        val aarDirPath = "${buildPath}/aarDir${index}"
        val baseSmali = "${buildPath}/baseSmali"
        val aarSmaliPath = "${buildPath}/aarSmali${index}"
        ZipUtil.unzip(aarFile.absolutePath, aarDirPath)
        aarDirToArrSmali(commandArgs, aarDirPath, aarSmaliPath)
        var oldPackage=getManifestPackage("${aarSmaliPath}${AndroidManifest}")
        if (oldPackage!=null&&oldPackage.isNotBlank()){
            oldPackages.add(oldPackage)
        }
        mergeAarSmali(aarSmaliPath, baseSmali)

    }


    private fun checkArgs(commandArgs: CommandArgs): Boolean {
        println("mergeAAr  ")
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