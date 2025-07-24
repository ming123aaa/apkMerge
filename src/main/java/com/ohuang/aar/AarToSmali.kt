package com.ohuang.aar

import com.oh.gameSdkTool.CommandArgs
import com.oh.gameSdkTool.Jar2Dex
import com.ohuang.apkMerge.NumUtil
import com.ohuang.apkMerge.PublicXmlNode
import com.ohuang.apkMerge.copyPathAllFile
import com.ohuang.apkMerge.findSmaliClassesDirSort
import com.ohuang.apkMerge.findSmaliClassesDir
import com.ohuang.apkMerge.findSmaliCodeIdFirst
import com.ohuang.apkMerge.forEachAllFile

import com.ohuang.apkMerge.mergeXml
import com.ohuang.apkMerge.path2Package
import com.ohuang.apkMerge.readPublic
import com.ohuang.apkMerge.readPublicToIdNode
import com.ohuang.apkMerge.tryCatch
import com.ohuang.replacePackage.FileUtils
import com.ohuang.replacePackage.changTextLine
import java.io.File
import java.net.URLClassLoader
import java.util.TreeMap


fun copyToAArSmali(oldSmali: String, aarSmaliPath: String) {
    val list = setOf<String>("assets", "kotlin", "META-INF", "AndroidManifest.xml", "res", "lib", "libs", "jni", "aidl")
    File(oldSmali).listFiles()?.forEach {
        if (list.contains(it.name)) {
            copyPathAllFile(it.absolutePath, aarSmaliPath + "/" + it.name)
        }
        if (it.isDirectory && it.name == "unknown") {
            copyPathAllFile(it.absolutePath, aarSmaliPath)
        }
        if (it.isDirectory && it.name.startsWith("smali")) {
            copyPathAllFile(it.absolutePath, aarSmaliPath + "/" + it.name)
        }
    }
}

/**
 * Smali环境 转换成aarSmali环境
 */
fun setAarSmali(commandArgs: CommandArgs, baseSmali: String, packageName: String, buildRJarDir: String) {
//    changeRTxtUsePublicXml(baseSmali)

    changeRSmaliAndRTxt(packageName, baseSmali, buildRJarDir, commandArgs)
    writeAar_metadata(baseSmali)
    if (commandArgs.isChangeNotRSmali){
        val readPublicToIdNode = readPublicToIdNode(baseSmali + publicXml)
        makeIdUseRClass(baseSmali,packageName,readPublicToIdNode)
    }
    FileUtils.delete(File(baseSmali + publicXml))
    buildValueXml(baseSmali)
}

private fun makeIdUseRClass(baseSmali: String,packageName: String,map:Map<String,PublicXmlNode>) {
    var newPackageName=packageName.replace(".","/")
    var findSmaliClassesDirSort = findSmaliClassesDirSort(baseSmali)
    findSmaliClassesDirSort.forEach { dir ->

        forEachAllFile(File(dir)){
            if (it.isFile&&it.name.endsWith(".smali")){
                changTextLine(it.absolutePath) { string ->
                    changeIdSmaliLine(string,newPackageName,map)
                }
            }
            false
        }
    }
}

private fun changeIdSmaliLine(string: String,packageName:String,map:Map<String,PublicXmlNode>): String{
    val findSmaliCodeIdFirst = findSmaliCodeIdFirst(string)
    if (findSmaliCodeIdFirst != null) {
        val oldId = findSmaliCodeIdFirst
        if (map.contains(oldId)&&string.contains("const")){
            var node = map[oldId]
            if (node!=null) {
                var getRSmaliCode = "L${packageName}/R\$${node.type};->${node.name}:I"
                val newValue = string.replace("const/high16", "const").replace("const/16", "const")
                return newValue.replace("const", "sget").replace(oldId,getRSmaliCode)
            }
        }
    }
    return string
}

private const val valueXml = "/values.xml"

private fun buildValueXml(baseSmali: String) {
    File("$baseSmali/res").listFiles().forEach { file ->
        if (file.isDirectory && file.name.startsWith("values")) {
            mergeToValueXml(file)
        }
    }
}

private fun mergeToValueXml(valueDirFile: File) {
    valueDirFile.listFiles()?.forEach {
        if (it.name.endsWith(".xml") && it.name != "values.xml") {
            changeXml(it)
            mergeXml(it.absolutePath, valueDirFile.absolutePath + valueXml, valueDirFile.absolutePath + valueXml)
            FileUtils.delete(it)
        }
    }
}

private fun changeXml(file: File) {
    if (file.name == "dimens.xml") {
        changTextLine(path = file.absolutePath) { string ->
            string.replace("<item", "<dimen").replace("</item>", "</dimen>")
        }
    }
}

private fun writeAar_metadata(baseSmali: String) {
    var file = File("$baseSmali/META-INF/com/android/build/gradle/aar-metadata.properties")
    val content = """
        aarFormatVersion=1.0
        aarMetadataVersion=1.0
        minCompileSdk=1
        minCompileSdkExtension=0
        minAndroidGradlePluginVersion=1.0.0
    """.trimIndent()
    FileUtils.writeText(file, content)
}

