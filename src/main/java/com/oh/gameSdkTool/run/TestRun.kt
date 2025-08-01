package com.oh.gameSdkTool.run

import com.oh.gameSdkTool.CommandArgs
import com.oh.gameSdkTool.CommandRun
import com.ohuang.apkMerge.printAllSmaliFileInfo
import com.ohuang.apkMerge.replaceXmlAttr

class TestRun:CommandRun() {
    override fun isRun(commandArgs: CommandArgs): Boolean {
        return commandArgs.isTest
    }

    override fun run(commandArgs: CommandArgs) {
        replaceXmlAttr(commandArgs.basePath,mapOf("layout_constraintTop_toBottomOf" to "test_layout_constraintTop_toBottomOf",
            "actionMenuTextAppearance" to "test_actionMenuTextAppearance",
            "dialogPreferredPadding" to "test_dialogPreferredPadding"
        ))
    }





}