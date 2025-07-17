package com.oh.gameSdkTool.run

import com.oh.gameSdkTool.CommandArgs
import com.oh.gameSdkTool.CommandRun
import com.oh.gameSdkTool.bean.NewName
import com.oh.gameSdkTool.bean.OldName
import com.oh.gameSdkTool.bean.ResType
import com.ohuang.apkMerge.limitSize_smali_class_Dir
import com.ohuang.replacePackage.changTextLine
import kotlinx.coroutines.runBlocking
import java.util.regex.Pattern

class TestRun:CommandRun() {
    override fun isRun(commandArgs: CommandArgs): Boolean {
        return commandArgs.isTest
    }

    override fun run(commandArgs: CommandArgs) {

    }





}