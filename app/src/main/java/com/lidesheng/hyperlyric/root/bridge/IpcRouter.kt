package com.lidesheng.hyperlyric.root.bridge

import android.app.Application
import android.os.Handler
import android.os.Looper
import com.lidesheng.hyperlyric.root.island.renderer.BaseIslandRenderer
import com.lidesheng.hyperlyric.root.utils.HookLogger

object IpcRouter {

    private const val TAG = "IpcRouter"
    private val mainHandler = Handler(Looper.getMainLooper())

    fun initialize(app: Application) {
        LyriconBridge.routing(app) {
            onCommand(AppBridgeConstants.REQUEST_UPDATE_LYRIC_STYLE) {
                HookLogger.d(TAG, "接收到样式更新请求")
                mainHandler.post { BaseIslandRenderer.refreshActiveIsland() }
            }
            onCommand("com.lidesheng.hyperlyric.REFRESH_ISLAND") {
                HookLogger.d(TAG, "接收到超级岛刷新请求")
                mainHandler.post { BaseIslandRenderer.refreshActiveIsland() }
            }
            onCommand("com.lidesheng.hyperlyric.UPDATE_LYRIC_ANIM") {
                HookLogger.d(TAG, "接收到歌词动画刷新请求")
                mainHandler.post { BaseIslandRenderer.refreshActiveIsland() }
            }
        }
    }
}
