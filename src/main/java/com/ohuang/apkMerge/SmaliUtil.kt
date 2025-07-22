package com.ohuang.apkMerge


import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.lang.StringBuilder
import java.util.LinkedList
import java.util.TreeMap
import java.util.TreeSet


/**
 * 获取R.smali下的字段名称
 */
private fun getRSmaliFieldName(path: String): List<String> {
    val fileReader = FileReader(path)
    val linkedList = LinkedList<String>()
    val readLines = fileReader.readLines()
    readLines.forEach {
        if (it.startsWith(".field public static final")) {
            val s = StringBuilder().append(it).toString().replace(".field public static final", "")
            val split = s.split(":I =")
            if (split.size != 2) {
                return@forEach
            }
            val name = split[0].replace(" ", "")
            if (name.isNotEmpty()) {
                linkedList.add(name)
            }
        }
    }

    fileReader.close()
    return linkedList
}

/**
 *  map 数据结构<type,<name,id>>
 *   oldIdMap 数据结构<id,PublicXmlNode>
 */
private fun changeR_styleable_smali(path: String, map: Map<String, Map<String, String>>, oldIdMap: Map<String, PublicXmlNode>) {
    val fileReader = FileReader(path)
    val sb = StringBuilder()
    val readLines = fileReader.readLines()
    readLines.forEach {
        val oldId = it.trim()
        val newId = getNewId(oldIdMap, oldId, map)
        sb.append(it.replace(oldId, newId)).append("\n")
    }
    val fileWriter = FileWriter(path)
    // 把替换完成的字符串写入文件内
    fileWriter.write(sb.toString().toCharArray());
    // 关闭文件流，释放资源
    fileReader.close()
    fileWriter.close()
}


/**
 *
 *  map 数据结构<type,<name,id>>
 *   oldIdMap 数据结构<id,PublicXmlNode>
 */
fun changeNotRSmali(path: String, map: Map<String, Map<String, String>>, oldIdMap: Map<String, PublicXmlNode>) {
    val fileReader = FileReader(path)
    val sb = StringBuilder()
    val readLines = fileReader.readLines()

    readLines.forEach {
        val findSmaliCodeIdFirst = findSmaliCodeIdFirst(it)
        if (findSmaliCodeIdFirst != null) {
            val oldId = findSmaliCodeIdFirst
            val newId = getNewId(oldIdMap, oldId, map)
            val newValue = it.replace("const/high16", "const").replace("const/16", "const")
            sb.append(newValue.replace(oldId, newId)).append("\n")
        } else {
            sb.append(it).append("\n")
        }
    }
    val fileWriter = FileWriter(path)
    // 把替换完成的字符串写入文件内
    fileWriter.write(sb.toString().toCharArray());
    // 关闭文件流，释放资源
    fileReader.close()
    fileWriter.close()
}

private val idRegexPattern = Regex("0x7f[0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f]")
fun findSmaliCodeIdFirst(code: String): String? {
    val find = idRegexPattern.find(code)
    return find?.value
}


private fun getNewId(
    oldIdMap: Map<String, PublicXmlNode>,
    oldId: String,
    map: Map<String, Map<String, String>>
): String {
    if (oldIdMap.containsKey(oldId)) {
        val publicXmlNode = oldIdMap[oldId]
        publicXmlNode?.let {
            if (map.containsKey(publicXmlNode.type)) {
                val typeMap = map[publicXmlNode.type]
                if (typeMap != null && typeMap.containsKey(publicXmlNode.name)) {
                    val id = typeMap[publicXmlNode.name]
                    if (id != null) {
                        return id
                    }
                }
            }
        }

    }
    return oldId
}


/**
 * 改变R.smali的值，修改id
 */
fun changeRSmaliId(path: String, map: Map<String, String>) {

    var fileReader = FileReader(path)

    var sb = StringBuilder()


    var readLines = fileReader.readLines()
    readLines.forEach {
        if (it.startsWith(".field public static final")) {
            var s = StringBuilder().append(it).toString().replace(".field public static final", "")
            var split = s.split(":I =")
            if (split.size != 2) {
                sb.append(it)
                sb.append("\n")
                return@forEach
            }
            var name = split[0].replace(" ", "")
            val id=split[1].replace(" ", "")
            replaceSmaliId(map, name, it, id, sb)

        } else if (it.startsWith(".field public static ")) {
            val s = StringBuilder().append(it).toString().replace(".field public static", "")
            val split = s.split(":I =")
            if (split.size != 2) {
                sb.append(it)
                sb.append("\n")
                return@forEach
            }
            val name = split[0].replace(" ", "")
            val id=split[1].replace(" ", "")
            replaceSmaliId(map, name, it, id, sb)
        } else {
            sb.append(it)
        }
        sb.append("\n")
    }
    val fileWriter = FileWriter(path)
    // 把替换完成的字符串写入文件内
    fileWriter.write(sb.toString().toCharArray());
    // 关闭文件流，释放资源
    fileReader.close()
    fileWriter.close()

}

