package com.ohuang.apkMerge

import com.oh.gameSdkTool.CommandArgs
import com.ohuang.replacePackage.copyPathAllFile
import java.io.File

fun mergeApkSmali(
    channelSmali: String,
    baseSmali: String,
    isNewId: Boolean = false,
    commandArgs: CommandArgs
) {
    var startTime = System.currentTimeMillis()
    println("开始合并")
    mergeApkPre(
        channelSmali = channelSmali,
        baseSmali = baseSmali,
        isRenameClassPackage = commandArgs.isRenameClassPackage,
        isRenameRes = commandArgs.isRenameRes,
        notUseDefaultKeepClassPackage = commandArgs.notUseDefaultKeepClassPackage
    )
    mergeApk(
        channelSmali = channelSmali,
        baseSmali = baseSmali,
        isNewId = isNewId,
        isUseChannelCodeFirst = commandArgs.isChannelCode,
        isUseChannelApktoolYml = commandArgs.isUseChannelApktoolYml,
        isReplaceApplication = commandArgs.isReplaceApplication,
        isChangeNotRSmali = commandArgs.isChangeNotRSmali,
        isUseChannelFileAssets = commandArgs.isUseChannelFileAssets || commandArgs.isChannelRes,
        isUseChannelFileLib = commandArgs.isUseChannelFileLib || commandArgs.isChannelRes,
        isUseChannelFileRes = commandArgs.isUseChannelFileRes || commandArgs.isChannelRes,
        isUseChannelFileManifest = commandArgs.isUseChannelFileManifest || commandArgs.isChannelRes,
        isUseChannelFileOther = commandArgs.isUseChannelFileOther || commandArgs.isChannelRes,
    )
    println("合并完成:用时${(System.currentTimeMillis()-startTime)/1000}s")

}
var count=0L
private fun getMergeApkPreCount(): Long{
    return  count++
}
private fun mergeApkPre(
    channelSmali: String,
    baseSmali: String,
    isRenameClassPackage: Boolean,
    isRenameRes: Boolean,
    notUseDefaultKeepClassPackage: Boolean
) {
    mergeApkPreReplace(channelSmali) //合并前替换

    var startsWithName = getMaigicNum_a_z(System.currentTimeMillis())
    if (isRenameRes) { //资源冲突重命名
        mergeApkRenameRes(channelSmali = channelSmali, baseSmali = baseSmali, startsWithName = startsWithName)
    }
    if (isRenameClassPackage) { //class冲突重命名
        mergeApkRenameClass(
            channelSmali = channelSmali,
            baseSmali = baseSmali,
            startsWithName = startsWithName,
            notUseDefaultKeepClassPackage = notUseDefaultKeepClassPackage
        )
    }
    mergeApkRenameKeepJson(
        channelSmali = channelSmali,
        baseSmali = baseSmali
    )//合并keep规则

}


/***
 * 合并反编译后的apk
 * 从path合并到path2
 */
