package com.ohuang.apkMerge

import com.oh.gameSdkTool.bean.NewName
import com.oh.gameSdkTool.bean.OldName
import org.dom4j.Document
import org.dom4j.io.OutputFormat
import org.dom4j.io.SAXReader
import org.dom4j.io.XMLWriter
import java.io.File
import java.io.FileWriter


/**
 * 安全的合并xml 并去除重复项 (冲突默认path2的xml优先)
 * usePath ture  若冲突优先使用path里面的xml内容
 */
fun mergeXmlSafe(path: String, path2: String, outPath: String, usePath: Boolean = false) {
    if (usePath) {
        mergeXml(path2, path, outPath)
    } else {
        mergeXml(path, path2, outPath)
    }
    xmlRemoveIdenticalElement(outPath)
}

/**
 * 将xml1合并到xml2上
 * 合并后path2的内容在前
 */
fun mergeXml(path: String, path2: String, outPath: String) {
    if (!File(path).exists() || !File(path2).exists()) {
        if (File(path).exists()) {
            copyFile(path, outPath)
        } else if (File(path2).exists()) {
            copyFile(path2, outPath)
        }
        return
    }
    var saxReader = SAXReader()
    saxReader.encoding = "utf-8"
    var saxReader1 = SAXReader()
    saxReader1.encoding = "utf-8"
    var read = saxReader.read(path)
    var read1 = saxReader1.read(path2)
    var rootElement = read.rootElement
    var rootElement1 = read1.rootElement
    rootElement.elements().forEach {
        var createCopy = it.createCopy()
        rootElement1.add(createCopy)
    }

    //保存
    saveXml(outPath, read1)

}

fun saveXml(path2: String, read1: Document?) {
    var writer: XMLWriter? = null
    try {
        val createPrettyPrint = OutputFormat.createPrettyPrint()

        createPrettyPrint.encoding = "utf-8"
        writer = XMLWriter(FileWriter(path2), createPrettyPrint);
        writer.write(read1)

    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        writer?.close()
    }
}

/**
 * 根据key移除重复项
 */
fun xmlRemoveIdenticalElement(path: String, key: String = "name") {
    var saxReader = SAXReader()
    saxReader.encoding = "utf-8"
    var read = saxReader.read(path)
    val map = HashMap<String, Boolean>()
    var elements = read.rootElement.elements()
    elements.forEach {
        val k = it.attributeValue(key)
        if (k != null) {
            if (map.containsKey(k)) {
                read.rootElement.remove(it)
            } else {
                map[k] = true
            }
        }
    }
    //保存
    saveXml(path, read)
}

/**
 *  名称修改
 */
fun xmlReplaceName(xmlPath: String, renameMap: Map<OldName, NewName>) {
    var saxReader = SAXReader()
    saxReader.encoding = "utf-8"
    var read = saxReader.read(xmlPath)

    var elements = read.rootElement.elements()
    elements.forEach {
        val name = it.attributeValue("name")
        if (name != null) {
            if (renameMap.containsKey(name)) {
                it.setAttributeValue("name", renameMap[name])
            }
        }
    }
    //保存
    saveXml(xmlPath, read)
}

