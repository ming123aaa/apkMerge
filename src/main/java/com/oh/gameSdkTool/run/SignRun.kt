package com.oh.gameSdkTool.run

import com.oh.gameSdkTool.CommandArgs
import com.oh.gameSdkTool.CommandRun
import com.oh.gameSdkTool.Decompile
import com.oh.gameSdkTool.bean.toSignConfigBean
import com.oh.gameSdkTool.config.LibConfig
import java.io.File

/**
 * apk签名
 */
class SignRun: CommandRun()  {
    override fun isRun(commandArgs: CommandArgs): Boolean {
        return commandArgs.isSign
    }

    override fun run(commandArgs: CommandArgs) {
        val baseApk = commandArgs.baseApk
        val signConfig = commandArgs.signConfig
        val outPath = commandArgs.outPath
        val libs = commandArgs.libs
        if (libs.isEmpty()){
            println("请通过-libs设置环境")
            return
        }
        if (baseApk.isEmpty()){
            println("请通过-baseApk设置需要签名的apk")
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
        Decompile.signApk(javaexe = LibConfig.getJava(commandArgs), apksigner = LibConfig.apksignerPath(commandArgs),
            oldApkPath=baseApk,outFile= File(outPath), signConfigBean = commandArgs.toSignConfigBean()
        )
    }
}