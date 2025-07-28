package com.ohuang.apkMerge

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.oh.gameSdkTool.bean.NewName
import com.oh.gameSdkTool.bean.OldName
import com.oh.gameSdkTool.bean.ResType
import com.ohuang.replacePackage.FileUtils
import java.io.File

private const val publicXml = "/res/values/public.xml"
private const val servicesDir = "/META-INF//services"

private const val keepClassJson = "/assets/keepClassPackage.json"  //冲突不修改的class  Set<String>
private const val keepResNameJson = "/assets/keepResNamePackage.json" //冲突不修改的res  Map<resType,Set<name>>





private fun readKeepClassJson(root: String): Set<String> {
    val file = File(root + keepClassJson)
    val set = HashSet<String>()
    if (file.exists() && file.isFile) {
        tryCatch(false) {
            var readText = FileUtils.readText(file.absolutePath)
            val mySet = Gson().fromJson<Set<String>>(readText, object : TypeToken<Set<String>>() {}.type)
            set.addAll(mySet)
        }
    }
    return set
}


private fun readKeepResNameJson(root: String): Map<ResType, Set<String>> {
    val file = File(root + keepResNameJson)
    val map = HashMap<String, Set<String>>()
    if (file.exists() && file.isFile) {
        tryCatch(false) {
            var readText = FileUtils.readText(file.absolutePath)
            val myMap =
                Gson().fromJson<Map<ResType, Set<String>>>(
                    readText,
                    object : TypeToken<Map<ResType, Set<String>>>() {}.type
                )
            map.putAll(myMap)
        }
    }
    return map
}

fun mergeApkRenameKeepJson(
    channelSmali: String,
    baseSmali: String
) {
    //合并keepResNamePackage.json
    var keepResName = mutableMapOf<ResType, MutableSet<String>>()
    readKeepResNameJson(channelSmali).forEach { type, set ->
        if (!keepResName.contains(type)){
            keepResName[type]=mutableSetOf<String>()
        }
        var strings: MutableSet<String> = keepResName[type]!!
        strings.addAll(set)

    }
    readKeepResNameJson(baseSmali).forEach { type, set ->
        if (!keepResName.contains(type)){
            keepResName[type]=mutableSetOf<String>()
        }
        var strings: MutableSet<String> = keepResName[type]!!
        strings.addAll(set)

    }

    var jsonRes = Gson().toJson(keepResName)
    FileUtils.writeText(File(channelSmali + keepResNameJson), jsonRes) //两个环境都需要重新写入json 避免后续合并被覆盖
    FileUtils.writeText(File(baseSmali + keepResNameJson), jsonRes)// 两个环境都需要重新写入json 避免后续合并被覆盖

    //合并keepClassPackage.json
    var keepClass = HashSet<String>()
    keepClass.addAll(readKeepClassJson(channelSmali))
    keepClass.addAll(readKeepClassJson(baseSmali))
    var jsonClass = Gson().toJson(keepClass)
    FileUtils.writeText(File(channelSmali + keepClassJson), jsonClass) //两个环境都需要重新写入json 避免后续合并被覆盖
    FileUtils.writeText(File(baseSmali + keepClassJson), jsonClass) //两个环境都需要重新写入json 避免后续合并被覆盖


}


/**
 * 处理res冲突
 */
fun mergeApkRenameRes(
    channelSmali: String,
    baseSmali: String, startsWithName: String
) {
    val base = readPublic(baseSmali + publicXml)
    val channel = readPublic(channelSmali + publicXml)
    var resRenameMap = HashMap<ResType, HashMap<OldName, NewName>>()
    channel.forEach { type, map ->
        val typeMap = HashMap<OldName, NewName>()
        if (base.contains(type)) {
            val baseTypeMap = base[type]!!
            map.forEach { key, v ->
                if (baseTypeMap.contains(key)) {
                    typeMap[key] = startsWithName + "_" + key
                }
            }
        }
        if (typeMap.isNotEmpty()) {
            resRenameMap[type] = typeMap
        }
    }

    useKeepResName(channelSmali, resRenameMap) //res保持不变的规则
    writeLogResMapJson(channelSmali, resRenameMap)
    renameRes(rootPath = channelSmali, resRenameMap) //开始重命名冲突文件
}

private fun useKeepResName(channelSmali: String, resRenameMap: HashMap<ResType, HashMap<OldName, NewName>>) {
    var readKeepResNameJson = readKeepResNameJson(channelSmali)
    readKeepResNameJson.forEach { type, names ->
        names.forEach({
            resRenameMap[type]?.remove(it)
        })
    }
    writeLogKeepResNameJson(channelSmali, readKeepResNameJson)
}

/**
 * 处理class冲突
 */
