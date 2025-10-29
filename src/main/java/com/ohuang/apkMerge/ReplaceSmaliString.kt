package com.ohuang.apkMerge

import com.oh.gameSdkTool.bean.ApkConfigBean
import com.oh.gameSdkTool.bean.ReplaceStringForFileName
import com.ohuang.replacePackage.FileUtils
import java.io.File
import kotlin.collections.forEach

/**
 *  替换smali文件内的字符串
 */
fun replaceSmaliString(rootPath: String, apkConfigBean: ApkConfigBean) {
    if (apkConfigBean.replaceStringSmali.isNotEmpty()) {
        println("---开始替换smali文件的内容---")
        findSmaliClassesDir(rootPath).forEach { path ->
            var file = File(path)
            var absolutePath = file.absolutePath
            var length = absolutePath.length
            forEachAllFile(file) { childFile ->
                var name = childFile.absolutePath.substring(length).replace("\\", "/")
                replaceString(childFile, name, apkConfigBean.replaceStringSmali)
                false
            }
        }
    }
}

private fun replaceString(file: File, namePath: String, replaceStringManifest: List<ReplaceStringForFileName>) {
    if (!file.exists()) {
        return
    }
    var isShouldReplace = false
    replaceStringManifest.forEach {
        if (it.fileName.isEmpty() || namePath.contains(it.fileName)) {
            isShouldReplace = true
        }
    }
    if (!isShouldReplace) {
        return
    }
    val readText = FileUtils.readText(file.absolutePath)
    val newTxt = replaceString(namePath, readText, replaceStringManifest)
    FileUtils.writeText(file, newTxt)

}

private fun replaceString(
    namePath: String,
    oldString: String,
    replaceStringManifest: List<ReplaceStringForFileName>
): String {
    var newString = oldString
    replaceStringManifest.forEach {
        tryCatch {
            if (it.fileName.isEmpty() || namePath.contains(it.fileName)) {
                if (it.matchString.isNotEmpty()) {
                    newString = replaceString(it, newString)
                }
                if (it.appendString.isNotEmpty()){
                    newString=newString+it.appendString
                }
            }
        }
    }
    return newString
}

private fun replaceString(it: ReplaceStringForFileName, oldString: String): String =
    if (it.isRegex) {
        oldString.replace(it.matchString.toRegex(), it.replaceString)
    } else {
        oldString.replace(it.matchString, it.replaceString)
    }
