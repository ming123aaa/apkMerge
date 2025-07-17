package com.oh.gameSdkTool.channelApk

import com.oh.gameSdkTool.CommandArgs
import com.oh.gameSdkTool.Decompile
import com.ohuang.apkMerge.parentMkdirs
import java.io.File

object CacheDecompile {

    fun decompile(commandArgs: CommandArgs, baseApkPath:String, buildPath:String):String{
        val path = "$buildPath/cache/baseSmaliPath"
        val file = File(path)
        if (file.exists()){
            return path
        }
        file.parentMkdirs()
        Decompile.apkToSmali(commandArgs = commandArgs,smaliPath=path,apkPath=baseApkPath)
        return path
    }
    fun decompileNoCode(commandArgs: CommandArgs, baseApkPath:String, buildPath:String):String{
        val path = "$buildPath/cache/baseSmaliPath_noCode"
        val file = File(path)
        if (file.exists()){
            return path
        }
        file.parentMkdirs()
        Decompile.apkToSmaliNoCode(commandArgs = commandArgs,smaliPath=path,apkPath=baseApkPath)
        return path
    }
}