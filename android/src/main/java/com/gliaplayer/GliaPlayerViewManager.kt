package com.gliaplayer

import android.graphics.Color
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewManagerDelegate
import com.facebook.react.uimanager.annotations.ReactProp
import com.facebook.react.viewmanagers.GliaPlayerViewManagerInterface
import com.facebook.react.viewmanagers.GliaPlayerViewManagerDelegate

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

  @ReactProp(name = "color")
  override fun setColor(view: GliaPlayerView?, color: Int?) {
    view?.setBackgroundColor(color ?: Color.TRANSPARENT)
  }

  companion object {
    const val NAME = "GliaPlayerView"
  }
}
