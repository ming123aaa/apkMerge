package com.ohuang.apkMerge

import com.oh.gameSdkTool.bean.NewName
import com.oh.gameSdkTool.bean.OldName
import com.ohuang.replacePackage.changTextLine
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption



fun changeClassPackage(rootPath: String, changeClassMap: Map<OldName, NewName>) {
    val findSmaliClasses = findSmaliClassesDir(rootPath)

    val resFile=File("$rootPath/res")
    if (changeClassMap.isNotEmpty() && findSmaliClasses.isNotEmpty()&&resFile.exists()) {
        println("开始重命名冲突Class-$rootPath")
        val pathMap = HashMap<String, String>()
        val classMap = HashMap<String, String>()
        var newClassDir="$rootPath/smali_classes${findSmaliClasses.size+1}"

        changeClassMap.forEach { (t, u) ->
            if (t.replace(".","").isNotBlank()&&u.replace(".","").isNotBlank()) {
                pathMap[t.replace(".", "/")] = u.replace(".", "/")
                classMap[t]=u
            }
        }
        findSmaliClasses.forEach {
            renamePackage(it, newClassDir,pathMap)
        }
        findSmaliClasses.forEach {
            changeSmaliFile(it, pathMap)
        }
        changeSmaliFile(newClassDir, pathMap)
        forEachAllFile(File("$rootPath/res")){
            if (it.endsWith(".xml")){
                renameForXml(it,classMap)
            }
            false
        }
        renameForXml(File("$rootPath/AndroidManifest.xml"),classMap)
    }
}

private fun renameForXml(xmlFile:File,changeClassMap:Map<OldName,NewName>){
    changTextLine(xmlFile.absolutePath){
        return@changTextLine hasChangeText(it,changeClassMap,".")
    }
}

private fun renamePackage(smaliDir: String, newDir: String, pathMap: Map<String, String>) {
    pathMap.forEach { t, u ->
        if (t.isBlank()||u.isBlank()){
            return@forEach
        }
        val oldPath = smaliDir + "/" + t
        val newPath = newDir + "/" + u
        val file = File(oldPath)
        if (file.exists()) {
            tryCatch {
                tryMoveFile(oldPath,newPath)
            }
        }
    }
}

private fun tryMoveFile(oldPath: String, newPath: String) {
    try {
        val old = Paths.get(oldPath).toAbsolutePath().normalize()
        val new = Paths.get(newPath).toAbsolutePath().normalize()

        // 检查源文件是否存在
        if (!Files.exists(old)) {
            return
        }

        // 确保目标目录存在
        Files.createDirectories(new.parent)

        // 尝试移动文件（支持跨磁盘）
        if (old.fileSystem == new.fileSystem) {
            // 同一文件系统，直接移动
            Files.move(old, new, StandardCopyOption.REPLACE_EXISTING)
        } else {
            // 跨文件系统，先复制再删除
            Files.copy(old, new, StandardCopyOption.REPLACE_EXISTING)
            Files.delete(old)
        }

    } catch (e: AccessDeniedException) {
        println("权限不足: ${e.message}")
    } catch (e: FileAlreadyExistsException) {
        println("目标文件已存在且无法替换: ${e.message}")
    } catch (e: IOException) {
        println("IO 错误: ${e.message}")
    }
}


private fun changeSmaliFile(smaliDir: String, pathMap: Map<String, String>) {
    forEachAllFile(File(smaliDir)) { it ->
        if (it.name.endsWith(".smali")) {
            changTextLine(it.absolutePath) { text ->
                return@changTextLine hasChangeText(text, pathMap,"/")
            }
        }
        return@forEachAllFile false
    }
}

private fun hasChangeText(text: String, pathMap: Map<String, String>,endWith: String): String {
    var string = text
    pathMap.forEach {
        string = string.replace(it.key+endWith, it.value+endWith)
    }
    return string

}