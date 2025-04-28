package com.forkbombsquad.stillalivelarp.utils

import android.content.Context
import android.graphics.BitmapFactory
import androidx.lifecycle.LifecycleCoroutineScope
import com.forkbombsquad.stillalivelarp.services.managers.SharedPrefsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jsoup.nodes.Document
import java.net.HttpURLConnection
import java.net.URL

class ImageDownloader {
    companion object {

        enum class ImageKey(val key: String, val url: String) {
            SKILL_TREE("skillTree", "https://stillalivelarp.com/skilltree"),
            SKILL_TREE_DARK("skillTreeDark", "https://stillalivelarp.com/skilltree/dark"),
            TREATING_WOUNDS("treatingWounds", "https://stillalivelarp.com/healing")
        }

        fun download(context: Context, lifecycleScope: LifecycleCoroutineScope, key: ImageKey, onCompletion: (success: Boolean) -> Unit) {
            downloadPage(lifecycleScope, key.url) { imagePath ->
                if (imagePath != null) {
                    downloadFromUrl(context, lifecycleScope, imagePath, key) {
                        onCompletion(it)
                    }
                } else {
                    onCompletion(false)
                }

            }
        }
        private fun downloadFromUrl(context: Context, lifecycleScope: LifecycleCoroutineScope, path: String, key: ImageKey, onCompletion: (success: Boolean) -> Unit) {
            lifecycleScope.launch {
                GlobalScope.launch(Dispatchers.IO) {
                    val url = URL(path)
                    val connection = url.openConnection() as? HttpURLConnection
                    connection?.doInput = true
                    connection?.connect()
                    val responseCode = connection?.responseCode ?: -1
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val imageStream = connection?.inputStream
                        val bmp = BitmapFactory.decodeStream(imageStream)
                        imageStream?.close()
                        SharedPrefsManager.shared.set(context, key.key, bmp)
                        onCompletion(true)
                    } else {
                        onCompletion(false)
                    }
                }
            }
        }

        private fun downloadPage(lifecycleScope: LifecycleCoroutineScope, url: String, onCompletion: (imagePath: String?) -> Unit) {
            lifecycleScope.launch {
                val jsoupAsyncTask = JsoupAsyncTask(url) { doc ->
                    onCompletion(getImagePath(doc))
                }
                jsoupAsyncTask.execute()
            }
        }

        private fun getImagePath(document: Document?): String? {
            val imageElement = document?.getElementById("image")
            return imageElement?.attr("src")
        }
    }
}