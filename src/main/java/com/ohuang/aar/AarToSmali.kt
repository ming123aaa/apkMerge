package com.ohuang.aar

import com.oh.gameSdkTool.CommandArgs
import com.oh.gameSdkTool.Decompile
import com.oh.gameSdkTool.Jar2Dex
import com.oh.gameSdkTool.bean.AarConfigData
import com.ohuang.apkMerge.AndroidManifest
import com.ohuang.apkMerge.copyPathAllFile
import com.ohuang.apkMerge.findSmaliClassesDir
import com.ohuang.apkMerge.forEachAllFile
import com.ohuang.apkMerge.getManifestPackage
import com.ohuang.replacePackage.FileUtils
import com.ohuang.replacePackage.changTextLine
import java.io.File

/**
 * 解压后的aar  转化成smali环境
 */
fun aarDirToArrSmali(commandArgs: CommandArgs, aarDir: String, aarSmaliPath: String) {
    copyPathAllFile(aarDir, aarSmaliPath)
    var deleteJar = findJar(aarSmaliPath)
    deleteJar.forEach { t ->
        FileUtils.delete(t)
    }
    var jars = findJar(aarDir)
    var num=1
    var aarSmaliFile = File(aarSmaliPath)
    jars.forEach { t ->
        var smailDir = getSmailDir(aarSmaliFile.absolutePath, num)
        Jar2Dex.jar2smail(commandArgs, t.absolutePath, smailDir)
        num++
    }
}

/**
 *  修改引用的R文件
 */
fun aarRSmaliChangePackage(aarSmaliPath: String, oldPackages: Set<String>,newPackageName: String){
    if(oldPackages.isEmpty()){
        return
    }
    var replaceMap: MutableMap<String, String> = HashMap ()
    oldPackages.forEach { old->
        var oldName = "L"+old.replace(".","/")+"/R$"
        var newName = "L"+newPackageName.replace(".","/")+"/R$"
        replaceMap[oldName] = newName
    }

    findSmaliClassesDir(aarSmaliPath).forEach{
        forEachAllFile(File(it)){
            changTextLine(it.absolutePath) { string ->
                var newString=string
                replaceMap.forEach { old, new ->
                    newString=newString.replace(old,new)
                }
             return@changTextLine newString
            }
            false
        }
    }
}

private fun getSmailDir(path: String, num: Int): String {
    if (num == 1) {
        return "$path/smali"
    } else {
        return "$path/smali_classes$num"
    }
}

private fun findJar(path: String):List<File>{
    return File(path).listFiles().filter {
        it.isFile && it.name.endsWith(".jar")
    }.toList()
}