package com.oh.gameSdkTool.run

import com.oh.gameSdkTool.CommandArgs
import com.oh.gameSdkTool.CommandRun
import com.ohuang.apkMerge.printAllSmaliFileInfo

class SmaliInfoRun: CommandRun()   {
    override fun isRun(commandArgs: CommandArgs): Boolean {

        return commandArgs.showSmaliInfo
    }

    override fun run(commandArgs: CommandArgs) {
        if (commandArgs.basePath.isNotEmpty()){
          printAllSmaliFileInfo(commandArgs.basePath)
        }
    }
}