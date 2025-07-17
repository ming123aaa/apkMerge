package com.ohuang.apkMerge

import com.oh.gameSdkTool.bean.NewName
import com.oh.gameSdkTool.bean.OldName
import com.oh.gameSdkTool.bean.ResType
import com.ohuang.replacePackage.changTextLine
import java.io.File
import java.util.regex.Pattern


fun renameRes(rootPath: String, renameResMap: Map<ResType, Map<OldName, NewName>>) {
    val findSmaliClasses = findSmaliClassesDir(rootPath)
    val resFile = File("$rootPath/res")
    if (renameResMap.isNotEmpty() && findSmaliClasses.isNotEmpty() && resFile.exists()) {
        println("开始重命名冲突Res-$rootPath")
        renameResFile(rootPath = rootPath, renameResMap = renameResMap)
        renameResValues(rootPath = rootPath, renameResMap = renameResMap)
        renamePublicXml(rootPath = rootPath, renameResMap = renameResMap)
        renameXmlRef(rootPath = rootPath, renameResMap = renameResMap)
        changeRSmaliId(rootPath = rootPath, renameResMap = renameResMap)
        renameSmali(rootPath = rootPath, renameResMap = renameResMap)
    }

}

/**
 * 修改
 */
private fun changeRSmaliId(rootPath: String, renameResMap: Map<ResType, Map<OldName, NewName>>) {
    val findSmaliClasses = findSmaliClassesDir(rootPath)
    val data= ArrayList<String>()
    findSmaliClasses.forEach { t ->
        searchFileInPath(t,"R.smali",data)
    }
    data.forEach { t ->
        val file = File(t)
        var dirPath=file.parentFile.absolutePath
        renameResMap.forEach{
            var myFile = File("$dirPath/R\$" + it.key + ".smali")
            if (myFile.exists()&&myFile.isFile){
                changeRSmaliName(myFile.absolutePath,it.value)
            }
        }

    }
}

private fun renameSmali(rootPath: String, renameResMap: Map<ResType, Map<OldName, NewName>>) {
    val smailPattern=getSmailPattern(renameResMap)

    findSmaliClassesDir(rootPath).forEach { t ->
        forEachAllFile(File(t)){
            if (it.isFile&&it.name.endsWith(".smali")){
                changTextLine(it.absolutePath) { string ->
                    return@changTextLine replaceRefSmail(string,renameResMap,smailPattern)
                }
            }
            false
        }

    }
}

fun getSmailPattern( renameResMap: Map<ResType, Map<OldName, NewName>>):Pattern{
    var types = ""
    renameResMap.forEach {
        types = types + it.key + "|"
    }
    val smailPattern = Pattern.compile("""R\$"""+"(${types});->([^\\s\"'></:]+):")
    return smailPattern
}
fun replaceRefSmail(text: String, replace: Map<ResType, Map<OldName, NewName>>, smailPattern: Pattern): String {
    val matcher = smailPattern.matcher(text)
    val sb = StringBuffer()
    while (matcher.find()) {
        val resTypeStr = matcher.group(1) // type
        val oldName = matcher.group(2) // name

        // 检查资源类型是否在 replace 映射表中
        val resType = resTypeStr
        val replacementMap = replace[resType]

        // 如果找到对应的替换规则，则替换；否则保留原内容
        val newName = replacementMap?.get(oldName) ?: oldName
        val replacement = "R\\$${resTypeStr};->$newName:"
        try {
            matcher.appendReplacement(sb, replacement)
        }catch (e: Exception){
            println("text=$text"+" resTypeStr=${resTypeStr} oldName=${oldName} replacement=${replacement}")
            throw e
        }
    }
    matcher.appendTail(sb)

    return sb.toString()
}

/**
 * 修改xml引用的资源
 */
