package com.oh.gameSdkTool

import com.oh.gameSdkTool.bean.SignConfigBean
import com.oh.gameSdkTool.bean.toSignConfigBean
import com.oh.gameSdkTool.config.GlobalConfig
import com.oh.gameSdkTool.config.LibConfig

import com.ohuang.replacePackage.ExecUtil
import com.ohuang.replacePackage.FileUtils
import com.ohuang.replacePackage.ZipUtil
import com.ohuang.replacePackage.copyPathAllFile
import java.io.File

object Decompile {

    fun apkToSmali(commandArgs: CommandArgs, smaliPath: String, apkPath: String) {
        if (apkPath.endsWith(".apk")) {
            apkToSmali(
                javaexe = LibConfig.getJava(commandArgs), apktool = LibConfig.apkToolPath(commandArgs),
                apkPath = apkPath, smaliPath = smaliPath
            )
        }else if(apkPath.endsWith(".zip")){
            println("解压$apkPath 到$smaliPath")
            ZipUtil.unzip(apkPath,smaliPath)
        }else{
            val file = File(apkPath)
            if (file.isDirectory){
                println("$apkPath 复制到 $smaliPath")
                copyPathAllFile(apkPath,smaliPath)
            }else{
                println("不支持反编译:$apkPath")
            }
        }

    }

    fun apkToSmaliNoCode(commandArgs: CommandArgs, smaliPath: String, apkPath: String){
        if (apkPath.endsWith(".apk")) {
            apkToSmaliNoCode(
                javaexe = LibConfig.getJava(commandArgs), apktool = LibConfig.apkToolPath(commandArgs),
                apkPath = apkPath, smaliPath = smaliPath
            )
        }else if(apkPath.endsWith(".zip")){
            println("解压$apkPath 到$smaliPath")
            ZipUtil.unzip(apkPath,smaliPath)
        }else{
            val file = File(apkPath)
            if (file.isDirectory){
                println("$apkPath 复制到 $smaliPath")
                copyPathAllFile(apkPath,smaliPath)
            }else{
                println("不支持反编译:$apkPath")
            }
        }
    }

    private fun apkToSmali(javaexe: String?, apktool: String, apkPath: String, smaliPath: String) {
        val java: String = if (javaexe.isNullOrEmpty()) {
            "java"
        } else {
            javaexe.replace(" ", "\" \"")
        }
        val cmd =
            "$java -Dfile.encoding=utf-8 -jar  \"$apktool\" d --only-main-classes \"$apkPath\"  -o \"${smaliPath}\" -f"
        useApkToolDecompile(javaexe, apktool, apkPath, smaliPath, cmd)
    }

    private fun useApkToolDecompile(javaexe: String?, apktool: String, apkPath: String, smaliPath: String, cmd:String){
        println("开始反编译")
        val file = File(smaliPath)
        if (file.exists()) {
            println("删除文件${file.name}")
            val delete = FileUtils.delete(file)
            if (delete) {
                println("删除成功")
            } else {
                println("删除失败")
            }
        }

        println(cmd)
        val arrayOf = arrayOf(
            "cmd.exe", "/c", cmd
        )

        val exec = ExecUtil.exec(
            arrayOf,
            60 * 60, GlobalConfig.isLog
        )
        println(exec)
    }

    /**
     * 不解代码
     */
    private fun apkToSmaliNoCode(javaexe: String?, apktool: String, apkPath: String, smaliPath: String) {
        val java: String = if (javaexe.isNullOrEmpty()) {
            "java"
        } else {
            javaexe.replace(" ", "\" \"")
        }
        val cmd =
            "$java -Dfile.encoding=utf-8 -jar  \"$apktool\" d  \"$apkPath\"  -o \"${smaliPath}\" -f --no-src"
        useApkToolDecompile(javaexe, apktool, apkPath, smaliPath, cmd)
    }

    /**
     * 不解代码和资源
     */
    private fun apkToSmaliNoCodeRes(javaexe: String?, apktool: String, apkPath: String, smaliPath: String) {
        val java: String = if (javaexe.isNullOrEmpty()) {
            "java"
        } else {
            javaexe.replace(" ", "\" \"")
        }
        val cmd =
            "$java -Dfile.encoding=utf-8 -jar  \"$apktool\" d  \"$apkPath\"  -o \"${smaliPath}\" -f --no-src --no-res"
        useApkToolDecompile(javaexe, apktool, apkPath, smaliPath, cmd)
    }

    /**
     * 不解代码
     */
    private fun apkToSmaliNoRes(javaexe: String?, apktool: String, apkPath: String, smaliPath: String) {
        val java: String = if (javaexe.isNullOrEmpty()) {
            "java"
        } else {
            javaexe.replace(" ", "\" \"")
        }
        val cmd =
            "$java -Dfile.encoding=utf-8 -jar  \"$apktool\" d  \"$apkPath\"  -o \"${smaliPath}\" -f --no-res"
        useApkToolDecompile(javaexe, apktool, apkPath, smaliPath, cmd)
    }

    fun smaliToApk(commandArgs: CommandArgs, smaliPath: String, outPath: String,signConfigBean:SignConfigBean= commandArgs.toSignConfigBean()): File {
        return smaliToApk(
            javaexe = LibConfig.getJava(commandArgs),
            apktool = LibConfig.apkToolPath(commandArgs),
            zipalign = LibConfig.zipalignPath(commandArgs),
            apksigner = LibConfig.apksignerPath(commandArgs),
            smaliPath = smaliPath,
            outPath = outPath, signConfigBean = signConfigBean
        )
    }

