package com.oh.gameSdkTool.bean

import com.google.gson.Gson
import com.oh.gameSdkTool.CommandArgs
import com.ohuang.replacePackage.FileUtils
import java.io.File
import java.util.TreeMap

/**
 * apk修改配置  只用于json保存
 */
class ApkConfig {
    var packageName: String = ""
    var iconImgPath = "" //图片相对于配置文件的路径
    var iconSize = "-xxhdpi"
    var appName = ""
    var versionCode=""
    var versionName=""
    var minSdkVersion=""
    var targetSdkVersion=""
    var abiNames:List<String> = ArrayList<String>() //需要保留的abi架构,为空就是保留所有的架构
    var metaDataMap:Map<String,String> = TreeMap<String,String>() // meta-data修改
    var replaceStringManifest:List<ReplaceStringData> = emptyList() // AndroidManifest.xml 字符串替换   用于复杂的数据替换
    var deleteFileList:List<String> = emptyList() //需要删除的文件， 示例 res/mipmap-anydpi
    var changeClassPackage:Map<OldName,NewName> = emptyMap() // 修改class所在的包名  com.xxx.yyy 中间用.隔开
    var renameResMap:Map<ResType,Map<OldName,NewName>> = emptyMap() // Map<type,Map<oldName,newName>>
    var smaliClassSizeMB:Long=0   //限制smaliClass文件的大小,避免方法数量超出限制无法打包,推荐值50MB， 若smaliClassSizeMB<=0或smaliClassSizeMB>=1000将不限制文件大小
    var deleteSmaliPaths: List<String> =emptyList() //需要删除的smail的文件   aa/bb   aa/cc.smali
    var isDeleteSameNameSmali: Boolean=true  //是否删除相同名称的smali文件
    var deleteManifestNodeNames: Set<String> =emptySet() //根据name删除的AndroidManifest.xml对应的节点
}
typealias ResType =String
typealias OldName =String
typealias NewName =String

class ReplaceStringData{

    var isReplaceFirst=false
    var isRegex=false
    var matchString:String=""
    var replaceString:String=""

}


