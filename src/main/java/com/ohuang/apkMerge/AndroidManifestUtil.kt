package com.ohuang.apkMerge

import org.dom4j.Attribute
import org.dom4j.Element
import org.dom4j.io.SAXReader
import java.io.File
import javax.xml.crypto.dsig.Manifest
import kotlin.collections.set


const val AndroidManifest="/AndroidManifest.xml"

/**
 *  path 合并到mainPath   mainPath内容在前
 *
 */
fun mergeManifest(path: String, mainPath: String, outPath: String = mainPath) {
    val saxReader = SAXReader()
    val read = saxReader.read(path)
    val rootElement = read.rootElement
    val packge = rootElement.attribute("package").data as String

    val saxReader2 = SAXReader()
    val read2 = saxReader2.read(mainPath)
    val rootElement2 = read2.rootElement
    val applictionElement2 = rootElement2.element("application")
    val packge2 = rootElement2.attribute("package").data as String

    rootElement.elements().forEach {
        if (it.name.equals("application")) {
            it.elements().forEach { appe ->
                var createCopy = appe.createCopy()
                if (createCopy.name.equals("provider")) {
                    tryCatch(false) {
                        var data = createCopy.attribute("authorities").data as String
                        var replace = data.replace(packge, packge2)
                        createCopy.setAttributeValue("authorities", replace)
                    }
                } else if (createCopy.name.equals("activity")) {
                    tryCatch(false) {
                        var data = createCopy.attribute("taskAffinity").data as String
                        var replace = data.replace(packge, packge2)
                        createCopy.setAttributeValue("taskAffinity", replace)
                    }
                }
                applictionElement2.add(createCopy)
            }
            var attributes = applictionElement2.attributes()
            it.attributes()?.forEach { att ->
                tryCatch({
                    val attribute = applictionElement2.attribute(att.name)
                    var data = attribute.data
                }) {
                    val clone = att.clone() as Attribute
                    clone.parent = null
                    attributes.add(clone)
                }
            }
            applictionElement2.setAttributes(attributes)
        } else {
            var createCopy = it.createCopy()
            if (it.name.equals("uses-permission") || it.name.equals("permission")) {
                var data = createCopy.attribute("name").data as String
                var replace = data.replace(packge, packge2)
                createCopy.setAttributeValue("name", replace)
            }
            rootElement2.add(createCopy)
        }
    }
    rootElement2.remove(applictionElement2)
    rootElement2.add(applictionElement2)
    saveXml(outPath, read2)
}

fun manifestDeleteName(manifestPath: String,deleteNames: Set<String>){
    if (!File(manifestPath).exists()||deleteNames.isEmpty()){
        return
    }
    tryCatch{
        val saxReader = SAXReader()
        val read = saxReader.read(manifestPath)
        val rootElement = read.rootElement
        deleteChildElement(rootElement,deleteNames)
        tryCatch(false){
            val applicationElement = rootElement.element("application")
            deleteChildElement(applicationElement,deleteNames)
        }
        saveXml(manifestPath, read)
    }
}


private fun deleteChildElement(rootElement: Element,deleteNames: Set<String>){
    val removeAppElement=mutableListOf<Element>()
    rootElement.elements().forEach{ child  ->
        tryCatch(false){
            val nodeTye=child.name
            val name = child.attribute("name").data as String
            if (name.isNotEmpty()) {
                if (deleteNames.contains(name)){
                    println("删除<$nodeTye  name=$name/>")
                    removeAppElement.add(child)
                }
            }
        }
    }
    removeAppElement.forEach { element ->
        rootElement.remove(element)
    }
}

/**
 * path 合并到mainPath   mainPath内容在前
 * 更安全的合并  重复的数据不会被覆盖
 */
