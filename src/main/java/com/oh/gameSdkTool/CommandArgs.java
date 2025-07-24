package com.oh.gameSdkTool;

import com.beust.jcommander.Parameter;

public class CommandArgs {

    @Parameter(names = {"-javaPath"}, description = "java.exe 路径,可不填")
    public String javaPath = "";

    @Parameter(names = {"-libs"}, description = "运行环境的路径,路径需要包含一些需要用到jar或可执行程序\n" +
            "如(apktool.jar、zipalign.exe、apksigner.jar)")
    public String libs = "";

    @Parameter(names = {"-baseApk","-basePath"}, description = "主包路径")
    public String basePath = "";

    @Parameter(names = {"-channelApk","-channelPath"}, description = "渠道框架包路径")
    public String channelPath = "";

    @Parameter(names = {"-out", "-o","-outPath"}, description = "输出路径")
    public String outPath = "";

    @Parameter(names = {"-baseSmali"}, description = "主包smali环境路径，运行后会将框架打入这个smali环境")
    public String baseSmali="";

    @Parameter(names = {"-channelSmali"}, description = "渠道框架smali环境路径")
    public String channelSmali="";


    @Parameter(names = {"-log"},description ="打印运行日志")
    public boolean isLog=false;


    @Parameter(names = {"-generateMergeChannelApk"},description = "合并渠道后生成apk 需要配合命令-baseApk、-channelApk" +
            "、-out、-apkConfig" +
            "、-signConfig、-libs一起使用")
    public Boolean isGenerateMergeChannelApk=false;



    @Parameter(names = {"-channelRes","-isCoverFile","-useChannelRes"},description = "优先使用渠道包内的res、assets、AndroidManifest.xml等，" +
            "等价于-isUseChannelFileAssets、-isUseChannelFileLib、-isUseChannelFileRes、-isUseChannelFileManifest、-isUseChannelFileOther命令同时使用")
    public Boolean isChannelRes=false;

    @Parameter(names = {"-isUseChannelFileAssets"},description = "优先使用渠道包内的assets")
    public Boolean isUseChannelFileAssets=false;

    @Parameter(names = {"-isUseChannelFileLib"},description = "优先使用渠道包内的lib")
    public Boolean isUseChannelFileLib=false;

    @Parameter(names = {"-isUseChannelFileRes"},description = "优先使用渠道包内的res")
    public Boolean isUseChannelFileRes=false;

    @Parameter(names = {"-isUseChannelFileManifest"},description = "优先使用渠道包内的AndroidManifest.xml的内容")
    public Boolean isUseChannelFileManifest=false;

    @Parameter(names = {"-isUseChannelFileOther"},description = "优先使用渠道包内的其他文件,其他文件指的是(kotlin、unknown)")
    public Boolean isUseChannelFileOther=false;

    @Parameter(names = {"-channelCode","-useChannelCode"},description = "优先使用渠道的代码")
    public Boolean isChannelCode=false;

    @Parameter(names = {"-useChannelApktoolYml"},description = "使用渠道包的apktool.yml")
    public Boolean isUseChannelApktoolYml=false;

    @Parameter(names = {"-replaceApplication"},description = "合并AndroidManifest.xml替换Application类  若配合-useChannelRes命令使用application会替换成变成主包的,否则application替换成渠道包的")
    public Boolean isReplaceApplication=false;

    @Parameter(names = {"-changeNotRSmali"},description = "修改没有引用R文件的id,通过修改0X7f开头的值实现。")
    public Boolean isChangeNotRSmali=false;


    @Parameter(names = {"-isRenameRes"},description = "res出现重名情况,是否重命名。apk合并时使用")
    public Boolean isRenameRes=false;

    @Parameter(names = {"-isRenameClassPackage"},description = "class出现重名情况,是否修改package。apk合并时使用")
    public Boolean isRenameClassPackage=false;

    @Parameter(names = {"-notUseDefaultKeepClassPackage"},description = "禁用默认的keep class规则。apk合并时使用")
    public boolean notUseDefaultKeepClassPackage=false;

    @Parameter(names = {"-apkConfig"}, description = "设置ApkConfig")
    public String apkConfig = "";

    @Parameter(names = "-signConfig", description = "设置签名配置文件")
    public String signConfig="";


    @Parameter(names = "-channelConfig", description = "设置ChannelConfig.json配置文件")
    public String channelConfig="";

    @Parameter(names = {"-aarConfig"}, description = "设置aarConfig.json配置文件")
    public String aarConfig="";


    @Parameter(names = {"-test"}, description = "action命令,测试运行")
    public boolean isTest = false;

    @Parameter(names = {"-outApkConfig"}, description = "action命令,输出ApkConfig.json的模板  需要使用-out设置输出路径的文件夹")
    public boolean isOutApkConfig = false;


    @Parameter(names = "-sign",description = "action命令,给apk签名,配合-libs,-out、-signConfig和-baseApk一起使用")
    public boolean isSign=false;

    @Parameter(names = "-toApk",description = "action命令,smali环境生成apk,配合-libs,-out、-signConfig和-baseSmali一起使用")
    public boolean isToApk =false;

    @Parameter(names = "-outSignConfig", description = "action命令,输出signConfig.json的模板  需要使用-out设置输出路径的文件夹")
    public boolean isOutSignConfig = false;

    @Parameter(names = {"-outChannelConfig"},description = "action命令,输出ChannelConfig.json的模板  需要使用-out设置输出路径的文件夹")
    public boolean isOUtChannelConfig=false;

    @Parameter(names = {"-changeSmali"}, description = "action命令,需要修改的smali路径，配合-ApkConfig一起使用 可用于换包名、图标、名称")
    public String changeSmali = "";

    @Parameter(names = {"-changeApk"}, description = "action命令,需要修改apk的路径,配合-ApkConfig、-out、-signConfig、libs一起使用 可用于换包名、图标、名称")
    public String changeApk = "";
    @Parameter(names = {"-mergeSmali"}, description = "action命令,合并smali环境 需要配合-baseSmali和-channelSmali一起使用" +
            " 使用-useChannelRes在文件冲突时会覆盖baseSmali的文件")
    public Boolean isMergeSmali = false;

    @Parameter(names = {"-decompile"},description = "action命令,反编译apk ,配合-baseApk、-out、libs一起使用")
    public boolean isDecompile=false;

    @Parameter(names = {"-generateMultipleChannelApk"},description = "action命令,批量生成渠道apk 需要配合命令-baseApk、-channelConfig" +
            "、-libs一起使用")
    public Boolean isGenerateMultipleChannelApk=false;

    @Parameter(names = {"-toUnzip"},description = "action命令,解压apk 需要配合-basePath、-out")
    public boolean toUnzip=false;

    @Parameter(names = {"-toZip"},description = "action命令,压缩apk 需要配合-basePath、-out")
    public boolean toZip=false;

    @Parameter(names = {"-apk2aar","-apkToAar"},description = "action命令,将apk转化为aar,配合命令-basePath、-out、-lib、-aarConfig")
    public boolean isApk2Aar=false;

    @Parameter(names = {"-mergeAar"},description = "action命令,合并aar,配合命令-out、-lib、-aarConfig")
    public boolean isMergeAar=false;

    @Parameter(names = {"-help", "-h"}, help = true, description = "查看帮助")
    public boolean help;

}
