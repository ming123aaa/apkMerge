package com.oh.gameSdkTool.run

import com.google.gson.Gson
import com.oh.gameSdkTool.CommandArgs
import com.oh.gameSdkTool.CommandRun
import com.oh.gameSdkTool.bean.ApkConfig
import com.oh.gameSdkTool.bean.SignConfig
import com.ohuang.replacePackage.FileUtils
import java.io.File

/**
 * SignConfig。json模板生成
 */
class OutSignConfig:CommandRun() {
    override fun isRun(commandArgs: CommandArgs): Boolean {
        return commandArgs.isOutSignConfig
    }

    override fun run(commandArgs: CommandArgs) {
        println("执行 -outSignConfig")
        val outPath = commandArgs.outPath
        if (outPath.isEmpty()){
            println("请设置 -outPath")
            return
        }
        val file = File(outPath)
        file.mkdirs()
        val signConfig = SignConfig()
        signConfig.signFileName="签名文件的名称  需要和SignConfig.json配置文件放在同一个文件夹下"
        signConfig.signVersion="可填写v1 v2 v3"
        val toJson = Gson().toJson(signConfig)
        FileUtils.writeText(File(file.absolutePath+"/SignConfig.json"),toJson)
    }
}