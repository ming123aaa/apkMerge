package com.ohuang.apkMerge

import com.oh.gameSdkTool.bean.ApkConfigBean
import com.oh.gameSdkTool.bean.ReplaceStringForFileName
import com.oh.gameSdkTool.bean.ReplaceStringForPath
import com.ohuang.replacePackage.FileUtils
import java.io.File

fun replaceFileString(rootPath: String, apkConfigBean: ApkConfigBean) {
    if (apkConfigBean.replaceStringFile.isNotEmpty()) {
        println("---开始替换指定文件的内容---")
        apkConfigBean.replaceStringFile.forEach { data ->
            if (data.namePath.isNotEmpty()) {
                if (data.namePath.startsWith("/")) {
                    replaceFile(File(rootPath + data.namePath), data)
                } else {
                    replaceFile(File(rootPath + "/" + data.namePath), data)
                }

            }
        }


    }
}

private fun replaceFile(file: File, replaceStringForPath: ReplaceStringForPath) {
    if (!file.exists()) {
        return
    }
    if (file.isDirectory) {
        return
    }
    tryCatch() {
        val readText = FileUtils.readText(file.absolutePath)
        println("替换指定文件内容-${file.absolutePath}")
        var newTxt = replaceString(replaceStringForPath, readText)
        if (replaceStringForPath.appendString.isNotEmpty()) {
            newTxt = newTxt + replaceStringForPath.appendString
        }
        FileUtils.writeText(file, newTxt)
    }

}

private fun replaceString(it: ReplaceStringForPath, oldString: String): String =
    if (it.isRegex) {
        oldString.replace(it.matchString.toRegex(), it.replaceString)
    } else {
        oldString.replace(it.matchString, it.replaceString)
    }