private fun renameXmlRef(rootPath: String, renameResMap: Map<ResType, Map<OldName, NewName>>) {

    val xmlPattern=getXmlPattern(renameResMap)
    File("$rootPath/res").listFiles()?.forEach { it ->
        if (it.isDirectory) {
            it.listFiles()?.forEach { xmlFile ->
                if (xmlFile.isFile && xmlFile.name.endsWith(".xml")) {
                    replaceXmlRef(xmlFile.absolutePath, renameResMap, xmlPattern = xmlPattern)
                }
            }

        }
    }
    val file = File("$rootPath/AndroidManifest.xml")
    if (file.exists()) {
        replaceXmlRef("$rootPath/AndroidManifest.xml", renameResMap, xmlPattern = xmlPattern)
    }
}



private fun replaceXmlRef(xmlPath: String, renameResMap: Map<ResType, Map<OldName, NewName>>, xmlPattern: Pattern) {
    changTextLine(xmlPath) {
        return@changTextLine replaceRef(it, renameResMap, xmlPattern = xmlPattern)
    }
}
private fun getXmlPattern(renameResMap: Map<ResType, Map<OldName, NewName>>):Pattern{
    var types = ""
    renameResMap.forEach {
        types = types + it.key + "|"
    }
    val xmlPattern = Pattern.compile("@(${types})/([^\\s\"'></]+)")
    return xmlPattern
}


fun replaceRef(text: String, replace: Map<ResType, Map<OldName, NewName>>, xmlPattern: Pattern): String {
    val matcher = xmlPattern.matcher(text)
    val sb = StringBuffer()
    while (matcher.find()) {
        val resTypeStr = matcher.group(1) // type
        val oldName = matcher.group(2) // name

        // 检查资源类型是否在 replace 映射表中
        val resType = resTypeStr
        val replacementMap = replace[resType]

        // 如果找到对应的替换规则，则替换；否则保留原内容
        val newName = replacementMap?.get(oldName) ?: oldName
        val replacement = "@$resTypeStr/$newName"
        matcher.appendReplacement(sb, replacement)
    }
    matcher.appendTail(sb)

    return sb.toString()
}

private fun renamePublicXml(rootPath: String, renameResMap: Map<ResType, Map<OldName, NewName>>) {
    val file = File("$rootPath/res/values/public.xml")
    if (file.exists() && file.isFile) {
        publicXmlReName(file.absolutePath, renameResMap)
    }
}

private fun renameResValues(rootPath: String, renameResMap: Map<ResType, Map<OldName, NewName>>) {
    File("$rootPath/res").listFiles()?.forEach {
        if (it.isDirectory && it.name.startsWith("value")) {
            valueRename(it.absolutePath, renameResMap)
        }
    }
}

fun valueRename(valuePath: String, renameResMap: Map<ResType, Map<OldName, NewName>>) {
    File(valuePath).listFiles()?.forEach {
        if (it.isFile && it.name.endsWith(".xml") && !it.name.startsWith("public")) {
            var nameWithoutExtension = it.nameWithoutExtension
            if (nameWithoutExtension.endsWith("s")) {
                nameWithoutExtension = nameWithoutExtension.substring(0, nameWithoutExtension.length - 1)
            }
            if (renameResMap.containsKey(nameWithoutExtension)) {
                xmlReplaceName(it.absolutePath, renameResMap[nameWithoutExtension]!!)
            }
        }
    }
}

private fun renameResFile(rootPath: String, renameResMap: Map<ResType, Map<OldName, NewName>>) {
    File("$rootPath/res").listFiles()?.forEach {
        if (it.isDirectory && !it.name.startsWith("value")) {
            val typeName = it.name.split("-")[0]
            if (renameResMap.containsKey(typeName)) {
                renameFile(it.absolutePath, renameResMap[typeName]!!)
            }

        }
    }
}

private fun renameFile(rootPath: String, renameMap: Map<OldName, NewName>) {
    if (renameMap.isEmpty()) {
        return
    }
    File(rootPath).listFiles()?.forEach {
        if (it.isFile) {
            val name=it.name.substringBefore(".")
            val end=it.name.substringAfter(".","")
            if (renameMap.containsKey(name)) {
                val newFile = rootPath + "/" + renameMap[name] +"."+ end
                it.renameTo(File(newFile))
            }
        }
    }

}
