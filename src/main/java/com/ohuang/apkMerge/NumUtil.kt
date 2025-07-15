package com.ohuang.apkMerge

fun ox16ToInt(num: String): Int {
    return NumUtil.ox2Int(num)
//    var removeSuffix = num.replace("0x","")
//    return  Integer.parseInt(removeSuffix, 16)
}


fun intTo0X16(num: Int): String {
    return NumUtil.int2ox(num);
//    return "0x"+Integer.toHexString(num)
}


fun getMaigicNum_a_z(long: Long): String {
    var s = ""
    var num = long
    var isAdd = false
    if (num < 0) {
        isAdd = true
        num=-num
    }
    if (num == 0L) {
        return "a"
    }
    while (num > 0) {
        s = getA_z_char(num) + s
        num/=26
    }
    return if (isAdd){
        "_$s"
    }else{
        s
    }
}

private fun getA_z_char(num: Long): Char {
    var a = num % 26
    var char: Char = 'a' + a.toInt()
    return char
}