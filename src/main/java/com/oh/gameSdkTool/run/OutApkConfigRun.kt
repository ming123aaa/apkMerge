package com.oh.gameSdkTool.run

import com.google.gson.Gson
import com.oh.gameSdkTool.CommandArgs
import com.oh.gameSdkTool.CommandRun
import com.oh.gameSdkTool.bean.ApkConfig
import com.oh.gameSdkTool.bean.ReplaceStringData
import com.ohuang.replacePackage.FileUtils
import java.io.File

/**
 * 生成ApkConfig.json的模板说明
 */
class OutApkConfigRun : CommandRun() {
    override fun isRun(commandArgs: CommandArgs): Boolean {
        return commandArgs.isOutApkConfig
    }

    override fun run(commandArgs: CommandArgs) {
        println("执行 -outApkConfig")
        val outPath = commandArgs.outPath
        if (outPath.isEmpty()){
            println("请设置 -outPath")
            return
        }
        val file = File(outPath)
        file.mkdirs()
        val apkConfig = ApkConfig()
        apkConfig.appName="app的名称"
        apkConfig.packageName="包名"
        apkConfig.iconImgPath="图片的文件名,需要和本文件放在同一目录下"
        apkConfig.iconSize="-xxhdpi"
        apkConfig.metaDataMap= mapOf("data1" to "meta-data数据修改","data2" to "meta-data数据修改")
        apkConfig.replaceStringManifest= listOf(ReplaceStringData().apply {
            matchString="用于替换androidManifest.xml的数据"
            replaceString="这是替换后的数据"
        })

        val toJson = Gson().toJson(apkConfig)
        FileUtils.writeText(File(file.absolutePath+"/ApkConfig.json"),toJson)
    }
}