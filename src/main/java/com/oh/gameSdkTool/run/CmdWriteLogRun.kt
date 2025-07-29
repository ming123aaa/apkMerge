package com.oh.gameSdkTool.run

import com.oh.gameSdkTool.CommandArgs
import com.oh.gameSdkTool.CommandRun
import com.oh.gameSdkTool.ReplaceAPk
import com.oh.gameSdkTool.bean.toAarConfigData
import com.oh.gameSdkTool.bean.toApkConfig
import com.ohuang.aar.aarDirToArrSmali
import com.ohuang.aar.RSmaliChangePackage
import com.ohuang.aar.aarSmaliToAAr
import com.ohuang.aar.mergeAarSmali
import com.ohuang.apkMerge.AndroidManifest
import com.ohuang.apkMerge.getManifestPackage
import com.ohuang.apkMerge.tryCatch
import com.ohuang.replacePackage.ExecUtil
import com.ohuang.replacePackage.FileUtils
import com.ohuang.replacePackage.ZipUtil
import java.io.File

class CmdWriteLogRun : CommandRun() {
    override fun isRun(commandArgs: CommandArgs): Boolean {
        return commandArgs.runCmdForWriteLog.isNotBlank()
    }

    override fun run(commandArgs: CommandArgs) {
        if (!checkArgs(commandArgs)) {
            return
        }
        tryCatch(true) {
            var outPath = commandArgs.outPath
            var file = File(outPath)
            ExecUtil.exec(arrayOf("cmd.exe", "/c", commandArgs.runCmdForWriteLog), 30 * 60, {
                FileUtils.appendText(file, it)
            }, {
                FileUtils.appendText(file, it)
            })

        }
    }


    private fun checkArgs(commandArgs: CommandArgs): Boolean {
        if (commandArgs.outPath.isEmpty()) {
            println("没有使用 -out 命令")
            return false
        }
        return true
    }
}