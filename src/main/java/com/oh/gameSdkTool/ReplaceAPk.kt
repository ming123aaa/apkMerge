package com.oh.gameSdkTool

import com.oh.gameSdkTool.bean.ApkConfigBean
import com.oh.gameSdkTool.bean.CompileSdkInfo
import com.oh.gameSdkTool.bean.ReplaceStringData
import com.ohuang.apkMerge.*
import com.ohuang.replacePackage.*
import org.dom4j.Element
import org.dom4j.dom.DOMElement
import org.dom4j.io.SAXReader
import java.io.File
import java.util.TreeSet

object ReplaceAPk {

    fun replaceApK(rootPath: String, apkConfigBean: ApkConfigBean) {
        var startTime = System.currentTimeMillis()
        println("开始根据ApkConfig修改内容")
        renameRes(rootPath, renameResMap = apkConfigBean.renameResMap)
        changeClassPackage(rootPath, changeClassMap = apkConfigBean.changeClassPackage) //修改class的包名
        replaceManifestAndYml(
            rootPath = rootPath,
            apkConfigBean = apkConfigBean
        )
        replaceDeleteFile(rootPath,apkConfigBean) //删除指定文件
        limitMaxSize_smali_class_Dir(rootPath, apkConfigBean.smaliClassSizeMB) //限制单个smali_classes文件夹大小
        if (apkConfigBean.isOptimizeSmaliClass) {
            limitDex_smali_class_Dir(rootPath) //限制65535
        }
        println("ApkConfig内容修改完成:用时${(System.currentTimeMillis() - startTime) / 1000}s")

    }

    private fun replaceDeleteFile(rootPath: String, apkConfigBean: ApkConfigBean){
        deleteFileList(rootPath, apkConfigBean.deleteFileList)  //删除指定文件
        deleteAbi("$rootPath/lib", apkConfigBean.abiNames) //删除指定abi的so文件
        deleteSmaliPath(rootPath, apkConfigBean.deleteSmaliPaths) //删除smali文件
        if (apkConfigBean.isDeleteSameNameSmali) {
            deleteSameNameSmali(rootPath)//删除相同名称smali
        }
        deleteEmpty_smali_class_Dir(rootPath)//删除空的smali_classes文件夹
    }


    private fun deleteFileList(rootPath: String, list: List<String>) {
        val rootFile = File(rootPath)
        val rootAbsolutePath = rootFile.absolutePath
        list.forEach { path ->
            if (path.isEmpty()) {
                return@forEach
            }
            var realPath = if (path.startsWith("/")) {
                path
            } else {
                "/$path"
            }
            val file = File(rootAbsolutePath + realPath)
            if (file.exists()) {
                println("删除文件-" + file.absolutePath)
                FileUtils.delete(file)

            }
        }
    }



