package com.ohuang.apkMerge

import com.oh.gameSdkTool.bean.NewName
import com.oh.gameSdkTool.bean.OldName
import com.oh.gameSdkTool.config.GlobalConfig
import com.ohuang.replacePackage.changTextLine
import com.ohuang.replacePackage.moveFile
import org.dom4j.Element
import org.dom4j.QName
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption


fun changeClassPackage(rootPath: String, changeClassMap: Map<OldName, NewName>) {
    val findSmaliClasses = findSmaliClassesDir(rootPath)

    val resFile = File("$rootPath/res")
    if (changeClassMap.isNotEmpty() && findSmaliClasses.isNotEmpty() && resFile.exists()) {
        println("开始重命名冲突Class-$rootPath")
        val pathMap = HashMap<String, String>()
        val classMap = HashMap<String, String>()


        changeClassMap.forEach { (t, u) ->
            if (t.replace(".", "").isNotBlank() && u.replace(".", "").isNotBlank()) {
                pathMap[t.replace(".", "/")] = u.replace(".", "/")
                classMap[t] = u
            }
        }
        findSmaliClasses.forEach {
            renamePackageFile(it, it, pathMap)
        }
        findSmaliClasses.forEach {
            changeSmaliFile(it, pathMap)
        }

        forEachAllFile(File("$rootPath/res")) {
            if (it.name.endsWith(".xml")) {
                renameForXml(it, classMap)
            }
            false
        }
        renameForXml(File("$rootPath/AndroidManifest.xml"), classMap)
    }
}

@Deprecated("请使用renameForXml()")
private fun renameForXmlReplaceString(xmlFile: File, changeClassMap: Map<OldName, NewName>) {
    changTextLine(xmlFile.absolutePath) {
        return@changTextLine hasChangeText(it, changeClassMap, startWiths = listOf<String>("/", "<", "\"", ">"), ".")
    }
}

private fun renameForXml(xmlFile: File, changeClassMap: Map<OldName, NewName>) {
    tryCatch(GlobalConfig.isLog) {
        var readXml = readXml(xmlFile.absolutePath)
        var rootElement = readXml.rootElement
        replaceXmlClass(rootElement, changeClassMap)
        saveXml(xmlFile.absolutePath, readXml)
    }
}

private fun shouldChangeText(text: String, changeClassMap: Map<OldName, NewName>): String {
    if (text.isBlank()) {
        return ""
    }
    changeClassMap.forEach {
        if (text.startsWith(it.key)) {
            var value = it.value
            return value + text.substring(it.key.length)
        }
    }
    return ""
}


private fun replaceXmlClass(element: Element, changeClassMap: Map<OldName, NewName>) {
    var name = shouldChangeText(element.name, changeClassMap)

    if (name.isNotEmpty()) {
        element.name = name
    }
    var stringValue = shouldChangeText(element.textTrim, changeClassMap)
    if (stringValue.isNotEmpty()) {
        element.text = stringValue
    }
    element.attributes().toList().forEach { attr ->
        var value = shouldChangeText(attr.value,changeClassMap)
        if (value.isNotEmpty()) {
            element.remove(attr)
            element.addAttribute(attr.qName,value)
        }
    }
    element.elements().forEach {
        replaceXmlClass(it, changeClassMap)
    }

}

private fun renamePackageFile(smaliDir: String, newDir: String, pathMap: Map<String, String>) {
    pathMap.forEach { oldClassPath, newClassPath ->
        if (oldClassPath.isBlank() || newClassPath.isBlank()) {
            return@forEach
        }
        val oldPath = "$smaliDir/$oldClassPath"
        val newPath = "$newDir/$newClassPath"
        val file = File(oldPath)
        if (file.exists()) {

            moveFile(oldPath, newPath)

        }
    }
}


private fun changeSmaliFile(smaliDir: String, pathMap: Map<String, String>) {
    var start = listOf<String>("L")
    forEachAllFile(File(smaliDir)) { it ->
        if (it.name.endsWith(".smali")) {
            changTextLine(it.absolutePath) { text ->
                return@changTextLine hasChangeText(text, pathMap, start, "/")
            }
        }
        return@forEachAllFile false
    }
}

private fun hasChangeText(
    text: String,
    pathMap: Map<String, String>,
    startWiths: List<String>,
    endWith: String
): String {
    var string = text

    pathMap.forEach {
        if (string.contains(it.key)) {
            startWiths.forEach { startWith ->
                val oldString = startWith + it.key + endWith
                if (string.contains(oldString)) {
                    string = string.replace(oldString, startWith + it.value + endWith)
                }
            }
        }
    }
    return string

}