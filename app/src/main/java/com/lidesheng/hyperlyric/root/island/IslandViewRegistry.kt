package com.lidesheng.hyperlyric.root.island

import android.view.ViewGroup
import java.util.WeakHashMap

internal object IslandViewRegistry {
    private val activeIslandPkgNames: MutableMap<ViewGroup, String> =
        java.util.Collections.synchronizedMap(WeakHashMap<ViewGroup, String>())

    fun register(view: ViewGroup, packageName: String) {
        activeIslandPkgNames[view] = packageName
    }

    fun unregister(view: ViewGroup) {
        activeIslandPkgNames.remove(view)
    }

    fun snapshotAttached(packageName: String? = null): List<Pair<ViewGroup, String>> {
        val result = mutableListOf<Pair<ViewGroup, String>>()
        synchronized(activeIslandPkgNames) {
            val iterator = activeIslandPkgNames.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                val viewGroup = entry.key
                if (viewGroup.isAttachedToWindow) {
                    if (packageName == null || entry.value == packageName) {
                        result += viewGroup to entry.value
                    }
                } else {
                    iterator.remove()
                }
            }
        }
        return result
    }
}