fun mergeApkRenameClass(
    channelSmali: String,
    baseSmali: String, startsWithName: String,
    notUseDefaultKeepClassPackage: Boolean
) {

    val baseFile = File(baseSmali)
    val channelFile = File(channelSmali)
    val baseSetPackage = HashSet<String>() //baseSmali存在的 class package
    val channelSetPackage = HashSet<String>() //baseSmali存在的 class package
    val replaceMap = HashMap<String, String>() //冲突后生成的替换规则
    findSmaliClassesDir(baseFile.absolutePath).forEach { path ->
        var rootClassFile = File(path)
        var startIndex=rootClassFile.absolutePath.length
        forEachAllFile(rootClassFile) {
            if (it.name.endsWith(".smali")) {
                var packageName = it.parentFile.absolutePath.substring(startIndex).path2Package()
                baseSetPackage.add(packageName)
            }
            return@forEachAllFile false
        }
    }
    writeLogJsonFile(baseSmali,"package",Gson().toJson(baseSetPackage))
    findSmaliClassesDir(channelFile.absolutePath).forEach { path ->
        var rootClassFile = File(path)
        var startIndex=rootClassFile.absolutePath.length
        forEachAllFile(rootClassFile) {
            if (it.name.endsWith(".smali")) {
                var packageName = it.parentFile.absolutePath.substring(startIndex).path2Package()
                channelSetPackage.add(packageName)
            }
            return@forEachAllFile false
        }
    }
    writeLogJsonFile(channelSmali,"package",Gson().toJson(channelSetPackage))
    findSmaliClassesDir(channelFile.absolutePath).forEach { path ->
        var rootClassFile = File(path)
        var startIndex=rootClassFile.absolutePath.length
        forEachDir(rootClassFile) { file -> //遍历文件夹
            if (file.absolutePath == rootClassFile.absolutePath) {
                return@forEachDir false
            }
            var packageName = file.absolutePath.substring(startIndex).path2Package()

            if (packageName.isNotBlank() && baseSetPackage.contains(packageName)) { //如果package相同，添加替换规则,
                replaceMap[packageName] = getChangePackageName(packageName, startsWithName)
                return@forEachDir true  //不继续遍历子文件了
            }
            return@forEachDir false
        }
    }

    useKeepClass(channelSmali, notUseDefaultKeepClassPackage, replaceMap) //需要保持不变的packageName
    writeLogClassPackageMapJson(channelSmali, replaceMap)
    changeClassPackage(channelSmali, replaceMap) //开始重命名冲突文件
}


private fun useKeepClass(
    channelSmali: String,
    notUseDefaultKeepClassPackage: Boolean,
    replaceMap: MutableMap<String, String>
) {
    val keepClassSet = hashSetOf<String>()
    if (!notUseDefaultKeepClassPackage) { //使用默认保持不变的规则
        keepClassSet.add("kotlin")
        var servicesFile = File(channelSmali + servicesDir)
        if (servicesFile.exists() && servicesFile.isDirectory) {
            servicesFile.listFiles()?.forEach { file ->
                if (file.isFile) {
                    keepClassSet.add(file.name)
                }
            }
        }
    }
    keepClassSet.addAll(readKeepClassJson(channelSmali))//读取文件
    writeLogKeepClassJson(channelSmali, keepClassSet)
    val needRemoveKey = HashSet<String>()
    replaceMap.forEach a1@{ key, value ->
        keepClassSet.forEach b1@{ t ->
            var a = "$t."
            var b = "$key."
            if (a.contains(b) || b.contains(a)) {
                needRemoveKey.add(key)
                return@a1
            }
        }
    }
    needRemoveKey.forEach { key ->
        replaceMap.remove(key)
    }

}

private fun writeLogKeepClassJson(channelSmali: String, set: Set<String>) {
    writeLogJsonFile(channelSmali, "KeepClass", Gson().toJson(set))
}

private fun writeLogKeepResNameJson(channelSmali: String, map: Map<ResType, Set<String>>) {
    writeLogJsonFile(channelSmali, "KeepRes", Gson().toJson(map))
}

private fun writeLogResMapJson(channelSmali: String, map: Map<ResType, HashMap<OldName, NewName>>) {
    writeLogJsonFile(channelSmali, "ResMap", Gson().toJson(map))
}


private fun writeLogClassPackageMapJson(channelSmali: String, map: Map<OldName, NewName>) {
    writeLogJsonFile(channelSmali, "ClassPackageMap", Gson().toJson(map))
}


private fun writeLogJsonFile(channelSmali: String, name: String, json: String) {
    var file = File(channelSmali)
    val parentPath = file.parentFile.absolutePath

    val fileName = "${name}_" + file.name + ".json"
    FileUtils.writeText(File(parentPath, fileName), json)
}

/**
 *   // 返回ture就不继续访问更深的子文件夹
 */
private fun forEachDir(file: File, call: (File) -> Boolean) {

    if (!file.isDirectory) {
        return
    }
    if (call(file)) {// 返回ture就不继续访问更深的子文件夹
        return
    }

    file.listFiles()?.forEach { f0 ->
        forEachDir(f0, call)
    }

}

/**
 * 新类名命名规则
 */
private fun getChangePackageName(packageName: String, startsWithName: String): String {
    var split = packageName.split(".")
    val stringBuilder = StringBuilder()
    stringBuilder.append(startsWithName).append("_")
    split.forEach { string ->
        stringBuilder.append(string).append(".")
    }
    return stringBuilder.deleteAt(stringBuilder.length - 1).toString()
}

fun String.path2Package(): String {
    var a = replace("/", ".").replace("\\", ".")
    if (a.startsWith(".")) {
        a = a.substring(1)
    }
    return a
}