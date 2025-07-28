package com.ohuang.apkMerge

import com.oh.gameSdkTool.bean.NewName
import com.oh.gameSdkTool.bean.OldName
import com.ohuang.replacePackage.changTextLine
import com.ohuang.replacePackage.moveFile
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
//        var newClassDir = "$rootPath/smali_classes${findSmaliClasses.size + 1}"

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
//        changeSmaliFile(newClassDir, pathMap)
        forEachAllFile(File("$rootPath/res")) {
            if (it.name.endsWith(".xml")) {
                renameForXml(it, classMap)
            }
            false
        }
        renameForXml(File("$rootPath/AndroidManifest.xml"), classMap)
    }
}

private fun renameForXml(xmlFile: File, changeClassMap: Map<OldName, NewName>) {
    changTextLine(xmlFile.absolutePath) {
        return@changTextLine hasChangeText(it, changeClassMap, startWiths = listOf<String>("/", "<", "\""), ".")
    }
}

private fun renamePackageFile(smaliDir: String, newDir: String, pathMap: Map<String, String>) {
    pathMap.forEach { t, u ->
        if (t.isBlank() || u.isBlank()) {
            return@forEach
        }
        val oldPath = smaliDir + "/" + t
        val newPath = newDir + "/" + u
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
                    return@hasChangeText string.replace(oldString, startWith + it.value + endWith)
                }
            }
        }
    }
    return string

}