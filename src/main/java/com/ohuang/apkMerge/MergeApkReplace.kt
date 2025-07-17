package com.ohuang.apkMerge

import com.oh.gameSdkTool.ReplaceAPk
import com.oh.gameSdkTool.bean.ApkConfigBean
import com.oh.gameSdkTool.bean.getApkConfigBean
import com.ohuang.replacePackage.FileUtils
import java.io.File

private const val mergeApkContent="/assets/mergeApkContent"
private const val assets_merge_ApkConfig = "/assets/mergeApkContent/ApkConfig.json" //存在则触发合并前修改内容


private fun getConfig(rootPath: String):ApkConfigBean{
    return getApkConfigBean(rootPath+assets_merge_ApkConfig)
}

/**
 * 合并前根据ApkConfig修改内容
 */
fun mergeApkPreReplace(channelRootPath: String) {
    if (File(channelRootPath+assets_merge_ApkConfig).exists()){
        tryCatch{
            println("合并前ApkConfig修改内容:${channelRootPath+assets_merge_ApkConfig}")
            var config = getConfig(channelRootPath)
            ReplaceAPk.replaceApK(channelRootPath,config)
            FileUtils.delete(File(mergeApkContent))
        }

    }
}