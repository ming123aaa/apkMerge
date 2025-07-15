package com.oh.gameSdkTool

import com.oh.gameSdkTool.config.GlobalConfig
import com.oh.gameSdkTool.run.*

object ArgsRunTime {

    fun run(commandArgs: CommandArgs) {

        GlobalConfig.isLog=commandArgs.isLog
        val arrayOf = arrayOf(
            TestRun(),
            GenerateMergeChannelApkRun(),
            MergeSmaliRun(),
            OutApkConfigRun(),
            ChangeSmaliRun(),
            OutSignConfig(),
            ChangeApkRun(),
            SignRun(),
            ToApkRun(),
            DecompileApkRun(),
            OutChannelConfigRun(),
            GenerateMultipleChannelApkRun()
        )

        for (run in arrayOf) {
            val runCommandArgs = run.runCommandArgs(commandArgs)
            if (runCommandArgs) {
                break
            }
        }
    }
}