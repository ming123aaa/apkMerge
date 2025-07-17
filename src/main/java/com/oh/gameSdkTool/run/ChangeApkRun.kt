package com.oh.gameSdkTool.run

import com.oh.gameSdkTool.CommandArgs
import com.oh.gameSdkTool.CommandRun
import com.oh.gameSdkTool.Decompile
import com.oh.gameSdkTool.ReplaceAPk
import com.oh.gameSdkTool.bean.toApkConfigBean
import com.ohuang.replacePackage.FileUtils
import java.io.File

/**
 * 修改apk重新生成
 */

class ChangeApkRun: CommandRun() {
    override fun isRun(commandArgs: CommandArgs): Boolean {
       return commandArgs.changeApk.isNotEmpty()
    }

    override fun run(commandArgs: CommandArgs) {
        if (checkArgs(commandArgs)){
            val changeApk = commandArgs.changeApk
            val outPath = commandArgs.outPath
            val buildPath= "$outPath/build"
            val file = File(buildPath)
            FileUtils.delete(file)
            file.mkdirs()
            val smaliPath= "$buildPath/smaliPath"
            println("---开始反编译:$changeApk")
            Decompile.apkToSmali(commandArgs = commandArgs, smaliPath = smaliPath, apkPath = changeApk)
            println("---反编译完成$changeApk 输出:$smaliPath")
            val toApkConfigBean = commandArgs.toApkConfigBean()
            println("---开始修改smali环境")
            ReplaceAPk.replaceApK(
                rootPath = smaliPath,
                apkConfigBean=toApkConfigBean
            )
            ReplaceAPk.optimizeAndroidManifest(smaliPath)
            val smaliToApk = Decompile.smaliToApk(commandArgs, smaliPath, buildPath)
            val outApk=File("$outPath/out.apk")
            outApk.delete()
            smaliToApk.renameTo(outApk)
            println("apk生成完成:$outApk")
        }
    }

    private fun checkArgs(commandArgs: CommandArgs):Boolean{
        if (commandArgs.changeApk.isEmpty()){
            println("没有使用 -changeApk 命令")
            return false
        }
        if (commandArgs.apkConfig.isEmpty()){
            println("没有使用 -apkConfig 命令")
            return false
        }
        if (commandArgs.signConfig.isEmpty()){
            println("没有使用 -signConfig 命令")
            return false
        }
        if (commandArgs.libs.isEmpty()){
            println("没有使用 -libs 命令")
            return false
        }
        if (commandArgs.outPath.isEmpty()){
            println("没有使用 -out 命令")
            return false
        }
        return true
    }
}