private fun mergeApk(
    channelSmali: String,
    baseSmali: String,
    isNewId: Boolean,
    isUseChannelCodeFirst: Boolean,
    isUseChannelApktoolYml: Boolean,
    isReplaceApplication: Boolean,
    isChangeNotRSmali: Boolean,
    isUseChannelFileAssets: Boolean,
    isUseChannelFileLib: Boolean,
    isUseChannelFileRes: Boolean,
    isUseChannelFileManifest: Boolean,
    isUseChannelFileOther: Boolean,
) {


    println("---开始复制res---")
    mergeRes(channelSmali, baseSmali, isNewId, isUseChannelFileRes)
    println("---开始复制lib---")
    copyPathAllFile("$channelSmali/lib", "$baseSmali/lib", isCover = isUseChannelFileLib)
    println("---开始复制assets---")
    copyPathAllFile("$channelSmali/assets", "$baseSmali/assets", isCover = isUseChannelFileAssets)
    println("---开始复制kotlin---")
    copyPathAllFile("$channelSmali/kotlin", "$baseSmali/kotlin", isCover = isUseChannelFileOther)
    println("---开始复制unknown---")
    copyPathAllFile("$channelSmali/unknown", "$baseSmali/unknown", isCover = isUseChannelFileOther)
    println("---开始复制META-INF---")
    copyPathAllFile("$channelSmali/META-INF", "$baseSmali/META-INF", isCover = isUseChannelCodeFirst)
    println("---开始复制smali和修改R.smali----")

    val newSmali = if (isUseChannelCodeFirst) {
        copySmaliClassForFirst(channelSmali, baseSmali)
    } else {
        copySmaliClass(channelSmali, baseSmali)
    }
    updateRSmaliId(
        public = "$baseSmali/res/values/public.xml",
        oldPublic = "$channelSmali/res/values/public.xml",
        smaliList = newSmali,
        isChangeNotRSmali = isChangeNotRSmali
    )
    println("---合并AndroidManifest.xml---")

    if (isUseChannelFileManifest) {
        preMergeManifestSetLauncherActivity("$baseSmali/AndroidManifest.xml", "$channelSmali/AndroidManifest.xml")
        mergeSafeManifest(
            "$baseSmali/AndroidManifest.xml",
            "$channelSmali/AndroidManifest.xml",
            "$baseSmali/AndroidManifest.xml",
            isReplaceApplication
        )
    } else {
        preMergeManifestSetLauncherActivity("$channelSmali/AndroidManifest.xml", "$baseSmali/AndroidManifest.xml")
        mergeSafeManifest(
            "$channelSmali/AndroidManifest.xml",
            "$baseSmali/AndroidManifest.xml",
            isReplaceApplication = isReplaceApplication
        )
    }

    mergeYml(
        channelYml = "$channelSmali/apktool.yml",
        baseYml = "$baseSmali/apktool.yml",
        isUseChannelApktoolYml = isUseChannelApktoolYml
    )

}

 fun mergeRes(
    channelSmali: String,
    baseSmali: String,
    isNewId: Boolean=false,
    isUseChannelFileRes: Boolean=false
) {
     val targetPath = "$baseSmali/res"
    File("$channelSmali/res").listFiles()?.forEach {
        if (it.isDirectory) {
            if (it.name.startsWith("values")) {
                mergeValueXml(
                    it.absolutePath,
                    targetPath + "/${it.name}",
                    isNewID = isNewId,
                    usePath = isUseChannelFileRes
                )
            } else {
                copyResChildDir(it.absolutePath, targetPath + "/${it.name}", isCover = isUseChannelFileRes)
            }

        }
    }
}

/**
 * 复制res/xxx  文件夹的内容
 * 如何文件名存在(忽略后缀名) isCover是否覆盖
 */
private fun copyResChildDir(
    sourceRootPath: String,
    targetRootPath: String,
    isCover: Boolean = false
) {
    val sourceDir = File(sourceRootPath)
    val targetDir = File(targetRootPath)

    // 确保目标目录存在
    if (!targetDir.exists()) {
        targetDir.mkdirs()
    }

    // 构建目标目录中现有文件的映射表（文件名不带后缀 -> 完整路径）
    val existFileMap = HashMap<String, String>()
    targetDir.listFiles()?.forEach { file ->
        if (file.isFile) {
            val fileNameBeforeDot = getFileNameBeforeDot(file.name)
            existFileMap[fileNameBeforeDot] = file.absolutePath
        }
    }

    // 处理源目录中的文件
    sourceDir.listFiles()?.forEach { sourceFile ->
        if (sourceFile.isFile) {
            val sourceFileName = getFileNameBeforeDot(sourceFile.name)
            val targetFilePath = File(targetDir, sourceFile.name).absolutePath

            when {
                // 目标目录不存在同名文件 -> 直接复制
                !existFileMap.containsKey(sourceFileName) -> {
                    sourceFile.copyTo(File(targetFilePath), overwrite = true)
                }
                // 存在同名文件且允许覆盖 ->后缀名可能不同需要删除原文件 覆盖复制
                isCover -> {
                    val oldPath = existFileMap[sourceFileName]
                    val isDelete = File(oldPath).delete()
                    if (!isDelete) {
                        println("删除 $oldPath 失败")
                    }
                    sourceFile.copyTo(File(targetFilePath), overwrite = true)
                }
                // 存在同名文件且不允许覆盖 -> 跳过
                else -> {

                }
            }
        }
    }
}

private fun getFileNameBeforeDot(fileName: String): String {
    return fileName.substringBefore('.')
}


