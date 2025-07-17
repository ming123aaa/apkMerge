package com.oh.gameSdkTool.bean;

import com.google.gson.Gson
import com.oh.gameSdkTool.CommandArgs
import com.ohuang.replacePackage.FileUtils
import java.io.File

data class SignConfigBean(
    var signingPath: String,
    var alias: String,
    var passWord: String, var signVersion: String,
    var keyPassWord: String
)

fun SignConfig.toSignConfigBean(path: String): SignConfigBean {
    return SignConfigBean(
        signingPath = "$path/$signFileName",
        alias = alias,
        passWord = passWord,
        signVersion = signVersion,
        keyPassWord = keyPassWord
    )
}


fun CommandArgs.toSignConfigBean(): SignConfigBean {
    return getSignConfigBean(signConfig)
}

fun getSignConfigBean(signConfigPath: String): SignConfigBean {
    val readText = FileUtils.readText(signConfigPath)
    val gson = Gson()
    val fromJson = runCatching { gson.fromJson(readText, SignConfig::class.java) }
        .onFailure {
            throw RuntimeException("$signConfigPath  is not SignConfig.json")
        }.getOrNull() ?: throw RuntimeException("$signConfigPath  is not SignConfig.json")
    val parentFile = File(signConfigPath).parentFile
    return fromJson.toSignConfigBean(parentFile.absolutePath)
}
