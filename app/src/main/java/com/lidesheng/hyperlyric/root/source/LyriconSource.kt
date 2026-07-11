package com.lidesheng.hyperlyric.root.source

import android.app.Application
import com.lidesheng.hyperlyric.lyric.source.LyricSink
import com.lidesheng.hyperlyric.lyric.source.LyricSource
import com.lidesheng.hyperlyric.root.island.renderer.BaseIslandRenderer
import com.lidesheng.hyperlyric.root.LyriconDataBridge
import com.lidesheng.hyperlyric.root.utils.HookLogger
import io.github.proify.lyricon.lyric.model.Song
import io.github.proify.lyricon.subscriber.ActivePlayerListener
import io.github.proify.lyricon.subscriber.ConnectionListener
import io.github.proify.lyricon.subscriber.LyriconFactory
import io.github.proify.lyricon.subscriber.LyriconSubscriber
import io.github.proify.lyricon.subscriber.ProviderInfo
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

class LyriconSource : LyricSource {

    companion object {
        private const val TAG = "LyriconSource"
        private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
    }

    private fun Song.toLocalSong(): com.lidesheng.hyperlyric.lyric.model.Song {
        val jsonString = json.encodeToString(this)
        return json.decodeFromString(jsonString)
    }

    override val id = "lyricon"
    override val displayName = "Lyricon"

    @Volatile
    private var sink: LyricSink? = null
    private var app: Application? = null
    @Volatile
    private var subscriber: LyriconSubscriber? = null

    override fun isAvailable(): Boolean = true

    override fun start(sink: LyricSink) {
        if (this.subscriber != null) {
            HookLogger.w(TAG, "Lyricon 数据源已在运行，跳过重复启动")
            return
        }
        this.sink = sink
        val application = app ?: run {
            HookLogger.w(TAG, "Application 未初始化，无法启动")
            return
        }
        initializeSubscriber(application)
        HookLogger.i(TAG, "Lyricon 数据源已启动")
    }

    override fun stop() {
        try {
            subscriber?.unsubscribeActivePlayer(activePlayerListener)
            subscriber?.unregister()
            subscriber?.destroy()
        } catch (e: Exception) {
            HookLogger.e(TAG, "清理 Subscriber 时发生错误", e)
        } finally {
            subscriber = null
            sink?.onStop()
            sink = null
        }
        HookLogger.i(TAG, "Lyricon 数据源已停止")
    }

    fun initialize(app: Application) {
        this.app = app

        LyriconDataBridge.onAiTranslationComplete = {
            BaseIslandRenderer.refreshActiveIsland()
        }
    }

    private fun initializeSubscriber(app: Application) {
        val sub = LyriconFactory.createSubscriber(app)
        subscriber = sub

        sub.addConnectionListener(connectionListener)
        sub.subscribeActivePlayer(activePlayerListener)
        sub.register()
    }

    private val connectionListener = object : ConnectionListener {
        override fun onConnected(subscriber: LyriconSubscriber) {
            HookLogger.i(TAG, "Subscriber 已连接")
        }

        override fun onReconnected(subscriber: LyriconSubscriber) {
            HookLogger.i(TAG, "Subscriber 已重连")
        }

        override fun onDisconnected(subscriber: LyriconSubscriber) {
            HookLogger.w(TAG, "Subscriber 已断开")
        }

        override fun onConnectTimeout(subscriber: LyriconSubscriber) {
            HookLogger.w(TAG, "Subscriber 连接超时")
        }
    }

    private val activePlayerListener = object : ActivePlayerListener {
        override fun onActiveProviderChanged(providerInfo: ProviderInfo?) {
            sink?.onStop()
            LyriconDataBridge.updateLyricPackage(providerInfo?.playerPackageName)
        }

        override fun onSongChanged(song: Song?) {
            val localSong = song?.toLocalSong()
            LyriconDataBridge.updateSong(localSong)
            sink?.onSongChanged(localSong)
            BaseIslandRenderer.refreshActiveIsland()
        }

        override fun onPlaybackStateChanged(isPlaying: Boolean) {
            sink?.onPlaybackStateChanged(isPlaying)
        }

        override fun onPositionChanged(position: Long) {
            sink?.onPositionChanged(position)
        }

        override fun onSeekTo(position: Long) {}

        override fun onReceiveText(text: String?) {
            sink?.onPlainText(text)
        }

        override fun onDisplayTranslationChanged(isDisplayTranslation: Boolean) {
            LyriconDataBridge.isDisplayTranslation = isDisplayTranslation
            BaseIslandRenderer.refreshActiveIsland()
        }

        override fun onDisplayRomaChanged(isDisplayRoma: Boolean) {
            LyriconDataBridge.isDisplayRoma = isDisplayRoma
            BaseIslandRenderer.refreshActiveIsland()
        }
    }
}