fun mergeSafeManifest(
    path: String,
    mainPath: String,
    outPath: String = mainPath,
    isReplaceApplication: Boolean = false
) {
    if (!File(path).exists() || !File(mainPath).exists()) {
        return
    }
    tryCatch {
        val saxReader = SAXReader()
        val read = saxReader.read(path)
        val rootElement = read.rootElement
        val packge = rootElement.attribute("package").data as String
        val saxReader2 = SAXReader()
        val read2 = saxReader2.read(mainPath)
        val rootElement2 = read2.rootElement
        val applictionElement2 = rootElement2.element("application")
        var packge2 = packge
        tryCatch {
            packge2 = rootElement2.attribute("package").data as String
        }


        val existElementMap = HashMap<String, Boolean>()//收集已存在元素
        applictionElement2.elements().forEach { aap ->
            tryCatch {
                val name = aap.attribute("name").data as String
                if (name.isNotEmpty()) {
                    existElementMap[aap.name + "_<_" + name] = true
                }
            }
        }

        rootElement.elements().forEach {
            if (it.name.equals("application")) {
                copyApplicationElement(
                    it,
                    existElementMap,
                    packge,
                    packge2,
                    applictionElement2,
                    isReplaceApplication
                )
            } else {
                val createCopy = it.createCopy()
                if (it.name.equals("uses-permission") || it.name.equals("permission")) {
                    val data = createCopy.attribute("name").data as String
                    val replace = data.replace(packge, packge2)
                    createCopy.setAttributeValue("name", replace)
                }
                rootElement2.add(createCopy)
            }
        }
        rootElement2.remove(applictionElement2)
        rootElement2.add(applictionElement2)
        saveXml(outPath, read2)
    }
}

private fun copyApplicationElement(
    appliction: Element,
    existElementMap: HashMap<String, Boolean>,
    packge: String,
    packge2: String,
    mainApplictionElement: Element, isReplaceApplication: Boolean
) {
    appliction.elements().forEach aa@{ appe ->
        tryCatch {
            val name = appe.attribute("name").data as String
            if (name.isNotEmpty()) {
                if (existElementMap.containsKey(appe.name + "_<_" + name)) {
                    println("跳过 <${appe.name} name=\"${name}\"/> 处理")
                    return@aa
                }
            }
        }
        val createCopy = appe.createCopy()

        if (createCopy.name.equals("provider")) {
            tryCatch(false) {
                val data = createCopy.attribute("authorities").data as String
                val replace = data.replace(packge, packge2)
                createCopy.setAttributeValue("authorities", replace)
            }
        } else if (createCopy.name.equals("activity")) {
            tryCatch(false) {
                val data = createCopy.attribute("taskAffinity").data as String
                val replace = data.replace(packge, packge2)
                createCopy.setAttributeValue("taskAffinity", replace)
            }
        }
        mainApplictionElement.add(createCopy)
    }


    val attributes = mainApplictionElement.attributes()
    appliction.attributes()?.forEach { att ->  //把application没有的属性加上
        tryCatch({
            val attribute = mainApplictionElement.attribute(att.name)
            var data = attribute.data
        }) {
            val clone = att.clone() as Attribute
            clone.parent = null
            attributes.add(clone)
        }
    }
    mainApplictionElement.setAttributes(attributes)
    if (isReplaceApplication) {
        tryCatch {
            val attribute = appliction.attribute("name")
            val applicationName = attribute.data as String
            println("applicationName 替换成 $applicationName")
            if (applicationName.isNotEmpty()) {
                mainApplictionElement.setAttributeValue("name", applicationName)
            }
        }
    }
}

/**
 *
 * 在合并前修改两个包的启动activity
 * 在合并前的处理 mergeManifest 前对启动activity处理
 *
 * <meta-data
android:name="Launcher_Activity_Name"
android:value="" />
 */
