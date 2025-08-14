package com.oh.gameSdkTool

import com.beust.jcommander.JCommander
import com.oh.gameSdkTool.config.GlobalConfig
import com.oh.gameSdkTool.run.*

object ArgsRunTime {

    fun run(commandArgs: CommandArgs,jCommander:JCommander) {

        GlobalConfig.isLog=commandArgs.isLog
        val arrayOf = arrayOf(
            TestRun(),
            GenerateMergeChannelApkRun(),
            GenerateMultipleChannelApkRun(),
            MergeSmaliRun(),
            OutApkConfigRun(),
            ChangeSmaliRun(),
            OutSignConfig(),
            ChangeApkRun(),
            SignRun(),
            ToApkRun(),
            DecompileApkRun(),
            OutChannelConfigRun(),
            UnzipRun(),
            ZipRun(),
            AArMergeRun(),
            ApkToAarRun(),
            SmaliInfoRun(),
            CmdWriteLogRun()
        )
        var runCommandArgs=false
        for (run in arrayOf) {
             runCommandArgs = run.runCommandArgs(commandArgs)
            if (runCommandArgs) {
                break
            }
        }
        if (!runCommandArgs){
            jCommander.usage()
        }
    }
}