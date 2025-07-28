package com.oh.gameSdkTool

import com.oh.gameSdkTool.bean.ApkConfigBean
import com.oh.gameSdkTool.bean.ReplaceStringData
import com.ohuang.apkMerge.*
import com.ohuang.replacePackage.*
import org.dom4j.Element
import org.dom4j.dom.DOMElement
import org.dom4j.io.SAXReader
import java.io.File
import java.io.FileWriter
import java.lang.StringBuilder
import java.util.TreeSet

object ReplaceAPk {

    fun replaceApK(rootPath: String, apkConfigBean: ApkConfigBean) {
        var startTime = System.currentTimeMillis()
        println("开始根据ApkConfig修改内容")
        renameRes(rootPath, renameResMap = apkConfigBean.renameResMap)
        changeClassPackage(rootPath, changeClassMap = apkConfigBean.changeClassPackage) //修改class的包名
        replaceApK(
            rootPath = rootPath,
            packageName = apkConfigBean.packageName,
            icon = apkConfigBean.iconImgPath,
            roundIcon = apkConfigBean.iconImgPath,
            iconSize = apkConfigBean.iconSize,
            appName = apkConfigBean.appName,
            versionCode = apkConfigBean.versionCode,
            versionName = apkConfigBean.versionName,
            armeabis = apkConfigBean.abiNames,
            minSdkVersion = apkConfigBean.minSdkVersion,
            targetSdkVersion = apkConfigBean.targetSdkVersion,
            metaDataMap = apkConfigBean.metaDataMap,
            replaceStringManifest = apkConfigBean.replaceStringManifest
        )
        deleteFileList(rootPath, apkConfigBean.deleteFileList)  //删除指定文件
        manifestDeleteName(rootPath + AndroidManifest, apkConfigBean.deleteManifestNodeNames)//删除AndroidManifest 指定节点
        deleteSmaliPath(rootPath, apkConfigBean.deleteSmaliPaths) //删除smali文件
        if (apkConfigBean.isDeleteSameNameSmali) {
            deleteSameNameSmali(rootPath)//删除相同名称smali
        }
        deleteEmpty_smali_class_Dir(rootPath)//删除空的smali_classes文件夹
        limitMaxSize_smali_class_Dir(rootPath,apkConfigBean.smaliClassSizeMB) //限制单个smali_classes文件夹大小
        limitDex_smali_class_Dir(rootPath) //限制65535
        println("ApkConfig内容修改完成:用时${(System.currentTimeMillis() - startTime) / 1000}s")

    }


    private fun deleteFileList(rootPath: String, list: List<String>) {
        val rootFile = File(rootPath)
        val rootAbsolutePath = rootFile.absolutePath
        list.forEach { path ->
            if (path.isEmpty()){
                return@forEach
            }
            var realPath = if (path.startsWith("/")) {
                path
            } else {
                "/$path"
            }
            val file = File(rootAbsolutePath+realPath)
            if (file.exists()) {
                println("删除文件-" + file.absolutePath)
                FileUtils.delete(file)

            }
        }
    }


    /**
     * 改包名 图标 应用名
     * iconSize  -xxhdpi
     */
    private fun replaceApK(
        rootPath: String,
        packageName: String,
        icon: String,
        roundIcon: String,
        iconSize: String = "-xxhdpi",
        appName: String,
        versionCode: String,
        versionName: String,
        minSdkVersion: String,
        targetSdkVersion: String,
        armeabis: List<String>,
        metaDataMap: Map<String, String>,
        replaceStringManifest: List<ReplaceStringData>
    ) {
        deleteAbi("$rootPath/lib", armeabis)
        replaceManifest(
            rootPath,
            packageName,
            versionCode,
            versionName,
            roundIcon,
            iconSize,
            icon,
            appName,
            minSdkVersion,
            targetSdkVersion,
            metaDataMap
        )
        replaceStringManifest(rootPath, replaceStringManifest)
        replaceApktoolYml(
            ymlFilePath = "$rootPath/apktool.yml",
            versionCode = versionCode,
            versionName = versionName,
            minSdkVersion = minSdkVersion,
            targetSdkVersion = targetSdkVersion
        )
    }