/**
 * 修改R.smali内的参数名称
 */
fun changeRSmaliName(path: String, map: Map<String, String>) {

    var fileReader = FileReader(path)

    var sb = StringBuilder()

    var readLines = fileReader.readLines()
    readLines.forEach {
        if (it.startsWith(".field public static final")) {
            var s = StringBuilder().append(it).toString().replace(".field public static final", "")
            var split = s.split(":I =")
            if (split.size != 2) {
                sb.append(it)
                sb.append("\n")
                return@forEach
            }
            var name = split[0].replace(" ", "")
            replaceSmaliName(map, name, it, sb)

        } else if (it.startsWith(".field public static ")) {
            val s = StringBuilder().append(it).toString().replace(".field public static", "")
            val split = s.split(":I =")
            if (split.size != 2) {
                sb.append(it)
                sb.append("\n")
                return@forEach
            }
            val name = split[0].replace(" ", "")
            replaceSmaliName(map, name, it, sb)
        } else {
            sb.append(it)
        }
        sb.append("\n")
    }
    val fileWriter = FileWriter(path)
    // 把替换完成的字符串写入文件内
    fileWriter.write(sb.toString().toCharArray());
    // 关闭文件流，释放资源
    fileReader.close()
    fileWriter.close()

}

private fun replaceSmaliId(
    map: Map<String, String>,
    name: String,
    text: String,
    oldId: String,
    sb: StringBuilder
) {
    if (map.containsKey(name)) {
        var id = map[name]
        if (id != null && id.isNotEmpty()) {
            var sp = StringBuilder().append(text).toString().replace(oldId.replace(" ", ""), id)
            sb.append(sp)
        } else {
            sb.append(text)
        }
    } else {
        sb.append(text)
    }
}

private fun replaceSmaliName(
    map: Map<String, String>,
    name: String,
    it: String,
    sb: StringBuilder
) {
    if (map.containsKey(name)) {
        var newName = map[name]
        if (newName != null && newName.isNotEmpty()) {
            var sp = StringBuilder().append(it).toString().replace(name, newName)
            sb.append(sp)
        } else {
            sb.append(it)
        }
    } else {
        sb.append(it)
    }
}


fun updateRSmaliId(public: String, oldPublic: String = "", smaliList: List<String>, isChangeNotRSmali: Boolean) {
    if (!File(public).exists()) {
        println("更新R.Smali取消  $public 不存在")
        return
    }
    val readPublic = readPublic(public)
    val oldPublic: Map<String, PublicXmlNode>? = if (oldPublic.isNotEmpty()) {
        if (File(oldPublic).exists()) {
            readPublicToIdNode(oldPublic)
        } else {
            null
        }
    } else {
        null
    }

    val data = ArrayList<String>()

    smaliList.forEach {
        searchFileInPath(it, "R.smali", data)
    }

    data.forEach { itS ->
//        println("----开始修改" + itS + "-----")
        val file1 = File(itS)
        val parentFile = file1.parentFile
        parentFile.listFiles()?.forEach tt@{ itF ->
            if (itF.isFile) {
                if (itF.name.startsWith("R\$")) {
                    val name = StringBuilder().append(itF.name).toString()
                    val type = name.replace("R\$", "").replace(".smali", "")
                    if (type == "styleable") {
                        oldPublic?.let {
                            changeR_styleable_smali(path = itF.absolutePath, map = readPublic, oldIdMap = it)
                        }
                        return@tt
                    }
                    val hashMap = readPublic[type]
                    if (hashMap != null) {
                        changeRSmaliId(itF.absolutePath, hashMap)
                    }
                }
            }
        }
    }
    if (isChangeNotRSmali) {
        oldPublic?.let {
            updateNotRSmali(smaliList = smaliList, map = readPublic, oldIdMap = oldPublic)
        }
    }
}

private fun updateNotRSmali(
    smaliList: List<String>,
    map: Map<String, Map<String, String>>,
    oldIdMap: Map<String, PublicXmlNode>
) {
    smaliList.forEach {
        forEachAllFile(File(it)) { smaliFile ->
            if (!smaliFile.name.endsWith(".smali") || smaliFile.name.startsWith("R.") || smaliFile.name.startsWith("R$")) {
                return@forEachAllFile false
            }
            changeNotRSmali(smaliFile.absolutePath, map = map, oldIdMap = oldIdMap)
            return@forEachAllFile false
        }
    }
}


/**
 * 修改public.xml 后更新R.smali
 */
fun updateRSmali2(public: String, oldPublic: String = "", path: String) {
    var file = File(path)
    val data = ArrayList<String>()

    if (file.isDirectory) {
        searchFileInPath(file.absolutePath, "R.smali", data)
    } else if ("R.smali" == file.name) {
        data.add(file.absolutePath)
    }
    updateRSmaliId(public = public, oldPublic = oldPublic, smaliList = data, isChangeNotRSmali = false)
}

