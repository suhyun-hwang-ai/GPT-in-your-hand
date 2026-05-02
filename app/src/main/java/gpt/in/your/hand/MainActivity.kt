package gpt.`in`.your.hand

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import gpt.`in`.your.hand.ui.ChatScreen
import gpt.`in`.your.hand.ui.ChatViewModel
import gpt.`in`.your.hand.ui.defaultModelPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private const val TAG = "MainActivity"

/**
 * tarot 브랜치 — 멀티턴 채팅 + 카드 인터랙션.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val vm: ChatViewModel = viewModel()
                    ChatScreen(viewModel = vm)
                    EnsureModelLoaded(vm)
                }
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun EnsureModelLoaded(vm: ChatViewModel) {
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
