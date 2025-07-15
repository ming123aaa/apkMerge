package com.oh.gameSdkTool.config

import com.oh.gameSdkTool.CommandArgs

object LibConfig {

    const val apkTool = "apktool.jar"
    const val apksigner = "apksigner.jar"
    const val zipalign = "zipalign.exe"



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