    /**
     *  根据字符串直接替换
     */
    private fun replaceStringManifest(rootPath: String, replaceStringManifest: List<ReplaceStringData>) {
        if (replaceStringManifest.isEmpty()) {
            return
        }
        val androidManifestPath = "$rootPath/AndroidManifest.xml"
        val file = File(androidManifestPath)
        if (!file.exists()) {
            return
        }
        val readText = FileUtils.readText(androidManifestPath)
        val newTxt = replaceString(readText, replaceStringManifest)
        FileUtils.writeText(file, newTxt)
    }

    private fun replaceString(oldString: String, replaceStringManifest: List<ReplaceStringData>): String {
        var newString = oldString
        replaceStringManifest.forEach {
            tryCatch {
                if (it.matchString.isNotEmpty()) {
                    newString = replaceString(it, newString)
                }
            }
        }
        return newString
    }

    private fun replaceString(it: ReplaceStringData, oldString: String): String = if (it.isReplaceFirst) {
        if (it.isRegex) {
            oldString.replaceFirst(it.matchString.toRegex(), it.replaceString)
        } else {
            oldString.replaceFirst(it.matchString, it.replaceString)
        }
    } else {
        if (it.isRegex) {
            oldString.replace(it.matchString.toRegex(), it.replaceString)
        } else {
            oldString.replace(it.matchString, it.replaceString)
        }
    }


    private fun replaceManifest(
        rootPath: String,
        packageName: String,
        versionCode: String,
        versionName: String,
        roundIcon: String,
        iconSize: String,
        icon: String,
        appName: String,
        minSdkVersion: String,
        targetSdkVersion: String,
        metaDataMap: Map<String, String>,
    ) {

        val path2 = "$rootPath/AndroidManifest.xml"
        if (!File(path2).exists()) {
            return
        }
        val saxReader2 = SAXReader()
        val read2 = saxReader2.read(path2)
        val rootElement2 = read2.rootElement
        val applictionElement2 = getOrAddApplictionElement(rootElement2)
        replacePackage(rootElement2, packageName, applictionElement2)
        replaceVersion(versionCode, rootElement2, versionName, minSdkVersion, targetSdkVersion)
        replaceMetaData(applictionElement2, metaDataMap)
        replaceIcon(applictionElement2, roundIcon, iconSize, rootPath, icon)
        replaceAppName(appName, applictionElement2, rootPath)
        saveXml(path2, read2)
    }

    private fun replaceMetaData(applictionElement2: Element?, metaDataMap: Map<String, String>) {
        if (metaDataMap.isEmpty()) {
            return
        }
        val keySet = TreeSet<String>()
        applictionElement2?.elements()?.forEach {
            if (it.name.equals("meta-data")) {
                val name = it.attribute("name").data as String
                if (metaDataMap.containsKey(name)) {
                    it.setAttributeValue("value", metaDataMap[name])
                    keySet.add(name)
                }
            }
        }
        metaDataMap.forEach {
            if (!keySet.contains(it.key)) {
                val domElement = DOMElement("meta-data")
                domElement.addAttribute("android:name", it.key)
                domElement.addAttribute("android:value", it.value)
                applictionElement2?.add(domElement)
            }
        }

    }

    private fun replaceVersion(
        versionCode: String,
        rootElement2: Element,
        versionName: String,
        minSdkVersion: String,
        targetSdkVersion: String
    ) {
        if (versionCode.isNotEmpty()) {
            rootElement2.addAttribute("android:versionCode", versionCode)
        }
        if (versionName.isNotEmpty()) {
            rootElement2.addAttribute("android:versionName", versionName)
        }
        if (minSdkVersion.isNotEmpty() || targetSdkVersion.isNotEmpty()) {
            var isChangeSdkVersion = false
            rootElement2.elements().forEach {
                if (it.name.equals("uses-sdk")) {
                    it.addAttribute("android:minSdkVersion", minSdkVersion)
                    it.addAttribute("android:targetSdkVersion", targetSdkVersion)
                    isChangeSdkVersion = true
                }
            }
            if (!isChangeSdkVersion) {
                val domElement = DOMElement("uses-sdk")
                domElement.addAttribute("android:minSdkVersion", minSdkVersion)
                domElement.addAttribute("android:targetSdkVersion", targetSdkVersion)
                rootElement2.add(domElement)
            }
        }
    }

