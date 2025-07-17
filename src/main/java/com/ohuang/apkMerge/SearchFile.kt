package com.ohuang.apkMerge

import java.io.File

/**
 * 在path 路径下寻找名字叫 fileName的文件
 */
fun searchFileInPath(path: String, fileName: Array<String>, data: MutableList<String>) {
    var file = File(path)
    file.listFiles()?.forEach { f0 ->
        if (f0.isDirectory) {
            fileName.forEach { fn ->
                if (fn == f0.name) {
                    data.add(f0.absolutePath)
                }
            }

            searchFileInPath(f0.absolutePath, fileName, data)
        } else {
            fileName.forEach { fn ->
                if (fn == f0.name) {
                    data.add(f0.absolutePath)
                }
            }
        }
    }
}

/**
 *
 *  call:(File)->Boolean)//File 只有会回调文件，文件夹不会回调   返回ture 可停止遍历
 *  @return ture 停止遍历
 */


fun forEachAllFile(file: File, call: (File) -> Boolean): Boolean {

    if (file.isFile) {
        return call(file)
    }
    file.listFiles()?.forEach { f0 ->
        val forEachAllFile = forEachAllFile(f0, call)
        if (forEachAllFile) {
            return true
        }
    }

    return false
}


enum class ForEachDir{
    Stop,Next,StopChild
}
/**
 * 遍历文件夹
 *  call: (File) -> Boolean  //返回ture 停止遍历
 */
fun forEachAllDir(file: File, call: (File) -> ForEachDir): ForEachDir {

    if (file.isFile) {
        return ForEachDir.Next
    }
    var state = call(file)
    if (state!=ForEachDir.Next) {
        return state
    }
    file.listFiles()?.forEach { f0 ->
        val forEachAllFile = forEachAllDir(f0, call)
        if (forEachAllFile==ForEachDir.Stop) {
            return ForEachDir.Stop
        }
    }

    return ForEachDir.Next
}



fun searchFileInPath(path: String, fileName: String, data: MutableList<String>) {
    val file = File(path)
    if (file.isFile) {
        if (fileName == file.name) {
            data.add(file.absolutePath)
        }
        return
    }
    file.listFiles()?.forEach { f0 ->
        if (f0.isDirectory) {
            if (fileName == f0.name) {
                data.add(f0.absolutePath)
            }
            searchFileInPath(f0.absolutePath, fileName, data)
        } else {
            if (fileName == f0.name) {
                data.add(f0.absolutePath)
            }
        }
    }
}