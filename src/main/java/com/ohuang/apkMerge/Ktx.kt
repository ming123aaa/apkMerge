package com.ohuang.apkMerge

import java.io.File

inline fun tryCatch(isLog: Boolean=true,method:()->Unit){
    try {
        method.invoke()
    }catch (e:Throwable){
        if (isLog){
            e.printStackTrace()
        }
    }
}

fun tryCatch(method:()->Unit,catch:(Throwable)->Unit){
    try {
        method.invoke()
    }catch (e:Throwable){
         catch.invoke(e)
    }
}

fun File.parentMkdirs():Boolean{
   return parentFile.mkdirs()
}