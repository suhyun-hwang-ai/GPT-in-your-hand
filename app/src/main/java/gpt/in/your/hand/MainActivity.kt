package gpt.`in`.your.hand

import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import gpt.`in`.your.hand.ui.SingleTurnScreen
import gpt.`in`.your.hand.ui.SingleTurnViewModel
import gpt.`in`.your.hand.ui.defaultModelPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private const val TAG = "MainActivity"

/**
 * 책 17장에서 컨텍스트·멀티턴이 들어가기 전 main 브랜치 종료 지점의 한 턴 대화 앱.
 *
 * 모델 GGUF 파일은 다음 위치 중 하나에서 찾는다.
 * 1) assets/model.Q4_K_M.gguf  (있으면 filesDir로 1회 복사)
 * 2) filesDir/model.Q4_K_M.gguf  (사용자가 adb push 등으로 직접 둔 경우)
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val vm: SingleTurnViewModel = viewModel()
                    SingleTurnScreen(viewModel = vm)
                    EnsureModelLoaded(vm)
                }
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun EnsureModelLoaded(vm: SingleTurnViewModel) {
        androidx.compose.runtime.LaunchedEffect(Unit) {
            val targetPath = defaultModelPath(application)
            val ready = withContext(Dispatchers.IO) {
                ensureModelOnDisk(targetPath)
            }
            if (ready) {
                vm.loadModel(targetPath)
            } else {
                Log.w(TAG, "모델 파일을 찾을 수 없음: $targetPath")
            }
        }
    }

    /**
     * filesDir에 모델이 없으면 assets에서 복사. 둘 다 없으면 false.
     */
    private fun ensureModelOnDisk(targetPath: String): Boolean {
        val target = File(targetPath)
        if (target.exists() && target.length() > 0) return true

        val assetName = "model.Q4_K_M.gguf"
        return try {
            assets.open(assetName).use { input ->
                target.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            target.exists() && target.length() > 0
        } catch (t: Throwable) {
            Log.w(TAG, "assets에 모델 없음 또는 복사 실패: ${t.message}")
            false
        }
    }
}