private const val publicXml = "/res/values/public.xml"
private fun changeRTxtUsePublicXml(baseSmali: String) {
    var readPublic = readPublic(baseSmali + publicXml)
    var stringBuilder = StringBuilder()
    readPublic.forEach { type, map ->
        map.forEach { name, id ->
            var string = "int $type $name 0x0"
            stringBuilder.append(string).append("\n")
        }
    }
    FileUtils.writeText(File(baseSmali + RText), stringBuilder.toString())
    FileUtils.delete(File(baseSmali + publicXml))
}

private fun changeRSmaliAndRTxt(
    packageName: String,
    baseSmali: String,
    buildRJarDir: String, //将R.smali构建成jar
    commandArgs: CommandArgs
) {
    var packageName1 = packageName
    if (packageName1.isEmpty()) {
        throw RuntimeException("packageName is null")
    }
    var findSmaliClassesDirSort = findSmaliClassesDirSort(baseSmali)
    var TempDirFile = File("$buildRJarDir/RSmaliTemp")
    var RSmaliJar = File("$buildRJarDir/RSmaliTemp/RSmali.Jar")
    var RSmaliTempFile = File("$buildRJarDir/RSmaliTemp/smali")
    var typeMapClass = TreeMap<String, HashSet<String>>()
    var styleableClass = HashSet<String>()
    var oldPackages = HashSet<String>()
    findSmaliClassesDirSort.forEach {
        var smaliDirFile = File(it)
        var startIndex = smaliDirFile.absolutePath.length
        forEachAllFile(smaliDirFile) {
            if (it.isFile && it.name.endsWith(".smali") && (it.name.startsWith("R$") || it.name == "R.smali")) {
                var packageName = it.parentFile.absolutePath.substring(startIndex).path2Package()
                oldPackages.add(packageName)
                if (it.name == ("R\$styleable.smali")) {
                    styleableClass.add("$packageName.R\$styleable") //记录类名,用于后面反射获取值
                } else if (it.name.startsWith("R$")) {
                    val type = it.name.replace("R$", "").replace(".smali", "")
                    if (!typeMapClass.contains(type)) {
                        typeMapClass[type] = HashSet()
                    }
                    typeMapClass[type]!!.add("$packageName.R\$${type}")
                }
                var newFile = (RSmaliTempFile.absolutePath + it.absolutePath.substring(startIndex))
                copyPathAllFile(it.absolutePath, newFile)
                FileUtils.delete(it)
            }
            false
        }
    }
    RSmaliChangePackage(baseSmali, oldPackages, packageName1) //修改
    Jar2Dex.smali2jar(commandArgs, RSmaliTempFile.absolutePath, RSmaliJar.absolutePath)//构建jar

    if (RSmaliJar.isFile && RSmaliJar.exists()) {
        var urlClassLoader = URLClassLoader(arrayOf(RSmaliJar.toURI().toURL()), System::class.java.classLoader) //加载jar
        appendRTxtNormal(urlClassLoader, baseSmali, typeMapClass)  //通过classLoder获取R.class的值添加到R.txt中
        appendRTxtStyleable(urlClassLoader, baseSmali, styleableClass) //通过classLoder获取R$styleable.class的值添加到R.txt中
    }

}

private fun appendRTxtNormal(
    urlClassLoader: URLClassLoader,
    baseSmali: String,
    typeMapClass: TreeMap<String, HashSet<String>>
) {
    typeMapClass.forEach { type, classSet ->
        var intMap: HashMap<String, Int> = HashMap()
        classSet.forEach { className ->
            tryCatch {
                var loadClass = urlClassLoader.loadClass(className)
                if (loadClass != null) {
                    getRClassValue(loadClass, intMap)
                }
            }
        }
        var stringBuilder = StringBuilder()
        intMap.forEach { name, id ->
            if (id.shr(24) == 0x01) {
                stringBuilder.append("\n").append("int $type $name ${NumUtil.int2ox(id)}")
            }else {
                stringBuilder.append("\n").append("int $type $name 0x0")
            }
        }
        FileUtils.appendText(File(baseSmali + RText), stringBuilder.toString())
    }
}

class StyleableItem {
    var name: String = ""
    var idList: List<Int> = emptyList()
    var childMap: LinkedHashMap<String, Int> = LinkedHashMap()
}

private fun appendRTxtStyleable(classLoader: ClassLoader, baseSmali: String, styleableClass: Set<String>) {
    var intMap = LinkedHashMap<String, Int>()
    var intsMap = LinkedHashMap<String, List<Int>>()
    var names = LinkedHashSet<String>()

    styleableClass.forEach { className ->
        tryCatch {
            var loadClass = classLoader.loadClass(className)
            if (loadClass != null) {
                getRStyleableValue(loadClass, intMap, intsMap, names)
            }
        }
    }
    var styleableItemMap: LinkedHashMap<String, StyleableItem> = createStyleableItemMap(intsMap, intMap)

    var stringBuilder = StringBuilder()
    styleableItemMap.forEach { name, item ->
        var ints = item.idList
        var newList = ints.map {
            if (it.shr(24) == 0x01) {
                return@map NumUtil.int2ox(it)
            }
            return@map "0x0"
        }
        var string = "{ " + newList.joinToString(", ") + " }"
        stringBuilder.append("\n").append("int[] styleable $name $string")
        item.childMap.forEach { name, value ->
            stringBuilder.append("\n").append("int styleable $name $value")
        }
    }

    FileUtils.appendText(File(baseSmali + RText), stringBuilder.toString())
    buildDeclareXml(baseSmali + declareXml, styleableItemMap)
}

