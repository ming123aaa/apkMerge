package com.oh.gameSdkTool.channelApk

import com.oh.gameSdkTool.CommandArgs
import com.oh.gameSdkTool.bean.*
import com.ohuang.apkMerge.tryCatch
import com.ohuang.replacePackage.FileUtils
import java.io.File
import java.util.ArrayList

object ChannelApkBuild {

    private fun run(
        commandArgs: CommandArgs,
        baseApk: String,
        buildPath: String,
        outPath: String,
        data: ChannelConfigItemBean
    ):Boolean {
        return getBuild(data.mode).run(commandArgs = commandArgs, baseApk, buildPath, outPath, data)
    }

    fun build(commandArgs: CommandArgs) {
        tryCatch{
            val channelConfigBean = getChannelConfigBean(channelConfigFile = File(commandArgs.channelConfig))
            deleteFile(channelConfigBean.buildDirFile)
            deleteFile(channelConfigBean.outDirFile)
            val baseApk = commandArgs.basePath
            val fairChannels=ArrayList<String>()
            val successChannels=ArrayList<String>()
            channelConfigBean.items.forEach {
                var isSuccess=false
                tryCatch {
                    isSuccess=run(
                        commandArgs = commandArgs,
                        baseApk = baseApk,
                        buildPath = channelConfigBean.buildDirFile.absolutePath,
                        outPath = channelConfigBean.outDirFile.absolutePath,
                        it
                    )
                }
                if (!isSuccess){
                    fairChannels.add(it.channelName)
                }else{
                    successChannels.add(it.channelName)
                }
            }
            successChannels.forEach {
                println("渠道 $it 生成apk完成")
            }
            fairChannels.forEach {
                println("渠道: $it 生成apk失败")
            }
        }
    }

    private fun deleteFile(file:File){
        println("---删除 ${file.absolutePath}")
        FileUtils.delete(file)
    }


    private fun getBuild(mode: String): BaseChannelApkBuild {
        return when (mode) {
            MODE_SKIP -> SkipChannelApkBuild()
            MODE_SIMPLE -> SimpleChannelApkBuild()
            MODE_SIMPLE_Fast -> SimpleFastChannelApkBuild()
            MODE_MERGE -> MergeChannelApkBuild()
            MODE_LIST->MergeListChannelApkBuild()
            MODE_MERGE_Reverse->MergeReverseChannelApkBuild()
            else -> {
                SkipChannelApkBuild()
            }
        }
    }

}