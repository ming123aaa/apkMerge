package com.ohuang.apkMerge

import java.io.File



/**
 * 合并value文件的xml
 */
fun mergeValueXml(path: String, path2: String,isNewID: Boolean=false,usePath:Boolean=false){
    var file = File(path)

    file.listFiles()?.forEach {
        if (!it.name.equals("public.xml")) {
            var file1 = File(path2 + "/${it.name}")
            if (file1.exists()) {
                mergeXmlSafe(it.absolutePath, file1.absolutePath,file1.absolutePath,usePath=usePath)
            } else {
                it.copyTo(file1)
            }
        }else{
            var file1 = File(path2 + "/${it.name}")
            if (file1.exists()) {
                if (isNewID){
                    mergePublicXmlWewId(it.absolutePath, file1.absolutePath)
                }else {
                    mergePublicXml(it.absolutePath, file1.absolutePath)
                }
            } else {
                it.copyTo(file1)
            }

        }

    }
}

