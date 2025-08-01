package com.ohuang.apkMerge

import com.oh.gameSdkTool.ReplaceAPk.setNameAttribute
import com.oh.gameSdkTool.bean.NewName
import com.oh.gameSdkTool.bean.OldName
import com.ohuang.replacePackage.copyFile
import org.dom4j.Document
import org.dom4j.io.OutputFormat
import org.dom4j.io.SAXReader
import org.dom4j.io.XMLWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter


/**
 * 安全的合并xml 并去除重复项 (冲突默认path2的xml优先)
 * usePath ture  若冲突优先使用path里面的xml内容
 */
fun mergeXmlSafe(channelXmlPath: String, baseXmlPath: String, outPath: String, isUseChannelFileRes: Boolean = false) {
    if (isUseChannelFileRes) {
        mergeXml(baseXmlPath, channelXmlPath, outPath)
    } else {
        mergeXml(channelXmlPath, baseXmlPath, outPath)
    }
    xmlRemoveIdenticalElement(outPath)
}

/**
 * 将xml1合并到xml2上
 * 合并后path2的内容在前
 */
fun mergeXml(newXmlPath: String, baseXmlPath: String, outPath: String) {
    if (!File(newXmlPath).exists() || !File(baseXmlPath).exists()) {
        if (File(newXmlPath).exists()) {
            copyFile(newXmlPath, outPath)
        } else if (File(baseXmlPath).exists()) {
            copyFile(baseXmlPath, outPath)
        }
        return
    }
    var saxReader = SAXReader()
    saxReader.encoding = "utf-8"
    var saxReader1 = SAXReader()
    saxReader1.encoding = "utf-8"
    var read = saxReader.readSafe(newXmlPath)
    var read1 = saxReader1.readSafe(baseXmlPath)
    var rootElement = read.rootElement
    var rootElement1 = read1.rootElement
    rootElement.elements().forEach {
        var createCopy = it.createCopy()
        rootElement1.add(createCopy)
    }


    //保存
    saveXml(outPath, read1)
}

fun SAXReader.readSafe(path: String): Document {
    FileInputStream(path).use {
        return this.read(it)
    }
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
    var read = saxReader.readSafe(path)
    val map = HashMap<String, Boolean>()
    var elements = read.rootElement.elements().toList()
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

fun readXml(path: String):Document{
    var saxReader = SAXReader()
    saxReader.encoding = "utf-8"
    return saxReader.readSafe(path)
}

/**
 *  名称修改
 */
fun xmlReplaceName(xmlPath: String, renameMap: Map<OldName, NewName>) {
    var saxReader = SAXReader()
    saxReader.encoding = "utf-8"
    var read = saxReader.readSafe(xmlPath)

    var elements = read.rootElement.elements()
    elements.forEach {
        val name = it.attributeValue("name")
        if (name != null) {
            if (renameMap.containsKey(name)) {
                it.setNameAttribute(name="name", value = renameMap[name])
            }
        }
    }
    //保存
    saveXml(xmlPath, read)
}

/**
 *
 * styles.xml 修改item name
<style name="AndroidThemeColorAccentYellow">
<item name="oldName">#ffffff00</item>
</style>
 */
fun xmlStylesReplaceItemName(xmlPath: String,attrsRenameMap: Map<OldName, NewName>){
    var saxReader = SAXReader()
    saxReader.encoding = "utf-8"
    var read = saxReader.readSafe(xmlPath)

    var elements = read.rootElement.elements()
    elements.forEach {styyleItem->
        styyleItem.elements().forEach { item->
            if (item.name == "item"){
                val name = item.attributeValue("name")
                if (name != null) {
                    if (attrsRenameMap.containsKey(name)) {
                        item.setNameAttribute(name="name", value = attrsRenameMap[name])
                    }
                }
            }

        }

    }
    //保存
    saveXml(xmlPath, read)
}

