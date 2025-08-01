package com.oh.gameSdkTool.channelApk

import com.oh.gameSdkTool.CommandArgs
import com.oh.gameSdkTool.bean.ChannelConfigItemBean
import com.ohuang.apkMerge.parentMkdirs
import java.io.File

abstract class BaseChannelApkBuild() {


    protected abstract fun decompile(commandArgs:CommandArgs, baseApk: String, buildPath:String):String


    fun run(commandArgs:CommandArgs,baseApk:String, buildPath:String,outPath:String,data: ChannelConfigItemBean):Boolean{
        println("开始反编译主包 channelName=${data.channelName} mode=${data.mode}  class=${javaClass}")
        val baseApkSmaliPath = decompile(commandArgs,baseApk,buildPath)
        println("准备smali环境 channelName=${data.channelName} mode=${data.mode}  class=${javaClass}")
        val buildSmaliPath = getBuildSmaliPath(baseApkSmaliPath, buildPath,data)
        println("开始build渠道 channelName=${data.channelName} mode=${data.mode}  class=${javaClass}")
        val outApk=outPath+"/${getOutFileName(data)}"
        File(outApk).parentMkdirs()
        build(commandArgs,buildSmaliPath,buildPath,outApk, data)
        println("output=${outApk} 编译${if (File(outApk).exists()) "成功" else "失败"}")
        return File(outApk).exists()
    }

    protected  open fun getOutFileName(data: ChannelConfigItemBean):String{
        return "${data.apkName.ifEmpty { data.channelName }}.apk"
    }

    protected abstract fun build(commandArgs:CommandArgs, baseApkSmaliPath:String, buildPath:String, outApkPath:String, data: ChannelConfigItemBean)


    protected abstract fun getBuildSmaliPath(baseSmali:String, buildPath:String,data: ChannelConfigItemBean):String
}

