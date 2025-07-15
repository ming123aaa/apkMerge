package com.ohuang.apkMerge

import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.lang.StringBuilder

/**
 * 复制文件夹内所有文件
 * sourceRootPath为源目录
 * targetRootPath为拷贝到的目录
 */
fun copyPathAllFile(
    sourceRootPath: String,
    targetRootPath: String,
    path: String = "",
    isCover: Boolean = false
    ,isLog : Boolean=false
) {
    var targetFile = File(targetRootPath + path)
    var sourceRootFile = File(sourceRootPath + path)
    if (sourceRootFile.isFile){
        copyFile(sourceRootPath,targetRootPath,isCover,isLog)
        return
    }
    if (!targetFile.exists()) {
        targetFile.mkdirs()
    }

    sourceRootFile.listFiles()?.forEach {
        if (it.isDirectory) {
            var newPath = path + "/${it.name}"
            copyPathAllFile(sourceRootPath, targetRootPath, newPath, isCover,isLog)
        } else {
            copyFile(it.absolutePath, targetRootPath + path + "/" + it.name, isCover,isLog)
        }
    }
}

private var index=0L
private var lastTime=0L
private var firstTime=0L
fun copyFile(sourcePath: String, targetPath: String, isCover: Boolean = false,isLog:Boolean=false) {
    if (isLog) {
        index++
        if (System.currentTimeMillis() - lastTime > 5000) {
            lastTime = System.currentTimeMillis()
            println("已复制了" + index + "文件  用时${(lastTime - firstTime) / 1000}s")
        }
    }

    var file = File(sourcePath)
    if (file.exists()) {
        var file1 = File(targetPath)
        if (!file1.exists() || isCover) {
            file.copyTo(file1,overwrite=true)
        }
    }
}


fun copayFileLog( sourceRootPath: String,
                  targetRootPath: String, isCover: Boolean = false){
    index=0
    firstTime=System.currentTimeMillis()
    lastTime=System.currentTimeMillis()
    copyPathAllFile(sourceRootPath, targetRootPath, isCover = isCover, isLog = true)
    println("复制完成  复制$index 文件  用时${(System.currentTimeMillis()- firstTime)/1000}s")

}



/**
 *  复制文件+关键字替换
 */
fun copyAndReplaceFile(sourcePath: String, targetPath: String, replaceMap: Map<String, String>) {
    var file = File(targetPath)
        file.parentFile?.mkdirs()
    var fileReader = FileReader(sourcePath)

    var sb = StringBuilder()


    var readLines = fileReader.readLines()
    readLines.forEach {
        var stringBuilder = StringBuilder(it)
        replaceMap.forEach { (t, u) ->
            var replace = stringBuilder.toString().replace(t, u)
            stringBuilder = StringBuilder(replace)
        }
        sb.append(stringBuilder).append("\n")

    }


    var fileWriter = FileWriter(targetPath)
    // 把替换完成的字符串写入文件内
    fileWriter.write(sb.toString().toCharArray());
    // 关闭文件流，释放资源
    fileReader.close()
    fileWriter.close()
}