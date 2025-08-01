package com.ohuang.apkMerge

import com.oh.gameSdkTool.ReplaceAPk.setNameAttribute
import com.oh.gameSdkTool.bean.NewName
import com.oh.gameSdkTool.bean.OldName
import com.oh.gameSdkTool.bean.ResType
import com.ohuang.replacePackage.copyFile
import org.dom4j.io.SAXReader
import java.io.File
import java.util.*

/**
 *   数据结构为 map<type,<name,id>>
 *   type 为mipmap attr string 等类型
 *
 */
fun readPublic(path: String): TreeMap<String, TreeMap<String, String>> {
    var saxReader = SAXReader()
    var read = saxReader.readSafe(path)
    var rootElement = read.rootElement

    var elements = rootElement.elements().toList()
    var hashMap = TreeMap<String, TreeMap<String, String>>()
    elements.forEach {
        var type = it.attribute("type").data as String
        var name = it.attribute("name").data as String
        var id = it.attribute("id").data as String

        if (hashMap.containsKey(type)){
            var map = hashMap[type]
            if (map==null){
                var map=TreeMap<String,String>()
                map[name]=id
                hashMap[type]=map
            }else {
                map[name] = id
            }
        }else{
            var map=TreeMap<String,String>()
            map[name]=id
            hashMap[type]=map

        }
    }
    return hashMap
}

data class PublicXmlNode(val id:String,val type:String,val name:String)

/**
 * 返回数据结构  map<id,PublicXmlNode>
 *     id一般为16进制 0x7f000000
 */
fun readPublicToIdNode(path: String):Map<String,PublicXmlNode>{
    val saxReader = SAXReader()
    val read = saxReader.readSafe(path)
    val rootElement = read.rootElement

    val elements = rootElement.elements().toList()

    val hashMap = HashMap<String, PublicXmlNode>()

    elements.forEach {
        val type = it.attribute("type").data as String
        val name = it.attribute("name").data as String
        val id = it.attribute("id").data as String
        val publicXmlNode = PublicXmlNode(id = id, type = type, name = name)
        hashMap[id]=publicXmlNode
    }
    return hashMap
}

/**
 * 合并两个public.xml  path合并到path2
 */
fun mergePublicXml(newXmlPath: String, baseXmlPath: String, outPath: String = baseXmlPath) {
    if (!File(newXmlPath).exists()||!File(baseXmlPath).exists()){
        if (File(newXmlPath).exists()){
            copyFile(newXmlPath, baseXmlPath)
        }
        return
    }
    var saxReader = SAXReader()
    var read = saxReader.readSafe(newXmlPath)
    var rootElement = read.rootElement
    var elements = rootElement.elements().toList()

    var saxReader2 = SAXReader()
    var read2 = saxReader2.readSafe(baseXmlPath)
    var rootElement2 = read2.rootElement
    var elements2 = rootElement2.elements().toList()

    var map = HashMap<String, HashMap<String, Int>>()
    var maxMap = HashMap<String, Int>()

    var maxId = 0


    elements2.forEach {
        var type = it.attribute("type").data as String
        var name = it.attribute("name").data as String
        var id = it.attribute("id").data as String
        var ox16ToInt = ox16ToInt(id)
        if (ox16ToInt > maxId) {//记录最大id
            maxId = ox16ToInt
        }
        if (maxMap.containsKey(type)) {  //记录同类型的最大id
            if (ox16ToInt > maxMap[type]!!) {
                maxMap[type] = ox16ToInt
            }
        } else {
            maxMap[type] = ox16ToInt
        }
        if (map.containsKey(type)) { //根据类型保存name,id
            map[type]?.put(name, ox16ToInt)
        } else {
            var hashMap = HashMap<String, Int>()
            hashMap[name] = ox16ToInt
            map[type]=hashMap
        }

    }

    elements.forEach {
        var type = it.attribute("type").data as String
        var name = it.attribute("name").data as String
        var id = it.attribute("id").data as String
        var ox16ToInt = ox16ToInt(id)
        var newId = 0
        if (maxMap.containsKey(type)) { //获取同类型下的最大id

            var lastId = maxMap[type]
            newId = if (lastId!!.shr(16) == (lastId + 1).shr(16)) { //检查id是否超出类型的最大值
                lastId + 1
            } else {
                ((maxId.shr(16)) + 1).shl(16)//超出类型最大值时，生成新的id段
            }

        } else {
            newId = ((maxId.shr(16)) + 1).shl(16) //生成新的id段
        }
        if (map.containsKey(key = type)) {
            var hashMap = map[type]!!
            if (hashMap.containsKey(name)) { //如果name已存在
                return@forEach
            } else {
                hashMap[name] = newId
            }
        } else {
            var hashMap = HashMap<String, Int>()
            hashMap[name] = newId
        }
        maxMap[type] = newId
        if (newId > maxId) {
            maxId = newId
        }
        var createCopy = it.createCopy()
        createCopy.setNameAttribute(name="id", value = intTo0X16(newId))
        rootElement2.add(createCopy)
    }
    saveXml(outPath, read2)
}




fun publicXmlReName(publicXml:String,renameResMap: Map<ResType, Map<OldName, NewName>>){
    var saxReader = SAXReader()
    var read = saxReader.readSafe(publicXml)
    var rootElement = read.rootElement
    var elements = rootElement.elements().toList()

    elements.forEach {
        val type = it.attribute("type").data as String
        val name = it.attribute("name").data as String
        if (renameResMap.containsKey(key = type)) {
            val replaceMap=renameResMap[type]!!
            if (replaceMap.containsKey(name)){
                it.setNameAttribute(name="name", value = replaceMap[name])
            }
        }

    }
    saveXml(publicXml, read)
}
