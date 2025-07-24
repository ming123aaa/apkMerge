package com.ohuang.replacePackage

import com.ohuang.apkMerge.readSafe
import com.ohuang.apkMerge.saveXml
import org.dom4j.io.SAXReader
import java.io.File
import java.util.*

fun findAndDelete(path: String, dir: String, fileName: String) {

    var file = File(path)
    file.listFiles()?.forEach { file0 ->
        if (file0.isDirectory) {
            if (file0.name.contains(dir)) {
                file0.listFiles()?.forEach {
                    if (it.name.split(".")[0] == fileName) {
                        println(it.path)
                        it.delete()

                    }
                }
            }
        }
    }
}

/***
 *  path更目录
 */
fun findAndReplace(path: String, dir: String, fileName: String,replacePath:String,copy: Boolean=true){

    var file = File(path)
    var file2 =File(replacePath)
    file.listFiles()?.forEach { file0 ->
        if (file0.isDirectory) {
            if (file0.name.contains(dir)) {
                file0.listFiles()?.forEach {
                    if (it.name.split(".")[0] == fileName) {
                        var split = file2.name.split(".")
                        var tragert=it.parentFile.absolutePath+"/"+fileName+"."+split[split.size-1]
                        println("删除---"+it.absolutePath)
                        it.delete()
                        if (copy) {
                            println("添加---$tragert")
                            file2.copyTo(File(tragert), true)
                        }
                    }

                }
            }
        }
    }
}




/**
 * 寻找res文件在哪个文件夹  如要寻找@drawable/new512文件再哪个文件夹下存在
 * path res文件路径
 * dir  在带有该字符串的文件夹下寻找   如 drawable
 * filename  文件名   如 new512
 */
fun findFileInPath(path: String, dir: String, fileName: String): ArrayList<String> {
    val arrayList = ArrayList<String>()
    var file = File(path)
    file.listFiles()?.forEach { file0 ->
        if (file0.isDirectory) {
            if (file0.name.contains(dir)) {
                file0.listFiles()?.forEach {
                    if (it.name.split(".")[0] == fileName) {
                        arrayList.add(it.absolutePath)

                    }

                }
            }
        }
    }
    return arrayList
}

fun changeAppName(resPath:String,name:String,appName:String){
    var findFileInPath = findFileInPath(resPath, "values", "strings")

    findFileInPath.forEach {
        replaceAppName(it, name, appName)
    }
}

private fun replaceAppName(it: String, name: String, appName: String) {
    var saxReader = SAXReader()
    saxReader.encoding = "utf-8"
    var read = saxReader.readSafe(it)
    var rootElement = read.rootElement

    rootElement.elements().forEach { el ->
        var k = el.attributeValue("name") as String
        if (k == name) {
            println("修改$name--- $it")
            el.text = appName


            return@forEach
        }
    }
    saveXml(it, read)
}

