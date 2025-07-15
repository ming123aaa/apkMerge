package com.oh.gameSdkTool.run

import com.oh.gameSdkTool.CommandArgs
import com.oh.gameSdkTool.CommandRun
import com.oh.gameSdkTool.Decompile

/**
 * 反编译apk
 */
class DecompileApkRun: CommandRun() {
    override fun isRun(commandArgs: CommandArgs): Boolean {
        return commandArgs.isDecompile
    }

    override fun run(commandArgs: CommandArgs) {
        if (!check(commandArgs)){
            return
        }
        Decompile.apkToSmali(commandArgs = commandArgs, smaliPath = commandArgs.outPath, apkPath = commandArgs.baseApk)
        println("反编译完成")
    }

    fun check(commandArgs: CommandArgs):Boolean{
        if (commandArgs.libs.isEmpty()){
            println("请使用 -libs")
            return false
        }
        if (commandArgs.baseApk.isEmpty()){
            println("请使用 -baseApk")
            return false
        }

        if (commandArgs.outPath.isEmpty()){
            println("请使用 -out 设置输出反编译后路径")
            return false
        }
        return true
    }
}