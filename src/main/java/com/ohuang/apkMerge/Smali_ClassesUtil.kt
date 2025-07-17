package com.ohuang.apkMerge

import com.ohuang.replacePackage.FileUtils
import com.ohuang.replacePackage.copyFile
import com.ohuang.replacePackage.moveFile

import java.io.File


private const val MB_DIV_B = 1024 * 1024  // 字节和MB转换
private const val temp_smali = "/temp_smali"

/**
 * 限制单个smali_Class的大小
 */
fun limitSize_smali_class_Dir(baseSmail: String, maxMB: Long) {
    if (maxMB > 0 && maxMB < 1000) {
        var findSmaliClass = findSmali_Class(baseSmail)
        if (findSmaliClass.isNotEmpty()) {
            var tempFileDir = File(baseSmail + temp_smali)
            findSmaliClass.forEach { path ->
                var file = File(path)
                moveFile(path, tempFileDir.absolutePath + "/" + file.name)
            }
            copySmali_Class(oldPath = tempFileDir.absolutePath, newPath = baseSmail, maxMB = maxMB)
            FileUtils.delete(tempFileDir)
        }

    }
}

/**
 *   '
 *   删除smali文件
 */
fun deleteSmaliPath(baseSmail: String,deletePath: List<String>){
    if (deletePath.isNotEmpty()){
        var findSmaliClass = findSmali_Class(baseSmail)
        if (findSmaliClass.isNotEmpty()) {
            println("开始删除指定smali文件")
            findSmaliClass.forEach { classDir ->
               var file= File(classDir)
               deletePath.forEach { delete ->
                   var path= file.absolutePath+if (delete.startsWith("/")){
                       delete
                   }else{
                       "/$delete"
                   }
                   var deleteFile = File(path)
                   if (deleteFile.exists()){
                       FileUtils.delete(deleteFile)
                   }
               }
            }
        }
    }
}

/**
 *
 * 删除同名的smali
 *
 */
fun deleteSameNameSmali(baseSmail: String){
    println("开始删除同名不会被加载的smali")
    var findSmaliClass = findSmali_Class(baseSmail)
    var smaliPath= HashSet<String>()
    var deletePath= HashSet<String>()
    findSmaliClass.forEach { classDir ->
        var file= File(classDir)
        var  startIndex=file.absolutePath.length
        forEachAllFile(file){
            if (it.name.endsWith(".smali")){
                var path=it.absolutePath.substring(startIndex)
                if (smaliPath.contains(path)){
                    deletePath.add(it.absolutePath)
                }else{
                    smaliPath.add(path)
                }
            }
            false
        }
    }
    deletePath.forEach { t ->
        FileUtils.delete(File(t))
    }
}

private fun copySmali_Class(oldPath: String, newPath: String, maxMB: Long) {
    var smailMaxMB = 0L
    if (maxMB <= 0) {
        smailMaxMB = 50
    } else {
        smailMaxMB = maxMB
    }
    println("限制smali_classes大小 $smailMaxMB MB")
    var findSmaliClass = findSmali_Class(oldPath)
    var class_index = 1
    var maxFileSize = smailMaxMB * MB_DIV_B
    var totalSize: Long = 0
    findSmaliClass.forEach { oldSmailDir ->
        var oldFile = File(oldSmailDir)
        println("开始复制:" + oldFile.absolutePath + " -> " + getSmailDir(newPath, num = class_index))
        forEachAllFile(oldFile) { smailFile ->
            var size = smailFile.length()
            if (totalSize + size > maxFileSize) {
                class_index++
                println("开始复制:" + oldFile.absolutePath + " -> " + getSmailDir(newPath, num = class_index))
                totalSize = 0 //重新赋值
            }
            totalSize += size
            var substring = smailFile.absolutePath.substring(oldFile.absolutePath.length)
            var rootPath = getSmailDir(newPath, num = class_index)
            copyFile(smailFile.absolutePath, rootPath + substring, isCover = true)
            false
        }
        class_index++
        totalSize = 0
    }
}

private fun getSmailDir(path: String, num: Int): String {
    if (num == 1) {
        return "$path/smali"
    } else {
        return "$path/smali_classes$num"
    }
}

private fun findSmali_Class(path: String): List<String> {
    val smailDirFilePaths = ArrayList<String>()
    var num = 1
    var cacheFile = File(getSmailDir(path, num))
    while (cacheFile.exists() && cacheFile.isDirectory) {
        smailDirFilePaths.add(cacheFile.absolutePath)
        num++
        cacheFile = File(getSmailDir(path, num))
    }
    return smailDirFilePaths
}