package com.ohuang.aar

import com.ohuang.replacePackage.FileUtils
import java.io.File

const val declareXml = "/res/values/z_my_declare.xml"

private const val xmlHeader = """
<?xml version="1.0" encoding="utf-8"?>
<resources>
"""

private const val xmlEnd = "</resources>"

private fun parentHeader(name: String): String {
    return " <declare-styleable name=\"${name}\">"
}

private fun parentEnd(): String {
    return " </declare-styleable>"
}

private fun child(name: String): String {
    var split = name.split("_")
    if (split.size >= 2) {
        if (split[0] == "android") {
            var newString=name.substring(split[0].length+1)
            return "    <attr name=\"android:${newString}\"/>"
        } else {
            return "    <attr name=\"${name}\"/>"
        }
    } else {
        return "    <attr name=\"${name}\"/>"

    }
}

fun buildDeclareXml(
    path: String, itemMap: LinkedHashMap<String, StyleableItem>
) {
    var stringBuilder = StringBuilder()
    stringBuilder.append(xmlHeader)

    itemMap.forEach { name ,item->
        stringBuilder.append("\n").append(parentHeader(name))
        var start = name.length + 1
        item.childMap.forEach {
            stringBuilder.append("\n").append(child(it.key.substring(start)))
        }
        stringBuilder.append("\n").append(parentEnd())

    }

    stringBuilder.append(xmlEnd)
    FileUtils.writeText(File(path), stringBuilder.toString())
}