    private fun smaliToApk(
        javaexe: String?,
        apktool: String,
        zipalign: String,
        apksigner: String,
        signConfigBean: SignConfigBean,
        outPath: String,
        smaliPath: String
    ): File {
        val toApk = toApk(javaexe = javaexe, apktool = apktool, outPath = outPath, smaliPath = smaliPath)
        val toZipalign = toZipalign(zipalign = zipalign, outPath = outPath, toApk.absolutePath)
        return toSigner(
            javaexe = javaexe,
            apksigner = apksigner,
            signConfigBean = signConfigBean,
            outPath = outPath,
            oldApkPath = toZipalign.absolutePath
        )
    }


    private fun toSigner(
        javaexe: String?,
        apksigner: String,
        signConfigBean: SignConfigBean,
        outPath: String,
        oldApkPath: String
    ): File {
        println("开始签名")
        var file = File("$outPath/out-sign.apk")
        if (file.exists()) {
            println("删除文件${file.name}")
            var delete = file.delete()
            if (delete) {
                println("删除成功")
            } else {
                println("删除失败")
            }
        }
        if (File(oldApkPath).exists()) {
            var execAndPrint = signApk(javaexe, apksigner, oldApkPath, file, signConfigBean)
            println(execAndPrint)
        }else{
            println("文件不存在:${oldApkPath}")
        }

        return file
    }

    fun signApk(
        javaexe: String?,
        apksigner: String,
        oldApkPath: String,
        outFile: File,
        signConfigBean: SignConfigBean
    ): String? {
        var java: String = if (javaexe.isNullOrEmpty()) {
            "java"
        } else {
            javaexe.replace(" ", "\" \"")
        }

        val signVersionCmd = when (signConfigBean.signVersion) {
            "v1" -> "--v1-signing-enabled true --v2-signing-enabled false --v3-signing-enabled false"
            "v2" -> "--v1-signing-enabled true --v2-signing-enabled true --v3-signing-enabled false"
            "v3" -> "--v1-signing-enabled true --v2-signing-enabled true --v3-signing-enabled true"
            else -> ""
        }
        val keyPsdCmd = if(signConfigBean.keyPassWord.isNotBlank()){
            " --key-pass pass:\"${signConfigBean.keyPassWord}\""
        }else{""}

        val cmd =
            "$java -Dfile.encoding=utf-8 -jar  \"$apksigner\" sign  --in  \"${oldApkPath}\"  " +
                    "--out \"${outFile.absolutePath}\" $signVersionCmd --ks  " +
                    "\"${signConfigBean.signingPath}\"  --ks-pass pass:\"${signConfigBean.passWord}\" --ks-key-alias \"${signConfigBean.alias}\""+keyPsdCmd
        println(cmd)
        var execAndPrint = ExecUtil.exec(
            arrayOf(
                "cmd.exe", "/c", cmd
            ),
            60 * 60,
            GlobalConfig.isLog
        )
        return execAndPrint
    }

    private fun toZipalign(zipalign: String, outPath: String, oldApkPath: String): File {
        println("开始资源文件对齐")
        var file = File(outPath + "/temp1-align.apk")
        if (file.exists()) {
            println("删除文件${file.name}")
            var delete = file.delete()
            if (delete) {
                println("删除成功")
            } else {
                println("删除失败")
            }
        }
        if (File(oldApkPath).exists()) {
            zipAlign(zipalign, oldApkPath, file)
        }else{
            println("不存在文件:$oldApkPath")
        }
        return file
    }

    private fun zipAlign(zipalign: String, oldApkPath: String, file: File) {
        val cmd =
            "${zipalign.replace(" ", "\" \"")}  -f -v 4 \"${oldApkPath}\"  \"${file.absolutePath}\""
        println(cmd)
        var arrycmd = arrayOf("cmd.exe", "/c", cmd)
        var exec = ExecUtil.execWaitStringStop(
            arrycmd,
            30 * 60, "Verification succesful",GlobalConfig.isLog
        )
        println(exec)
    }

    private fun toApk(javaexe: String?, apktool: String, outPath: String, smaliPath: String): File {
        println("开始生成apk")
        var file = File(outPath + "/temp1.apk")
        if (file.exists()) {
            println("删除文件${file.name}")
            var delete = file.delete()
            if (delete) {
                println("删除成功")
            } else {
                println("删除失败")
            }
        }
        apkTook2Apk(javaexe, apktool, smaliPath, file)
        return file
    }

    private fun apkTook2Apk(javaexe: String?, apktool: String, smaliPath: String, outApkfile: File) {
        var java = if (javaexe.isNullOrEmpty()) {
            "java"
        } else {
            javaexe.replace(" ", "\" \"")
        }
        val cmd =
            "$java -Dfile.encoding=utf-8 -jar \"$apktool\" b \"${smaliPath}\" -o \"${outApkfile.absolutePath}\" --use-aapt2"
        println(cmd)
        var execAndPrint = ExecUtil.exec(arrayOf("cmd.exe", "/c", cmd), 60 * 60, GlobalConfig.isLog)
        println(execAndPrint)
    }
}