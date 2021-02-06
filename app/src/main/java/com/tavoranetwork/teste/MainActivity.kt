package com.tavoranetwork.teste

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Base64.encodeToString
import android.util.TypedValue
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
import android.webkit.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginTop
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    private var webView: WebView? = null
    private var webview_loading: ProgressBar? = null
    private var webview_error: View? = null
    private var videoFrame: FrameLayout? = null
    private var toolbar: Toolbar? = null
    private var searchButton: ImageButton? = null
    private var searchbarIsVisible: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.webview)
        webview_loading = findViewById(R.id.webview_loading)
        webview_error = findViewById(R.id.error_layout)
        videoFrame = findViewById(R.id.videoFrame)
        toolbar = findViewById(R.id.toolbar)
        searchButton = findViewById(R.id.searchButton)

        webView?.settings?.javaScriptEnabled = true
        webView?.settings?.allowContentAccess = true
        webView?.settings?.allowFileAccess = true

        webView?.webViewClient = object : WebViewClient() {

            override fun onLoadResource(view: WebView?, url: String?) {
                injectCSS("style.css")
                super.onLoadResource(view, url)
            }


            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                if (errorResponse?.statusCode == 404)
                    webview_error?.visibility = VISIBLE

                print(errorResponse?.statusCode)
                //super.onReceivedHttpError(view, request, errorResponse)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                webview_loading?.visibility = VISIBLE
                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                //injectCSS()
                webview_loading?.visibility = GONE
                super.onPageStarted(view, url, favicon)
            }

        }


        webView?.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(fullScreenContent: View?, callback: CustomViewCallback?) {
                super.onShowCustomView(fullScreenContent, callback)
                fullScreenContent?.let { fullScreenView ->
                    hideSystemUI()




                    videoFrame?.removeAllViews()
                    videoFrame?.visibility = VISIBLE
                    videoFrame?.addView(fullScreenView)
                    toolbar?.margin(top = 20F)
                }
            }

            override fun onHideCustomView() {
                super.onHideCustomView()
                showSystemUI()
                videoFrame?.visibility = GONE
                window.decorView.windowInsetsController?.setSystemBarsAppearance(
                    0,
                    APPEARANCE_LIGHT_STATUS_BARS
                )
                videoFrame?.removeAllViews()
            }
        }

        searchButton?.setOnClickListener {
            if (!searchbarIsVisible) {
                searchbarIsVisible = true
                webView?.loadUrl(
                    "javascript:(" +
                            "function() {" +
                            "document.getElementById(\"form-search-resp\").style[\"display\"] = \"block\";\n" +
                            "}" +
                            ")()"
                )
            } else {
                searchbarIsVisible = false
                webView?.loadUrl(
                    "javascript:(" +
                            "function() {" +
                            "document.getElementById(\"form-search-resp\").style[\"display\"] = \"none\";\n" +
                            "}" +
                            ")()"
                )
            }
        }

        webView?.loadUrl("https://animesonline.cc/tv/")


    }

    private fun injectCSS(filename: String) {
        try {
            val inputStream: InputStream = assets.open(filename)
            val buffer = ByteArray(inputStream.available())
            inputStream.read(buffer)
            inputStream.close()
            val encoded: String = encodeToString(buffer, Base64.NO_WRAP)
            webView?.loadUrl(
                "javascript:(" +
                        "function() {" +
                        "var parent = document.getElementsByTagName('head').item(0);" +
                        "var style = document.createElement('style');" +
                        "style.type = 'text/css';" +  // Tell the browser to BASE64-decode the string into your script !!!
                        "style.innerHTML = window.atob('" + encoded + "');" +
                        "parent.appendChild(style)" +
                        "}" +
                        ")()"
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        webView?.goBack()
    }


    fun showSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // show app content in fullscreen, i. e. behind the bars when they are shown (alternative to
            // deprecated View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION and View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            window.setDecorFitsSystemWindows(false)
            // finally, show the system bars
            window.insetsController?.show(WindowInsets.Type.systemBars())
        } else {
            // Shows the system bars by removing all the flags
            // except for the ones that make the content appear under the system bars.
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }
    }

    fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                // Default behavior is that if navigation bar is hidden, the system will "steal" touches
                // and show it again upon user's touch. We just want the user to be able to show the
                // navigation bar by swipe, touches are handled by custom code -> change system bar behavior.
                // Alternative to deprecated SYSTEM_UI_FLAG_IMMERSIVE.
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_BARS_BY_TOUCH
                // make navigation bar translucent (alternative to deprecated
                // WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
                // - do this already in hideSystemUI() so that the bar
                // is translucent if user swipes it up
                window.navigationBarColor = getColor(R.color.black)
                // Finally, hide the system bars, alternative to View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                // and SYSTEM_UI_FLAG_FULLSCREEN.
                it.hide(WindowInsets.Type.systemBars())
            }
        } else {
            // Enables regular immersive mode.
            // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
            // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    // Do not let system steal touches for showing the navigation bar
                    View.SYSTEM_UI_FLAG_IMMERSIVE
                            // Hide the nav bar and status bar
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            // Keep the app content behind the bars even if user swipes them up
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            // make navbar translucent - do this already in hideSystemUI() so that the bar
            // is translucent if user swipes it up
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        }
    }

    fun View.margin(
        left: Float? = null,
        top: Float? = null,
        right: Float? = null,
        bottom: Float? = null
    ) {
        layoutParams<ViewGroup.MarginLayoutParams> {
            left?.run { leftMargin = dpToPx(this) }
            top?.run { topMargin = dpToPx(this) }
            right?.run { rightMargin = dpToPx(this) }
            bottom?.run { bottomMargin = dpToPx(this) }
        }
    }

    inline fun <reified T : ViewGroup.LayoutParams> View.layoutParams(block: T.() -> Unit) {
        if (layoutParams is T) block(layoutParams as T)
    }

    fun View.dpToPx(dp: Float): Int = context.dpToPx(dp)
    fun Context.dpToPx(dp: Float): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).toInt()


}