package com.oh.gameSdkTool.run

import com.oh.gameSdkTool.CommandArgs
import com.oh.gameSdkTool.CommandRun
import com.ohuang.replacePackage.FileUtils
import com.ohuang.replacePackage.ZipUtil
import java.io.File

class UnzipRun(): CommandRun() {
    override fun isRun(commandArgs: CommandArgs): Boolean {
        return commandArgs.toUnzip
    }

    override fun run(commandArgs: CommandArgs) {
        if(!checkArgs(commandArgs)){
            return
        }
        var basePath = commandArgs.basePath
        var outPath= commandArgs.outPath
        var file = File(basePath)
        var outFile = File(outPath)
        if (file.exists()&&file.isFile) {
            if (outFile.exists()) {
                println("正在删除$outPath")
                FileUtils.delete(outFile)
            }
            println("开始解压文件-$basePath")
            ZipUtil.unzip(file.absolutePath,outFile.absolutePath)
        }else{
            println("$basePath 不是一个文件")
        }
    }

    private fun checkArgs(commandArgs: CommandArgs): Boolean {
        println("执行解压文件命令")
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