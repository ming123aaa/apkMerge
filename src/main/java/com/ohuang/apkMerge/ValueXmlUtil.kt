package com.ohuang.apkMerge

import com.oh.gameSdkTool.bean.NewName
import com.oh.gameSdkTool.bean.OldName
import com.ohuang.replacePackage.FileUtils
import org.dom4j.Attribute
import org.dom4j.Element
import org.dom4j.Namespace
import org.dom4j.QName
import java.io.File



/**
 * 合并value文件的xml
 * usePath path优先
 */
fun mergeValueXml(channelResPath: String, baseResPath: String, isUseChannelFileRes: Boolean = false) {
    var file = File(channelResPath)

    file.listFiles()?.forEach {
        if (!it.name.equals("public.xml")) {
            var baseFile = File(baseResPath + "/${it.name}")
            if (baseFile.exists()) {
                mergeXmlSafe(
                    it.absolutePath,
                    baseFile.absolutePath,
                    baseFile.absolutePath,
                    isUseChannelFileRes = isUseChannelFileRes
                )
            } else {
                it.copyTo(baseFile)
            }
        } else {
            var baseFile = File(baseResPath + "/${it.name}")
            if (baseFile.exists()) {
                mergePublicXml(it.absolutePath, baseFile.absolutePath, baseFile.absolutePath)
            } else {
                it.copyTo(baseFile)
            }

        }

    }
}

fun replaceXmlAttr(xml: String, attrsRenameMap: Map<OldName, NewName>) {
    var readXml = readXml(xml)
    replaceXmlElementAttrName(readXml.rootElement,attrsRenameMap)
    saveXml(xml, readXml)
}
private const val ApkNamespaceURI="http://schemas.android.com/apk/res-auto"
private fun replaceXmlElementAttrName(element: Element, renameResMap:  Map<OldName, NewName>) {
    element.attributes().toList().forEach { attr ->
        var namespaceURI = attr.namespaceURI
        var newName=attr.name
        if (namespaceURI==ApkNamespaceURI){
            var name = attr.name
            if (renameResMap.containsKey(name)){
                newName = renameResMap[name]!!
            }
        }
        var value = attr.value
        value = changeAttrRef(value, renameResMap)
        element.remove(attr)
        element.addAttribute(QName(newName, attr.namespace),value)
    }

    var stringValue = element.textTrim
    element.text = changeAttrRef(stringValue, renameResMap)
    element.elements().toList().forEach {
        replaceXmlElementAttrName(it, renameResMap)
    }
}

private fun changeAttrRef(string: String, renameResMap:  Map<OldName, NewName>): String{
    if (string.startsWith("?")){
        if (string.startsWith("?attr/")){
            var name = string.substring("?attr/".length)
            if (renameResMap.containsKey(name)){
                var newName = renameResMap[name]!!
                return "?attr/$newName"
            }
        }else{
            var name = string.substring(1)
            if (renameResMap.containsKey(name)){
                var newName = renameResMap[name]!!
                return "?$newName"
            }
        }
    }
    return string
}



