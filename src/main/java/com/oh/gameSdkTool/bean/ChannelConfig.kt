package com.oh.gameSdkTool.bean

/**
 *  ChannelConfig.json 数据结构
 *
 */
class ChannelConfig {
    var buildDirFile="build"
    var outDirFile="outApk"
    var enableChannelNames:List<String> = ArrayList<String>()  //设置可用渠道(channelName 或者 channelGroupName)都行  为空时所有渠道都可用
    var disableChannelNames:List<String> = ArrayList<String>()  //设置可用渠道(channelName 或者 channelGroupName)都行  为空时所有渠道都可用
    var items:List<ChannelConfigItem> = ArrayList<ChannelConfigItem>()
}

class ChannelConfigItem{
    var channelName=""  //渠道的名称  必须设置
    var apkConfigFile=""  //apkConfig.json的相对路径 必须设置
    var signConfigFile="" //signConfig.json的相对路径 必须设置
    var mode=""  //修改渠道包的模式 默认skip模式 必须设置
    var extraCmd:List<String> = ArrayList<String>() //用于设置合并代码时额外的cmd参数
    var channelApkFile=""  //合并渠道包框架的apk
    var listMergeConfigs:List<MergeConfig> = ArrayList<MergeConfig>()
    var channelEnable=true //是否可用
    var channelGroupName="" //组名
    var apkName="" //不设置自动使用渠道名
}

class MergeConfig{
    var enable=true
    var extraCmd:List<String> = ArrayList<String>() //用于设置合并代码时额外的cmd参数
    var channelApkFile=""  //合并渠道包框架的apk
}

const val MODE_SKIP="skip"// skip:跳过处理
const val MODE_SIMPLE="simple" //simple模式:不合并只进行修改包名图标等操作
const val MODE_SIMPLE_Fast="simple_fast" //simple_fast模式:比simple模式更快(若修改了图标和应用名称,请用simple模式)
const val MODE_MERGE="merge" //merge模式:合并代码资源
const val MODE_LIST="merge_list" //merge_list模式:合并代码资源,支持多个包合并
const val MODE_MERGE_Reverse="merge_reverse" //merge_reverse:反向合并代码资源,渠道包作为主包