    private fun replacePackage(
        rootElement2: Element,
        packageName: String,
        applictionElement2: Element
    ) {
        val packge = rootElement2.attribute("package").data as String
        if (packageName.isNotEmpty()) {  //包名替换
            rootElement2.setAttributeValue("package", packageName)
            rootElement2.elements().forEach {
                if (it.name.equals("uses-permission") || it.name.equals("permission")) {
                    val data = it.attribute("name").data as String
                    val replace = data.replace(packge, packageName)
                    it.setAttributeValue("name", replace)
                }

            }
            applictionElement2.elements().forEach { appChild ->
                if (appChild.name.equals("provider")) {
                    val data = appChild.attribute("authorities").data as String
                    val replace = data.replace(packge, packageName)
                    appChild.setAttributeValue("authorities", replace)
                }
                if (appChild.name.equals("activity")) {
                    tryCatch(false) {
                        val data = appChild.attribute("taskAffinity").data as String
                        val replace = data.replace(packge, packageName)
                        appChild.setAttributeValue("taskAffinity", replace)
                    }
                }
            }
        }
    }

    private fun replaceAppName(appName: String, applictionElement2: Element, rootPath: String) {
        tryCatch(false) { //替换label
            if (appName.isNotEmpty()) {
                var s = applictionElement2.attribute("label").data as String
                var s1 = s.replace("@", "")
                var split = s1.split("/")
                if (split.size == 2) {
                    if (split[0] == "string") {
                        changeAppName("$rootPath/res", split[1], appName)
                    }
                } else {
                    applictionElement2.setAttributeValue("label", appName)
                }
            }

        }
    }

    private fun replaceIcon(
        applictionElement2: Element,
        roundIcon: String,
        iconSize: String,
        rootPath: String,
        icon: String
    ) {
        tryCatch(false) { //替换roundIcon
            var s = applictionElement2.attribute("roundIcon").data as String
            var s1 = s.replace("@", "")
            var split = s1.split("/")
            if (split.size == 2) {
                if (roundIcon.isNotEmpty()) {
                    if (iconSize.isEmpty()) {
                        findAndReplace("$rootPath/res", split[0], split[1], roundIcon)
                    } else {
                        findAndReplace("$rootPath/res", split[0], split[1], roundIcon, false)
                        var file = File(roundIcon)
                        var split1 = file.name.split(".")
                        var path = "$rootPath/res/${split[0]}$iconSize/${split[1]}.${split1[split1.size - 1]}"
                        println("添加文件--$path")
                        file.copyTo(File(path))
                    }
                } else if (icon.isNotEmpty()) {
                    if (iconSize.isEmpty()) {
                        findAndReplace("$rootPath/res", split[0], split[1], icon)
                    } else {
                        findAndReplace("$rootPath/res", split[0], split[1], icon, false)
                        var file = File(icon)
                        var split1 = file.name.split(".")
                        var path = "$rootPath/res/${split[0]}$iconSize/${split[1]}.${split1[split1.size - 1]}"
                        println("添加文件--$path")
                        file.copyTo(File(path))
                    }
                }
            }
        }
        tryCatch(false) {  //替换icon
            var s = applictionElement2.attribute("icon").data as String
            var s1 = s.replace("@", "")
            var split = s1.split("/")
            if (split.size == 2) {
                if (icon.isNotEmpty()) {
                    if (iconSize.isEmpty()) {
                        findAndReplace("$rootPath/res", split[0], split[1], icon)
                    } else {
                        findAndReplace("$rootPath/res", split[0], split[1], icon, false)
                        var file = File(icon)
                        var split1 = file.name.split(".")
                        var path = "$rootPath/res/${split[0]}$iconSize/${split[1]}.${split1[split1.size - 1]}"
                        println("添加文件--$path")
                        file.copyTo(File(path))
                    }
                }
            }
        }
    }


    private fun deleteAbi(libPath: String, armeabis: List<String>) {
        val file = File(libPath)
        if (armeabis.isEmpty()) {
            return
        }
        file.listFiles()?.forEach {
            if (it.isDirectory) {
                if (armeabis.contains(it.name)) {
                    println("保留-${it.name}")
                } else {
                    println("删除-${it.path}")
                    FileUtils.delete(it)
                }

            }
        }


    }


    /**
     * 优化 optimizeAndroidManifest
     */
    fun optimizeAndroidManifest(rootPath: String) {
        tryCatch {
            val path2 = "$rootPath/AndroidManifest.xml"
            val saxReader2 = SAXReader()
            val read2 = saxReader2.read(path2)
            val rootElement2 = read2.rootElement
            val applictionElement2 = rootElement2.element("application")
            applictionElement2.setAttributeValue("extractNativeLibs", "true")
            saveXml(path2, read2)
        }

    }
}