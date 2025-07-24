package com.ohuang.aar

import com.oh.gameSdkTool.CommandArgs
import com.oh.gameSdkTool.Jar2Dex
import com.ohuang.apkMerge.copySmaliClass
import com.ohuang.apkMerge.findSmaliClassesDirSort
import com.ohuang.apkMerge.mergeRes
import com.ohuang.apkMerge.mergeSafeManifest
import com.ohuang.replacePackage.FileUtils
import com.ohuang.replacePackage.ZipUtil
import com.ohuang.replacePackage.copyPathAllFile
import java.io.File


const val proguard = "/proguard.txt"

const val RText = "/R.txt"
const val PublicText = "/public.txt"
const val AndroidManifest = "/AndroidManifest.xml"
private val fileNameFilter =
    setOf<String>("proguard.txt", "R.txt", "public.txt", "AndroidManifest.xml")

fun mergeAarSmali(oldAArSmali: String, newAArSmali: String) {
    File(oldAArSmali).listFiles().forEach {
        if (it.name.startsWith("smali") || fileNameFilter.contains(it.name)) {//拦截复制文件
            return@forEach
        }
        copyPathAllFile(oldAArSmali, newAArSmali, "/" + it.name)
    }
    copySmaliClass(oldAArSmali, newAArSmali)
    mergeRes(oldAArSmali, newAArSmali)
    mergeSafeManifest(path = oldAArSmali + AndroidManifest, mainPath = newAArSmali + AndroidManifest)
    mergeText(oldAArSmali, newAArSmali, proguard)
    mergeText(oldAArSmali, newAArSmali, RText)
    mergeText(oldAArSmali, newAArSmali, PublicText)
}

/**
 * aar smali环境
 * aar AAR文件
 * classBuildDir  jar生成需要文件夹
 */
fun aarSmaliToAAr(commandArgs: CommandArgs, aarSmaliPath: String, aar: String, classBuildDir: String) {
    var findSmaliClassesDir = findSmaliClassesDirSort(aarSmaliPath)
    var aarSmaliDir = File("$classBuildDir/temp_jar") //临时存放jar的目录
    if (!aarSmaliDir.exists()) {
        aarSmaliDir.mkdirs()
    }
    var num = 1
    var jars = ArrayList<String>()
    findSmaliClassesDir.forEach { t ->
        var classJar = getClassJar(aarSmaliDir.absolutePath, num)
        println("生成jar:$classJar")
        Jar2Dex.smali2jar(commandArgs, t, classJar)
        jars.add(classJar)
        num++
    }
    findSmaliClassesDir.forEach { t ->
        println("删除smali:$t")
        FileUtils.delete(File(t))
    }
    mergeJar(jarPaths = jars, outJarPath = getClassJar(aarSmaliPath, 1), classBuildDir)

    ZipUtil.toZip(aar, aarSmaliPath, true)
}

/**合并jar
 * @param jarPath jar路径
 **/
fun mergeJar(jarPaths: List<String>, outJarPath: String, classBuildDir: String) {
    if (jarPaths.isEmpty()) {
        return
    }
    if (jarPaths.size == 1) { //只有一个jar
        copyPathAllFile(jarPaths[0], outJarPath)
        return
    }
    var jarNum = 1
    var tempFile = File(classBuildDir)
    val mergeFile = File("${tempFile.absolutePath}/classes") //合并后的文件
    if (!mergeFile.exists()) {
        mergeFile.mkdirs()
    }
    jarPaths.forEach {
        val file = File(it)
        if (file.exists() && file.isFile) {
            var outJarDir = "${tempFile.absolutePath}/classes$jarNum"
            ZipUtil.unzip(file.absolutePath, outJarDir)  //解压
            copyPathAllFile(outJarDir, mergeFile.absolutePath)  //合并
        }
        jarNum++
    }
    ZipUtil.toZip(outJarPath, mergeFile.absolutePath, true)  //压缩
}

private fun getClassJar(rootPath: String, num: Int): String {
    if (num == 1) {
        return "$rootPath/classes.jar"
    }
    return "$rootPath/classes$num.jar"
}

private fun mergeText(oldAArSmali: String, newAArSmali: String, fileName: String) {
    val oldPath = oldAArSmali + fileName
    val newPath = newAArSmali + fileName
    var oldString = FileUtils.readText(oldPath)
    var string = FileUtils.readText(newPath)
    FileUtils.writeText(File(newPath), string + "\n" + oldString)
}




