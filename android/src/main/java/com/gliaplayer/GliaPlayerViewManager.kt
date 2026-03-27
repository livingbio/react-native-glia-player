package com.gliaplayer

import android.graphics.Color
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewManagerDelegate
import com.facebook.react.uimanager.annotations.ReactProp
import com.facebook.react.viewmanagers.GliaPlayerViewManagerInterface
import com.facebook.react.viewmanagers.GliaPlayerViewManagerDelegate
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@ReactModule(name = GliaPlayerViewManager.NAME)
class GliaPlayerViewManager : SimpleViewManager<GliaPlayerView>(),
  GliaPlayerViewManagerInterface<GliaPlayerView> {
  private val mDelegate: ViewManagerDelegate<GliaPlayerView>

  init {
    mDelegate = GliaPlayerViewManagerDelegate(this)
  }

  override fun getDelegate(): ViewManagerDelegate<GliaPlayerView>? {
    return mDelegate
  }

  override fun getName(): String {
    return NAME
  }

  public override fun createViewInstance(context: ThemedReactContext): GliaPlayerView {
    return GliaPlayerView(context)
  }

  @ReactProp(name = "slotKey")
  override fun setSlotKey(view: GliaPlayerView?, slotKey: String?) {
    if (slotKey == null) {
      view?.emitOnPageLoaded(GliaPlayerView.OnPageLoadedEventResult.error)
      return;
    }
    view?.let { gliaplayerView ->
      CoroutineScope(Dispatchers.IO).launch {
        // Initialize the Google Mobile Ads SDK on a background thread.
        MobileAds.initialize(gliaplayerView.context) {
          CoroutineScope(Dispatchers.Main).launch {
            MobileAds.registerWebView(gliaplayerView)
            gliaplayerView.initGliaPlayer(slotKey)
          }
        }
      }
    }
  }

  override fun pause(view: GliaPlayerView?) {
    view?.pause()
  }

  override fun resume(view: GliaPlayerView?) {
    view?.resume()
  }

  override fun getExportedCustomBubblingEventTypeConstants(): Map<String, Any> =
    mapOf(
      "onPageLoaded" to
        mapOf(
          "phasedRegistrationNames" to
            mapOf(
              "bubbled" to "onPageLoaded",
              "captured" to "onPageLoadedCapture"
            )
        )
    )

  companion object {
    const val NAME = "GliaPlayerView"
  }
}