fun preMergeManifestSetLauncherActivity(path: String, mainPath: String) {
    if (!File(path).exists() || !File(mainPath).exists()) {
        return
    }
    tryCatch {
        if (hasReplaceLauncherActivity(mainPath)) {
            println("需要修改Launcher_Activity_Name  path=$mainPath")
            val findAndDeleteLauncherActivity = findAndDeleteLauncherActivity(path)
            println("Launcher_Activity_Name=$findAndDeleteLauncherActivity  path=$path")
            if (findAndDeleteLauncherActivity.isNotEmpty()) {
                setReplaceLauncherActivity(mainPath, findAndDeleteLauncherActivity)
            }
        } else if (hasReplaceLauncherActivity(path)) {
            println("需要修改Launcher_Activity_Name path=$path")
            val findAndDeleteLauncherActivity = findAndDeleteLauncherActivity(mainPath)
            println("Launcher_Activity_Name=$findAndDeleteLauncherActivity  path=$mainPath")
            if (findAndDeleteLauncherActivity.isNotEmpty()) {
                setReplaceLauncherActivity(path, findAndDeleteLauncherActivity)
            }
        }
    }

}

/**
 * 找到启动activity
 * 并删除activity启动标记
 */
private fun findAndDeleteLauncherActivity(path: String): String {
    var activityName = ""
    val saxReader = SAXReader()
    val read = saxReader.read(path)
    val rootElement = read.rootElement
    val data = rootElement.attribute("package").data
    rootElement.elements().forEach aaa@{
        if (it.name.equals("application")) {
            it.elements().forEach {
                if (it.name.equals("activity")) {
                    tryCatch {
                        val name = it.attribute("name").data as String
                        if (hasLauncherActivity(it)) {
                            deleteLauncherActivityCategory(it)
                            activityName = name
                            return@aaa
                        }
                    }

                }
            }
        }
    }
    saveXml(path, read)
    if (activityName.startsWith(".")) {
        activityName = "${data}${activityName}"
    }
    return activityName
}

private fun deleteLauncherActivityCategory(activityElement: Element) {
    val map = HashMap<Element, Element>()
    tryCatch {
        activityElement.elements()?.forEach { node ->
            node.elements()?.forEach {
                var name = it.attribute("name").data as String
                if ("android.intent.category.LAUNCHER" == name) {
                    map[it] = node
                }
                if ("android.intent.action.MAIN" == name) {
                    map[it] = node
                }
            }
        }
    }

    map.forEach {
        it.value.remove(it.key)
    }

}

/**
 * 判断是否是启动activity
 */
private fun hasLauncherActivity(activityElement: Element): Boolean {
    var hasLauncherActivity = false
    tryCatch {
        activityElement.elements()?.forEach { node ->
            node.elements()?.forEach {
                var name = it.attribute("name").data as String
                if ("android.intent.category.LAUNCHER" == name) {
                    hasLauncherActivity = true
                }
            }
        }
    }
    return hasLauncherActivity
}


/**
 * 判断是否需要修改启动activity
 */
private fun hasReplaceLauncherActivity(path: String): Boolean {
    var hasReplaceLauncherActivity = false
    if (!File(path).exists()) {
        return false
    }
    val saxReader = SAXReader()
    val read = saxReader.read(path)
    val rootElement = read.rootElement
    rootElement.elements().forEach aaa@{
        if (it.name.equals("application")) {
            it.elements().forEach {
                if (it.name.equals("meta-data")) {
                    tryCatch {
                        val name = it.attribute("name").data as String
                        if ("Launcher_Activity_Name" == name) {
                            hasReplaceLauncherActivity = true
                            return@aaa
                        }
                    }

                }
            }
        }
    }
    return hasReplaceLauncherActivity
}

private fun setReplaceLauncherActivity(path: String, activityName: String): Boolean {
    var hasReplaceLauncherActivity = false
    val saxReader = SAXReader()
    val read = saxReader.read(path)
    val rootElement = read.rootElement
    var data = rootElement.attribute("package").data
    rootElement.elements().forEach {
        if (it.name.equals("application")) {
            it.elements().forEach {
                if (it.name.equals("meta-data")) {
                    tryCatch {
                        val name = it.attribute("name").data as String
                        if ("Launcher_Activity_Name" == name) {
                            hasReplaceLauncherActivity = true
                            it.setAttributeValue("value", activityName)
                        }
                    }

                }
            }
        }
    }
    saveXml(path, read)
    return hasReplaceLauncherActivity
}