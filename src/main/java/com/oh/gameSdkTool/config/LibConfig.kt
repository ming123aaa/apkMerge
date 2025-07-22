package com.oh.gameSdkTool.config

import com.oh.gameSdkTool.CommandArgs

object LibConfig {

    const val apkTool = "apktool.jar"
    const val apksigner = "apksigner.jar"
    const val zipalign = "zipalign.exe"
    const val dexToolDir = "dex-tools"
    const val buildTools = "build-tools"//android sdk build-tools
    const val baksmaliJar="baksmali.jar"
    const val smaliJar="smali.jar"


    fun smaliJar(commandArgs: CommandArgs):String{
        return commandArgs.libs+"/"+ smaliJar
    }
    fun baksmaliJar(commandArgs: CommandArgs):String{
        return commandArgs.libs+"/"+ baksmaliJar
    }

    fun buildTools(commandArgs: CommandArgs):String{
        return commandArgs.libs+"/"+ buildTools
    }
    fun dexToolDir(commandArgs: CommandArgs):String{
        return commandArgs.libs+"/"+ dexToolDir
    }

    fun apkToolPath(commandArgs: CommandArgs):String{
        return commandArgs.libs+"/"+ apkTool
    }

    fun apksignerPath(commandArgs: CommandArgs):String{
        return commandArgs.libs+"/"+ apksigner
    }

    fun zipalignPath(commandArgs: CommandArgs):String{
        return commandArgs.libs+"/"+ zipalign
    }

    fun getJava(commandArgs: CommandArgs):String{
        return commandArgs.javaPath.ifEmpty { "java" }
    }

}