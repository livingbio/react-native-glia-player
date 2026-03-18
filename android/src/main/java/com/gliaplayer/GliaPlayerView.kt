package com.gliaplayer

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.ReactContext
import com.facebook.react.uimanager.UIManagerHelper
import com.facebook.react.uimanager.events.Event
import java.net.URI
import androidx.browser.customtabs.CustomTabsIntent

class GliaPlayerView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {
  private lateinit var slot_key: String
  fun resume() {
    onResume()
  }

  fun pause() {
    onPause()
  }

  @SuppressLint("SetJavaScriptEnabled")
  fun initGliaPlayer(slot_key: String) {
    this.slot_key = slot_key
    // Allow third-party cookies for ad functionality
    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

    settings.apply {
      javaScriptEnabled = true
      domStorageEnabled = true
      mediaPlaybackRequiresUserGesture = false
    }

    // Set optimized WebViewClient for Custom Tabs
    webViewClient = object : WebViewClient() {
      override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
      ): Boolean {
        // Determine whether to override the behavior of the URL.
        // If the target URL has no host and no scheme, return early.
        if (request?.url?.host == null && request?.url?.scheme == null) {
          return false
        }

        val currentDomain = URI(view?.url).toURL().host
        // Handle custom URL schemes such as market:// by attempting to
        // launch the corresponding application in a new intent.
        if (!request.url.scheme.equals("http") &&
          !request.url.scheme.equals("https")) {
          val intent = Intent(Intent.ACTION_VIEW, request.url)
          // If the URL cannot be opened, return early.
          try {
            context.startActivity(intent)
          } catch (exception: ActivityNotFoundException) {
            Log.d("TAG", "Failed to load URL with scheme: ${request.url.scheme}")
          }
          return true
        }

        val targetDomain = request.url.host

        // If the current domain equals the target domain, the
        // assumption is the user is not navigating away from
        // the site. Reload the URL within the existing web view.
        if (currentDomain.equals(targetDomain)) {
          return false
        }

        // User is navigating away from the site, open the URL in
        // Custom Tabs to preserve the state of the web view.
        val customTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.launchUrl(context, request.url)
        return true
      }

      override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)

        // Inject JavaScript to change background color to black and add styles
        val jsCode = """
                    document.documentElement.style.backgroundColor = 'black';
                    document.body.style.margin = 'auto';
                    document.body.style.height = window.innerHeight + 'px';
                    document.body.style.display = 'flex';
                    document.body.style.justifyContent = 'center'; // Horizontal Center
                    document.body.style.alignItems = 'center';
                    const div = document.createElement('div');
                    div.style.cssText = 'position:absolute;top:10px;left:10px;background:rgba(0,0,0,0.8);color:white;padding:10px;border-radius:5px;z-index:10000;';
                  //  document.body.appendChild(div);

                    const container = document.querySelector('.gliaplayer-container');
                    container.style.transformOrigin = 'center center';

                    const handleResize = () => {
                        const winW = window.innerWidth
                        const winH = window.innerHeight
                        const conW = container.offsetWidth
                        const conH = container.offsetHeight
                        var percentage = 100;

                        if (winW > winH) {
                            var newWidth = winH * conW / conH;
                            if (newWidth < winW) {
                                div.innerHTML = 'UUU'
                                var percentage = (newWidth / winW) * 100;
                            }
                        }
                        document.body.style.width =  `${'$'}{percentage.toFixed(0)}%`;

                      //  observer.disconnect()
                    //    div.innerHTML += `寬度: ${'$'}{container.offsetWidth}px, 高度: ${'$'}{container.offsetHeight}px <br> ${'$'}{window.innerWidth}px  ${'$'}{window.innerHeight}px  ${'$'}{percentage}`;
                    };
                    const observer = new ResizeObserver(() => handleResize());
                    observer.observe(container);
                    window.addEventListener('resize', handleResize);
                    handleResize()
                    """.trimIndent()

        view?.evaluateJavascript(jsCode, null)
      }
    }

    // Load the content
    loadUrl("https://player.gliacloud.com/in-app-browser/${slot_key}")
    emitOnPageLoaded(OnPageLoadedEventResult.success)
  }

  fun emitOnPageLoaded(result: OnPageLoadedEventResult) {
    val reactContext = context as ReactContext
    val surfaceId = UIManagerHelper.getSurfaceId(reactContext)
    val eventDispatcher = UIManagerHelper.getEventDispatcherForReactTag(reactContext, id)
    val payload =
        Arguments.createMap().apply {
          putString("result", result.name)
        }
    val event = OnPageLoadedEvent(surfaceId, id, payload)

    eventDispatcher?.dispatchEvent(event)
  }

  enum class OnPageLoadedEventResult {
    success,
    error;
  }

  inner class OnPageLoadedEvent(
      surfaceId: Int,
      viewId: Int,
      private val payload: WritableMap
  ) : Event<OnPageLoadedEvent>(surfaceId, viewId) {
    override fun getEventName() = "onPageLoaded"

    override fun getEventData() = payload
  }
}
