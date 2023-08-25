package com.gmerge.tempfile

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.PixelFormat
import android.os.Bundle
import android.view.DragEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import com.gmerge.tempfile.databinding.FragmentFirstBinding

class MainFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    private var windowView: View? = null
    private var windowManager: WindowManager? = null
    private var isWindowVisible = false
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0
    private var initialTouchY = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonStart.setOnClickListener {
            if (!isWindowVisible) {
                showFloatingWindow()
            } else {
                hideFloatingWindow()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showFloatingWindow() {
        if (windowView == null) {
            windowView = LayoutInflater.from(requireContext()).inflate(R.layout.layout_floating_window, null)
        }

        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val screenHeight = Resources.getSystem().displayMetrics.heightPixels

        val params = WindowManager.LayoutParams(
            300, 200,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.x = screenWidth - 300
        params.y = 300

        windowManager = requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager?.addView(windowView, params)

        windowView?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX.toInt()
                    initialTouchY = event.rawY.toInt()
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX.toInt() - initialTouchX
                    val dy = event.rawY.toInt() - initialTouchY

                    params.x = initialX + dx
                    params.y = initialY + dy

                    windowManager?.updateViewLayout(windowView, params)
                }
            }
            true
        }

        windowView?.setOnDragListener { v, event ->
            when (event?.action) {
                DragEvent.ACTION_DROP -> {
                    val item = event.clipData.getItemAt(0)
                    val dragData = item.text?.toString() ?: ""

                    val textView = windowView?.findViewById<TextView>(R.id.textView)
                    val imageView = windowView?.findViewById<ImageView>(R.id.imageView)

                    // 判断拖拽的数据类型
                    if (item.uri != null && item.uri.toString().startsWith("content://")) {
                        // 处理图片的URI，显示在 ImageView 中
                        imageView?.setImageURI(item.uri)
                        textView?.text = "" // 清空文本
                    } else {
                        // 显示拖拽的文本数据
                        textView?.text = dragData
                        imageView?.setImageDrawable(null) // 清空图片
                    }
                }
            }
            true
        }

        isWindowVisible = true
    }


    private fun hideFloatingWindow() {
        windowManager?.removeView(windowView)
        windowView = null
        windowManager = null
        isWindowVisible = false
    }
}
