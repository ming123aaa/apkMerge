package com.oh.gameSdkTool

import com.oh.gameSdkTool.config.GlobalConfig
import com.oh.gameSdkTool.config.LibConfig
import com.ohuang.apkMerge.findDexFile
import com.ohuang.apkMerge.getNewSmailDir
import com.ohuang.replacePackage.ExecUtil
import com.ohuang.replacePackage.FileUtils
import java.io.File

object Jar2Dex {

    /**
     *  jar 转  dex
     *
     *  返回 文件dex列表
     */
    fun jar2dex(commandArgs: CommandArgs, jarPath: String, outDirPath: String): List<String> {
        println("jar2dex")
        if (!File(outDirPath).exists()){
            File(outDirPath).mkdirs()
        }

        var d8Jar2Dex = d8Jar2Dex(LibConfig.buildTools(commandArgs), jarPath, outDirPath)
        return d8Jar2Dex
//        jar2dex(LibConfig.dexToolDir(commandArgs), jarPath, outPath)
    }

    fun dex2jar(commandArgs: CommandArgs, dexPath: String, outPath: String) {
        println("dex2jar")
        dex2jar(LibConfig.dexToolDir(commandArgs), dexPath, outPath)
    }

    fun smail2Dex(commandArgs: CommandArgs, smaliPath: String, outPath: String) {
        println("smail2Dex")
//        smail2Dex(LibConfig.dexToolDir(commandArgs), smaliPath, outPath)
        smali2DexUseBaksmali(LibConfig.getJava(commandArgs), LibConfig.smaliJar(commandArgs), smaliPath, outPath)
    }


    fun jar2smail(commandArgs: CommandArgs, jarPath: String, dirOutPath: String): List<String> {
        println("jar2smail")
        var file = File(dirOutPath)
        var dexTemp = file.absolutePath + "/jar2smail_dexTemp"
        var jar2dex = jar2dex(commandArgs, jarPath, dexTemp)
        var smaliFiles= ArrayList<String>()
        jar2dex.forEachIndexed({ index, s ->
            var nextSmailDir = getNewSmailDir(dirOutPath)
            dex2smail(commandArgs, s, nextSmailDir)
            smaliFiles.add(nextSmailDir)
        })

        FileUtils.delete(File(dexTemp))
        return smaliFiles
    }

    fun smali2jar(commandArgs: CommandArgs, smaliPath: String, outPath: String) {
        println("smali2jar")
        var file = File(outPath)
        var dexTemp = file.absolutePath + "_dexTemp.dex"
        smail2Dex(commandArgs, smaliPath, dexTemp)
        dex2jar(commandArgs, dexTemp, outPath)
        FileUtils.delete(File(dexTemp))
    }

    fun dex2smail(commandArgs: CommandArgs, dexPath: String, outPath: String) {
        println("dex2smail")
//        dex2smail(LibConfig.dexToolDir(commandArgs), dexPath, outPath)
        dex2SmaliUseBaksmali(LibConfig.getJava(commandArgs), LibConfig.baksmaliJar(commandArgs), dexPath, outPath)
    }

    private fun dex2smail(dexToolDir: String, dexPath: String, outPath: String) {
        val exe = "$dexToolDir/d2j-dex2smali.bat"
        val cmd = "\"$exe\" -o \"$outPath\" \"$dexPath\""
        var arrayOf = arrayOf(cmd)
        println(arrayOf.joinToString(" "))
        ExecUtil.exec(arrayOf, 60 * 30, GlobalConfig.isLog)
    }

    private fun smail2Dex(dexToolDir: String, smaliPath: String, outPath: String) {
        val exe = "$dexToolDir/d2j-smali.bat"
        val cmd = "\"$exe\" -o \"$outPath\" \"$smaliPath\""
        var arrayOf = arrayOf(cmd)
        println(arrayOf.joinToString(" "))
        ExecUtil.exec(arrayOf, 60 * 30, GlobalConfig.isLog)
    }

    private fun dex2SmaliUseBaksmali(java: String, baksmaliJar: String, dexPath: String, outPath: String) {
        var arrayOf = arrayOf(java, "-Dfile.encoding=utf-8", "-jar", baksmaliJar, "d", "-o", outPath, dexPath)
        println(arrayOf.joinToString(" "))
        ExecUtil.exec(arrayOf, 60 * 30, GlobalConfig.isLog)
    }

    private fun smali2DexUseBaksmali(java: String, smaliJar: String, smaliPath: String, outPath: String) {
        var arrayOf =
            arrayOf(java, "-Dfile.encoding=utf-8", "-jar", smaliJar, "a", "--verbose", "-o", outPath, smaliPath)
        println(arrayOf.joinToString(" "))
        ExecUtil.exec(arrayOf, 60 * 30, GlobalConfig.isLog)
    }

    private fun dex2jar(dexToolDir: String, dexPath: String, outPath: String) {
        val exe = "$dexToolDir/d2j-dex2jar.bat"

        val cmd = "\"$exe\" -f \"$dexPath\" -o \"$outPath\""
        var arrayOf = arrayOf(cmd)
        println(arrayOf.joinToString(" "))
        ExecUtil.exec(arrayOf, 60 * 30, GlobalConfig.isLog)
    }

    private fun jar2dex(dexToolDir: String, jarPath: String, outPath: String) {
        val exe = "$dexToolDir/d2j-jar2dex.bat"
        val cmd = "\"$exe\" -f \"$jarPath\" -o \"$outPath\""
        var arrayOf = arrayOf(cmd)
        println(arrayOf.joinToString(" "))
        ExecUtil.exec(arrayOf, 60 * 30, GlobalConfig.isLog)
    }

    private fun d8Jar2Dex(buildTools: String, jarPath: String, outDirPath: String): List<String> {
        val exe = "$buildTools/d8.bat"
        var outDir: File = File(outDirPath)

        val cmd = "\"$exe\" --output \"${outDir.absolutePath}\" --min-api 21 \"$jarPath\""
        var arrayOf = arrayOf(cmd)
        println(arrayOf.joinToString(" "))
        ExecUtil.exec(arrayOf, 60 * 30, GlobalConfig.isLog, GlobalConfig.isLog)
       return findDexFile(outDir.absolutePath)
    }
}