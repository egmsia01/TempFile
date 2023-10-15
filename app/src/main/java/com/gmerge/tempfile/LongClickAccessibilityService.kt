package com.gmerge.tempfile

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

class LongClickAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            val eventType: Int = it.eventType
            // 处理长按事件
            if (eventType == AccessibilityEvent.TYPE_VIEW_LONG_CLICKED) {
                // it.source 可以获取到触发事件的元素
                Toast.makeText(this, "长按事件触发", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onInterrupt() {
        // 服务被中断时会调用此方法
    }
}
