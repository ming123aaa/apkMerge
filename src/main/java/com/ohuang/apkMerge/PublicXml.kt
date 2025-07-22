package com.ohuang.apkMerge

import com.oh.gameSdkTool.bean.NewName
import com.oh.gameSdkTool.bean.OldName
import com.oh.gameSdkTool.bean.ResType
import org.dom4j.io.SAXReader
import java.io.File

/**
 *   数据结构为 map<type,<name,id>>
 *   type 为mipmap attr string 等类型
 *
 */
fun readPublic(path: String): HashMap<String, HashMap<String, String>> {
    var saxReader = SAXReader()
    var read = saxReader.read(path)
    var rootElement = read.rootElement

    var elements = rootElement.elements()
    var hashMap = HashMap<String, HashMap<String, String>>()
    elements.forEach {
        var type = it.attribute("type").data as String
        var name = it.attribute("name").data as String
        var id = it.attribute("id").data as String

        if (hashMap.containsKey(type)){
            var map = hashMap[type]
            if (map==null){
                var map=HashMap<String,String>()
                map[name]=id
                hashMap[type]=map
            }else {
                map[name] = id
            }
        }else{
            var map=HashMap<String,String>()
            map[name]=id
            hashMap[type]=map

        }
    }
    return hashMap
}

data class PublicXmlNode(val id:String,val type:String,val name:String)

/**
 * 返回数据结构  map<id,PublicXmlNode>
 */
fun readPublicToIdNode(path: String):Map<String,PublicXmlNode>{
    val saxReader = SAXReader()
    val read = saxReader.read(path)
    val rootElement = read.rootElement

    val elements = rootElement.elements()

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
fun mergePublicXml(path: String, path2: String) {
    if (!File(path).exists()||!File(path2).exists()){
        if (File(path).exists()){
            copyFile(path, path2)
        }
        return
    }
    var saxReader = SAXReader()
    var read = saxReader.read(path)
    var rootElement = read.rootElement
    var elements = rootElement.elements()

    var saxReader2 = SAXReader()
    var read2 = saxReader2.read(path2)
    var rootElement2 = read2.rootElement
    var elements2 = rootElement2.elements()

    var map = HashMap<String, HashMap<String, Int>>()
    var maxMap = HashMap<String, Int>()

    var maxId = 0


    elements2.forEach {
        var type = it.attribute("type").data as String
        var name = it.attribute("name").data as String
        var id = it.attribute("id").data as String
        var ox16ToInt = ox16ToInt(id)
        if (ox16ToInt > maxId) {
            maxId = ox16ToInt
        }
        if (maxMap.containsKey(type)) {
            if (ox16ToInt > maxMap[type]!!) {
                maxMap[type] = ox16ToInt
            }
        } else {
            maxMap[type] = ox16ToInt
        }
        if (map.containsKey(type)) {
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
        if (maxMap.containsKey(type)) {

            var lastId = maxMap[type]
            newId = if (lastId!!.shr(16) == (lastId + 1).shr(16)) {
                lastId + 1
            } else {
                ((maxId.shr(16)) + 1).shl(16)
            }


        } else {
            newId = ((maxId.shr(16)) + 1).shl(16)
        }
        if (map.containsKey(key = type)) {
            var hashMap = map[type]!!
            if (hashMap.containsKey(name)) {
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
        createCopy.setAttributeValue("id", intTo0X16(newId))
        rootElement2.add(createCopy)
    }
    saveXml(path2, read2)
}


fun mergePublicXmlWewId(path: String, path2: String) {
    val saxReader = SAXReader()
    val read = saxReader.read(path)
    val rootElement = read.rootElement
    val elements = rootElement.elements()

    val saxReader2 = SAXReader()
    val read2 = saxReader2.read(path2)
    val rootElement2 = read2.rootElement
    val elements2 = rootElement2.elements()

    val map = HashMap<String, HashMap<String, Int>>()


    var maxId = 0x7f800000


    elements2.forEach {
        val type = it.attribute("type").data as String
        val name = it.attribute("name").data as String
        val id = it.attribute("id").data as String
        val ox16ToInt = ox16ToInt(id)
        if (ox16ToInt > maxId) {
            maxId = ox16ToInt
        }

        if (map.containsKey(type)) {
            map[type]?.put(name, ox16ToInt)
        } else {
            val hashMap = HashMap<String, Int>()
            hashMap[name] = ox16ToInt
            map[type]=hashMap
        }

    }

    elements.forEach {
        val type = it.attribute("type").data as String
        val name = it.attribute("name").data as String
        val id = it.attribute("id").data as String
        var ox16ToInt = ox16ToInt(id)


        if (map.containsKey(key = type)) {
            val hashMap = map[type]!!
            if (hashMap.containsKey(name)) {
                return@forEach
            } else {
                hashMap[name] = 0
            }
        }
        val createCopy = it.createCopy()
        rootElement2.add(createCopy)
    }
    saveXml(path2, read2)
}

fun publicXmlReName(publicXml:String,renameResMap: Map<ResType, Map<OldName, NewName>>){
    var saxReader = SAXReader()
    var read = saxReader.read(publicXml)
    var rootElement = read.rootElement
    var elements = rootElement.elements()

    elements.forEach {
        val type = it.attribute("type").data as String
        val name = it.attribute("name").data as String
        if (renameResMap.containsKey(key = type)) {
            val replaceMap=renameResMap[type]!!
            if (replaceMap.containsKey(name)){
                it.setAttributeValue("name",replaceMap[name])
            }
        }

    }
    saveXml(publicXml, read)
}
