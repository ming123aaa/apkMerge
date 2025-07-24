package com.oh.gameSdkTool.run

import com.oh.gameSdkTool.CommandArgs
import com.oh.gameSdkTool.CommandRun

class TestRun:CommandRun() {
    override fun isRun(commandArgs: CommandArgs): Boolean {
        return commandArgs.isTest
    }

    override fun run(commandArgs: CommandArgs) {

    }





}