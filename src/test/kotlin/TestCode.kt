import com.ohuang.apkMerge.findSmaliCodeIdFirst
import com.ohuang.apkMerge.mergeYml
import com.ohuang.apkMerge.preMergeManifestSetLauncherActivity

import org.junit.jupiter.api.Test

class TestCode {
    @Test
    fun `test preMergeManifestSetLauncherActivity`() {
        preMergeManifestSetLauncherActivity(
            "C:\\Users\\ali213\\Desktop\\aaa\\AndroidManifest.xml",
            "C:\\Users\\ali213\\Desktop\\bbb\\AndroidManifest.xml"
        )
    }

    @Test
    fun `test mergeYml`() {
        com.ohuang.apkMerge.mergeYml(
            channelYml = "E:\\gameSdkTool\\channel_sgm\\build\\merge\\sgm_test\\channelSmali\\apktool.yml",
            baseYml = "E:\\gameSdkTool\\channel_sgm\\build\\merge\\sgm_test\\baseSmali\\apktool.yml",
            outYml = "E:\\gameSdkTool\\channel_sgm\\build\\merge\\sgm_test\\out\\apktool.yml",
            isUseChannelApktoolYml = true
        )
    }

    @Test
    fun `test findSmaliCodeIdFirst`(){
        var findSmaliCodeIdFirst = findSmaliCodeIdFirst("")
        println("0:"+findSmaliCodeIdFirst)
        findSmaliCodeIdFirst= findSmaliCodeIdFirst("0xffasasas")
        println("1:"+findSmaliCodeIdFirst)
        findSmaliCodeIdFirst= findSmaliCodeIdFirst("0xff1234")
        println("2:"+findSmaliCodeIdFirst)
        findSmaliCodeIdFirst= findSmaliCodeIdFirst("0x7f1234aa")
        println("3:"+findSmaliCodeIdFirst)
        findSmaliCodeIdFirst= findSmaliCodeIdFirst("const p0,0x7f1234aa")
        println("4:"+findSmaliCodeIdFirst)
    }
}