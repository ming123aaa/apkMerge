import com.ohuang.apkMerge.findSmaliCodeIdFirst
import com.ohuang.apkMerge.mergeYml
import com.ohuang.apkMerge.preMergeManifestSetLauncherActivity
import com.ohuang.replacePackage.moveFile

import org.junit.jupiter.api.Test

class TestCode {
    @Test
    fun `test moveFile`() {
        moveFile("C:\\Users\\28060\\Desktop\\test\\a","C:\\Users\\28060\\Desktop\\test\\b")

    }


}