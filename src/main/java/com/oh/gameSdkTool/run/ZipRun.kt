package com.oh.gameSdkTool.run

import com.oh.gameSdkTool.CommandArgs
import com.oh.gameSdkTool.CommandRun
import com.ohuang.replacePackage.FileUtils
import com.ohuang.replacePackage.ZipUtil
import java.io.File

class ZipRun : CommandRun() {
    override fun isRun(commandArgs: CommandArgs): Boolean {
        return commandArgs.toZip
    }

    override fun run(commandArgs: CommandArgs) {
        if (!checkArgs(commandArgs)) {
            return
        }
        var basePath = commandArgs.basePath
        var outPath = if(commandArgs.outPath.endsWith(".zip")){
            commandArgs.outPath
        }else{
            commandArgs.outPath + ".zip"
        }
        var file = File(basePath)
        var outFile = File(outPath)
        if (file.exists()) {
            if (outFile.exists()) {
                println("正在删除$outPath")
                FileUtils.delete(outFile)
            }
            println("开始压缩文件-$basePath")
            ZipUtil.toZip(outFile.absolutePath, file.absolutePath, true)
        }

    }

    private fun checkArgs(commandArgs: CommandArgs): Boolean {
        println("执行压缩文件命令")
        if (commandArgs.basePath.isEmpty()) {
            println("没有使用 -basePath 命令")
            return false
        }

        if (commandArgs.outPath.isEmpty()) {
            println("没有使用 -out 命令")
            return false
        }
        return true
    }
}