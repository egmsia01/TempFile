package com.gmerge.tempfile

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.DragEvent
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity

import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.gmerge.tempfile.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var context: Context

    private var windowView: View? = null
    private var windowManager: WindowManager? = null
    private var isWindowVisible = false
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0
    private var initialTouchY = 0
    private val textView = windowView?.findViewById<TextView>(R.id.textView)
    private val imageView = windowView?.findViewById<ImageView>(R.id.imageView)

    private val overlayPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (checkOverlayPermission()) {
                    showFloatingWindow()
                } else {
                    Toast.makeText(this, "请授予悬浮窗权限！", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.buttonStart.setOnClickListener {
            if (!isWindowVisible) {
                if (checkOverlayPermission()) {
                    showFloatingWindow()
                } else {
                    requestOverlayPermission()
                }
            } else {
                hideFloatingWindow()
            }
        }
    }

    private fun checkOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(this)
    }

    private fun requestOverlayPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName"))
        overlayPermissionLauncher.launch(intent)
    }

    @SuppressLint("ClickableViewAccessibility", "InflateParams")
    private fun showFloatingWindow() {

        if (windowView == null) {
            windowView =
                LayoutInflater.from(context).inflate(R.layout.layout_floating_window, null)
        }

        val screenWidth = Resources.getSystem().displayMetrics.widthPixels

        val params = WindowManager.LayoutParams(
            300, 200,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.x = screenWidth - 300
        params.y = 300

        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
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

        windowView?.setOnDragListener { _, event ->
            when (event?.action) {
                DragEvent.ACTION_DROP -> {
                    val item = event.clipData.getItemAt(0)
                    val dragData = item.text?.toString() ?: ""

                    // 判断拖拽的数据类型
                    if (item.uri != null && item.uri.toString().startsWith("content://")) {
                        // 处理图片的URI，显示在 ImageView 中
                        imageView?.setImageURI(item.uri)
                        // 清空文本
                        textView?.text = ""
                    } else {
                        // 显示拖拽的文本数据
                        textView?.text = dragData
                        // 清空图片
                        imageView?.setImageDrawable(null)
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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND) {
            if ("text/plain" == intent.type) {
                handleSharedText()
            } else if (intent.type?.startsWith("image/") == true) {
                handleSharedImage()
            }
        }
    }

    private fun handleSharedText() {
        // 处理文本
        Log.d("handleSharedText", "handleSharedText")
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        textView?.text = sharedText
    }

    private fun handleSharedImage() {
        // 处理图片
        Log.d("handleSharedImage", "handleSharedImage")
        val imageUri = intent.data
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        imageView?.setImageBitmap(bitmap)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

}