fun findSmaliClassesDir(path: String): MutableList<String> {
    val file1 = File(path)
    val mutableList = arrayListOf<String>()
    file1.listFiles()?.forEach {
        if (it.isDirectory) {
            if (it.name.startsWith("smali_classes") || it.name == "smali") {
                mutableList.add(it.absolutePath)
            }
        }
    }
    return mutableList
}

/**
 * 复制smali
 * 从path 复制smali 到outPath
 * 复制到末尾
 * 返回新复制的smali list
 */
fun copySmaliClass(path: String, outPath: String): List<String> {
    val oldList = findSmaliClassDirSort(path)
    val outList = findSmaliClassDirSort(outPath)
    var number=1
    if(outList.isEmpty()){
        number=1
    }else{
        number=outList.size+1
    }
    val data = arrayListOf<String>()
    oldList.forEach{
        val file = File(it)
        var smailDir = getSmailDir(outPath, number)
        copyPathAllFile(file.absolutePath, smailDir)
        data.add(smailDir)
    }
    return data
}



/**
 * 复制smali
 * 从path 复制smali 到outPath
 * 复制到头部
 *
 *  返回复制之后的路径
 */
fun copySmaliClassForFirst(path: String, outPath: String): List<String> {
    val newSmali = findSmaliClassesDir(path)
    val outExitsSmali = findSmaliClassesDir(outPath)
    val newSize = newSmali.size
    var outExitsSize = outExitsSmali.size
    //先将 outFile的smali从命名  必须从高的开始重名,不然可能出现文件冲突
    if (newSize == 0) {
        return listOf()
    }
    while (outExitsSize > 0) {
        if (outExitsSize == 1) {
            File("$outPath/smali").renameTo(File("$outPath/smali_classes${newSize + outExitsSize}"))
        } else {
            File("$outPath/smali_classes${outExitsSize}").renameTo(File("$outPath/smali_classes${newSize + outExitsSize}"))
        }
        outExitsSize--
    }
    val outFileList = ArrayList<String>()
    newSmali.forEach {
        val file = File(it)
        val name = file.name
        copyPathAllFile(file.absolutePath, "$outPath/$name", isCover = true)
        outFileList.add("$outPath/$name")
    }
    return outFileList
}






/**
 * 根据R.smali生成R.txt
 */
fun generateRTxt(path: String, RTxtPath: String) {
    val hashMap = findInSmaliResNameMap(path)
    val file1 = File(RTxtPath)
    if (file1.parentFile != null) {
        file1.parentFile.mkdirs()
    }
    val fileWriter = FileWriter(file1)
    hashMap.forEach {
        val key = it.key
        it.value.forEach { name ->
            fileWriter.write("int ${key} ${name} 0x0\n")
        }
    }
    fileWriter.close()
}

/**
 *
 *
 *   map<type,names>
 *
 *
 */
private fun findInSmaliResNameMap(path: String): TreeMap<String, MutableSet<String>> {
    val file = File(path)
    val data = ArrayList<String>()
    if (file.isDirectory) {
        searchFileInPath(file.absolutePath, "R.smali", data)
    } else if ("R.smali" == file.name) {
        data.add(file.absolutePath)
    }
    val hashMap = TreeMap<String, MutableSet<String>>()

    data.forEach { itS ->
        println("----开始查找$itS-----")
        val file1 = File(itS)
        val parentFile = file1.parentFile
        parentFile.listFiles()?.forEach { itF ->
            if (itF.isFile) {
                if (itF.name.startsWith("R\$")) {
                    val type = itF.name.replace(".smali", "").replace("R\$", "")

                    var set: MutableSet<String> = TreeSet()
                    if (hashMap.containsKey(type)) {
                        set = hashMap[type]!!
                    } else {
                        hashMap[type] = set
                    }
                    val rSmaliFieldName = getRSmaliFieldName(itF.absolutePath)
                    set.addAll(rSmaliFieldName)
                }
            }
        }
    }
    return hashMap
}


/**
 * 删除R.smali
 */
fun deleteRSmali(path: String) {

    var file = File(path)
    val data = ArrayList<String>()

    if (file.isDirectory) {
        searchFileInPath(file.absolutePath, "R.smali", data)
    } else if ("R.smali" == file.name) {
        data.add(file.absolutePath)
    }

    data.forEach { itS ->
        println("----开始删除$itS-----")
        var file1 = File(itS)
        var parentFile = file1.parentFile
        parentFile.listFiles()?.forEach { itF ->
            if (itF.isFile) {
                if (itF.name.startsWith("R\$")) {
                    var delete = itF.delete()
                    if (delete) {
                        println("删除文件" + itF.absolutePath)
                    } else {
                        println("删除文件失败" + itF.absolutePath)
                    }
                } else if (itF.name.equals("R.smali")) {
                    var delete = itF.delete()
                    if (delete) {
                        println("删除文件" + itF.absolutePath)
                    } else {
                        println("删除文件失败" + itF.absolutePath)
                    }
                }
            }
        }
    }
}
