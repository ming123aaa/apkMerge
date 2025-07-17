package com.ohuang.apkMerge

import com.ohuang.replacePackage.FileUtils
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

import java.util.TreeSet


fun replaceApktoolYml(
    ymlFilePath: String, versionCode: String,
    versionName: String,
    minSdkVersion: String,
    targetSdkVersion: String,
) {
    val map=HashMap<String,String>()
    if (minSdkVersion.isNotBlank()){
        map["minSdkVersion:"]="  minSdkVersion: $minSdkVersion"
    }
    if (versionCode.isNotBlank()){
        map["versionCode:"]="  versionCode: $versionCode"
    }
    if (versionName.isNotBlank()){
        map["versionName:"]="  versionName: $versionName"
    }
    if (targetSdkVersion.isNotBlank()){
        map["targetSdkVersion:"]="  targetSdkVersion: $targetSdkVersion"
    }
    val file = File(ymlFilePath)
    if (file.exists() && file.isFile) {
        val stringBuilder = StringBuilder()
        BufferedReader(FileReader(file)).lines().forEach {
            val newString=getChangeString(map,it)
            stringBuilder.append(newString)
            stringBuilder.append("\n")
        }
        FileUtils.writeText(file,stringBuilder.toString())
    }

}

private fun getChangeString(map: Map<String, String>, string: String): String {
    var newString = string
    map.forEach { t, u ->
        if (string.contains(t)){
            newString=u
        }
    }
    return newString
}

fun mergeYml(channelYml: String, baseYml: String, outYml: String = baseYml, isUseChannelApktoolYml: Boolean = false) {
    tryCatch {
        if (isUseChannelApktoolYml) {
            mergeYml(baseYml = channelYml, addYml = baseYml, outYml = outYml)
        } else {
            mergeYml(baseYml = baseYml, addYml = channelYml, outYml = outYml)
        }

    }
}

/**
 *
 */
private fun mergeYml(baseYml: String, addYml: String, outYml: String) {
    val baseText = FileUtils.readText(baseYml)
    val addYmlTxt = FileUtils.readText(addYml)
    if (baseText.isEmpty() || addYmlTxt.isEmpty()) {
        if (addYmlTxt.isNotEmpty()) {
            FileUtils.writeText(File(outYml), addYmlTxt)
        } else {
            FileUtils.writeText(File(outYml), baseText)
        }
        return
    }

    val ymlNotCompressFile = getYmlNotCompress(baseText)
    val needAddNotCompress = getYmlNotCompress(addYmlTxt)

    if (needAddNotCompress.isEmpty()) {
        FileUtils.writeText(File(outYml), baseText)
        return
    }
    FileUtils.writeText(File(outYml), addYmlNotCompress(baseText, ymlNotCompressFile, needAddNotCompress))
}

private fun addYmlNotCompress(ymlTxt: String, oldSet: Set<String>, needAddSet: Set<String>): String {
    val split = ymlTxt.split("\n")
    val stringBuilder = StringBuilder()
    split.forEach { string ->
        stringBuilder.append(string).append("\n")
        if (string.startsWith("doNotCompress:")) {
            needAddSet.forEach { newTxt ->
                if (!oldSet.contains(newTxt)) { //原来就已经有的就不需添加了
                    stringBuilder.append(newTxt).append("\n")
                }
            }

        }
    }
    return stringBuilder.toString()
}

private fun getYmlNotCompress(ymlString: String): Set<String> {
    val split = ymlString.split("\n")
    val hashSet = TreeSet<String>()
    var isNotCompress = false
    split.forEach {
        if (isNotCompress) {
            if (it.startsWith("-")) {
                hashSet.add(it)
            } else {
                isNotCompress = false
            }
        } else {
            if (it.startsWith("doNotCompress:")) {
                isNotCompress = true
            }
        }
    }
    return hashSet
}