private fun isStyleableItemChildName(
    parent: String,
    realChildName: String,
    parentIds: Map<String, List<Int>>
): Boolean {
    var split = realChildName.split("_")
    var isChild = true
    if (split.size > 1) {
        var string = parent
        split.forEach { a ->
            string += "_$a"
            if (parentIds.contains(string)) {
                isChild = false
            }
        }
    }
    return isChild
}

private fun createStyleableItemMap(
    intsMap: LinkedHashMap<String, List<Int>>,
    intMap: LinkedHashMap<String, Int>
): LinkedHashMap<String, StyleableItem> {
    var styleableItemMap: LinkedHashMap<String, StyleableItem> = LinkedHashMap()
    intsMap.forEach { name, idList ->
        var childIdMap = TreeMap<Int, String>() //根据id排序
        var filter = intMap.filter { entry -> entry.key.startsWith("${name}_") } //获取可能是child
        var startIndex = name.length + 1
        filter.forEach { childName, id ->
            var realChild = childName.substring(startIndex)
            if (isStyleableItemChildName(name, realChild, intsMap)) {
                childIdMap[id] = childName
            }
        }
        var childMap = LinkedHashMap<String, Int>()
        childIdMap.forEach { id, childName ->
            childMap[childName] = id
        }

        var styleableItem = StyleableItem().apply {
            this.name = name
            this.idList = idList
            this.childMap = java.util.LinkedHashMap(childMap)
        }
        styleableItemMap[name] = styleableItem
    }
    return styleableItemMap
}


private fun getRClassValue(
    clazz: Class<*>,
    intMap: HashMap<String, Int>
) {
    clazz.fields.forEach { fied ->
        tryCatch {
            var name = fied.name
            if (fied.type == Int::class.java) {
                var get = fied.get(null)
                if (get != null) {
                    val value = get as Int
                    intMap[name] = value
                }

            }
        }

    }
}

private fun getRStyleableValue(
    clazz: Class<*>,
    intMap: HashMap<String, Int>,
    intsMap: HashMap<String, List<Int>>,
    names: MutableSet<String>
) {
    clazz.fields.forEach { fied ->
        tryCatch {
            var name = fied.name
            if (fied.type == Int::class.java) {
                var get = fied.get(null)
                if (get != null) {
                    val value = get as Int
                    intMap[name] = value
                    names.add(name)
                }

            } else if (fied.type == IntArray::class.java) {
                var get = fied.get(null)
                if (get != null) {
                    var intArry = get as IntArray
                    var value = intArry.toList()
                    intsMap[name] = value
                    names.add(name)
                }
            }
        }

    }
}


/**
 * 解压后的aar  转化成smali环境
 */
fun aarDirToArrSmali(commandArgs: CommandArgs, aarDir: String, aarSmaliPath: String) {
    copyPathAllFile(aarDir, aarSmaliPath)
    var deleteJar = findJar(aarSmaliPath)
    deleteJar.forEach { t ->
        FileUtils.delete(t)
    }
    var jars = findJar(aarDir)
    var num = 1
    var aarSmaliFile = File(aarSmaliPath)
    jars.forEach { t ->
        var smailDir = getSmailDir(aarSmaliFile.absolutePath, num)
        Jar2Dex.jar2smail(commandArgs, t.absolutePath, smailDir)
        num++
    }
}

/**
 *  修改引用的R文件
 *  com.xxx.R  -> newPackageName.R
 */
fun RSmaliChangePackage(aarSmaliPath: String, oldPackages: Set<String>, newPackageName: String) {
    if (oldPackages.isEmpty()) {
        return
    }
    var replaceMap: MutableMap<String, String> = HashMap()
    oldPackages.forEach { old ->
        var oldName = "L" + old.replace(".", "/") + "/R$"
        var newName = "L" + newPackageName.replace(".", "/") + "/R$"
        replaceMap[oldName] = newName
    }

    findSmaliClassesDir(aarSmaliPath).forEach {
        forEachAllFile(File(it)) {
            changTextLine(it.absolutePath) { string ->
                var newString = string
                replaceMap.forEach { old, new ->
                    newString = newString.replace(old, new)
                }
                return@changTextLine newString
            }
            false
        }
    }
}

private fun getSmailDir(path: String, num: Int): String {
    if (num == 1) {
        return "$path/smali"
    } else {
        return "$path/smali_classes$num"
    }
}

private fun findJar(path: String): List<File> {
    return File(path).listFiles().filter {
        it.isFile && it.name.endsWith(".jar")
    }.toList()
}