    private fun replaceManifestAndYml(
        rootPath: String,
        apkConfigBean: ApkConfigBean
    ) {

        manifestDeleteName(rootPath + AndroidManifest, apkConfigBean.deleteManifestNodeNames)//删除AndroidManifest 指定节点
        manifestSetAttributeByName(rootPath+AndroidManifest, apkConfigBean.manifestNodeSetAttributeMapByName) //设置AndroidManifest节点属性
        replaceManifest(
            rootPath,
            apkConfigBean = apkConfigBean
        )
        replaceStringManifest(rootPath, apkConfigBean.replaceStringManifest)
        replaceApktoolYml(
            ymlFilePath = "$rootPath/apktool.yml",
            versionCode = apkConfigBean.versionCode,
            versionName = apkConfigBean.versionName,
            minSdkVersion = apkConfigBean.minSdkVersion,
            targetSdkVersion = apkConfigBean.targetSdkVersion
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
        apkConfigBean: ApkConfigBean
    ) {

        val path2 = "$rootPath/AndroidManifest.xml"
        if (!File(path2).exists()) {
            return
        }
        val saxReader2 = SAXReader()
        val read2 = saxReader2.readSafe(path2)
        val rootElement2 = read2.rootElement
        val applictionElement2 = getOrAddApplictionElement(rootElement2)
        replacePackage(rootElement2, apkConfigBean.packageName, applictionElement2)

        replaceCompileSdkInfo(rootElement2, apkConfigBean.compileSdkInfo)
        replaceVersion(
            apkConfigBean.versionCode,
            rootElement2,
            apkConfigBean.versionName,
            apkConfigBean.minSdkVersion,
            apkConfigBean.targetSdkVersion
        )
        replaceMetaData(applictionElement2, apkConfigBean.metaDataMap)
        replaceIcon(
            applictionElement2,
            apkConfigBean.iconImgPath,
            apkConfigBean.iconSize,
            rootPath,
            apkConfigBean.iconImgPath
        )
        replaceAppName(apkConfigBean.appName, applictionElement2, rootPath)
        apkConfigBean.applicationSetAttributeMap.forEach{name,value->
            if (name.isNotEmpty()) {
                applictionElement2.setAndroidAttribute(name, value)
            }
        }
        saveXml(path2, read2)
    }

    private fun replaceMetaData(applictionElement2: Element?, metaDataMap: Map<String, String>) {
        if (metaDataMap.isEmpty()) {
            return
        }
        val keySet = TreeSet<String>()
        applictionElement2?.elements()?.toList()?.forEach {
            if (it.name.equals("meta-data")) {
                val name = it.attribute("name").data as String
                if (metaDataMap.containsKey(name)) {
                    it.setAndroidAttribute("value", metaDataMap[name])
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

    private fun replaceCompileSdkInfo(rootElement2: Element, compileSdkInfo: CompileSdkInfo) {
        if (compileSdkInfo.compileSdkVersion.isNotEmpty()) {
            rootElement2.setAndroidAttribute("compileSdkVersion", compileSdkInfo.compileSdkVersion)
        }
        if (compileSdkInfo.compileSdkVersionCodename.isNotEmpty()) {
            rootElement2.setAndroidAttribute("compileSdkVersionCodename", compileSdkInfo.compileSdkVersionCodename)
        }

        if (compileSdkInfo.platformBuildVersionCode.isNotEmpty()) {
            rootElement2.setNameAttribute(
                name = "platformBuildVersionCode",
                value = compileSdkInfo.platformBuildVersionCode
            )

        }

        if (compileSdkInfo.platformBuildVersionName.isNotEmpty()) {
            rootElement2.setNameAttribute(
                name = "platformBuildVersionName",
                value = compileSdkInfo.platformBuildVersionName
            )
        }
    }

    fun Element.setAndroidAttribute(name: String, value: String?) {
        setNameAttribute(nameSpace = "android", name = name, value = value)
    }

    fun Element.setNameAttribute( name: String, value: String?,nameSpace: String = "") {
        if (value == null) {
            return
        }
        var attribute = this.attribute(name)
        if (attribute == null) {
            if (nameSpace.isEmpty()) {
                this.addAttribute(name, value)
            } else {
                this.addAttribute("$nameSpace:$name", value)
            }
        } else {
            attribute.data = value
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
            rootElement2.setAndroidAttribute("versionCode", versionCode)
        }
        if (versionName.isNotEmpty()) {
            rootElement2.setAndroidAttribute("versionName", versionName)
        }
        if (minSdkVersion.isNotEmpty() || targetSdkVersion.isNotEmpty()) {
            println("replaceVersion----minSdkVersion:$minSdkVersion  targetSdkVersion:$targetSdkVersion")
            if (minSdkVersion.isEmpty() || targetSdkVersion.isEmpty()) {
                println("replaceVersion------warn: minSdkVersion or targetSdkVersion is empty")
            }

            var isChangeSdkVersion = false
            rootElement2.elements().forEach {
                if (it.name.equals("uses-sdk")) {
                    if (minSdkVersion.isNotEmpty()) {
                        it.setAndroidAttribute("minSdkVersion", minSdkVersion)
                    }
                    if (targetSdkVersion.isNotEmpty()) {
                        it.setAndroidAttribute("targetSdkVersion", targetSdkVersion)
                    }
                    isChangeSdkVersion = true
                }
            }
            if (!isChangeSdkVersion) {
                val domElement = DOMElement("uses-sdk")
                if (minSdkVersion.isNotEmpty()) {
                    domElement.setAndroidAttribute("minSdkVersion", minSdkVersion)
                }
                if (targetSdkVersion.isNotEmpty()) {
                    domElement.setAndroidAttribute("targetSdkVersion", targetSdkVersion)
                }
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
            rootElement2.setNameAttribute(name = "package", value = packageName)
            rootElement2.elements().forEach {
                if (it.name.equals("uses-permission") || it.name.equals("permission")) {
                    tryCatch(false) {
                        val data = it.attribute("name").data as String
                        val replace = data.replace(packge, packageName)
                        it.setAndroidAttribute("name", replace)
                    }
                }

            }
            applictionElement2.elements().forEach { appChild ->
                if (appChild.name.equals("provider")) {
                    tryCatch(false) {
                        var attribute = appChild.attribute("authorities")
                        val data = attribute.data as String
                        val replace = data.replace(packge, packageName)
                        appChild.setAndroidAttribute("authorities", replace)
                    }
                }
                if (appChild.name.equals("activity")) {
                    tryCatch(false) {
                        var attribute = appChild.attribute("taskAffinity")
                        val data = attribute.data as String
                        val replace = data.replace(packge, packageName)
                        appChild.setAndroidAttribute("taskAffinity", replace)
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
                    applictionElement2.setAndroidAttribute("label", appName)
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
            val read2 = saxReader2.readSafe(path2)
            val rootElement2 = read2.rootElement
            val applictionElement2 = rootElement2.element("application")
            applictionElement2.setAndroidAttribute("extractNativeLibs", "true")
            saveXml(path2, read2)
        }

    }
}